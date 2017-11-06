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
import io.github.connorhartley.guardian.launch.Bootstrap;
import io.github.connorhartley.guardian.launch.component.CorePluginComponent;
import io.github.connorhartley.guardian.launch.message.SimpleComponentMessage;
import io.github.connorhartley.guardian.phase.GuardianPhaseRegistry;
import io.github.connorhartley.guardian.sequence.GuardianSequenceListener;
import io.github.connorhartley.guardian.sequence.GuardianSequenceManager;
import io.github.connorhartley.guardian.sequence.GuardianSequenceRegistry;
import io.github.connorhartley.guardian.util.ElementProvider;
import net.kyori.event.SimpleEventBus;
import org.slf4j.Logger;
import org.spongepowered.api.config.DefaultConfig;
import org.spongepowered.api.event.Event;
import org.spongepowered.api.event.Listener;
import org.spongepowered.api.event.game.state.GameInitializationEvent;
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

    /* Bootstrap Fields */
    private final Bootstrap bootstrap;

    /* Core Fields */
    public final ElementProvider<Long> timeProvider = new ElementProvider<>();

    public final ElementProvider<GuardianState> stateProvider = new ElementProvider<>();
    public final ElementProvider<SimpleEventBus<GuardianEvent, GuardianListener>> eventBusProvider = new ElementProvider<>();
    public final ElementProvider<ModuleController<GuardianPlugin>> moduleControllerProvider = new ElementProvider<>();

    /* Registry Fields */
    public final ElementProvider<GuardianDetectionRegistry> detectionRegistryProvider = new ElementProvider<>();
    public final ElementProvider<GuardianCheckRegistry> checkRegistryProvider = new ElementProvider<>();
    public final ElementProvider<GuardianSequenceRegistry> sequenceRegistryProvider = new ElementProvider<>();
    public final ElementProvider<GuardianHeuristicRegistry> heuristicRegistryProvider = new ElementProvider<>();
    public final ElementProvider<GuardianPenaltyRegistry> penaltyRegistryProvider = new ElementProvider<>();
    public final ElementProvider<GuardianPhaseRegistry> phaseRegistryProvider = new ElementProvider<>();

    /* Manager Fields */
    public final ElementProvider<GuardianConfiguration> guardianConfigurationProvider = new ElementProvider<>();
    public final ElementProvider<GuardianSequenceManager> sequenceManagerProvider = new ElementProvider<>();
    public final ElementProvider<GuardianSequenceManager.SequenceTask> sequenceTaskProvider = new ElementProvider<>();

    public final ElementProvider<GuardianSequenceListener> sequenceListenerProvider = new ElementProvider<>();

    @Inject
    public GuardianPlugin(Logger logger, PluginContainer pluginContainer,
                          @DefaultConfig(sharedRoot = false) Path configDirectory) {
        this.logger = logger;
        this.pluginContainer = pluginContainer;
        this.configDirectory = configDirectory;

        this.bootstrap = new Bootstrap(this.logger, this);
    }

    @Listener
    public void onGameInitialization(GameInitializationEvent event) {
        this.bootstrap.addComponent("core", new CorePluginComponent(this.logger, this));

        this.bootstrap.send(Bootstrap.ComponentRequest.STARTUP,
                new SimpleComponentMessage(System.currentTimeMillis(), "Game initialization.", this),
                this.bootstrap.getIds());
    }

    public final Logger getLogger() {
        return this.logger;
    }

    public final PluginContainer getPluginContainer() {
        return this.pluginContainer;
    }

    public Path getConfigDirectory() {
        return this.configDirectory.getParent();
    }

    public final ModuleController<GuardianPlugin> getModuleController() {
        return this.moduleControllerProvider.get();
    }

    @Override
    public final SimpleEventBus<GuardianEvent, GuardianListener> getEventBus() {
        return this.eventBusProvider.get();
    }

    @Override
    public final GuardianState getState() {
        return this.stateProvider.get();
    }

    @Override
    public final DetectionRegistry getDetectionRegistry() {
        return this.detectionRegistryProvider.get();
    }

    @Override
    public final CheckRegistry getCheckRegistry() {
        return this.checkRegistryProvider.get();
    }

    @Override
    public final HeuristicRegistry getHeuristicRegistry() {
        return this.heuristicRegistryProvider.get();
    }

    @Override
    public final PenaltyRegistry getPenaltyRegistry() {
        return this.penaltyRegistryProvider.get();
    }

    @Override
    public final SequenceRegistry getSequenceRegistry() {
        return this.sequenceRegistryProvider.get();
    }

    @Override
    public final PhaseRegistry getPhaseRegistry() {
        return this.phaseRegistryProvider.get();
    }

    @Override
    public final SequenceManager<Event> getSequenceManager() {
        return null;
    }
}
