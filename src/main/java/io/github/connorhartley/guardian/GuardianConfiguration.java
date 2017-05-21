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
import io.github.connorhartley.guardian.storage.container.StorageKey;
import io.github.connorhartley.guardian.storage.container.StorageValue;
import io.github.connorhartley.guardian.util.ConfigurationCommentDocument;
import ninja.leaping.configurate.ConfigurationNode;
import ninja.leaping.configurate.commented.CommentedConfigurationNode;
import ninja.leaping.configurate.loader.ConfigurationLoader;
import org.apache.commons.lang3.StringUtils;

import java.io.IOException;
import java.nio.file.Path;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;

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

    private ConfigurationCommentDocument globalHeader;

    public StorageValue<String, Integer> configHeader;
    public StorageValue<String, String> configVersion;
    public StorageValue<String, List<String>> configEnabledDetections;
    public StorageValue<String, Integer> configLoggingLevel;

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

            this.globalHeader = new ConfigurationCommentDocument(50, " ");
            this.globalHeader.addLogo(ConfigurationCommentDocument.LOGO)
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

            this.configHeader = new StorageValue<>(new StorageKey<>("a"),
                    this.globalHeader.export(),
                    1, new TypeToken<Integer>() {
            });

            this.configVersion = new StorageValue<>(new StorageKey<>("version"),
                    new ConfigurationCommentDocument(50, " ")
                                    .addHeader("Guardian Version")
                                    .addParagraph(new String[]{
                                            "VOLATILE: Do not touch this!",
                                            "Defines the version of Guardian for configuration ",
                                            "purposes."
                                    })
                                    .export(),
                    this.plugin.getPluginContainer().getVersion().orElse("unknown"), new TypeToken<String>() {
            });

            this.configEnabledDetections = new StorageValue<>(new StorageKey<>("enabled"),
                    new ConfigurationCommentDocument(50, " ")
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

            this.configLoggingLevel = new StorageValue<>(new StorageKey<>("logging-level"),
                    new ConfigurationCommentDocument(50, " ")
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
            this.configVersion.<ConfigurationNode>createStorage(this.configurationNode);
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
                this.configVersion.<ConfigurationNode>loadStorage(this.configurationNode);
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
                this.configVersion.<ConfigurationNode>updateStorage(this.configurationNode);
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
    public Path getLocation() {
        return this.configFile;
    }

}
