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
import com.ichorpowered.guardian.api.detection.DetectionRegistry;
import com.ichorpowered.guardian.api.detection.check.CheckRegistry;
import com.ichorpowered.guardian.api.detection.heuristic.HeuristicRegistry;
import com.ichorpowered.guardian.api.detection.module.ModuleRegistry;
import com.ichorpowered.guardian.api.detection.penalty.PenaltyRegistry;
import com.ichorpowered.guardian.api.event.GuardianEvent;
import com.ichorpowered.guardian.api.event.GuardianListener;
import com.ichorpowered.guardian.api.sequence.SequenceManager;
import com.ichorpowered.guardian.api.sequence.SequenceRegistry;
import com.ichorpowered.guardian.api.util.ImplementationException;
import com.me4502.modularframework.ModuleController;
import io.github.connorhartley.guardian.detection.GuardianDetectionRegistry;
import io.github.connorhartley.guardian.detection.check.GuardianCheckRegistry;
import io.github.connorhartley.guardian.sequence.GuardianSequenceListener;
import io.github.connorhartley.guardian.sequence.GuardianSequenceManager;
import io.github.connorhartley.guardian.sequence.GuardianSequenceRegistry;
import io.github.connorhartley.guardian.util.CauseHelper;
import io.github.connorhartley.guardian.util.ConsoleFormatter;
import net.kyori.event.SimpleEventBus;
import org.bstats.MetricsLite;
import org.fusesource.jansi.Ansi;
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
import org.spongepowered.api.text.Text;
import org.spongepowered.api.text.format.TextColors;

import java.nio.file.Path;

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
                ),
                @Dependency(
                        id = "nucleus",
                        optional = true
                )
        }
)
public class GuardianPlugin implements Guardian<Event> {

    public static Text GUARDIAN_PREFIX = Text.of(TextColors.DARK_AQUA, "[", TextColors.AQUA, "GuardianPlugin",
            TextColors.DARK_AQUA, "] ", TextColors.RESET);

    /* Injection Fields */

    private final PluginContainer pluginContainer;
    private final MetricsLite metrics;
    private final Path configDir;
    private final Logger logger;

    /* System Fields */

    private GuardianState lifeState;
    private ModuleController<GuardianPlugin> moduleSubsystem;

    /* Registries / Managers */
    private GuardianDetectionRegistry detectionRegistry;
    private GuardianCheckRegistry checkRegistry;
    private GuardianSequenceRegistry sequenceRegistry;
    private GuardianSequenceManager sequenceManager;
    private GuardianLoader guardianLoader;

    /* Tasks */

    private GuardianSequenceManager.SequenceTask sequenceManagerTask;

    /* Event Handlers */

    private GuardianSequenceListener sequenceListener;

    /* Utilities */

    private final CauseHelper causeHelper;

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
        this.getLogger().info(ConsoleFormatter.builder()
                .bg(Ansi.Color.YELLOW, "                  START PHASE                  ")
                .build().get()
        );

        this.getLogger().info(ConsoleFormatter.builder()
                .of("Prerequisite Check                    ")
                .bg(Ansi.Color.YELLOW, "   20%   ")
                .build().get()
        );

        this.detectionRegistry = new GuardianDetectionRegistry(this);
        this.checkRegistry = new GuardianCheckRegistry(this);
        this.sequenceRegistry = new GuardianSequenceRegistry(this);

        this.lifeState = GuardianState.PRE_INITIALIZATION;

        // Post : PRE_INITIALIZATION Event

        this.getLogger().info(ConsoleFormatter.builder()
                .of("System Initialization                 ")
                .bg(Ansi.Color.YELLOW, "   40%   ")
                .build().get()
        );

        this.sequenceManager = new GuardianSequenceManager(this, this.sequenceRegistry);
        this.sequenceManagerTask = new GuardianSequenceManager.SequenceTask(this, this.sequenceManager);
        this.sequenceListener = new GuardianSequenceListener(this);
        this.guardianLoader = new GuardianLoader(this);

        this.lifeState = GuardianState.INITIALIZATION;

        // Post : INITIALIZATION Event
    }

    @Listener
    public void onServerStarting(GameStartingServerEvent event) {
        this.getLogger().info(ConsoleFormatter.builder()
                .of("Module Registration                   ")
                .bg(Ansi.Color.YELLOW, "   60%   ")
                .build().get()
        );

        // TODO: Register modules.

        this.lifeState = GuardianState.POST_INITIALIZATION;

        // Post : POST_INITIALIZATION Event

        this.getLogger().info(ConsoleFormatter.builder()
                .of("Test                                  ")
                .bg(Ansi.Color.YELLOW, "   80%   ")
                .build().get()
        );

        this.sequenceManagerTask.start();

        this.lifeState = GuardianState.STARTING;

        // Post : STARTING Event
    }

    @Listener
    public void onServerStarted(GameStartedServerEvent event) {
        this.getLogger().info(ConsoleFormatter.builder()
                .of("Start                                 ")
                .bg(Ansi.Color.YELLOW, "  100%   ")
                .build().get()
        );

        this.lifeState = GuardianState.STARTED;

        // Post : STARTED Event
    }

    public Logger getLogger() {
        return this.logger;
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
        return null;
    }

    @Override
    public CheckRegistry getCheckRegistry() {
        return null;
    }

    @Override
    public HeuristicRegistry getHeuristicRegistry() {
        return null;
    }

    @Override
    public PenaltyRegistry getPenaltyRegistry() {
        return null;
    }

    @Override
    public SequenceRegistry getSequenceRegistry() {
        return null;
    }

    @Override
    public SequenceManager<Event> getSequenceManager() {
        return this.sequenceManager;
    }

}
