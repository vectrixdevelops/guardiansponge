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
import com.me4502.modularframework.ModuleController;
import com.me4502.modularframework.ShadedModularFramework;
import com.me4502.modularframework.module.ModuleWrapper;
import com.me4502.precogs.detection.DetectionType;
import com.me4502.precogs.service.AntiCheatService;
import io.github.connorhartley.guardian.context.Context;
import io.github.connorhartley.guardian.context.ContextController;
import io.github.connorhartley.guardian.context.ContextProvider;
import io.github.connorhartley.guardian.data.DataKeys;
import io.github.connorhartley.guardian.data.tag.PunishmentTagData;
import io.github.connorhartley.guardian.detection.Detection;
import io.github.connorhartley.guardian.detection.check.Check;
import io.github.connorhartley.guardian.detection.check.CheckController;
import io.github.connorhartley.guardian.punishment.PunishmentController;
import io.github.connorhartley.guardian.sequence.Sequence;
import io.github.connorhartley.guardian.sequence.SequenceController;
import io.github.connorhartley.guardian.sequence.action.Action;
import io.github.connorhartley.guardian.service.GuardianAntiCheatService;
import ninja.leaping.configurate.ConfigurationOptions;
import ninja.leaping.configurate.commented.CommentedConfigurationNode;
import ninja.leaping.configurate.loader.ConfigurationLoader;
import org.bstats.MetricsLite;
import org.slf4j.Logger;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.config.DefaultConfig;
import org.spongepowered.api.data.DataRegistration;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.entity.living.player.User;
import org.spongepowered.api.event.Listener;
import org.spongepowered.api.event.filter.cause.First;
import org.spongepowered.api.event.game.GameReloadEvent;
import org.spongepowered.api.event.game.state.GameInitializationEvent;
import org.spongepowered.api.event.game.state.GameStartedServerEvent;
import org.spongepowered.api.event.game.state.GameStoppingEvent;
import org.spongepowered.api.event.network.ClientConnectionEvent;
import org.spongepowered.api.plugin.Dependency;
import org.spongepowered.api.plugin.Plugin;
import org.spongepowered.api.plugin.PluginContainer;

import java.io.File;

@Plugin(
        id = GuardianInfo.ID,
        name = GuardianInfo.NAME,
        version = GuardianInfo.VERSION,
        description = GuardianInfo.DESCRIPTION,
        authors = {
                "Connor Hartley (vectrix)",
                "Matthew Miller (me4502)"
        },
        dependencies = {
                @Dependency(
                        id = "precogs"
                ),
                @Dependency(
                        id = "elderguardian",
                        version = GuardianInfo.ELDER_VERSION,
                        optional = true
                )
        }
)
public class Guardian implements ContextProvider {

    /* Logger */

    @Inject
    private Logger logger;

    public Logger getLogger() {
        return this.logger;
    }

    private int loggingLevel = 2;

    /* Plugin Instance */

    @Inject
    private PluginContainer pluginContainer;

    public PluginContainer getPluginContainer() {
        return this.pluginContainer;
    }

    /* Plugin Configuration */

    @Inject
    @DefaultConfig(sharedRoot = false)
    private File pluginConfig;

    @Inject
    @DefaultConfig(sharedRoot = false)
    private ConfigurationLoader<CommentedConfigurationNode> pluginConfigManager;

    private ConfigurationOptions configurationOptions;

    public ConfigurationOptions getConfigurationOptions() {
        return this.configurationOptions;
    }

    /* Plugin Metrics */

    @Inject
    private MetricsLite metricsLite;

    /* Module System */

    private ModuleController<Guardian> moduleController;

    public ModuleController<Guardian> getModuleController() {
        return this.moduleController;
    }

    /* Command */

    private GuardianCommand globalCommand;

    /* Configuration */

    private GuardianConfiguration globalConfiguration;

    /* Contexts */

    private GuardianContexts globalContexts;

    /* Detections */

    private GuardianDetections globalDetections;

    /* Context / Check / Sequence / Punishment */

    private PunishmentController punishmentController;
    private ContextController contextController;
    private CheckController checkController;
    private SequenceController sequenceController;

    private ContextController.ContextControllerTask contextControllerTask;
    private CheckController.CheckControllerTask checkControllerTask;
    private SequenceController.SequenceControllerTask sequenceControllerTask;

    /* Game Events */

    @Listener
    public void onGameInitialize(GameInitializationEvent event) {
        getLogger().info("Starting Guardian AntiCheat " + this.pluginContainer.getVersion().get());

        // Register Data

        Sponge.getServiceManager().setProvider(this, AntiCheatService.class, new GuardianAntiCheatService());

        DataRegistration.<PunishmentTagData, PunishmentTagData.Immutable>builder()
                .dataClass(PunishmentTagData.class)
                .immutableClass(PunishmentTagData.Immutable.class)
                .builder(new PunishmentTagData.Builder())
                .manipulatorId("punishmenttag")
                .buildAndRegister(this.pluginContainer);

        getLogger().info("Loading controllers.");

        // Register Controllers and Controller Tasks

        this.punishmentController = new PunishmentController(this);
        this.contextController = new ContextController(this);
        this.checkController = new CheckController(this);
        this.sequenceController = new SequenceController(this, this.checkController);

        this.contextControllerTask = new ContextController.ContextControllerTask(this, this.contextController);
        this.checkControllerTask = new CheckController.CheckControllerTask(this, this.checkController);
        this.sequenceControllerTask = new SequenceController.SequenceControllerTask(this, this.sequenceController);

        getLogger().info("Loading configurations.");

        // Load Configurations

        this.globalConfiguration = new GuardianConfiguration(this, this.pluginConfig, this.pluginConfigManager);
        this.configurationOptions = ConfigurationOptions.defaults();
        this.globalConfiguration.create();

        this.loggingLevel = this.globalConfiguration.configLoggingLevel.getValue();

        getLogger().info("Discovering internal detections.");

        // Register Internal Detections

        this.moduleController = ShadedModularFramework.registerModuleController(this, Sponge.getGame());
        this.moduleController.setPluginContainer(this.pluginContainer);

        // Set Internal Detection Configuration Directory and Options

        File detectionDirectory = new File(this.globalConfiguration.getLocation().getParentFile(), "detection");
        detectionDirectory.mkdir();

        this.moduleController.setConfigurationDirectory(detectionDirectory);
        this.moduleController.setConfigurationOptions(this.configurationOptions);

        // Register Guardian Commands

        this.globalCommand = new GuardianCommand(this);
        this.globalCommand.register();

        // Register Guardian Contexts

        this.globalContexts = new GuardianContexts(this.contextController);
        this.globalContexts.register();

        // Register Guardian Detections

        this.globalDetections = new GuardianDetections(this.moduleController);
        this.globalDetections.register();

        if (this.loggingLevel > 1 && this.moduleController.getModules().size() == 1) {
            getLogger().info("Discovered " + this.moduleController.getModules().size() + " module.");
        } else if (this.loggingLevel > 1) {
            getLogger().info("Discovered " + this.moduleController.getModules().size() + " modules.");
        }

        // Enable Modules

        this.moduleController.enableModules(moduleWrapper -> {
            if (this.globalConfiguration.configEnabledDetections.getValue().contains(moduleWrapper.getId())) {
                if (this.loggingLevel > 1) getLogger().info("Enabled: " + moduleWrapper.getName() + " v" + moduleWrapper.getVersion());
                return true;
            }
            return false;
        });

        // Register Detection Checks and Service Instance

        this.moduleController.getModules().stream()
                .filter(ModuleWrapper::isEnabled)
                .forEach(moduleWrapper -> {
                    if (!moduleWrapper.getModule().isPresent()) return;
                    if (moduleWrapper.getModule().get() instanceof Detection) {
                        Detection<Guardian> detection = (Detection) moduleWrapper.getModule().get();

                        detection.getChecks().forEach(check -> this.getSequenceController().register(check));

                        Sponge.getRegistry().register(DetectionType.class, detection);
                    }
                });

        this.globalConfiguration.update();
    }

    @Listener
    public void onServerStarted(GameStartedServerEvent event) {
        // Start Controller Tasks

        this.contextControllerTask.start();
        this.checkControllerTask.start();
        this.sequenceControllerTask.start();

        int loadedModules = 0;

        for (ModuleWrapper moduleWrapper : this.moduleController.getModules()) {
            if (moduleWrapper.isEnabled()) {
                loadedModules += 1;
            }
        }

        if (loadedModules == 1) {
            getLogger().info(loadedModules + " detection is protecting your server!");
        } else {
            getLogger().info(loadedModules + " detections are protecting your server!");
        }
    }

    @Listener
    public void onServerStopping(GameStoppingEvent event) {
        getLogger().info("Stopping Guardian AntiCheat.");
        this.globalConfiguration.update();

        this.sequenceControllerTask.stop();
        this.checkControllerTask.stop();
        this.contextControllerTask.stop();

        this.sequenceController.forceCleanup();

        this.moduleController.getModules().stream()
                .filter(ModuleWrapper::isEnabled)
                .forEach(moduleWrapper -> {
                    if (!moduleWrapper.getModule().isPresent()) return;
                    if (moduleWrapper.getModule().get() instanceof Detection) {
                        Detection<Guardian> detection = (Detection) moduleWrapper.getModule().get();

                        detection.getChecks().forEach(check -> this.getSequenceController().unregister(check));
                    }
                });

        this.moduleController.disableModules(moduleWrapper -> {
            if (this.loggingLevel > 1) getLogger().info("Disabled: " + moduleWrapper.getName() + " v" + moduleWrapper.getVersion());
            return true;
        });

        this.contextController.getContextRegistry().clear();

        getLogger().info("Stopped Guardian AntiCheat.");
    }

    @Listener
    public void onReload(GameReloadEvent event) {
        getLogger().warn("Freezing detection checks.");

        this.sequenceControllerTask.stop();
        this.checkControllerTask.stop();
        this.contextControllerTask.stop();

        this.sequenceController.forceCleanup();

        this.globalConfiguration.load();

        this.sequenceControllerTask.register();

        this.sequenceControllerTask.start();
        this.checkControllerTask.start();
        this.contextControllerTask.start();

        getLogger().info("Unfreezed detection checks.");
    }

    /* Player Events */

    @Listener
    public void onClientDisconnect(ClientConnectionEvent.Disconnect event, @First User user, @First Player player) {
        this.contextController.suspendFor(player);
        this.sequenceController.forceCleanup(player);
        user.remove(DataKeys.GUARDIAN_PUNISHMENT_TAG);
    }

    /**
     * Get Logging Level
     *
     * <p>Returns the logging level.</p>
     *
     * @return The logging level
     */
    public int getLoggingLevel() {
        return this.loggingLevel;
    }

    /**
     * Set Logging Level
     *
     * <p>Sets the logging level.</p>
     *
     * @param level The logging level
     */
    public void setLoggingLevel(int level) {
        if (level > 0 && level < 4) {
            this.loggingLevel = level;
        }
    }

    /**
     * Get Global Configuration
     *
     * <p>Returns the configuration used by Guardian.</p>
     *
     * @return The guardian configuration
     */
    public GuardianConfiguration getGlobalConfiguration() {
        return this.globalConfiguration;
    }

    /**
     * Get Global Detections
     *
     * <p>Returns the built-in {@link Detection}s by Guardian.</p>
     *
     * @return The guardian built-in detections
     */
    public GuardianDetections getGlobalDetections() {
        return this.globalDetections;
    }

    /**
     * Get Punishment Controller
     *
     * <p>Returns the built-in {@link PunishmentController} for controlling the punishments.</p>
     *
     * @return The punishment controller
     */
    public PunishmentController getPunishmentController() {
        return this.punishmentController;
    }

    @Override
    public ContextController getContextController() {
        return this.contextController;
    }

    /**
     * Get Context Controller
     *
     * <p>Returns the {@link ContextController} for controlling the running of {@link Context}s for {@link Action}s.</p>
     *
     * @return The context controller
     */
    public CheckController getCheckController() {
        return this.checkController;
    }

    /**
     * Get Sequence Controller
     *
     * <p>Returns the {@link SequenceController} for controlling the running of {@link Sequence}s for {@link Check}s.</p>
     *
     * @return The sequence controller
     */
    public SequenceController getSequenceController() {
        return this.sequenceController;
    }

}
