package io.github.connorhartley.guardian.launch.component;

import com.ichorpowered.guardian.api.GuardianState;
import com.ichorpowered.guardian.api.SimpleGuardian;
import com.ichorpowered.guardian.api.event.GuardianEvent;
import com.ichorpowered.guardian.api.event.GuardianListener;
import com.ichorpowered.guardian.api.event.origin.Origin;
import com.me4502.modularframework.ModuleController;
import com.me4502.modularframework.ShadedModularFramework;
import io.github.connorhartley.guardian.GuardianConfiguration;
import io.github.connorhartley.guardian.GuardianPlugin;
import io.github.connorhartley.guardian.PluginInfo;
import io.github.connorhartley.guardian.detection.GuardianDetectionRegistry;
import io.github.connorhartley.guardian.detection.check.GuardianCheckRegistry;
import io.github.connorhartley.guardian.detection.heuristics.GuardianHeuristicRegistry;
import io.github.connorhartley.guardian.detection.penalty.GuardianPenaltyRegistry;
import io.github.connorhartley.guardian.event.state.GuardianInitializationEvent;
import io.github.connorhartley.guardian.event.state.GuardianPreInitializationEvent;
import io.github.connorhartley.guardian.launch.Bootstrap;
import io.github.connorhartley.guardian.launch.Component;
import io.github.connorhartley.guardian.launch.ComponentState;
import io.github.connorhartley.guardian.launch.exception.ComponentException;
import io.github.connorhartley.guardian.launch.message.ComponentMessage;
import io.github.connorhartley.guardian.launch.message.SimpleComponentMessage;
import io.github.connorhartley.guardian.phase.GuardianPhaseRegistry;
import io.github.connorhartley.guardian.sequence.GuardianSequenceListener;
import io.github.connorhartley.guardian.sequence.GuardianSequenceManager;
import io.github.connorhartley.guardian.sequence.GuardianSequenceRegistry;
import io.github.connorhartley.guardian.util.ConsoleUtil;
import net.kyori.event.ASMEventExecutorFactory;
import net.kyori.event.SimpleEventBus;
import org.fusesource.jansi.Ansi;
import org.slf4j.Logger;
import org.spongepowered.api.Platform;
import org.spongepowered.api.Sponge;

public class CorePluginComponent implements Component {

    private final Logger logger;
    private final GuardianPlugin plugin;

    private final String initPrefix = ConsoleUtil.of(Ansi.Color.CYAN, " INIT ");

    private ComponentState componentState = ComponentState.STOP;

    public CorePluginComponent(final Logger logger, final GuardianPlugin plugin) {
        this.logger = logger;
        this.plugin = plugin;
    }

    @Override
    public <I extends ComponentMessage, O> O handle(Bootstrap.ComponentRequest<I, O> componentRequest, I input) throws ComponentException {
        switch (componentRequest.getId()) {
            case 0: {
                if (input instanceof SimpleComponentMessage) return (O) this.componentState;
            }
            case 1: {
                if (input instanceof SimpleComponentMessage) return (O) this.startup((SimpleComponentMessage) input);
                else throw new ComponentException("Input was of an incorrect type.");
            }
            case 2: {
                // This component should not restart.
                return (O) Boolean.valueOf(true);
            }
            case 3: {
                if (input instanceof SimpleComponentMessage) return (O) this.shutdown((SimpleComponentMessage) input);
                else throw new ComponentException("Input was of an incorrect type.");
            }
        }
        return null;
    }

    public Boolean startup(SimpleComponentMessage componentMessage) {
        this.componentState = ComponentState.PREPARE;

        SimpleGuardian.setInstance(this.plugin);

        this.plugin.stateProvider.provide(GuardianState.PRE_INITIALIZATION);

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

        this.logger.info(ConsoleUtil.of("Loaded pre-component systems: { me4502/modularframework v1.8.5, kyoripowered/event v1.0.0 }"));

        // PROVIDE: PRE_INITIALIZATION
        this.plugin.timeProvider.provide(componentMessage.getTime());
        this.plugin.eventBusProvider.provide(eventBus);
        this.plugin.moduleControllerProvider.provide(moduleController);

        this.plugin.eventBusProvider.get().post(new GuardianPreInitializationEvent(Origin.source(this.plugin.getPluginContainer()).build()));

        this.plugin.stateProvider.provide(GuardianState.INITIALIZATION);

        // State: INITIALIZATION
        this.logger.info(this.initPrefix + "Initializing storage.");

        GuardianConfiguration configuration = new GuardianConfiguration(this.plugin, this.plugin.getConfigDirectory());
        configuration.load();

        this.logger.info(this.initPrefix + "Initializing registries.");

        GuardianDetectionRegistry detectionRegistry = new GuardianDetectionRegistry(this.plugin);
        GuardianCheckRegistry checkRegistry = new GuardianCheckRegistry(this.plugin);
        GuardianSequenceRegistry sequenceRegistry = new GuardianSequenceRegistry(this.plugin);
        GuardianHeuristicRegistry heuristicRegistry = new GuardianHeuristicRegistry(this.plugin);
        GuardianPenaltyRegistry penaltyRegistry = new GuardianPenaltyRegistry(this.plugin);
        GuardianPhaseRegistry phaseRegistry = new GuardianPhaseRegistry(this.plugin);

        this.logger.info(this.initPrefix + "Initializing systems.");

        GuardianSequenceManager sequenceManager = new GuardianSequenceManager(this.plugin, sequenceRegistry);
        GuardianSequenceManager.SequenceTask sequenceTask = new GuardianSequenceManager.SequenceTask(this.plugin,
                sequenceManager);

        GuardianSequenceListener sequenceListener = new GuardianSequenceListener(this.plugin);

        // PROVIDE: INITIALIZATION
        this.plugin.guardianConfigurationProvider.provide(configuration);

        this.plugin.detectionRegistryProvider.provide(detectionRegistry);
        this.plugin.checkRegistryProvider.provide(checkRegistry);
        this.plugin.sequenceRegistryProvider.provide(sequenceRegistry);
        this.plugin.heuristicRegistryProvider.provide(heuristicRegistry);
        this.plugin.penaltyRegistryProvider.provide(penaltyRegistry);
        this.plugin.phaseRegistryProvider.provide(phaseRegistry);

        this.plugin.sequenceManagerProvider.provide(sequenceManager);
        this.plugin.sequenceTaskProvider.provide(sequenceTask);

        this.plugin.sequenceListenerProvider.provide(sequenceListener);

        this.plugin.eventBusProvider.get().post(new GuardianInitializationEvent(Origin.source(this.plugin.getPluginContainer()).build()));

        this.componentState = ComponentState.START;

        return true;
    }

    public Boolean shutdown(SimpleComponentMessage componentMessage) {
        this.logger.info(ConsoleUtil.of("Guardian has shutdown."));

        this.componentState = ComponentState.STOP;

        return true;
    }

}
