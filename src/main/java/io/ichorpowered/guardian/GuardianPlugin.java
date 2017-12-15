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
package io.ichorpowered.guardian;

import com.abilityapi.sequenceapi.SequenceManager;
import com.abilityapi.sequenceapi.SequenceRegistry;
import com.google.inject.Inject;
import com.ichorpowered.guardian.api.Guardian;
import com.ichorpowered.guardian.api.GuardianState;
import com.ichorpowered.guardian.api.detection.DetectionRegistry;
import com.ichorpowered.guardian.api.detection.check.CheckRegistry;
import com.ichorpowered.guardian.api.detection.heuristic.HeuristicRegistry;
import com.ichorpowered.guardian.api.detection.penalty.PenaltyRegistry;
import com.ichorpowered.guardian.api.event.GuardianEvent;
import com.ichorpowered.guardian.api.event.GuardianListener;
import com.ichorpowered.guardian.api.phase.PhaseRegistry;
import com.me4502.modularframework.ModuleController;
import io.ichorpowered.guardian.detection.GuardianDetectionRegistry;
import io.ichorpowered.guardian.detection.check.GuardianCheckRegistry;
import io.ichorpowered.guardian.detection.heuristics.GuardianHeuristicRegistry;
import io.ichorpowered.guardian.detection.penalty.GuardianPenaltyRegistry;
import io.ichorpowered.guardian.launch.FacetBootstrap;
import io.ichorpowered.guardian.launch.facet.CorePluginFacet;
import io.ichorpowered.guardian.launch.facet.GamePluginFacet;
import io.ichorpowered.guardian.launch.facet.InternalPluginFacet;
import io.ichorpowered.guardian.launch.message.SimpleFacetMessage;
import io.ichorpowered.guardian.phase.GuardianPhaseRegistry;
import io.ichorpowered.guardian.sequence.GuardianSequenceListener;
import io.ichorpowered.guardian.sequence.GuardianSequenceManager;
import io.ichorpowered.guardian.util.property.Property;
import io.ichorpowered.guardian.util.property.PropertyInjector;
import io.ichorpowered.guardian.util.property.PropertyInjectorFactory;
import io.ichorpowered.guardian.util.property.PropertyModifier;
import net.kyori.event.SimpleEventBus;
import org.slf4j.Logger;
import org.spongepowered.api.config.DefaultConfig;
import org.spongepowered.api.event.Event;
import org.spongepowered.api.event.Listener;
import org.spongepowered.api.event.game.GameReloadEvent;
import org.spongepowered.api.event.game.state.*;
import org.spongepowered.api.plugin.Dependency;
import org.spongepowered.api.plugin.Plugin;
import org.spongepowered.api.plugin.PluginContainer;

import java.nio.file.Path;

@Plugin(
        id = PluginInfo.ID,
        name = PluginInfo.NAME,
        version = PluginInfo.VERSION,
        description = PluginInfo.DESCRIPTION,
        authors = {
                "Connor Hartley (vectrix)",
                "Parker Lougheed (meronat)",
                "Matthew Miller (me4502)",
        },
        dependencies = {
            @Dependency(
                    id = "precogs",
                    version = PluginInfo.PRECOGS_VERSION
            ),
            @Dependency(
                    id = "elderguardian",
                    version = PluginInfo.ELDER_VERSION,
                    optional = true
            )
        }
)
public class GuardianPlugin implements Guardian<Event> {

    /* Injected Fields */
    private final Logger logger;
    private final PluginContainer pluginContainer;
    private final Path configDirectory;

    /* Facet Bootstrap Fields */
    private final FacetBootstrap facetBootstrap;
    private final PropertyInjector propertyInjector;

    /* Core Fields */
    @Property public Long coreTime;
    @Property public GuardianState state;
    @Property public SimpleEventBus<GuardianEvent, GuardianListener> eventBus;

    @Property private ModuleController<GuardianPlugin> moduleController;

    /* Registry Fields */
    @Property(modifier = PropertyModifier.FINAL) private GuardianDetectionRegistry detectionRegistry;
    @Property(modifier = PropertyModifier.FINAL) private GuardianCheckRegistry checkRegistry;
    @Property(modifier = PropertyModifier.FINAL) private GuardianHeuristicRegistry heuristicRegistry;
    @Property(modifier = PropertyModifier.FINAL) private GuardianPenaltyRegistry penaltyRegistry;
    @Property(modifier = PropertyModifier.FINAL) private GuardianPhaseRegistry phaseRegistry;

    @Property(modifier = PropertyModifier.FINAL) private SequenceRegistry<Event> sequenceRegistry;

    /* Manager Fields */
    @Property(modifier = PropertyModifier.FINAL) public Configuration configuration;
    @Property(modifier = PropertyModifier.FINAL) public GuardianSequenceManager sequenceManager;
    @Property(modifier = PropertyModifier.FINAL) public GuardianSequenceManager.SequenceTask sequenceTask;

    @Property(modifier = PropertyModifier.FINAL) public GuardianSequenceListener sequenceListener;
    @Property(modifier = PropertyModifier.FINAL) public Common common;

    @Inject
    public GuardianPlugin(Logger logger, PluginContainer pluginContainer,
                          @DefaultConfig(sharedRoot = false) Path configDirectory) {
        this.logger = logger;
        this.pluginContainer = pluginContainer;
        this.configDirectory = configDirectory;

        this.facetBootstrap = new FacetBootstrap(this.logger, this);
        this.propertyInjector = PropertyInjectorFactory.create(this);
    }

    // PLUGIN INITIALIZATION

    @Listener
    public void onGameInitialization(GameInitializationEvent event) {
        this.facetBootstrap.addComponent("core", new CorePluginFacet(this.logger, this));
        this.facetBootstrap.addComponent("common", new InternalPluginFacet(this.logger, this));
        this.facetBootstrap.addComponent("game", new GamePluginFacet(this.logger, this));

        this.facetBootstrap.send(FacetBootstrap.FacetRequest.STARTUP,
                new SimpleFacetMessage(System.currentTimeMillis(), "Game Initialization", this),
                "core");
    }

    @Listener
    public void onGameStartingServer(GameStartingServerEvent event) {
        this.facetBootstrap.send(FacetBootstrap.FacetRequest.STARTUP,
                new SimpleFacetMessage(System.currentTimeMillis(), "Server Starting", this),
                "common");
    }

    @Listener
    public void onGameStartedServer(GameStartedServerEvent event) {
        this.facetBootstrap.send(FacetBootstrap.FacetRequest.STARTUP,
                new SimpleFacetMessage(System.currentTimeMillis(), "Server Started", this),
                "game");
    }

    // PLUGIN RELOAD

    @Listener
    public void onGameReload(GameReloadEvent event) {
        this.facetBootstrap.send(FacetBootstrap.FacetRequest.RESTART,
                new SimpleFacetMessage(System.currentTimeMillis(), "Game Reload", this),
                "game");

        this.facetBootstrap.send(FacetBootstrap.FacetRequest.RESTART,
                new SimpleFacetMessage(System.currentTimeMillis(), "Game Reload", this),
                "common");
    }

    // PLUGIN SHUTDOWN

    @Listener
    public void onGameStoppingServer(GameStoppingServerEvent event) {
        this.facetBootstrap.send(FacetBootstrap.FacetRequest.SHUTDOWN,
                new SimpleFacetMessage(System.currentTimeMillis(), "Server Stopping", this),
                "game");
    }

    @Listener
    public void onGameStoppedServer(GameStoppedServerEvent event) {
        this.facetBootstrap.send(FacetBootstrap.FacetRequest.SHUTDOWN,
                new SimpleFacetMessage(System.currentTimeMillis(), "Server Stopped", this),
                "common");
    }

    @Listener
    public void onGameStopping(GameStoppingEvent event) {
        this.facetBootstrap.send(FacetBootstrap.FacetRequest.SHUTDOWN,
                new SimpleFacetMessage(System.currentTimeMillis(), "Game Stopping", this),
                "core");
    }

    // ACCESSORS

    public final PropertyInjector getPropertyInjector() {
        return this.propertyInjector;
    }

    public final Logger getLogger() {
        return this.logger;
    }

    public final PluginContainer getPluginContainer() {
        return this.pluginContainer;
    }

    public final Path getConfigDirectory() {
        return this.configDirectory.getParent();
    }

    public final ModuleController<GuardianPlugin> getModuleController() {
        return this.moduleController;
    }

    @Override
    public final SimpleEventBus<GuardianEvent, GuardianListener> getEventBus() {
        return this.eventBus;
    }

    @Override
    public final GuardianState getState() {
        return this.state;
    }

    @Override
    public final DetectionRegistry getDetectionRegistry() {
        return this.detectionRegistry;
    }

    @Override
    public final CheckRegistry getCheckRegistry() {
        return this.checkRegistry;
    }

    @Override
    public final HeuristicRegistry getHeuristicRegistry() {
        return this.heuristicRegistry;
    }

    @Override
    public final PenaltyRegistry getPenaltyRegistry() {
        return this.penaltyRegistry;
    }

    @Override
    public final SequenceRegistry<Event> getSequenceRegistry() {
        return this.sequenceRegistry;
}

    @Override
    public final PhaseRegistry getPhaseRegistry() {
        return this.phaseRegistry;
    }

    @Override
    public final SequenceManager<Event> getSequenceManager() {
        return this.sequenceManager;
    }
}
