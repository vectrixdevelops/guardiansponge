package io.github.connorhartley.guardian;

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
import com.ichorpowered.guardian.api.sequence.SequenceManager;
import com.ichorpowered.guardian.api.sequence.SequenceRegistry;
import com.me4502.modularframework.ModuleController;
import io.github.connorhartley.guardian.detection.GuardianDetectionRegistry;
import io.github.connorhartley.guardian.detection.check.GuardianCheckRegistry;
import io.github.connorhartley.guardian.detection.heuristics.GuardianHeuristicRegistry;
import io.github.connorhartley.guardian.detection.penalty.GuardianPenaltyRegistry;
import io.github.connorhartley.guardian.launch.FacetBootstrap;
import io.github.connorhartley.guardian.launch.facet.CorePluginFacet;
import io.github.connorhartley.guardian.launch.facet.GamePluginFacet;
import io.github.connorhartley.guardian.launch.facet.InternalPluginFacet;
import io.github.connorhartley.guardian.util.property.Property;
import io.github.connorhartley.guardian.launch.message.SimpleFacetMessage;
import io.github.connorhartley.guardian.phase.GuardianPhaseRegistry;
import io.github.connorhartley.guardian.sequence.GuardianSequenceListener;
import io.github.connorhartley.guardian.sequence.GuardianSequenceManager;
import io.github.connorhartley.guardian.sequence.GuardianSequenceRegistry;
import io.github.connorhartley.guardian.util.property.PropertyInjector;
import io.github.connorhartley.guardian.util.property.PropertyInjectorFactory;
import net.kyori.event.SimpleEventBus;
import org.slf4j.Logger;
import org.spongepowered.api.config.DefaultConfig;
import org.spongepowered.api.event.Event;
import org.spongepowered.api.event.Listener;
import org.spongepowered.api.event.game.state.GameInitializationEvent;
import org.spongepowered.api.event.game.state.GameStartedServerEvent;
import org.spongepowered.api.event.game.state.GameStartingServerEvent;
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

    /* FacetBootstrap Fields */
    private final FacetBootstrap facetBootstrap;
    private final PropertyInjector propertyInjector;

    /* Core Fields */
    @Property(alias = "coreTime") public Long time;
    @Property(alias = "state") public GuardianState state;
    @Property(alias = "eventBus") public SimpleEventBus<GuardianEvent, GuardianListener> eventBus;

    @Property(alias = "moduleController") private ModuleController<GuardianPlugin> moduleController;

    /* Registry Fields */
    @Property(alias = "detectionRegistry", effectFinal = true) private GuardianDetectionRegistry detectionRegistry;
    @Property(alias = "checkRegistry", effectFinal = true) private GuardianCheckRegistry checkRegistry;
    @Property(alias = "sequenceRegistry", effectFinal = true) private GuardianSequenceRegistry sequenceRegistry;
    @Property(alias = "heuristicRegistry", effectFinal = true) private GuardianHeuristicRegistry heuristicRegistry;
    @Property(alias = "penaltyRegistry", effectFinal = true) private GuardianPenaltyRegistry penaltyRegistry;
    @Property(alias = "phaseRegistry", effectFinal = true) private GuardianPhaseRegistry phaseRegistry;

    /* Manager Fields */
    @Property(alias = "configuration", effectFinal = true) public GuardianConfiguration guardianConfiguration;
    @Property(alias = "sequenceManager", effectFinal = true) public GuardianSequenceManager sequenceManager;
    @Property(alias = "sequenceTask", effectFinal = true) public GuardianSequenceManager.SequenceTask sequenceTask;

    @Property(alias = "sequenceListener", effectFinal = true) public GuardianSequenceListener sequenceListener;
    @Property(alias = "loader", effectFinal = true) public GuardianLoader guardianLoader;

    @Inject
    public GuardianPlugin(Logger logger, PluginContainer pluginContainer,
                          @DefaultConfig(sharedRoot = false) Path configDirectory) {
        this.logger = logger;
        this.pluginContainer = pluginContainer;
        this.configDirectory = configDirectory;

        this.facetBootstrap = new FacetBootstrap(this.logger, this);
        this.propertyInjector = PropertyInjectorFactory.create(this);
    }

    @Listener
    public void onGameInitialization(GameInitializationEvent event) {
        this.facetBootstrap.addComponent("core", new CorePluginFacet(this.logger, this));
        this.facetBootstrap.addComponent("internal", new InternalPluginFacet(this.logger, this));
        this.facetBootstrap.addComponent("game", new GamePluginFacet(this.logger, this));

        this.facetBootstrap.send(FacetBootstrap.FacetRequest.STARTUP,
                new SimpleFacetMessage(System.currentTimeMillis(), "Game Initialization", this),
                "core");
    }

    @Listener
    public void onGameStartingServer(GameStartingServerEvent event) {
        this.facetBootstrap.send(FacetBootstrap.FacetRequest.STARTUP,
                new SimpleFacetMessage(System.currentTimeMillis(), "Server Starting", this),
                "internal");
    }

    @Listener
    public void onGameStartedServer(GameStartedServerEvent event) {
        this.facetBootstrap.send(FacetBootstrap.FacetRequest.STARTUP,
                new SimpleFacetMessage(System.currentTimeMillis(), "Server Started", this),
                "game");
    }

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
    public final SequenceRegistry getSequenceRegistry() {
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
