/*
 * MIT License
 *
 * Copyright (c) 2016 Connor Hartley
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

import io.github.connorhartley.guardian.util.StorageValue;
import io.github.connorhartley.guardian.util.storage.StorageProvider;
import ninja.leaping.configurate.commented.CommentedConfigurationNode;
import ninja.leaping.configurate.loader.ConfigurationLoader;

import java.io.File;
import java.io.IOException;

public class GuardianConfiguration implements StorageProvider<File> {

    private Guardian plugin;

    private final File configFile;
    private final ConfigurationLoader<CommentedConfigurationNode> configManager;

    private CommentedConfigurationNode configurationNode;

    public StorageValue<CommentedConfigurationNode, String, String> configurationVersion =
            new StorageValue<>("version", this.plugin.getPluginContainer().getVersion().orElse("unknown"));

    public GuardianConfiguration(Guardian plugin, File configFile, ConfigurationLoader<CommentedConfigurationNode> configManager) {
        this.plugin = plugin;
        this.configFile = configFile;
        this.configManager = configManager;
    }

    @Override
    public void load() {
        try {
            if (!this.configFile.exists()) {
                this.configFile.getParentFile().mkdirs();
                this.configFile.createNewFile();
            }

            this.configurationNode = configManager.load(this.plugin.configurationOptions);

            this.configurationVersion.load(this.configurationNode);

            this.configManager.save(this.configurationNode);
        } catch (IOException e) {
            this.plugin.getLogger().error("A problem occurred attempting to load Guardians global configuration!", e);
        }
    }

    @Override
    public void update() {
        try {
            this.configurationNode = this.configManager.load(this.plugin.configurationOptions);

            this.configurationVersion.save(this.configurationNode);

            this.configManager.save(this.configurationNode);
        } catch (IOException e) {
            this.plugin.getLogger().error("A problem occurred attempting to update Guardians global configuration!", e);
        }
    }

    @Override
    public File getLocation() {
        return this.configFile;
    }

    public StorageValue<?, ?, ?>[] getConfigurationNodes() {
        return new StorageValue<?, ?, ?>[]{ this.configurationVersion };
    }

}
