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
import io.github.connorhartley.guardian.Guardian;
import io.github.connorhartley.guardian.context.ContextProvider;
import io.github.connorhartley.guardian.detection.Detection;
import io.github.connorhartley.guardian.detection.DetectionConfiguration;
import io.github.connorhartley.guardian.detection.DetectionTypes;
import io.github.connorhartley.guardian.detection.check.CheckType;
import io.github.connorhartley.guardian.event.sequence.SequenceFinishEvent;
import io.github.connorhartley.guardian.internal.checks.HorizontalSpeedCheck;
import io.github.connorhartley.guardian.internal.checks.VerticalSpeedCheck;
import io.github.connorhartley.guardian.internal.punishments.WarningPunishment;
import io.github.connorhartley.guardian.punishment.Punishment;
import io.github.connorhartley.guardian.storage.StorageConsumer;
import io.github.connorhartley.guardian.storage.container.StorageKey;
import io.github.connorhartley.guardian.storage.container.StorageValue;
import ninja.leaping.configurate.ConfigurationNode;
import org.apache.commons.math3.distribution.NormalDistribution;
import org.spongepowered.api.event.Listener;
import org.spongepowered.api.plugin.PluginContainer;

import java.io.File;
import java.time.LocalDateTime;
import java.util.*;

@Module(id = "speed",
        name = "Speed Detection",
        authors = { "Connor Hartley (vectrix)" },
        version = "0.0.20",
        onEnable = "onConstruction",
        onDisable = "onDeconstruction")
public class SpeedDetection extends Detection<Guardian> implements StorageConsumer {

    private static Module moduleAnnotation = SpeedDetection.class.getAnnotation(Module.class);

    private Guardian plugin;
    private List<CheckType> checkTypes;
    private Configuration configuration;
    private boolean ready = false;

    @Inject
    @ModuleContainer
    public PluginContainer moduleContainer;

    @Inject
    @ModuleConfiguration
    public ConfigurationNode configurationNode;

    public SpeedDetection() {
        super(moduleAnnotation.id(), moduleAnnotation.name());
    }

    @Override
    public void onConstruction() {
        this.moduleContainer.getInstance().ifPresent(plugin -> this.plugin = (Guardian) plugin);

        this.configuration = new Configuration(this);

        if (Configuration.getLocation().exists()) {
            for (StorageValue storageValue : this.getStorageNodes()) {
                storageValue.<ConfigurationNode>loadStorage(this.configurationNode);
            }
        }

        DetectionTypes.SPEED_DETECTION = Optional.of(this);

        this.checkTypes = Arrays.asList(new HorizontalSpeedCheck.Type(this), new VerticalSpeedCheck.Type(this));

        this.plugin.getPunishmentController().bind(WarningPunishment.class, this);

        this.ready = true;
    }

    @Listener
    public void onSequenceFinish(SequenceFinishEvent event) {
        if (!event.isCancelled()) {
            for (CheckType checkProvider : this.checkTypes) {
                if (checkProvider.getSequence().equals(event.getSequence())) {
                    double lower = this.configuration.configSeverityDistribution.getValue().get("lower");
                    double mean = this.configuration.configSeverityDistribution.getValue().get("mean");
                    double standardDeviation = this.configuration.configSeverityDistribution.getValue().get("standard-deviation");

                    NormalDistribution normalDistribution =
                            new NormalDistribution(mean, standardDeviation);

                    String type = "";

                    if (event.getResult().getDetectionTypes().size() > 0) {
                        type = event.getResult().getDetectionTypes().get(0);
                    }

                    double probability = normalDistribution.probability(lower, event.getResult().getSeverity());

                    Punishment punishment = Punishment.builder()
                            .reason(type)
                            .time(LocalDateTime.now())
                            .report(event.getResult())
                            .probability(probability)
                            .build();

                    this.getPlugin().getPunishmentController().execute(this, event.getUser(), punishment);
                }
            }
        }
    }

    @Override
    public void onDeconstruction() {
        this.ready = false;
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
    public List<CheckType> getChecks() {
        return this.checkTypes;
    }

    @Override
    public DetectionConfiguration getConfiguration() {
        return this.configuration;
    }

    @Override
    public ContextProvider getContextProvider() {
        return this.plugin;
    }

    @Override
    public StorageValue<?, ?>[] getStorageNodes() {
        return new StorageValue<?, ?>[] {
                this.configuration.configPunishmentProperties, this.configuration.configPunishmentLevels,
                this.configuration.configSeverityDistribution, this.configuration.configAnalysisTime,
                this.configuration.configTickBounds, this.configuration.configControlValues,
                this.configuration.configMaterialValues, this.configuration.configCustomPunishments
        };
    }

    @Override
    public boolean isReady() {
        return this.ready;
    }

    public static class Configuration implements DetectionConfiguration {

        private static File configFile;

        private final SpeedDetection speedDetection;

        StorageValue<String, Double> configAnalysisTime;
        StorageValue<String, Map<String, Double>> configTickBounds;
        StorageValue<String, Map<String, Double>> configControlValues;
        StorageValue<String, Map<String, Double>> configMaterialValues;
        StorageValue<String, Map<String, Double>> configPunishmentLevels;
        StorageValue<String, Map<String, String>> configPunishmentProperties;
        StorageValue<String, Map<String, List<String>>> configCustomPunishments;
        StorageValue<String, Map<String, Double>> configSeverityDistribution;

        private Configuration(SpeedDetection speedDetection) {
            this.speedDetection = speedDetection;

            configFile = new File(this.speedDetection.getPlugin().getGlobalConfiguration()
                    .getLocation().getParentFile(), "detection" + File.separator +
                    this.speedDetection.getId() + ".conf");

            initialize();
        }

        private void initialize() {
            this.configAnalysisTime = new StorageValue<>(new StorageKey<>("analysis-time"),
                    "Time taken to analyse the players speed. 2 seconds is recommended!",
                    2.0, new TypeToken<Double>() {
            });

            HashMap<String, Double> tickBounds = new HashMap<>();
            tickBounds.put("min", 0.75);
            tickBounds.put("max", 1.5);

            this.configTickBounds = new StorageValue<>(new StorageKey<>("tick-bounds"),
                    "Percentage of the analysis-time in ticks to compare the check time to ensure accurate reports.",
                    tickBounds, new TypeToken<Map<String, Double>>() {
            });

            HashMap<String, Double> punishmentLevels = new HashMap<>();
            punishmentLevels.put("warn", 0.1);
//            punishmentLevels.put("flag", 0.2);
//            punishmentLevels.put("report", 0.3);
//            punishmentLevels.put("kick", 0.5);

            this.configPunishmentLevels = new StorageValue<>(new StorageKey<>("punishment-levels"),
                    "Punishments that happen when the user reaches the individual severity threshold.",
                    punishmentLevels, new TypeToken<Map<String, Double>>() {
            });

            HashMap<String, String> punishmentProperties = new HashMap<>();
            punishmentProperties.put("channel", "admin");
            punishmentProperties.put("releasetime", "12096000");

            this.configPunishmentProperties = new StorageValue<>(new StorageKey<>("punishment-properties"),
                    "Properties that define certain properties for all the punishments in this detection.",
                    punishmentProperties, new TypeToken<Map<String, String>>() {
            });

            HashMap<String, List<String>> customPunishments = new HashMap<>();
            customPunishments.put("example", Collections.singletonList("msg %player% You have been prosecuted for illegal action!"));

            this.configCustomPunishments = new StorageValue<>(new StorageKey<>("custom-punishments"),
                    "Custom punishments that can execute custom commands.",
                    customPunishments, new TypeToken<Map<String, List<String>>>() {
            });

            HashMap<String, Double> severityDistribution = new HashMap<>();
            severityDistribution.put("lower", 0d);
            severityDistribution.put("mean", 25d);
            severityDistribution.put("standard-deviation", 15d);

            this.configSeverityDistribution = new StorageValue<>(new StorageKey<>("severity-distribution"),
                    "Normal distribution properties for calculating the over-shot value from the mean.",
                    severityDistribution, new TypeToken<Map<String, Double>>() {
            });

            // Player Control

            HashMap<String, Double> controlValues = new HashMap<>();
            controlValues.put("sneak", 1.015);
            controlValues.put("walk", 1.035);
            controlValues.put("sprint", 1.065);
            controlValues.put("fly", 1.075);

            this.configControlValues = new StorageValue<>(new StorageKey<>("control-values"),
                    "Magic values for movement the player controls that are added each tick.",
                    controlValues, new TypeToken<Map<String, Double>>() {
            });

            // Block Speed

            HashMap<String, Double> materialValues = new HashMap<>();
            materialValues.put("gas", 1.04);
            materialValues.put("solid", 1.025);
            materialValues.put("liquid", 1.015);

            this.configMaterialValues = new StorageValue<>(new StorageKey<>("material-values"),
                    "Magic values for materials touching the player that affect the players speed which are added each tick.",
                    materialValues, new TypeToken<Map<String, Double>>() {
            });
        }

        private static File getLocation() {
            return configFile;
        }

        @Override
        @SuppressWarnings("unchecked")
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
                } else if (name.equals("punishment-properties")) {
                    return Optional.of((StorageValue<K, E>) this.configPunishmentProperties);
                } else if (name.equals("custom-punishments")) {
                    return Optional.of((StorageValue<K, E>) this.configCustomPunishments);
                } else if (name.equals("severity-distribution")) {
                    return Optional.of((StorageValue<K, E>) this.configSeverityDistribution);
                }
            }
            return Optional.empty();
        }
    }
}
