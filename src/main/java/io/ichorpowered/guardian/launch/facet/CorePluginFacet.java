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

import com.abilityapi.sequenceapi.SequenceRegistry;
import com.ichorpowered.guardian.api.GuardianState;
import com.ichorpowered.guardian.api.SimpleGuardian;
import com.ichorpowered.guardian.api.event.GuardianEvent;
import com.ichorpowered.guardian.api.event.GuardianListener;
import com.ichorpowered.guardian.api.event.origin.Origin;
import com.me4502.modularframework.ModuleController;
import com.me4502.modularframework.ShadedModularFramework;
import io.ichorpowered.guardian.Common;
import io.ichorpowered.guardian.Configuration;
import io.ichorpowered.guardian.GuardianPlugin;
import io.ichorpowered.guardian.PluginInfo;
import io.ichorpowered.guardian.detection.GuardianDetectionRegistry;
import io.ichorpowered.guardian.detection.check.GuardianCheckRegistry;
import io.ichorpowered.guardian.detection.heuristics.GuardianHeuristicRegistry;
import io.ichorpowered.guardian.detection.penalty.GuardianPenaltyRegistry;
import io.ichorpowered.guardian.event.state.GuardianInitializationEvent;
import io.ichorpowered.guardian.event.state.GuardianPreInitializationEvent;
import io.ichorpowered.guardian.launch.Facet;
import io.ichorpowered.guardian.launch.FacetBootstrap;
import io.ichorpowered.guardian.launch.FacetState;
import io.ichorpowered.guardian.launch.exception.FacetException;
import io.ichorpowered.guardian.launch.message.FacetMessage;
import io.ichorpowered.guardian.launch.message.SimpleFacetMessage;
import io.ichorpowered.guardian.phase.GuardianPhaseRegistry;
import io.ichorpowered.guardian.sequence.GuardianSequenceListener;
import io.ichorpowered.guardian.sequence.GuardianSequenceManager;
import io.ichorpowered.guardian.util.ConsoleUtil;
import io.ichorpowered.guardian.util.property.PropertyInjector;
import net.kyori.event.ASMEventExecutorFactory;
import net.kyori.event.SimpleEventBus;
import org.fusesource.jansi.Ansi;
import org.slf4j.Logger;
import org.spongepowered.api.Platform;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.event.Event;

public class CorePluginFacet implements Facet {

    private final Logger logger;
    private final GuardianPlugin plugin;

    private final String facetPrefix = ConsoleUtil.of(Ansi.Color.CYAN, " CORE ");

    private FacetState facetState = FacetState.STOP;

    public CorePluginFacet(final Logger logger, final GuardianPlugin plugin) {
        this.logger = logger;
        this.plugin = plugin;
    }

    @Override
    public <I extends FacetMessage, O> O handle(FacetBootstrap.FacetRequest<I, O> facetRequest, I input) throws FacetException {
        switch (facetRequest.getId()) {
            case 0: {
                if (input instanceof SimpleFacetMessage) return (O) this.facetState;
            }
            case 1: {
                if (input instanceof SimpleFacetMessage) return (O) this.startup((SimpleFacetMessage) input);
                else throw new FacetException("Input was of an incorrect type.");
            }
            case 2: {
                if (input instanceof SimpleFacetMessage) return (O) this.restart((SimpleFacetMessage) input);
                else throw new FacetException("Input was of an incorrect type.");
            }
            case 3: {
                if (input instanceof SimpleFacetMessage) return (O) this.shutdown((SimpleFacetMessage) input);
                else throw new FacetException("Input was of an incorrect type.");
            }
        }
        return null;
    }

    public Boolean startup(SimpleFacetMessage componentMessage) {
        this.facetState = FacetState.PREPARE;

        SimpleGuardian.setInstance(this.plugin);

        PropertyInjector propertyInjector = this.plugin.getPropertyInjector();

        propertyInjector.inject("state", GuardianState.PRE_INITIALIZATION);

        // STATE: PRE_INITIALIZATION
        if (PluginInfo.EXPERIMENTAL) {
            this.logger.warn(ConsoleUtil.of(Ansi.Color.RED, "You are using an experimental build of Guardian."));
            this.logger.warn(ConsoleUtil.of(Ansi.Color.RED, "This may not be ready for a production environment. Use at your own risk!"));
        }

        this.logger.info(ConsoleUtil.of("Guardian v{} for Sponge {} and Minecraft {}",
                PluginInfo.VERSION,
                Sponge.getPlatform().getContainer(Platform.Component.API).getVersion().orElse("?").substring(0, 5),
                Sponge.getPlatform().getContainer(Platform.Component.GAME).getVersion().orElse("?")));

        ModuleController<GuardianPlugin> moduleController = ShadedModularFramework.registerModuleController(this.plugin, Sponge.getGame());
        moduleController.setPluginContainer(this.plugin.getPluginContainer());

        SimpleEventBus<GuardianEvent, GuardianListener> eventBus = new SimpleEventBus<>(new ASMEventExecutorFactory<GuardianEvent, GuardianListener>());

        this.logger.info(ConsoleUtil.of("Loaded pre-facet systems: { me4502/modularframework v1.8.5, kyoripowered/event v1.0.0 }"));

        // PROVIDE: PRE_INITIALIZATION
        propertyInjector.inject("coreTime", componentMessage.getTime());
        propertyInjector.inject("eventBus", eventBus);
        propertyInjector.inject("moduleController", moduleController);

        this.plugin.eventBus.post(new GuardianPreInitializationEvent(Origin.source(this.plugin.getPluginContainer()).build()));

        propertyInjector.inject("state", GuardianState.INITIALIZATION);

        // State: INITIALIZATION
        this.logger.info(this.facetPrefix + "Initializing storage.");

        Configuration configuration = new Configuration(this.plugin, this.plugin.getConfigDirectory());
        configuration.load();

        this.logger.info(this.facetPrefix + "Initializing registries.");

        GuardianDetectionRegistry detectionRegistry = new GuardianDetectionRegistry(this.plugin);
        GuardianCheckRegistry checkRegistry = new GuardianCheckRegistry(this.plugin);
        GuardianHeuristicRegistry heuristicRegistry = new GuardianHeuristicRegistry(this.plugin);
        GuardianPenaltyRegistry penaltyRegistry = new GuardianPenaltyRegistry(this.plugin);
        GuardianPhaseRegistry phaseRegistry = new GuardianPhaseRegistry(this.plugin);

        SequenceRegistry<Event> sequenceRegistry = new SequenceRegistry<>();

        this.logger.info(this.facetPrefix + "Initializing systems.");

        GuardianSequenceManager sequenceManager = new GuardianSequenceManager(sequenceRegistry);
        GuardianSequenceManager.SequenceTask sequenceTask = new GuardianSequenceManager.SequenceTask(this.plugin,
                sequenceManager);

        GuardianSequenceListener sequenceListener = new GuardianSequenceListener(this.plugin);
        Common common = new Common(this.plugin);

        // PROVIDE: INITIALIZATION
        propertyInjector.inject("configuration", configuration);

        propertyInjector.inject("detectionRegistry", detectionRegistry);
        propertyInjector.inject("checkRegistry", checkRegistry);
        propertyInjector.inject("sequenceRegistry", sequenceRegistry);
        propertyInjector.inject("heuristicRegistry", heuristicRegistry);
        propertyInjector.inject("penaltyRegistry", penaltyRegistry);
        propertyInjector.inject("phaseRegistry", phaseRegistry);

        propertyInjector.inject("sequenceManager", sequenceManager);
        propertyInjector.inject("sequenceTask", sequenceTask);

        propertyInjector.inject("sequenceListener", sequenceListener);
        propertyInjector.inject("common", common);

        this.plugin.eventBus.post(new GuardianInitializationEvent(Origin.source(this.plugin.getPluginContainer()).build()));

        this.facetState = FacetState.START;
        return true;
    }

    public Boolean restart(SimpleFacetMessage message) {
        this.logger.info(ConsoleUtil.of("Restarted. Reason '{}' by '{}'",
                message.getReason(), message.getSource().getClass().getName()));

        return true;
    }

    public Boolean shutdown(SimpleFacetMessage message) {
        this.logger.info(ConsoleUtil.of("Shutdown. Reason '{}' by '{}'",
                message.getReason(), message.getSource().getClass().getName()));

        this.facetState = FacetState.STOP;
        return true;
    }

}
