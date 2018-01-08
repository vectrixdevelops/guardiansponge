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

import com.abilityapi.sequenceapi.SequenceRegistry;
import com.ichorpowered.guardian.Common;
import com.ichorpowered.guardian.Configuration;
import com.ichorpowered.guardian.GuardianPlugin;
import com.ichorpowered.guardian.PluginInfo;
import com.ichorpowered.guardian.content.GuardianContentKeys;
import com.ichorpowered.guardian.detection.GuardianDetectionManager;
import com.ichorpowered.guardian.event.GuardianInitializationEvent;
import com.ichorpowered.guardian.event.GuardianPreInitializationEvent;
import com.ichorpowered.guardian.launch.Facet;
import com.ichorpowered.guardian.launch.FacetBootstrap;
import com.ichorpowered.guardian.launch.FacetState;
import com.ichorpowered.guardian.launch.exception.FacetException;
import com.ichorpowered.guardian.launch.message.FacetMessage;
import com.ichorpowered.guardian.launch.message.SimpleFacetMessage;
import com.ichorpowered.guardian.sequence.GuardianSequenceListener;
import com.ichorpowered.guardian.sequence.GuardianSequenceManager;
import com.ichorpowered.guardian.util.ConsoleUtil;
import com.ichorpowered.guardian.util.property.PropertyInjector;
import com.ichorpowered.guardianapi.GuardianState;
import com.ichorpowered.guardianapi.SimpleGuardian;
import com.ichorpowered.guardianapi.event.GuardianEvent;
import com.ichorpowered.guardianapi.event.GuardianListener;
import com.ichorpowered.guardianapi.event.origin.Origin;
import com.me4502.modularframework.ModuleController;
import com.me4502.modularframework.ShadedModularFramework;
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
                Sponge.getPlatform().getContainer(Platform.Component.API).getVersion().map(version -> version.substring(0, 5)).orElse("?"),
                Sponge.getPlatform().getContainer(Platform.Component.GAME).getVersion().orElse("?")));

        ModuleController<GuardianPlugin> moduleController = ShadedModularFramework.registerModuleController(this.plugin, Sponge.getGame());
        moduleController.setPluginContainer(this.plugin.getPluginContainer());

        SimpleEventBus<GuardianEvent, GuardianListener> eventBus = new SimpleEventBus<>(new ASMEventExecutorFactory<GuardianEvent, GuardianListener>());

        this.logger.info(ConsoleUtil.of("Loaded pre-facet systems: { me4502/modularframework v1.8.5, kyoripowered/event v1.0.0 }"));

        // PROVIDE: PRE_INITIALIZATION
        propertyInjector.inject("coreTime", componentMessage.getTime());
        propertyInjector.inject("moduleController", moduleController);
        propertyInjector.inject("eventBus", eventBus);

        this.plugin.getEventBus().post(new GuardianPreInitializationEvent(Origin.source(this.plugin.getPluginContainer()).build()));

        propertyInjector.inject("state", GuardianState.INITIALIZATION);

        // State: INITIALIZATION
        this.logger.info(this.facetPrefix + "Initializing storage.");

        Configuration configuration = new Configuration(this.plugin, this.plugin.getConfigDirectory());
        configuration.load();

        this.logger.info(this.facetPrefix + "Initializing registries.");

        GuardianDetectionManager detectionManager = new GuardianDetectionManager(this.plugin);

        SequenceRegistry<Event> sequenceRegistry = new SequenceRegistry<>();

        this.logger.info(this.facetPrefix + "Initializing systems.");

        GuardianSequenceManager sequenceManager = new GuardianSequenceManager(this.plugin, sequenceRegistry);
        GuardianSequenceManager.SequenceTask sequenceTask = new GuardianSequenceManager.SequenceTask(this.plugin,
                sequenceManager);

        GuardianSequenceListener sequenceListener = new GuardianSequenceListener(this.plugin);
        Common common = new Common(this.plugin);

        GuardianContentKeys contentKeys = new GuardianContentKeys();
        contentKeys.createKeys();

        // PROVIDE: INITIALIZATION
        propertyInjector.inject("common", common);
        propertyInjector.inject("configuration", configuration);

        propertyInjector.inject("detectionManager", detectionManager);
        propertyInjector.inject("sequenceManager", sequenceManager);
        propertyInjector.inject("sequenceTask", sequenceTask);

        propertyInjector.inject("sequenceRegistry", sequenceRegistry);

        propertyInjector.inject("sequenceListener", sequenceListener);

        this.plugin.getEventBus().post(new GuardianInitializationEvent(Origin.source(this.plugin.getPluginContainer()).build()));

        this.facetState = FacetState.START;
        return true;
    }

    public Boolean restart(SimpleFacetMessage message) {
        this.logger.info(ConsoleUtil.of("Restarted. Reason '{}' by '{}'",
                message.getReason(), message.getSource().getClass().getSimpleName()));

        return true;
    }

    public Boolean shutdown(SimpleFacetMessage message) {
        this.logger.info(ConsoleUtil.of("Shutdown. Reason '{}' by '{}'",
                message.getReason(), message.getSource().getClass().getSimpleName()));

        this.facetState = FacetState.STOP;
        return true;
    }

}
