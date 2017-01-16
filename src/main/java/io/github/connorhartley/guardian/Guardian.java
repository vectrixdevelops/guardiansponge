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

import com.google.inject.Inject;
import com.me4502.modularframework.ModuleController;
import ninja.leaping.configurate.ConfigurationOptions;
import ninja.leaping.configurate.commented.CommentedConfigurationNode;
import ninja.leaping.configurate.loader.ConfigurationLoader;
import org.slf4j.Logger;
import org.spongepowered.api.config.DefaultConfig;
import org.spongepowered.api.event.Listener;
import org.spongepowered.api.event.game.GameReloadEvent;
import org.spongepowered.api.event.game.state.GameStartedServerEvent;
import org.spongepowered.api.event.game.state.GameStartingServerEvent;
import org.spongepowered.api.event.game.state.GameStoppingEvent;
import org.spongepowered.api.plugin.Plugin;
import org.spongepowered.api.plugin.PluginContainer;

import java.io.File;
import java.util.Optional;

@Plugin(
        id = "guardian",
        name = "Guardian",
        version = "0.1.0-SNAPSHOT-01",
        description = "An experimental Sponge anticheat.",
        authors = {
                "ConnorHartley"
        }
)
public class Guardian {

    /* Logger */

    @Inject
    private Logger logger;

    public Logger getLogger() {
        return this.logger;
    }

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

    ConfigurationOptions configurationOptions;

    /* Module System */

    public ModuleController moduleController;

    /* Configuration */

    protected GuardianConfiguration globalConfiguration;

    /* Game Events */

    @Listener
    public void onServerStarting(GameStartingServerEvent event) {
        getLogger().info("Starting Guardian AntiCheat.");

        this.globalConfiguration = new GuardianConfiguration(this, this.pluginConfig, this.pluginConfigManager);

        this.configurationOptions = ConfigurationOptions.defaults();

        getLogger().info("Loading Global Configuration.");

        this.globalConfiguration.load();
    }

    @Listener
    public void onServerStarted(GameStartedServerEvent event) {}

    @Listener
    public void onServerStop(GameStoppingEvent event) {}

    @Listener
    public void onReload(GameReloadEvent event) {}

}
