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

import com.me4502.modularframework.exception.ModuleNotInstantiatedException;
import com.me4502.modularframework.module.ModuleWrapper;
import io.github.connorhartley.guardian.detection.Detection;
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

    public StorageValue<CommentedConfigurationNode, String, String> configVersion;
    public StorageValue<CommentedConfigurationNode, String, List<String>> configEnabledDetections;

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

                this.configurationNode = this.configManager.load(this.plugin.getConfigurationOptions());

                this.configVersion = new StorageValue<CommentedConfigurationNode, String, String>("version", this.plugin.getPluginContainer().getVersion().get(),
                        "Do not edit this!", null).load(this.configurationNode);
                this.configEnabledDetections = new StorageValue<CommentedConfigurationNode, String, List<String>>("enabled", Collections.emptyList(),
                        "Detections in this list will be enabled.", null).load(this.configurationNode);

                this.configManager.save(this.configurationNode);
            }
        } catch (IOException e) {
            this.plugin.getLogger().error("A problem occurred attempting to create Guardians global configuration!", e);
        }
    }

    @Override
    public void load() {
        try {
            if (!exists()) {
                this.create();
                return;
            }

            this.configurationNode = this.configManager.load(this.plugin.getConfigurationOptions());

            this.configVersion.load(this.configurationNode);
            this.configEnabledDetections.load(this.configurationNode);

            this.plugin.getModuleController().getModules().stream()
                    .filter(ModuleWrapper::isEnabled)
                    .forEach(moduleWrapper -> {
                        try {
                            Detection detection = (Detection) moduleWrapper.getModule();

                            if (detection instanceof StorageConsumer) {
                                for (StorageValue storageValue : ((StorageConsumer) detection).getStorageNodes()) {
                                    storageValue.load(this.configurationNode);
                                }
                            }
                        } catch (ModuleNotInstantiatedException e) {
                            this.plugin.getLogger().error("Failed to get internal: " + moduleWrapper.getName() + " v" + moduleWrapper.getVersion(), e);
                        }
                    });

            this.configManager.save(this.configurationNode);
        } catch (IOException e) {
            this.plugin.getLogger().error("A problem occurred attempting to load Guardians global configuration!", e);
        }
    }

    @Override
    public void update() {
        try {
            this.configurationNode = this.configManager.load(this.plugin.getConfigurationOptions());

            this.configVersion.save(this.configurationNode);
            this.configEnabledDetections.save(this.configurationNode);

            this.plugin.getModuleController().getModules().stream()
                    .filter(ModuleWrapper::isEnabled)
                    .forEach(moduleWrapper -> {
                        try {
                            Detection detection = (Detection) moduleWrapper.getModule();

                            if (detection instanceof StorageConsumer) {
                                for (StorageValue storageValue : ((StorageConsumer) detection).getStorageNodes()) {
                                    storageValue.save(this.configurationNode);
                                }
                            }
                        } catch (ModuleNotInstantiatedException e) {
                            this.plugin.getLogger().error("Failed to get internal: " + moduleWrapper.getName() + " v" + moduleWrapper.getVersion(), e);
                        }
                    });

            this.configManager.save(this.configurationNode);
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
    public StorageValue[] getStorageNodes() {
        return new StorageValue[]{ this.configVersion, this.configEnabledDetections };
    }

}
