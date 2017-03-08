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

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Maps;
import com.google.common.reflect.TypeToken;
import com.google.inject.Inject;
import com.me4502.modularframework.module.Module;
import com.me4502.modularframework.module.guice.ModuleConfiguration;
import com.me4502.modularframework.module.guice.ModuleContainer;
import com.me4502.precogs.detection.CommonDetectionTypes;
import io.github.connorhartley.guardian.Guardian;
import io.github.connorhartley.guardian.context.ContextProvider;
import io.github.connorhartley.guardian.detection.Detection;
import io.github.connorhartley.guardian.detection.DetectionConfiguration;
import io.github.connorhartley.guardian.detection.DetectionTypes;
import io.github.connorhartley.guardian.detection.Offense;
import io.github.connorhartley.guardian.detection.check.CheckProvider;
import io.github.connorhartley.guardian.event.check.CheckEndEvent;
import io.github.connorhartley.guardian.internal.checks.HorizontalSpeedCheck;
import io.github.connorhartley.guardian.sequence.report.ReportType;
import io.github.connorhartley.guardian.storage.StorageProvider;
import io.github.connorhartley.guardian.storage.container.StorageKey;
import io.github.connorhartley.guardian.storage.container.StorageValue;
import io.github.connorhartley.guardian.storage.StorageConsumer;
import ninja.leaping.configurate.ConfigurationNode;
import org.spongepowered.api.event.Listener;
import org.spongepowered.api.plugin.PluginContainer;

import java.io.File;
import java.io.IOException;
import java.time.LocalDateTime;
import java.util.*;

@Module(id = "speed_detection",
        name = "Speed Detection",
        authors = { "Connor Hartley (vectrix)" },
        version = "0.0.5",
        onEnable = "onConstruction",
        onDisable = "onDeconstruction")
public class SpeedDetection extends Detection<Guardian> implements StorageConsumer {

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

        if (Configuration.getLocation().exists()) {
            for (StorageValue storageValue : this.getStorageNodes()) {
                storageValue.<ConfigurationNode>loadStorage(this.internalConfigurationNode);
            }
        }

        DetectionTypes.SPEED_DETECTION = Optional.of(this);

        this.ready = true;
    }

    @Override
    public void onDeconstruction() {
        this.ready = false;
    }

    @Listener
    public void onCheckEnd(CheckEndEvent event) {
        if (event.getResult().isPresent()) {
            try {
                Offense offense = new Offense.Builder().dateAndTime(LocalDateTime.now())
                        .detection(event.getCheck().getProvider().getDetection())
                        .severity((Integer) event.getResult().get().getReports().get(ReportType.SEVERITY)).build();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    @Override
    public ContextProvider getContextProvider() {
        return this.plugin;
    }

    @Override
    public Guardian getPlugin() {
        return this.plugin;
    }

    @Override
    public SpeedDetection.Configuration getConfiguration() {
        return this.internalConfigurationProvider;
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
                this.internalConfigurationProvider.configControlValues, this.internalConfigurationProvider.configMaterialValues
        };
    }

    public static class Configuration implements DetectionConfiguration {

        private SpeedDetection speedDetection;

        private static File configFile;

        // Player Control

        public StorageValue<String, Map<String, Double>> configControlValues;

        // Block Speed

        public StorageValue<String, Map<String, Double>> configMaterialValues;

        public Configuration(SpeedDetection speedDetection) {
            this.speedDetection = speedDetection;

            configFile = new File(this.speedDetection.getPlugin().getGlobalConfiguration()
                    .getLocation().getParentFile(), "detection" + File.separator + speedDetection.getId() + ".conf");

            // Player Control

            HashMap<String, Double> controlValues = new HashMap<>();
            controlValues.put("sneak", 1.015);
            controlValues.put("walk", 1.035);
            controlValues.put("sprint", 1.065);
            controlValues.put("fly", 1.08);

            this.configControlValues = new StorageValue<>(new StorageKey<>("control-values"),
                    "Magic values for movement the player controls that are added each tick.",
                    controlValues, new TypeToken<Map<String, Double>>() {
            });

            // Block Speed

            HashMap<String, Double> materialValues = new HashMap<>();
            materialValues.put("gas", 1.045);
            materialValues.put("solid", 1.025);
            materialValues.put("liquid", 1.015);

            this.configMaterialValues = new StorageValue<>(new StorageKey<>("material-values"),
                    "Magic values for materials touching the player that affect the players speed which are added each tick.",
                    materialValues, new TypeToken<Map<String, Double>>() {
            });
        }

        public static File getLocation() {
            return configFile;
        }

        @Override
        public <K, E> StorageValue<K, E> get(K name, E defaultElement) {
            if (name instanceof String && defaultElement instanceof Map) {
                if (name.equals("control-values")) {
                    return (StorageValue<K, E>) this.configControlValues;
                } else if (name.equals("material-values")) {
                    return (StorageValue<K, E>) this.configMaterialValues;
                }
            }
            return null;
        }
    }
}
