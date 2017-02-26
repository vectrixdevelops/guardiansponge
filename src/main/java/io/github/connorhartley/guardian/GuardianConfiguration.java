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

import com.me4502.modularframework.module.ModuleWrapper;
import io.github.connorhartley.guardian.detection.Detection;
import io.github.connorhartley.guardian.util.StorageKey;
import io.github.connorhartley.guardian.util.StorageValue;
import io.github.connorhartley.guardian.util.storage.StorageConsumer;
import io.github.connorhartley.guardian.util.storage.StorageProvider;
import ninja.leaping.configurate.ConfigurationNode;
import ninja.leaping.configurate.commented.CommentedConfigurationNode;
import ninja.leaping.configurate.loader.ConfigurationLoader;

import java.io.File;
import java.io.IOException;
import java.util.Collections;
import java.util.List;

public class GuardianConfiguration implements StorageProvider<File>, StorageConsumer {

    private Guardian plugin;

    private final File configFile;
    private final ConfigurationLoader<CommentedConfigurationNode> configManager;

    private CommentedConfigurationNode configurationNode;

    public StorageValue<String, String> configVersion;
    public StorageValue<String, List<String>> configEnabledDetections;
    public StorageValue<String, Integer> configLoggingLevel;

    protected GuardianConfiguration(Guardian plugin, File configFile, ConfigurationLoader<CommentedConfigurationNode> configManager) {
        this.plugin = plugin;
        this.configFile = configFile;
        this.configManager = configManager;
    }

    @Override
    public void create() {
        try {
            if (!this.exists()) {
                this.configFile.getParentFile().mkdirs();
                this.configFile.createNewFile();
            }

            this.configurationNode = this.configManager.load(this.plugin.getConfigurationOptions());

            this.configVersion = new StorageValue<>(new StorageKey<>("version"), "Do not edit this! Internal use only!",
                    this.plugin.getPluginContainer().getVersion().orElse("unknown"), null);
            this.configEnabledDetections = new StorageValue<>(new StorageKey<>("enabled"), "Detections in this list will be enabled.",
                    Collections.singletonList("speed_detection"), null);
            this.configLoggingLevel = new StorageValue<>(new StorageKey<>("logging-level"), "1 for basic logging, 2 for more logging, 3 for detailed logging.",
                    2, null);

            this.configVersion.<ConfigurationNode>createStorage(this.configurationNode);
            this.configEnabledDetections.<ConfigurationNode>createStorage(this.configurationNode);
            this.configLoggingLevel.<ConfigurationNode>createStorage(this.configurationNode);

            this.configManager.save(this.configurationNode);
        } catch (IOException e) {
            this.plugin.getLogger().error("A problem occurred attempting to create Guardians global configuration!", e);
        }
    }

    @Override
    public void load() {
        try {
            if (this.exists()) {
                this.configurationNode = this.configManager.load(this.plugin.getConfigurationOptions());

                this.configVersion.<ConfigurationNode>loadStorage(this.configurationNode);
                this.configEnabledDetections.<ConfigurationNode>loadStorage(this.configurationNode);
                this.configLoggingLevel.<ConfigurationNode>loadStorage(this.configurationNode);

                this.plugin.getModuleController().getModules().stream()
                        .filter(ModuleWrapper::isEnabled)
                        .forEach(moduleWrapper -> {
                            if (!moduleWrapper.getModule().isPresent()) return;
                            Detection detection = (Detection) moduleWrapper.getModule().get();

                            if (detection instanceof StorageConsumer) {
                                for (StorageValue storageValue : ((StorageConsumer) detection).getStorageNodes()) {
                                    storageValue.<ConfigurationNode>loadStorage(this.configurationNode);
                                }
                            }
                        });

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

                this.configVersion.<ConfigurationNode>updateStorage(this.configurationNode);
                this.configEnabledDetections.<ConfigurationNode>updateStorage(this.configurationNode);
                this.configLoggingLevel.<ConfigurationNode>updateStorage(this.configurationNode);

                this.plugin.getModuleController().getModules().stream()
                        .filter(ModuleWrapper::isEnabled)
                        .forEach(moduleWrapper -> {
                            if (!moduleWrapper.getModule().isPresent()) return;
                            Detection detection = (Detection) moduleWrapper.getModule().get();

                            if (detection instanceof StorageConsumer) {
                                for (StorageValue storageValue : ((StorageConsumer) detection).getStorageNodes()) {
                                    storageValue.<ConfigurationNode>updateStorage(this.configurationNode);
                                }
                            }
                        });

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
        return this.configFile.exists();
    }

    @Override
    public File getLocation() {
        return this.configFile;
    }

    @Override
    public StorageValue<?, ?>[] getStorageNodes() {
        return new StorageValue<?, ?>[]{ this.configVersion };
    }

}
