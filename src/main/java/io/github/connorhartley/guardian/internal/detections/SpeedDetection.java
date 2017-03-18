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

import com.google.common.reflect.TypeToken;
import com.google.inject.Inject;
import com.me4502.modularframework.module.Module;
import com.me4502.modularframework.module.guice.ModuleConfiguration;
import com.me4502.modularframework.module.guice.ModuleContainer;
import com.me4502.precogs.detection.CommonDetectionTypes;
import com.me4502.precogs.detection.DetectionType;
import io.github.connorhartley.guardian.Guardian;
import io.github.connorhartley.guardian.context.ContextProvider;
import io.github.connorhartley.guardian.detection.Detection;
import io.github.connorhartley.guardian.detection.DetectionConfiguration;
import io.github.connorhartley.guardian.detection.DetectionTypes;
import io.github.connorhartley.guardian.detection.Offense;
import io.github.connorhartley.guardian.detection.check.CheckProvider;
import io.github.connorhartley.guardian.event.check.CheckEndEvent;
import io.github.connorhartley.guardian.internal.checks.HorizontalSpeedCheck;
import io.github.connorhartley.guardian.punishment.PunishmentType;
import io.github.connorhartley.guardian.sequence.report.ReportType;
import io.github.connorhartley.guardian.sequence.report.SequenceReport;
import io.github.connorhartley.guardian.storage.StorageConsumer;
import io.github.connorhartley.guardian.storage.container.StorageKey;
import io.github.connorhartley.guardian.storage.container.StorageValue;
import ninja.leaping.configurate.ConfigurationNode;
import org.spongepowered.api.event.Listener;
import org.spongepowered.api.plugin.PluginContainer;

import java.io.File;
import java.time.LocalDateTime;
import java.util.*;

@Module(id = "speed_detection",
        name = "Speed Detection",
        authors = { "Connor Hartley (vectrix)" },
        version = "0.0.8",
        onEnable = "onConstruction",
        onDisable = "onDeconstruction")
public class SpeedDetection extends Detection<Guardian> implements StorageConsumer {

    @Inject
    @ModuleContainer
    public PluginContainer moduleContainer;

    @Inject
    @ModuleConfiguration
    public ConfigurationNode internalConfigurationNode;

    private static Module moduleAnnotation = SpeedDetection.class.getAnnotation(Module.class);

    private Guardian plugin;
    private ConfigurationNode globalConfigurationNode;
    private Configuration internalConfigurationProvider;
    private List<CheckProvider> checkProviders;

    private boolean ready = false;

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

        this.checkProviders = Collections.singletonList(new HorizontalSpeedCheck.Provider(this));
    }

    @Override
    public void onDeconstruction() {
        this.ready = false;
    }

    @Override
    public List<CheckProvider> getChecks() {
        return this.checkProviders;
    }

    @Override
    public CommonDetectionTypes.Category getCategory() {
        return CommonDetectionTypes.Category.MOVEMENT;
    }

    @Override
    public Guardian getPlugin() {
        return this.plugin;
    }

    @Override
    public DetectionConfiguration getConfiguration() {
        return this.internalConfigurationProvider;
    }

    @Override
    public StorageValue<?, ?>[] getStorageNodes() {
        return new StorageValue<?, ?>[] {
                this.internalConfigurationProvider.configAnalysisTime, this.internalConfigurationProvider.configTickBounds,
                this.internalConfigurationProvider.configPunishmentLevels, this.internalConfigurationProvider.configSeverityDistribution,
                this.internalConfigurationProvider.configControlValues, this.internalConfigurationProvider.configMaterialValues
        };
    }

    @Override
    public ContextProvider getContextProvider() {
        return this.plugin;
    }

    @Override
    public boolean isReady() {
        return this.ready;
    }

    public static class Configuration implements DetectionConfiguration {

        private SpeedDetection speedDetection;

        private static File configFile;

        StorageValue<String, Double> configAnalysisTime;
        StorageValue<String, Map<String, Double>> configTickBounds;
        StorageValue<String, Map<String, Double>> configControlValues;
        StorageValue<String, Map<String, Double>> configMaterialValues;
        StorageValue<String, Map<PunishmentType, Double>> configPunishmentLevels;
        StorageValue<String, Map<String, Double>> configSeverityDistribution;

        Configuration(SpeedDetection speedDetection) {
            this.speedDetection = speedDetection;

            configFile = new File(this.speedDetection.getPlugin().getGlobalConfiguration()
                    .getLocation().getParentFile(), "detection" + File.separator + speedDetection.getId() + ".conf");

            initialize();
        }

        void initialize() {
            this.configAnalysisTime = new StorageValue<>(new StorageKey<>("analysis-time"),
                    "Time taken to analyse the players speed. 2 seconds is recommended!",
                    2d, null);

            HashMap<String, Double> tickBounds = new HashMap<>();
            tickBounds.put("min", 0.75);
            tickBounds.put("max", 1d);

            this.configTickBounds = new StorageValue<>(new StorageKey<>("tick-bounds"),
                    "Percentage of the analysis-time in ticks to compare the check time to ensure accurate reports.",
                    tickBounds, new TypeToken<Map<String, Double>>() {
            });

            HashMap<PunishmentType, Double> punishmentLevels = new HashMap<>();
            punishmentLevels.put(PunishmentType.WARN, 0.1);
            punishmentLevels.put(PunishmentType.FLAG, 0.2);
            punishmentLevels.put(PunishmentType.REPORT, 0.3);
            punishmentLevels.put(PunishmentType.KICK, 0.5);

            this.configPunishmentLevels = new StorageValue<>(new StorageKey<>("punishment-levels"),
                    "Punishments that happen when the user reaches the individual severity threshold.",
                    punishmentLevels, new TypeToken<Map<PunishmentType, Double>>() {
            });

            HashMap<String, Double> severityDistribution = new HashMap<>();
            severityDistribution.put("lower", 0d);
            severityDistribution.put("mean", 5d);
            severityDistribution.put("standard-deviation", 3d);

            this.configSeverityDistribution = new StorageValue<>(new StorageKey<>("severity-distribution"),
                    "Normal distribution properties for calculating the over-shot value from the mean.",
                    severityDistribution, new TypeToken<Map<String, Double>>() {
            });

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

        static File getLocation() {
            return configFile;
        }

        @Override
        public <K, E> Optional<StorageValue<K, E>> get(K name, E defaultElement) {
            if (name instanceof String) {
                if (name.equals("control-values")) {
                    return Optional.of((StorageValue<K, E>) this.configControlValues);
                } else if (name.equals("material-values")) {
                    return Optional.of((StorageValue<K, E>) this.configMaterialValues);
                } else if (name.equals("analysis-time")) {
                    return Optional.of((StorageValue<K, E>) this.configAnalysisTime);
                } else if (name.equals("tick-bounds")) {
                    return Optional.of((StorageValue<K, E>) this.configTickBounds);
                } else if (name.equals("punishment-levels")) {
                    return Optional.of((StorageValue<K, E>) this.configPunishmentLevels);
                } else if (name.equals("severity-distribution")) {
                    return Optional.of((StorageValue<K, E>) this.configSeverityDistribution);
                }
            }
            return Optional.empty();
        }
    }
}
