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
package io.github.connorhartley.guardian.internal.detections;

import com.google.inject.Inject;
import com.me4502.modularframework.module.Module;
import com.me4502.modularframework.module.guice.ModuleConfiguration;
import com.me4502.modularframework.module.guice.ModuleContainer;
import com.me4502.precogs.detection.CommonDetectionTypes;
import io.github.connorhartley.guardian.Guardian;
import io.github.connorhartley.guardian.context.ContextProvider;
import io.github.connorhartley.guardian.detection.Detection;
import io.github.connorhartley.guardian.detection.DetectionTypes;
import io.github.connorhartley.guardian.detection.check.CheckProvider;
import io.github.connorhartley.guardian.internal.checks.HorizontalSpeedCheck;
import io.github.connorhartley.guardian.storage.StorageProvider;
import io.github.connorhartley.guardian.storage.container.StorageKey;
import io.github.connorhartley.guardian.storage.container.StorageValue;
import io.github.connorhartley.guardian.storage.StorageConsumer;
import ninja.leaping.configurate.ConfigurationNode;
import org.spongepowered.api.plugin.PluginContainer;

import java.io.File;
import java.io.IOException;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

@Module(id = "speed_detection",
        name = "Speed Detection",
        authors = { "Connor Hartley (vectrix)" },
        version = "0.0.4",
        onEnable = "onConstruction",
        onDisable = "onDeconstruction")
public class SpeedDetection extends Detection implements StorageConsumer {

    @Inject
    @ModuleContainer
    public PluginContainer moduleContainer;

    @Inject
    @ModuleConfiguration
    public ConfigurationNode internalConfigurationNode;

    private Guardian plugin;
    private ConfigurationNode globalConfigurationNode;
    private Configuration internalConfigurationProvider;

    private boolean ready = false;

    private static final Module moduleAnnotation = SpeedDetection.class.getAnnotation(Module.class);

    public SpeedDetection() {
        super(moduleAnnotation.id(), moduleAnnotation.name());
    }

    @Override
    public void onConstruction() {
        if (!moduleContainer.getInstance().isPresent());
        this.plugin = (Guardian) moduleContainer.getInstance().get();

        this.globalConfigurationNode = this.plugin.getGlobalConfiguration().getConfigurationNode();
        this.internalConfigurationProvider = new Configuration(this);

        if (!Configuration.getLocation().exists()) {
            for (StorageValue storageValue : this.getStorageNodes()) {
                storageValue.<ConfigurationNode>createStorage(this.internalConfigurationNode);
            }
        }

        DetectionTypes.SPEED_DETECTION = Optional.of(this);

        this.ready = true;
    }

    @Override
    public void onDeconstruction() {
        this.ready = false;
    }

    @Override
    public ContextProvider getContextProvider() {
        return this.plugin;
    }

    @Override
    public Object getPlugin() {
        return this.plugin;
    }

    @Override
    public StorageConsumer getConfiguration() {
        return this;
    }

    @Override
    public boolean isReady() {
        return this.ready;
    }

    @Override
    public List<CheckProvider> getChecks() {
        return Collections.singletonList(new HorizontalSpeedCheck.Provider(this));
    }

    @Override
    public CommonDetectionTypes.Category getCategory() {
        return CommonDetectionTypes.Category.MOVEMENT;
    }

    @Override
    public StorageValue<?, ?>[] getStorageNodes() {
        return new StorageValue<?, ?>[] {
                this.internalConfigurationProvider.configSneakControl, this.internalConfigurationProvider.configWalkControl,
                this.internalConfigurationProvider.configSprintControl, this.internalConfigurationProvider.configFlyControl,
                this.internalConfigurationProvider.configAirModifier, this.internalConfigurationProvider.configGroundModifier,
                this.internalConfigurationProvider.configLiquidModifier
        };
    }

    public static class Configuration {

        private SpeedDetection speedDetection;

        private static File configFile;

        // Player Control

        public StorageValue<String, Double> configSneakControl;
        public StorageValue<String, Double> configWalkControl;
        public StorageValue<String, Double> configSprintControl;
        public StorageValue<String, Double> configFlyControl;

        // Block Speed

        public StorageValue<String, Double> configAirModifier;
        public StorageValue<String, Double> configGroundModifier;
        public StorageValue<String, Double> configLiquidModifier;

        public Configuration(SpeedDetection speedDetection) {
            this.speedDetection = speedDetection;

            configFile = new File(((Guardian) this.speedDetection.getPlugin()).getGlobalConfiguration()
                    .getLocation().getParentFile(), "detection" + File.separator + speedDetection.getId() + ".conf");

            // Player Control

            this.configSneakControl = new StorageValue<>(new StorageKey<>("sneak_control_modifier"), null,
                    1.015, null);

            this.configWalkControl = new StorageValue<>(new StorageKey<>("walk_control_modifier"), null,
                    1.035, null);

            this.configSprintControl = new StorageValue<>(new StorageKey<>("sprint_control_modifier"), null,
                    1.065, null);

            this.configFlyControl = new StorageValue<>(new StorageKey<>("fly_control_modifier"), null,
                    1.08, null);

            // Block Speed

            this.configAirModifier = new StorageValue<>(new StorageKey<>("air_block_amplifier"), null,
                    1.045, null);

            this.configGroundModifier = new StorageValue<>(new StorageKey<>("ground_block_amplifier"), null,
                    1.025, null);

            this.configLiquidModifier = new StorageValue<>(new StorageKey<>("liquid_block_amplifier"), null,
                    1.015, null);
        }

        public static File getLocation() {
            return configFile;
        }
    }
}
