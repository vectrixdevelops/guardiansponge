/*
 * MIT License
 *
 * Copyright (c) 2017 Connor Hartley
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */
package io.ichorpowered.guardian.launch.facet;

import com.ichorpowered.guardian.api.GuardianState;
import com.ichorpowered.guardian.api.detection.Detection;
import com.ichorpowered.guardian.api.detection.DetectionConfiguration;
import com.ichorpowered.guardian.api.detection.check.Check;
import com.ichorpowered.guardian.api.event.origin.Origin;
import com.ichorpowered.guardian.api.phase.PhaseManipulator;
import com.ichorpowered.guardian.api.phase.type.PhaseTypes;
import com.me4502.modularframework.module.ModuleWrapper;
import com.me4502.precogs.detection.DetectionType;
import io.ichorpowered.guardian.GuardianPlugin;
import io.ichorpowered.guardian.detection.AbstractDetection;
import io.ichorpowered.guardian.event.state.GuardianPostInitialization;
import io.ichorpowered.guardian.launch.Facet;
import io.ichorpowered.guardian.launch.FacetBootstrap;
import io.ichorpowered.guardian.launch.FacetState;
import io.ichorpowered.guardian.launch.exception.FacetException;
import io.ichorpowered.guardian.launch.message.FacetMessage;
import io.ichorpowered.guardian.util.ConsoleUtil;
import io.ichorpowered.guardian.util.property.PropertyInjector;
import ninja.leaping.configurate.objectmapping.ObjectMappingException;
import org.fusesource.jansi.Ansi;
import org.slf4j.Logger;
import org.spongepowered.api.Sponge;

import java.util.concurrent.atomic.AtomicInteger;

public class InternalPluginFacet implements Facet {

    private final Logger logger;
    private final GuardianPlugin plugin;

    private FacetState facetState = FacetState.STOP;

    public InternalPluginFacet(final Logger logger, final GuardianPlugin plugin) {
        this.logger = logger;
        this.plugin = plugin;
    }

    @Override
    public <I extends FacetMessage, O> O handle(FacetBootstrap.FacetRequest<I, O> facetRequest, I input) throws FacetException {
        switch (facetRequest.getId()) {
            case 0: {
                return (O) this.facetState;
            }
            case 1: {
                return (O) this.startup();
            }
            case 2: {
                // This facet should not restart.
                return (O) Boolean.valueOf(true);
            }
            case 3: {
                return (O) this.shutdown();
            }
        }
        return null;
    }

    public Boolean startup() {
        this.facetState = FacetState.PREPARE;

        PropertyInjector propertyInjector = this.plugin.getPropertyInjector();

        propertyInjector.inject("state", GuardianState.POST_INITIALIZATION);

        // STATE: POST_INITIALIZATION

        this.plugin.common.loadPenalties();
        this.plugin.common.loadChecks();
        this.plugin.common.loadModules(this.plugin.getModuleController());
        this.plugin.common.loadPhases();

        this.logger.info(ConsoleUtil.of(Ansi.Color.YELLOW, "Discovered {} module(s).",
                String.valueOf(this.plugin.getModuleController().getModules().size())));

        this.plugin.getModuleController().enableModules(moduleWrapper -> {
            try {
                if (this.plugin.configuration.getEnabledModules().contains(moduleWrapper.getId())) return true;
            } catch (ObjectMappingException e) {
                this.logger.error("Failed to acquire enable modules list from the configuration.", e);
            }
            return false;
        });

        AtomicInteger detections = new AtomicInteger(0);
        AtomicInteger checks = new AtomicInteger(0);
        AtomicInteger heuristics = new AtomicInteger(0);
        AtomicInteger penalties = new AtomicInteger(0);

        this.plugin.getModuleController().getModules().stream()
                .filter(ModuleWrapper::isEnabled)
                .forEach(moduleWrapper -> {
                    if (!moduleWrapper.getModule().isPresent()) return;
                    if (moduleWrapper.getModule().get() instanceof Detection) {
                        AbstractDetection detection = (AbstractDetection) moduleWrapper.getModule().get();

                        this.plugin.eventBus.register(detection);

                        Sponge.getRegistry().register(DetectionType.class, detection);

                        this.plugin.getDetectionRegistry().put(this, detection.getClass(), detection);

                        PhaseManipulator detectionManipulator = detection.getPhaseManipulator();

                        while (detectionManipulator.hasNext(PhaseTypes.CHECK)) {
                            Check<GuardianPlugin, DetectionConfiguration> check = detectionManipulator.next(PhaseTypes.CHECK);

                            assert check != null;

                            this.plugin.getSequenceRegistry().put(check.getSequence());
                        }

                        checks.addAndGet(detectionManipulator.size(PhaseTypes.CHECK));
                        heuristics.addAndGet(detectionManipulator.size(PhaseTypes.HEURISTIC));
                        penalties.addAndGet(detectionManipulator.size(PhaseTypes.PENALTY));

                        detections.incrementAndGet();
                    }
                });

        this.logger.info(ConsoleUtil.of(Ansi.Color.YELLOW, "Loaded {} punishment(s), {} heuristic(s) and " +
                "{} check(s) for {} detection(s).",
                String.valueOf(penalties.get()),
                String.valueOf(heuristics.get()),
                String.valueOf(checks.get()),
                String.valueOf(detections.get())));

        this.plugin.eventBus.post(new GuardianPostInitialization(this.plugin, Origin.source(this.plugin.getPluginContainer()).build()));

        Sponge.getEventManager().registerListeners(this.plugin, this.plugin.sequenceListener);

        this.facetState = FacetState.START;
        return true;
    }

    public Boolean restart() {
        return true;
    }

    public Boolean shutdown() {
        this.facetState = FacetState.STOP;
        return true;
    }
}
