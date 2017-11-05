package io.github.connorhartley.guardian;

import com.google.common.collect.Sets;
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
import io.github.connorhartley.guardian.launch.component.RegistryPluginComponent;
import io.github.connorhartley.guardian.launch.message.SimpleComponentMessage;
import io.github.connorhartley.guardian.phase.GuardianPhaseRegistry;
import io.github.connorhartley.guardian.sequence.GuardianSequenceRegistry;
import io.github.connorhartley.guardian.util.ObjectProvider;
import net.kyori.event.SimpleEventBus;
import org.slf4j.Logger;
import org.spongepowered.api.event.Event;
import org.spongepowered.api.event.Listener;
import org.spongepowered.api.event.game.state.GameInitializationEvent;
import org.spongepowered.api.plugin.Dependency;
import org.spongepowered.api.plugin.Plugin;
import org.spongepowered.api.plugin.PluginContainer;

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

    /* Bootstrap Fields */
    private final Bootstrap bootstrap;

    /* Core Fields */
    public final ObjectProvider<Long> timeProvider = new ObjectProvider<>();

    public final ObjectProvider<GuardianState> stateProvider = new ObjectProvider<>();
    public final ObjectProvider<SimpleEventBus<GuardianEvent, GuardianListener>> eventBusProvider = new ObjectProvider<>();
    public final ObjectProvider<ModuleController<GuardianPlugin>> moduleControllerProvider = new ObjectProvider<>();

    /* Registry Fields */
    public final ObjectProvider<GuardianDetectionRegistry> detectionRegistryProvider = new ObjectProvider<>();
    public final ObjectProvider<GuardianCheckRegistry> checkRegistryProvider = new ObjectProvider<>();
    public final ObjectProvider<GuardianSequenceRegistry> sequenceRegistryProvider = new ObjectProvider<>();
    public final ObjectProvider<GuardianHeuristicRegistry> heuristicRegistryProvider = new ObjectProvider<>();
    public final ObjectProvider<GuardianPenaltyRegistry> penaltyRegistryProvider = new ObjectProvider<>();
    public final ObjectProvider<GuardianPhaseRegistry> phaseRegistryProvider = new ObjectProvider<>();

    @Inject
    public GuardianPlugin(Logger logger, PluginContainer pluginContainer) {
        this.logger = logger;
        this.pluginContainer = pluginContainer;

        this.bootstrap = new Bootstrap(this.logger, this);
    }

    @Listener
    public void onGameInitialization(GameInitializationEvent event) {
        this.bootstrap.addComponent("core", new CorePluginComponent(this.logger, this));
        this.bootstrap.addComponent("registry", new RegistryPluginComponent(this.logger, this));

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
