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
package io.github.connorhartley.guardian;

import com.google.inject.Inject;
import com.ichorpowered.guardian.api.Guardian;
import com.ichorpowered.guardian.api.GuardianState;
import com.ichorpowered.guardian.api.detection.Detection;
import com.ichorpowered.guardian.api.detection.DetectionConfiguration;
import com.ichorpowered.guardian.api.detection.DetectionRegistry;
import com.ichorpowered.guardian.api.detection.check.Check;
import com.ichorpowered.guardian.api.detection.check.CheckRegistry;
import com.ichorpowered.guardian.api.detection.heuristic.HeuristicRegistry;
import com.ichorpowered.guardian.api.detection.module.ModuleRegistry;
import com.ichorpowered.guardian.api.detection.penalty.PenaltyRegistry;
import com.ichorpowered.guardian.api.event.GuardianEvent;
import com.ichorpowered.guardian.api.event.GuardianListener;
import com.ichorpowered.guardian.api.event.origin.Origin;
import com.ichorpowered.guardian.api.phase.PhaseManipulator;
import com.ichorpowered.guardian.api.phase.PhaseRegistry;
import com.ichorpowered.guardian.api.phase.type.PhaseTypes;
import com.ichorpowered.guardian.api.sequence.SequenceManager;
import com.ichorpowered.guardian.api.sequence.SequenceRegistry;
import com.ichorpowered.guardian.api.util.ImplementationException;
import com.me4502.modularframework.ModuleController;
import com.me4502.modularframework.ShadedModularFramework;
import com.me4502.modularframework.module.ModuleWrapper;
import com.me4502.precogs.detection.DetectionType;
import io.github.connorhartley.guardian.detection.AbstractDetection;
import io.github.connorhartley.guardian.detection.GuardianDetectionRegistry;
import io.github.connorhartley.guardian.detection.check.GuardianCheckRegistry;
import io.github.connorhartley.guardian.detection.penalty.GuardianPenaltyRegistry;
import io.github.connorhartley.guardian.event.state.GuardianInitializationEvent;
import io.github.connorhartley.guardian.event.state.GuardianPostInitialization;
import io.github.connorhartley.guardian.event.state.GuardianPreInitializationEvent;
import io.github.connorhartley.guardian.event.state.GuardianStartedEvent;
import io.github.connorhartley.guardian.event.state.GuardianStartingEvent;
import io.github.connorhartley.guardian.phase.GuardianPhaseRegistry;
import io.github.connorhartley.guardian.sequence.GuardianSequenceListener;
import io.github.connorhartley.guardian.sequence.GuardianSequenceManager;
import io.github.connorhartley.guardian.sequence.GuardianSequenceRegistry;
import io.github.connorhartley.guardian.util.CauseHelper;
import io.github.connorhartley.guardian.util.ConsoleFormatter;
import net.kyori.event.ASMEventExecutorFactory;
import net.kyori.event.SimpleEventBus;
import ninja.leaping.configurate.objectmapping.ObjectMappingException;
import org.bstats.MetricsLite;
import org.fusesource.jansi.Ansi;
import org.slf4j.Logger;
import org.spongepowered.api.Platform;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.config.DefaultConfig;
import org.spongepowered.api.event.Event;
import org.spongepowered.api.event.Listener;
import org.spongepowered.api.event.game.state.GameInitializationEvent;
import org.spongepowered.api.event.game.state.GameStartedServerEvent;
import org.spongepowered.api.event.game.state.GameStartingServerEvent;
import org.spongepowered.api.plugin.Dependency;
import org.spongepowered.api.plugin.Plugin;
import org.spongepowered.api.plugin.PluginContainer;
import org.spongepowered.api.text.Text;
import org.spongepowered.api.text.format.TextColors;

import java.nio.file.Path;
import java.util.concurrent.atomic.AtomicInteger;

import javax.annotation.Nullable;

@Plugin(
        id = PluginInfo.ID,
        name = PluginInfo.NAME,
        version = PluginInfo.VERSION,
        description = PluginInfo.DESCRIPTION,
        authors = {
                "Connor Hartley (vectrix)",
                "Matthew Miller (me4502)"
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

    public static Text GUARDIAN_PREFIX = Text.of(TextColors.DARK_AQUA, "[", TextColors.AQUA, "Guardian",
            TextColors.DARK_AQUA, "] ", TextColors.RESET);

    /* Injection Fields */

    private final PluginContainer pluginContainer;
    private final MetricsLite metrics;
    private final Path configDir;
    private final Logger logger;

    /* System Fields */

    private GuardianState lifeState;
    private ModuleController<GuardianPlugin> moduleSubsystem;

    /* Event System */
    private SimpleEventBus<GuardianEvent, GuardianListener> eventBus;


    /* Registries / Managers */
    private GuardianConfiguration configuration;
    private GuardianDetectionRegistry detectionRegistry;
    private GuardianCheckRegistry checkRegistry;
    private GuardianSequenceRegistry sequenceRegistry;
    private GuardianPenaltyRegistry penaltyRegistry;
    private GuardianPhaseRegistry phaseRegistry;
    private GuardianSequenceManager sequenceManager;
    private GuardianLoader guardianLoader;

    /* Tasks */

    private GuardianSequenceManager.SequenceTask sequenceManagerTask;

    /* Event Handlers */

    private GuardianSequenceListener sequenceListener;

    /* Utilities */

    private final CauseHelper causeHelper;

    /* Console Prefixes */

    private final String initializationPrefix = ConsoleFormatter.builder().fg(Ansi.Color.BLUE, " INIT ").buildAndGet();

    @Inject
    public GuardianPlugin(PluginContainer pluginContainer, Logger logger, MetricsLite metrics,
                          @DefaultConfig(sharedRoot = false) Path configDir) {
        this.pluginContainer = pluginContainer;
        this.logger = logger;
        this.metrics = metrics;
        this.configDir = configDir;
        this.causeHelper = new CauseHelper(this);
    }

    @Listener
    public void onGameInitialization(GameInitializationEvent event) {
        this.getLogger().warn(ConsoleFormatter.builder()
                .fg(Ansi.Color.RED, "You are using an extremely EXPERIMENTAL build of Guardian. \n" +
                        "It is advised you stick to stable build UNLESS you are testing this in a controlled environment!")
                .buildAndGet()
        );

        this.getLogger().info("Guardian v" + this.pluginContainer.getVersion().orElse("UNKNOWN") + " for Minecraft " +
                Sponge.getPlatform().getContainer(Platform.Component.GAME).getVersion().orElse("UNKNOWN") + " in SpongeAPI " +
                Sponge.getPlatform().getContainer(Platform.Component.API).getVersion().orElse("UNKNOWN").substring(0, 5)
        );

        this.getLogger().info("Subsystems [ me4502/modularframework v1.8.5, kyoripowered/event v1.0.0 ]");

        this.getLogger().info(this.initializationPrefix + " Running pre system analysis.");

        this.moduleSubsystem = ShadedModularFramework.registerModuleController(this, Sponge.getGame());
        this.moduleSubsystem.setPluginContainer(this.pluginContainer);

        ASMEventExecutorFactory<GuardianEvent, GuardianListener> eventExecutor = new ASMEventExecutorFactory<>();
        this.eventBus = new SimpleEventBus<>(eventExecutor);

        this.getLogger().info(this.initializationPrefix + " Running registry initialization.");

        this.detectionRegistry = new GuardianDetectionRegistry(this);
        this.checkRegistry = new GuardianCheckRegistry(this);
        this.sequenceRegistry = new GuardianSequenceRegistry(this);
        this.penaltyRegistry = new GuardianPenaltyRegistry(this);
        this.phaseRegistry = new GuardianPhaseRegistry(this);

        this.lifeState = GuardianState.PRE_INITIALIZATION;

        // Post : PRE_INITIALIZATION Event
        this.eventBus.post(new GuardianPreInitializationEvent(this, Origin.source(this.pluginContainer).build()));

        this.getLogger().info(this.initializationPrefix + " Running storage initialization.");

        this.configuration = new GuardianConfiguration(this, this.getConfigDirectory());
        this.configuration.load();

        this.getLogger().info(this.initializationPrefix + " Running system initialization.");

        this.sequenceManager = new GuardianSequenceManager(this, this.sequenceRegistry);
        this.sequenceManagerTask = new GuardianSequenceManager.SequenceTask(this, this.sequenceManager);
        this.sequenceListener = new GuardianSequenceListener(this);
        this.guardianLoader = new GuardianLoader(this);

        this.lifeState = GuardianState.INITIALIZATION;

        // Post : INITIALIZATION Event
        this.eventBus.post(new GuardianInitializationEvent(this, Origin.source(this.pluginContainer).build()));
    }

    @Listener
    public void onServerStarting(GameStartingServerEvent event) {
        this.getLogger().info(this.initializationPrefix + " Running module registration.");

        this.guardianLoader.loadPenalties();
        this.guardianLoader.loadChecks();
        this.guardianLoader.loadModules(this.moduleSubsystem);
        this.guardianLoader.loadPhases();

        this.getLogger().info(ConsoleFormatter.builder()
                .fg(Ansi.Color.YELLOW, "Discovered " + this.moduleSubsystem.getModules().size() + " module(s).")
                .buildAndGet()
        );

        this.moduleSubsystem.enableModules(moduleWrapper -> {
            try {
                if (this.configuration.getEnabledModules().contains(moduleWrapper.getId())) return true;
            } catch (ObjectMappingException e) {
                this.getLogger().error("Failed to acquire enable modules list from the configuration.", e);
            }
            return false;
        });

        AtomicInteger detections = new AtomicInteger(0);
        AtomicInteger checks = new AtomicInteger(0);
        AtomicInteger heuristics = new AtomicInteger(0);
        AtomicInteger penalties = new AtomicInteger(0);

        this.moduleSubsystem.getModules().stream()
                .filter(ModuleWrapper::isEnabled)
                .forEach(moduleWrapper -> {
                    if (!moduleWrapper.getModule().isPresent()) return;
                    if (moduleWrapper.getModule().get() instanceof Detection) {
                        AbstractDetection detection = (AbstractDetection) moduleWrapper.getModule().get();

                        this.eventBus.register(detection);

                        Sponge.getRegistry().register(DetectionType.class, detection);

                        PhaseManipulator detectionManipulator = detection.getPhaseManipulator();

                        while (detectionManipulator.hasNext(PhaseTypes.CHECK)) {
                            Check<GuardianPlugin, DetectionConfiguration> check = detectionManipulator.next(PhaseTypes.CHECK);
                            this.getSequenceRegistry().put(this, check.getClass(), check.getSequence());
                        }

                        checks.addAndGet(detectionManipulator.size(PhaseTypes.CHECK));
                        penalties.addAndGet(detectionManipulator.size(PhaseTypes.PENALTY));

                        detections.incrementAndGet();
                    }
                });

        this.getLogger().info(ConsoleFormatter.builder()
                .fg(Ansi.Color.YELLOW, "Loaded " + penalties.get() + " punishment(s), " + heuristics.get() +
                        " heuristic(s) and " + checks.get() + " check(s) for " + detections.get() + " detection(s).")
                .buildAndGet()
        );

        this.lifeState = GuardianState.POST_INITIALIZATION;

        // Post : POST_INITIALIZATION Event
        this.eventBus.post(new GuardianPostInitialization(this, Origin.source(this.pluginContainer).build()));

        this.getLogger().info(this.initializationPrefix + " Running post system analysis.");

        this.sequenceManagerTask.start();

        this.lifeState = GuardianState.STARTING;

        // Post : STARTING Event
        this.eventBus.post(new GuardianStartingEvent(this, Origin.source(this.pluginContainer).build()));
    }

    @Listener
    public void onServerStarted(GameStartedServerEvent event) {
        this.getLogger().info(this.initializationPrefix + " Completed initialization.");

        this.lifeState = GuardianState.STARTED;

        // Post : STARTED Event
        this.eventBus.post(new GuardianStartedEvent(this, Origin.source(this.pluginContainer).build()));
    }

    public Logger getLogger() {
        return this.logger;
    }

    public Path getConfigDirectory() {
        return this.configDir.getParent();
    }

    @Override
    public <T extends Guardian> T getInstance(@Nullable Class<T> aClass) throws ImplementationException {
        if (aClass == null || !aClass.isInstance(this)) throw new ImplementationException("Could not assign instance from class " + aClass);
        return (T) this;
    }

    @Override
    public GuardianState getState() {
        return this.lifeState;
    }

    @Override
    public SimpleEventBus<GuardianEvent, GuardianListener> getEventBus() {
        return null;
    }

    @Override
    public ModuleRegistry getModuleRegistry() {
        return null;
    }

    @Override
    public DetectionRegistry getDetectionRegistry() {
        return this.detectionRegistry;
    }

    @Override
    public CheckRegistry getCheckRegistry() {
        return this.checkRegistry;
    }

    @Override
    public HeuristicRegistry getHeuristicRegistry() {
        return null;
    }

    @Override
    public PenaltyRegistry getPenaltyRegistry() {
        return this.penaltyRegistry;
    }

    @Override
    public SequenceRegistry getSequenceRegistry() {
        return this.sequenceRegistry;
    }

    @Override
    public PhaseRegistry getPhaseRegistry() {
        return this.phaseRegistry;
    }

    @Override
    public SequenceManager<Event> getSequenceManager() {
        return this.sequenceManager;
    }

}
