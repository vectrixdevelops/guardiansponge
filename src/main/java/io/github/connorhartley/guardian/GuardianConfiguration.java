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
import io.github.connorhartley.guardian.storage.StorageProvider;
import io.github.connorhartley.guardian.storage.configuration.CommentDocument;
import io.github.connorhartley.guardian.storage.container.ConfigurationValue;
import io.github.connorhartley.guardian.storage.container.StorageKey;
import ninja.leaping.configurate.ConfigurationNode;
import ninja.leaping.configurate.commented.CommentedConfigurationNode;
import ninja.leaping.configurate.loader.ConfigurationLoader;
import org.apache.commons.lang3.StringUtils;
import tech.ferus.util.config.ConfigKey;

import java.io.IOException;
import java.nio.file.Path;
import java.time.LocalDate;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * Guardian Configuration
 *
 * Represents the configuration implementation for Guardian.
 */
public final class GuardianConfiguration implements StorageProvider<Path> {

    private CommentedConfigurationNode configurationNode;

    private final Guardian plugin;
    private final Path configFile;
    private final ConfigurationLoader<CommentedConfigurationNode> configManager;

    private CommentDocument globalHeader;

    public ConfigurationValue<String, Integer> configHeader;
    public ConfigurationValue<String, Map<String, String>> configDatabaseCredentials;
    public ConfigurationValue<String, Boolean> configDatabaseMigration;
    public ConfigurationValue<String, List<String>> configEnabledDetections;
    public ConfigurationValue<String, Integer> configLoggingLevel;

    GuardianConfiguration(Guardian plugin, Path configFile, ConfigurationLoader<CommentedConfigurationNode> configManager) {
        this.plugin = plugin;
        this.configFile = configFile;
        this.configManager = configManager;
    }

    @Override
    public void create() {
        try {
            if (!this.exists()) {
                this.configFile.toFile().mkdirs();
                this.configFile.toFile().createNewFile();
            }

            this.configurationNode = this.configManager.load(this.plugin.getConfigurationOptions());

            this.globalHeader = new CommentDocument(50, " ");
            this.globalHeader.addLogo(CommentDocument.LOGO)
                    .addHeader("AntiCheat")
                    .addParagraph(new String[]{
                            StringUtils.join("Date: ", LocalDate.now().toString()),
                            StringUtils.join("Version: ", this.plugin.getPluginContainer().getVersion().orElse("unknown")),
                            StringUtils.join("Authors: ", StringUtils.join(this.plugin.getPluginContainer().getAuthors(), ", ")),
                            ""
                    })
                    .addParagraph(new String[]{
                            "Guardian is an extensible AntiCheat for Sponge ",
                            "that gives you the flexibility required to customize ",
                            "the cheat detections to for your servers game play.",
                            ""
                    })
                    .addParagraph(new String[]{
                            "GitHub: https://github.com/ichorpowered/guardian",
                            "Discord: https://discord.gg/pvSFtMm",
                            ""
                    })
                    .addHeader("Global Configuration")
                    .addParagraph(new String[]{
                            "This is the global configuration for Guardian.",
                            "You will be able to set global options as well ",
                            "as core plugin properties. It is advised that ",
                            "you take caution when modifying this file."
                    });

            this.configHeader = new ConfigurationValue<>(new StorageKey<>("a"),
                    this.globalHeader.export(),
                    1, new TypeToken<Integer>() {
            });

            Map<String, String> databaseCredentials = new HashMap<>();
            databaseCredentials.put("type", "h2");
            databaseCredentials.put("version", "1");
            databaseCredentials.put("host", "database.db");
            databaseCredentials.put("port", "3306");
            databaseCredentials.put("username", "sql-admin");
            databaseCredentials.put("password", "secret-password");

            this.configDatabaseCredentials = new ConfigurationValue<>(new StorageKey<>("database-credentials"),
                    new CommentDocument(50, " ")
                            .addHeader("Database Credentials")
                            .addParagraph(new String[]{
                                    "Allows you to set the database type, address, username and password of ",
                                    "the database you want Guardian to use."
                            })
                            .export(),
                    databaseCredentials, new TypeToken<Map<String, String>>() {
            });

            this.configDatabaseMigration = new ConfigurationValue<>(new StorageKey<>("auto-migration"),
                    new CommentDocument(50, " ")
                                    .addHeader("Auto Migration")
                                    .addParagraph(new String[]{
                                            "Set whether the database should automatically migrate after a minor ",
                                            "change in database format.",
                                            "",
                                            "This MAY not cover major changes in databases, which may require to be ",
                                            "migrated manually."
                                    })
                                    .export(),
                    true, new TypeToken<Boolean>() {
            });

            this.configEnabledDetections = new ConfigurationValue<>(new StorageKey<>("enabled"),
                    new CommentDocument(50, " ")
                                    .addHeader("Enabled Modules")
                                    .addParagraph(new String[]{
                                            "Module ID's placed in here will be enabled ",
                                            "on startup! These are only for internal Guardian ",
                                            "detections.",
                                            "",
                                            "More information will be provided soon."
                                    })
                                    .export(),
                    Arrays.asList("speed", "fly", "jesus", "invalidmovement"), new TypeToken<List<String>>() {
            });

            this.configLoggingLevel = new ConfigurationValue<>(new StorageKey<>("logging-level"),
                    new CommentDocument(50, " ")
                                    .addHeader("Logging Level")
                                    .addParagraph(new String[]{
                                            "Sets the logging level of core and detections ",
                                            "for Guardian and its internal modules.",
                                            "",
                                            "1 for basic logging, 2 for more logging, 3 for detailed logging.",
                                            "",
                                            "More information will be provided soon."
                                    })
                                    .export(),
                    2, new TypeToken<Integer>() {
            });

            this.configHeader.<ConfigurationNode>createStorage(this.configurationNode);
            this.configDatabaseCredentials.<ConfigurationNode>createStorage(this.configurationNode);
            this.configDatabaseMigration.<ConfigurationNode>createStorage(this.configurationNode);
            this.configEnabledDetections.<ConfigurationNode>createStorage(this.configurationNode);
            this.configLoggingLevel.<ConfigurationNode>createStorage(this.configurationNode);

            this.configManager.save(this.configurationNode);
        } catch (Exception e) {
            this.plugin.getLogger().error("A problem occurred attempting to create Guardians global configuration!", e);
        }
    }

    @Override
    public void load() {
        try {
            if (this.exists()) {
                this.configurationNode = this.configManager.load(this.plugin.getConfigurationOptions());

                this.configHeader.<ConfigurationNode>loadStorage(this.configurationNode);
                this.configDatabaseCredentials.<ConfigurationNode>loadStorage(this.configurationNode);
                this.configDatabaseMigration.<ConfigurationNode>loadStorage(this.configurationNode);
                this.configEnabledDetections.<ConfigurationNode>loadStorage(this.configurationNode);
                this.configLoggingLevel.<ConfigurationNode>loadStorage(this.configurationNode);

                this.configManager.save(this.configurationNode);
            }
        } catch (IOException e) {
            this.plugin.getLogger().error("A problem occurred attempting to load Guardians global configuration!", e);
        }
    }

    @Override
    public void update() {
        try {
            if (this.exists()) {
                this.configurationNode = this.configManager.load(this.plugin.getConfigurationOptions());

                this.configHeader.<ConfigurationNode>updateStorage(this.configurationNode);
                this.configDatabaseCredentials.<ConfigurationNode>updateStorage(this.configurationNode);
                this.configDatabaseMigration.<ConfigurationNode>updateStorage(this.configurationNode);
                this.configEnabledDetections.<ConfigurationNode>updateStorage(this.configurationNode);
                this.configLoggingLevel.<ConfigurationNode>updateStorage(this.configurationNode);

                this.configManager.save(this.configurationNode);
            }
        } catch (IOException e) {
            this.plugin.getLogger().error("A problem occurred attempting to update Guardians global configuration!", e);
        }
    }

    public ConfigurationNode getConfigurationNode()  {
        return this.configurationNode;
    }

    @Override
    public boolean exists() {
        return this.configFile.toFile().exists();
    }

    @Override
    public Optional<Path> getLocation() {
        return Optional.ofNullable(this.configFile);
    }

}
