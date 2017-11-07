package io.github.connorhartley.guardian.launch.facet;

import com.ichorpowered.guardian.api.GuardianState;
import com.ichorpowered.guardian.api.SimpleGuardian;
import com.ichorpowered.guardian.api.event.GuardianEvent;
import com.ichorpowered.guardian.api.event.GuardianListener;
import com.ichorpowered.guardian.api.event.origin.Origin;
import com.me4502.modularframework.ModuleController;
import com.me4502.modularframework.ShadedModularFramework;
import io.github.connorhartley.guardian.GuardianConfiguration;
import io.github.connorhartley.guardian.GuardianLoader;
import io.github.connorhartley.guardian.GuardianPlugin;
import io.github.connorhartley.guardian.PluginInfo;
import io.github.connorhartley.guardian.detection.GuardianDetectionRegistry;
import io.github.connorhartley.guardian.detection.check.GuardianCheckRegistry;
import io.github.connorhartley.guardian.detection.heuristics.GuardianHeuristicRegistry;
import io.github.connorhartley.guardian.detection.penalty.GuardianPenaltyRegistry;
import io.github.connorhartley.guardian.event.state.GuardianInitializationEvent;
import io.github.connorhartley.guardian.event.state.GuardianPreInitializationEvent;
import io.github.connorhartley.guardian.launch.FacetBootstrap;
import io.github.connorhartley.guardian.launch.Facet;
import io.github.connorhartley.guardian.launch.FacetState;
import io.github.connorhartley.guardian.launch.exception.FacetException;
import io.github.connorhartley.guardian.util.property.PropertyInjector;
import io.github.connorhartley.guardian.util.property.PropertyInjectorFactory;
import io.github.connorhartley.guardian.launch.message.FacetMessage;
import io.github.connorhartley.guardian.launch.message.SimpleFacetMessage;
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

        PropertyInjector propertyInjector = PropertyInjectorFactory.create(this.plugin);

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

        GuardianConfiguration configuration = new GuardianConfiguration(this.plugin, this.plugin.getConfigDirectory());
        configuration.load();

        this.logger.info(this.facetPrefix + "Initializing registries.");

        GuardianDetectionRegistry detectionRegistry = new GuardianDetectionRegistry(this.plugin);
        GuardianCheckRegistry checkRegistry = new GuardianCheckRegistry(this.plugin);
        GuardianSequenceRegistry sequenceRegistry = new GuardianSequenceRegistry(this.plugin);
        GuardianHeuristicRegistry heuristicRegistry = new GuardianHeuristicRegistry(this.plugin);
        GuardianPenaltyRegistry penaltyRegistry = new GuardianPenaltyRegistry(this.plugin);
        GuardianPhaseRegistry phaseRegistry = new GuardianPhaseRegistry(this.plugin);

        this.logger.info(this.facetPrefix + "Initializing systems.");

        GuardianSequenceManager sequenceManager = new GuardianSequenceManager(this.plugin, sequenceRegistry);
        GuardianSequenceManager.SequenceTask sequenceTask = new GuardianSequenceManager.SequenceTask(this.plugin,
                sequenceManager);

        GuardianSequenceListener sequenceListener = new GuardianSequenceListener(this.plugin);
        GuardianLoader guardianLoader = new GuardianLoader(this.plugin);

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
        propertyInjector.inject("loader", guardianLoader);

        this.plugin.eventBus.post(new GuardianInitializationEvent(Origin.source(this.plugin.getPluginContainer()).build()));

        this.facetState = FacetState.START;
        return true;
    }

    public Boolean restart(SimpleFacetMessage message) {
        this.logger.info(ConsoleUtil.of("Restarted. Reason '{}' by '{}'.",
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
