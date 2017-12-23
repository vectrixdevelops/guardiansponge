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
package com.ichorpowered.guardian.launch.facet;

import com.ichorpowered.guardian.GuardianPlugin;
import com.ichorpowered.guardian.detection.AbstractDetection;
import com.ichorpowered.guardian.event.state.GuardianPostInitialization;
import com.ichorpowered.guardian.launch.Facet;
import com.ichorpowered.guardian.launch.FacetBootstrap;
import com.ichorpowered.guardian.launch.FacetState;
import com.ichorpowered.guardian.launch.exception.FacetException;
import com.ichorpowered.guardian.launch.message.FacetMessage;
import com.ichorpowered.guardian.util.ConsoleUtil;
import com.ichorpowered.guardian.util.property.PropertyInjector;
import com.ichorpowered.guardianapi.GuardianState;
import com.ichorpowered.guardianapi.detection.Detection;
import com.ichorpowered.guardianapi.detection.check.CheckModel;
import com.ichorpowered.guardianapi.detection.heuristic.HeuristicModel;
import com.ichorpowered.guardianapi.detection.penalty.PenaltyModel;
import com.ichorpowered.guardianapi.event.origin.Origin;
import com.me4502.modularframework.module.ModuleWrapper;
import com.me4502.precogs.detection.DetectionType;
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

        this.plugin.getCommon().loadPenalties();
        this.plugin.getCommon().loadChecks();
        this.plugin.getCommon().loadModules(this.plugin.getModuleController());

        this.logger.info(ConsoleUtil.of(Ansi.Color.YELLOW, "Discovered {} module(s).",
                String.valueOf(this.plugin.getModuleController().getModules().size())));

        this.plugin.getModuleController().enableModules(moduleWrapper -> {
            try {
                if (this.plugin.getConfiguration().getEnabledModules().contains(moduleWrapper.getId())) return true;
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

                        // Precogs detection registration.
                        Sponge.getRegistry().register(DetectionType.class, detection);

                        //
                        this.plugin.getEventBus().register(detection);
                        this.plugin.getDetectionManager().provideDetection(detection.getClass(), detection);

                        // Register the detection and inject its properties.
                        detection.getRegistration(this.plugin.getDetectionManager());

                        // Call the load method.
                        detection.onLoad();

                        if (this.plugin.getDetectionManager().getStageModel(CheckModel.class).isPresent())
                            detections.addAndGet(detection.getStageCycle().sizeFor(this.plugin.getDetectionManager().getStageModel(CheckModel.class).get()));

                        if (this.plugin.getDetectionManager().getStageModel(HeuristicModel.class).isPresent())
                            heuristics.addAndGet(detection.getStageCycle().sizeFor(this.plugin.getDetectionManager().getStageModel(HeuristicModel.class).get()));

                        if (this.plugin.getDetectionManager().getStageModel(PenaltyModel.class).isPresent())
                            penalties.addAndGet(detection.getStageCycle().sizeFor(this.plugin.getDetectionManager().getStageModel(PenaltyModel.class).get()));

                        detections.incrementAndGet();
                    }
                });

        this.logger.info(ConsoleUtil.of(Ansi.Color.YELLOW, "Loaded {} punishment(s), {} heuristic(s) and " +
                "{} check(s) for {} detection(s).",
                String.valueOf(penalties.get()),
                String.valueOf(heuristics.get()),
                String.valueOf(checks.get()),
                String.valueOf(detections.get())));

        this.plugin.getEventBus().post(new GuardianPostInitialization(this.plugin, Origin.source(this.plugin.getPluginContainer()).build()));

        Sponge.getEventManager().registerListeners(this.plugin, this.plugin.getSequenceListener());

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
