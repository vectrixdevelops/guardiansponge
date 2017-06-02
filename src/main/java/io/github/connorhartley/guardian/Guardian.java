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

import com.google.common.reflect.TypeToken;
import com.google.inject.Inject;
import com.me4502.modularframework.ModuleController;
import com.me4502.modularframework.ShadedModularFramework;
import com.me4502.modularframework.module.ModuleWrapper;
import com.me4502.precogs.detection.DetectionType;
import com.me4502.precogs.service.AntiCheatService;
import io.github.connorhartley.guardian.data.DataKeys;
import io.github.connorhartley.guardian.data.tag.PunishmentTagData;
import io.github.connorhartley.guardian.detection.Detection;
import io.github.connorhartley.guardian.detection.check.Check;
import io.github.connorhartley.guardian.detection.check.CheckController;
import io.github.connorhartley.guardian.punishment.PunishmentController;
import io.github.connorhartley.guardian.sequence.Sequence;
import io.github.connorhartley.guardian.sequence.SequenceController;
import io.github.connorhartley.guardian.service.GuardianAntiCheatService;
import io.github.connorhartley.guardian.storage.configuration.TupleSerializer;
import ninja.leaping.configurate.ConfigurationOptions;
import ninja.leaping.configurate.commented.CommentedConfigurationNode;
import ninja.leaping.configurate.loader.ConfigurationLoader;
import ninja.leaping.configurate.objectmapping.serialize.TypeSerializers;
import org.bstats.MetricsLite;
import org.slf4j.Logger;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.config.ConfigDir;
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
import org.spongepowered.api.text.Text;
import org.spongepowered.api.text.format.TextColors;
import org.spongepowered.api.util.Tuple;
import tech.ferus.util.sql.h2.H2Database;
import tech.ferus.util.sql.mysql.MySqlDatabase;
import tech.ferus.util.sql.sqlite.SqliteDatabase;

import java.io.File;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Map;

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
public class Guardian {

    public static Text GUARDIAN_PREFIX = Text.of(TextColors.DARK_AQUA, "[", TextColors.AQUA, "Guardian",
            TextColors.DARK_AQUA, "] ", TextColors.RESET);

    /* Injection Fields */

    private final ConfigurationLoader<CommentedConfigurationNode> pluginConfigManager;
    private final Path pluginConfig;
    private final Logger logger;
    private final PluginContainer pluginContainer;
    private final MetricsLite metrics;

    /* Subsystem Fields */

    private ModuleController<Guardian> moduleSubsystem;

    /* Controller Fields */

    private PunishmentController punishmentController;
    private CheckController checkController;
    private SequenceController sequenceController;

    private CheckController.CheckControllerTask checkControllerTask;
    private SequenceController.SequenceControllerTask sequenceControllerTask;


    /* Service Fields */

    private GuardianPermission guardianPermission;
    private GuardianCommand guardianCommand;
    private GuardianConfiguration guardianConfiguration;
    private GuardianDetection guardianDetection;
    private GuardianDatabase guardianDatabase;

    /* Additional Fields */

    private int loggingLevel = 2;
    private Map<String, String> databaseCredentials;
    private ConfigurationOptions configurationOptions;

    @Inject
    public Guardian(@DefaultConfig(sharedRoot = false) ConfigurationLoader<CommentedConfigurationNode> pluginConfigManager,
                    @ConfigDir(sharedRoot = false) Path pluginConfig, PluginContainer pluginContainer, MetricsLite metrics,
                    Logger logger) {
        this.pluginConfigManager = pluginConfigManager;
        this.pluginContainer = pluginContainer;
        this.pluginConfig = pluginConfig;
        this.metrics = metrics;
        this.logger = logger;
    }

    @Listener
    public void onGameInitialize(GameInitializationEvent event) {
        getLogger().info("Starting Guardian AntiCheat " + this.pluginContainer.getVersion().get());

        Sponge.getServiceManager().setProvider(this, AntiCheatService.class, new GuardianAntiCheatService());

        DataRegistration.<PunishmentTagData, PunishmentTagData.Immutable>builder()
                .dataClass(PunishmentTagData.class)
                .immutableClass(PunishmentTagData.Immutable.class)
                .builder(new PunishmentTagData.Builder())
                .manipulatorId("punishmenttag")
                .dataName("GuardianPunishmentTag")
                .buildAndRegister(this.pluginContainer);

        TypeSerializers.getDefaultSerializers().registerType(new TypeToken<Tuple<?, ?>>() {}, new TupleSerializer());

        getLogger().info("Loading system.");

        this.moduleSubsystem = ShadedModularFramework.registerModuleController(this, Sponge.getGame());
        this.moduleSubsystem.setPluginContainer(pluginContainer);

        this.punishmentController = new PunishmentController(this);
        this.checkController = new CheckController(this);
        this.sequenceController = new SequenceController(this, this.checkController);

        this.checkControllerTask = new CheckController.CheckControllerTask(this, this.checkController);
        this.sequenceControllerTask = new SequenceController.SequenceControllerTask(this, this.sequenceController);

        this.guardianPermission = new GuardianPermission(this);
        this.guardianCommand = new GuardianCommand(this);
        this.guardianConfiguration = new GuardianConfiguration(this, pluginConfig, pluginConfigManager);
        this.guardianDetection = new GuardianDetection(this.moduleSubsystem);

        getLogger().info("Loading configuration.");

        this.configurationOptions = ConfigurationOptions.defaults();
        this.guardianConfiguration.create();

        this.loggingLevel = this.guardianConfiguration.configLoggingLevel.getValue();

        File detectionDirectory = new File(this.guardianConfiguration.getLocation().get().toFile(), "detection");
        detectionDirectory.mkdir();

        this.moduleSubsystem.setConfigurationDirectory(detectionDirectory);
        this.moduleSubsystem.setConfigurationOptions(this.configurationOptions);

        this.guardianPermission.register();
        this.guardianCommand.register();
        this.guardianDetection.register();

        getLogger().info("Loading database.");

        this.databaseCredentials = this.guardianConfiguration.configDatabaseCredentials.getValue();

        switch (this.databaseCredentials.get("type")) {
            case "h2": {
                this.guardianDatabase = new GuardianDatabase(this,
                        new H2Database(
                                new File(this.guardianConfiguration.getLocation().get().toFile()
                                        .toString(), this.databaseCredentials.get("host")).toString()
                        ));
            }
            case "mysql": {
                this.guardianDatabase = new GuardianDatabase(this,
                        new MySqlDatabase(
                                this.databaseCredentials.get("host"),
                                Integer.valueOf(this.databaseCredentials.get("port")),
                                "database",
                                this.databaseCredentials.get("username"),
                                this.databaseCredentials.get("password")
                        ));
            }
            case "sqlite": {
                this.guardianDatabase = new GuardianDatabase(this,
                        new SqliteDatabase(
                                new File(this.guardianConfiguration.getLocation().get().toFile()
                                        .toString(), this.databaseCredentials.get("host")).toString()
                        ));
            }
        }

        this.guardianDatabase.create();

        if (this.loggingLevel > 1 && this.moduleSubsystem.getModules().size() == 1) {
            getLogger().info("Discovered " + this.moduleSubsystem.getModules().size() + " module.");
        } else if (this.loggingLevel > 1) {
            getLogger().info("Discovered " + this.moduleSubsystem.getModules().size() + " modules.");
        }

        // Enable Modules

        this.moduleSubsystem.enableModules(moduleWrapper -> {
            if (this.guardianConfiguration.configEnabledDetections.getValue().contains(moduleWrapper.getId())) {
                if (this.loggingLevel > 1) getLogger().info("Enabled: " + moduleWrapper.getName() + " v" + moduleWrapper.getVersion());
                return true;
            }
            return false;
        });

        // Register Detection Checks and Service Instance

        this.moduleSubsystem.getModules().stream()
                .filter(ModuleWrapper::isEnabled)
                .forEach(moduleWrapper -> {
                    if (!moduleWrapper.getModule().isPresent()) return;
                    if (moduleWrapper.getModule().get() instanceof Detection) {
                        Detection detection = (Detection) moduleWrapper.getModule().get();

                        detection.getChecks().forEach(check -> this.getSequenceController().register(check));

                        Sponge.getRegistry().register(DetectionType.class, detection);
                    }
                });

        this.guardianConfiguration.update();
    }

    @Listener
    public void onServerStarted(GameStartedServerEvent event) {
        this.checkControllerTask.start();
        this.sequenceControllerTask.start();

        int loadedModules = 0;

        for (ModuleWrapper moduleWrapper : this.moduleSubsystem.getModules()) {
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
        this.guardianConfiguration.update();

        this.sequenceControllerTask.stop();
        this.checkControllerTask.stop();

        this.sequenceController.forceCleanup();

        this.moduleSubsystem.getModules().stream()
                .filter(ModuleWrapper::isEnabled)
                .forEach(moduleWrapper -> {
                    if (!moduleWrapper.getModule().isPresent()) return;
                    if (moduleWrapper.getModule().get() instanceof Detection) {
                        Detection detection = (Detection) moduleWrapper.getModule().get();

                        detection.getChecks().forEach(check -> this.getSequenceController().unregister(check));
                    }
                });

        this.moduleSubsystem.disableModules(moduleWrapper -> {
            if (this.loggingLevel > 1) getLogger().info("Disabled: " + moduleWrapper.getName() + " v" + moduleWrapper.getVersion());
            return true;
        });

        getLogger().info("Stopped Guardian AntiCheat.");
    }

    @Listener
    public void onReload(GameReloadEvent event) {
        getLogger().warn("Freezing detection checks.");

        this.sequenceControllerTask.stop();
        this.checkControllerTask.stop();

        this.sequenceController.forceCleanup();

        this.guardianConfiguration.load();

        this.sequenceControllerTask.register();

        this.sequenceControllerTask.start();
        this.checkControllerTask.start();

        getLogger().info("Unfreezed detection checks.");
    }

    /* Player Events */

    @Listener
    public void onClientDisconnect(ClientConnectionEvent.Disconnect event, @First User user, @First Player player) {
        this.sequenceController.forceCleanup(player);
        user.remove(DataKeys.GUARDIAN_PUNISHMENT_TAG);
    }

    /**
     * Get Logger
     *
     * <p>Returns the {@link Logger} for Guardian.</p>
     *
     * @return The guardian logger
     */
    public Logger getLogger() {
        return this.logger;
    }

    /**
     * Get Plugin Container
     *
     * <p>Returns the {@link PluginContainer} for Guardian.</p>
     *
     * @return The guardian plugin container
     */
    public PluginContainer getPluginContainer() {
        return this.pluginContainer;
    }

    /**
     * Get Configuration Options
     *
     * <p>Returns the {@link ConfigurationOptions} for Guardian.</p>
     *
     * @return The guardian configuration options
     */
    public ConfigurationOptions getConfigurationOptions() {
        return this.configurationOptions;
    }

    /**
     * Get Module Controller
     *
     * <p>Returns the {@link ModuleController} for Guardian.</p>
     *
     * @return The guardian module controller
     */
    public ModuleController<Guardian> getModuleController() {
        return this.moduleSubsystem;
    }

    /**
     * Get Global Configuration
     *
     * <p>Returns the configuration used by Guardian.</p>
     *
     * @return The guardian configuration
     */
    public GuardianConfiguration getGlobalConfiguration() {
        return this.guardianConfiguration;
    }

    /**
     * Get Global Detections
     *
     * <p>Returns the built-in {@link Detection}s by Guardian.</p>
     *
     * @return The guardian built-in detections
     */
    public GuardianDetection getGlobalDetections() {
        return this.guardianDetection;
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


    /**
     * Get CheckController Controller
     *
     * <p>Returns the {@link CheckController} for controlling the running of {@link Check}s for their {@link Sequence}.</p>
     *
     * @return The check controller
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

}
