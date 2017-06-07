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
import com.me4502.modularframework.module.guice.ModuleContainer;
import com.me4502.precogs.detection.CommonDetectionTypes;
import io.github.connorhartley.guardian.Guardian;
import io.github.connorhartley.guardian.detection.Detection;
import io.github.connorhartley.guardian.detection.DetectionTypes;
import io.github.connorhartley.guardian.detection.check.CheckType;
import io.github.connorhartley.guardian.event.sequence.SequenceFinishEvent;
import io.github.connorhartley.guardian.internal.checks.FlyCheck;
import io.github.connorhartley.guardian.internal.punishments.CustomPunishment;
import io.github.connorhartley.guardian.internal.punishments.KickPunishment;
import io.github.connorhartley.guardian.internal.punishments.ReportPunishment;
import io.github.connorhartley.guardian.internal.punishments.ResetPunishment;
import io.github.connorhartley.guardian.internal.punishments.WarningPunishment;
import io.github.connorhartley.guardian.punishment.Punishment;
import io.github.connorhartley.guardian.storage.StorageProvider;
import io.github.connorhartley.guardian.storage.StorageSupplier;
import io.github.connorhartley.guardian.storage.configuration.CommentDocument;
import io.github.connorhartley.guardian.storage.container.ConfigurationValue;
import io.github.connorhartley.guardian.storage.container.StorageKey;
import ninja.leaping.configurate.ConfigurationNode;
import ninja.leaping.configurate.commented.CommentedConfigurationNode;
import ninja.leaping.configurate.loader.ConfigurationLoader;
import org.apache.commons.math3.distribution.NormalDistribution;
import org.spongepowered.api.event.Listener;
import org.spongepowered.api.event.game.GameReloadEvent;
import org.spongepowered.api.plugin.PluginContainer;
import org.spongepowered.api.util.Tuple;

import java.io.File;
import java.io.IOException;
import java.time.LocalDateTime;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Module(
        id = "fly",
        name = "Fly Detection",
        authors = { "Connor Hartley (vectrix)" },
        version = "0.0.20",
        onEnable = "onConstruction",
        onDisable = "onDeconstruction"
)
public class FlyDetection extends Detection<Guardian, FlyDetection.Configuration> {

    @Inject
    public FlyDetection(@ModuleContainer PluginContainer moduleContainer) {
        super(moduleContainer, "fly", "Fly Detection");
    }

    @Override
    public void onConstruction() {
        super.construct(
                this,
                () -> Collections.singletonList(new FlyCheck.Type<>(this)),
                () -> new Configuration(this,
                        this.getConfigFile().orElseThrow(Error::new), this.getConfigLoader().orElseThrow(Error::new)),
                CustomPunishment.class, ResetPunishment.class,
                WarningPunishment.class, KickPunishment.class,
                ReportPunishment.class
        );

        DetectionTypes.FLY_DETECTION = Optional.of(this);
        this.setReady(true);
    }

    @Override
    public void onDeconstruction() {
        this.setReady(false);

        this.getConfiguration().ifPresent(StorageProvider::update);
    }

    @Listener
    public void onReload(GameReloadEvent event) {
        this.getConfiguration().ifPresent(StorageProvider::update);

        getChecks().forEach(check -> this.getPlugin().getSequenceController().unregister(check));
        this.setChecks(null);

        this.getConfiguration().ifPresent(StorageProvider::load);

        this.setChecks(this.getCheckSupplier().create());
        getChecks().forEach(check -> this.getPlugin().getSequenceController().register(check));

        this.getConfiguration().ifPresent(StorageProvider::update);
    }

    @Listener
    public void onSequenceFinish(SequenceFinishEvent event) {
        if (!event.isCancelled() && this.canPunish()) {
            for (CheckType checkProvider : this.getChecks()) {
                if (checkProvider.getSequence().equals(event.getSequence())) {
                    double lower = this.getConfiguration().get().configSeverityDistribution.getValue().get("lower");
                    double mean = this.getConfiguration().get().configSeverityDistribution.getValue().get("mean");
                    double standardDeviation = this.getConfiguration().get().configSeverityDistribution.getValue().get("standard-deviation");

                    NormalDistribution normalDistribution =
                            new NormalDistribution(mean, standardDeviation);

                    String type = event.getResult().getDetectionType();

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
    public CommonDetectionTypes.Category getCategory() {
        return CommonDetectionTypes.Category.MOVEMENT;
    }

    public static class Configuration implements StorageSupplier<File> {

        public ConfigurationValue<String, Double> configAnalysisTime;
        public ConfigurationValue<String, Double> configMinimumAirTime;
        public ConfigurationValue<String, Double> configAltitudeMaximum;
        public ConfigurationValue<String, Map<String, Double>> configTickBounds;
        public ConfigurationValue<String, Map<String, Tuple<Double, Double>>> configPunishmentLevels;
        public ConfigurationValue<String, Map<String, String>> configPunishmentProperties;
        public ConfigurationValue<String, Map<String, List<String>>> configCustomPunishments;
        public ConfigurationValue<String, Map<String, Double>> configSeverityDistribution;

        private CommentedConfigurationNode configurationNode;

        private final FlyDetection flyDetection;
        private final File configFile;
        private final ConfigurationLoader<CommentedConfigurationNode> configManager;

        private Configuration(FlyDetection flyDetection, File configFile, ConfigurationLoader<CommentedConfigurationNode> configManager) {
            this.flyDetection = flyDetection;
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

                this.configurationNode = this.configManager.load(this.flyDetection.getPlugin().getConfigurationOptions());

                // Define Config Values

                this.configAnalysisTime = new ConfigurationValue<>(new StorageKey<>("analysis-time"),
                        new CommentDocument(45, " ")
                                        .addHeader("Analysis Time")
                                        .addParagraph(new String[]{
                                                "Refers to the time taken to run a sequence ",
                                                "of collecting data and after analyzing.",
                                                "",
                                                "Recommended time is 2.0 seconds."
                                        })
                                        .export(),
                        2.0, new TypeToken<Double>() {
                });

                this.configMinimumAirTime = new ConfigurationValue<>(new StorageKey<>("minimum-air-time"),
                        new CommentDocument(45, " ")
                                        .addHeader("Minimum Air Time")
                                        .addParagraph(new String[]{
                                                "Refers to the amount of ticks the player ",
                                                "must be flying, in order for the check to ",
                                                "become valid.",
                                                "",
                                                "Recommended time is 1.85 percent."
                                        })
                                        .export(),
                        1.85, new TypeToken<Double>() {
                });

                this.configAltitudeMaximum = new ConfigurationValue<>(new StorageKey<>("altitude-maximum"),
                        new CommentDocument(45, " ")
                                        .addHeader("Altitude Maximum")
                                        .addParagraph(new String[]{
                                                "Refers to the maximum altitude the player ",
                                                "can reach before they're considered flying.",
                                                "",
                                                "DEPRECATED",
                                                "",
                                                "Recommended 3.1 blocks."
                                        })
                                        .export(),
                        3.1, new TypeToken<Double>() {
                });

                Map<String, Double> tickBounds = new HashMap<>();
                tickBounds.put("min", 0.75);
                tickBounds.put("max", 1.5);

                this.configTickBounds = new ConfigurationValue<>(new StorageKey<>("tick-bounds"),
                        new CommentDocument(45, " ")
                                        .addHeader("Tick Bounds")
                                        .addParagraph(new String[]{
                                                "Refers to the minimum and maximum tick ",
                                                "bounds in order for a check to be valid.",
                                                "",
                                                "DEPRECATED",
                                                "",
                                                "Recommended 0.75 and 1.5 percent."
                                        })
                                        .export(),
                        tickBounds, new TypeToken<Map<String, Double>>() {
                });

                Map<String, Tuple<Double, Double>> punishmentLevels = new HashMap<>();
                punishmentLevels.put("warn&default", Tuple.of(0.1, 1.0));
                punishmentLevels.put("reset&default", Tuple.of(0.1, 1.0));

                this.configPunishmentLevels = new ConfigurationValue<>(new StorageKey<>("punishment-levels"),
                        new CommentDocument(45, " ")
                                        .addHeader("Punishment Levels")
                                        .addParagraph(new String[]{
                                                "Refers to the level bounds set ",
                                                "for a specific punishment to take ",
                                                "place on a player. Includes custom and ",
                                                "warning."
                                        })
                                        .export(),
                        punishmentLevels, new TypeToken<Map<String, Tuple<Double, Double>>>() {
                });

                Map<String, String> punishmentProperties = new HashMap<>();
                punishmentProperties.put("channel", "admin");
                punishmentProperties.put("releasetime", "12096000");

                this.configPunishmentProperties = new ConfigurationValue<>(new StorageKey<>("punishment-properties"),
                        new CommentDocument(45, " ")
                                        .addHeader("Punishment Properties")
                                        .addParagraph(new String[]{
                                                "Refers to properties the punishments may use ",
                                                "for certain actions to take place.",
                                                "",
                                                "DEPRECATED"
                                        })
                                        .export(),
                        punishmentProperties, new TypeToken<Map<String, String>>() {
                });

                Map<String, List<String>> customPunishments = new HashMap<>();
                customPunishments.put("example", Collections.singletonList("msg %player% You have been prosecuted for illegal action!"));

                this.configCustomPunishments = new ConfigurationValue<>(new StorageKey<>("custom-punishments"),
                        new CommentDocument(45, " ")
                                        .addHeader("Custom Punishments")
                                        .addParagraph(new String[]{
                                                "Allows you to setup custom punishments ",
                                                "with various options and commands."
                                        })
                                        .export(),
                        customPunishments, new TypeToken<Map<String, List<String>>>() {
                });

                Map<String, Double> severityDistribution = new HashMap<>();
                severityDistribution.put("lower", 0d);
                severityDistribution.put("mean", 10d);
                severityDistribution.put("standard-deviation", 5d);

                this.configSeverityDistribution = new ConfigurationValue<>(new StorageKey<>("severity-distribution"),
                        new CommentDocument(45, " ")
                                        .addHeader("Severity Distribution")
                                        .addParagraph(new String[]{
                                                "Refers to the normal distribution used ",
                                                "to calculate the severity from the value ",
                                                "received by the check.",
                                                "",
                                                "Recommended to use 0 as the lower mean as 10 and standard deviation as 5."
                                        })
                                        .export(),
                        severityDistribution, new TypeToken<Map<String, Double>>() {
                });

                // Create Config Values

                this.configAnalysisTime.<ConfigurationNode>createStorage(this.configurationNode);
                this.configMinimumAirTime.<ConfigurationNode>createStorage(this.configurationNode);
                this.configAltitudeMaximum.<ConfigurationNode>createStorage(this.configurationNode);
                this.configTickBounds.<ConfigurationNode>createStorage(this.configurationNode);
                this.configPunishmentLevels.<ConfigurationNode>createStorage(this.configurationNode);
                this.configPunishmentProperties.<ConfigurationNode>createStorage(this.configurationNode);
                this.configCustomPunishments.<ConfigurationNode>createStorage(this.configurationNode);
                this.configSeverityDistribution.<ConfigurationNode>createStorage(this.configurationNode);

                this.configManager.save(this.configurationNode);
            } catch (Exception e) {
                this.flyDetection.getPlugin().getLogger().error("A problem occurred attempting to create FlyDetection module's configuration!", e);
            }
        }

        @Override
        public void load() {
            try {
                if (this.exists()) {
                    this.configurationNode = this.configManager.load(this.flyDetection.getPlugin().getConfigurationOptions());

                    this.configAnalysisTime.<ConfigurationNode>loadStorage(this.configurationNode);
                    this.configMinimumAirTime.<ConfigurationNode>loadStorage(this.configurationNode);
                    this.configAltitudeMaximum.<ConfigurationNode>loadStorage(this.configurationNode);
                    this.configTickBounds.<ConfigurationNode>loadStorage(this.configurationNode);
                    this.configPunishmentLevels.<ConfigurationNode>loadStorage(this.configurationNode);
                    this.configPunishmentProperties.<ConfigurationNode>loadStorage(this.configurationNode);
                    this.configCustomPunishments.<ConfigurationNode>loadStorage(this.configurationNode);
                    this.configSeverityDistribution.<ConfigurationNode>loadStorage(this.configurationNode);

                    this.configManager.save(this.configurationNode);
                }
            } catch (IOException e) {
                this.flyDetection.getPlugin().getLogger().error("A problem occurred attempting to load FlyDetection module's configuration!", e);
            }
        }

        @Override
        public void update() {
            try {
                if (this.exists()) {
                    this.configurationNode = this.configManager.load(this.flyDetection.getPlugin().getConfigurationOptions());

                    this.configAnalysisTime.<ConfigurationNode>updateStorage(this.configurationNode);
                    this.configMinimumAirTime.<ConfigurationNode>updateStorage(this.configurationNode);
                    this.configAltitudeMaximum.<ConfigurationNode>updateStorage(this.configurationNode);
                    this.configTickBounds.<ConfigurationNode>updateStorage(this.configurationNode);
                    this.configPunishmentLevels.<ConfigurationNode>updateStorage(this.configurationNode);
                    this.configPunishmentProperties.<ConfigurationNode>updateStorage(this.configurationNode);
                    this.configCustomPunishments.<ConfigurationNode>updateStorage(this.configurationNode);
                    this.configSeverityDistribution.<ConfigurationNode>updateStorage(this.configurationNode);

                    this.configManager.save(this.configurationNode);
                }
            } catch (IOException e) {
                this.flyDetection.getPlugin().getLogger().error("A problem occurred attempting to update FlyDetection module's configuration!", e);
            }
        }

        @Override
        public boolean exists() {
            return this.configFile.exists();
        }

        @Override
        public Optional<File> getLocation() {
            return Optional.of(this.configFile);
        }

        @Override
        public <K, E> Optional<ConfigurationValue<K, E>> get(StorageKey<K> key, TypeToken<E> typeToken) {
            if (key.get() instanceof String) {
                if (key.get().equals("analysis-time") && typeToken.getRawType()
                        .equals(this.configAnalysisTime.getValueTypeToken().getRawType())) {
                    return Optional.of((ConfigurationValue<K, E>) this.configAnalysisTime);
                } else if (key.get().equals("minimum-air-time") && typeToken.getRawType()
                        .equals(this.configMinimumAirTime.getValueTypeToken().getRawType())) {
                    return Optional.of((ConfigurationValue<K, E>) this.configMinimumAirTime);
                } else if (key.get().equals("altitude-maximum") && typeToken.getRawType()
                        .equals(this.configAltitudeMaximum.getValueTypeToken().getRawType())) {
                    return Optional.of((ConfigurationValue<K, E>) this.configAltitudeMaximum);
                } else if (key.get().equals("tick-bounds") && typeToken.getRawType()
                        .equals(this.configTickBounds.getValueTypeToken().getRawType())) {
                    return Optional.of((ConfigurationValue<K, E>) this.configTickBounds);
                } else if (key.get().equals("punishment-properties") && typeToken.getRawType()
                        .equals(this.configPunishmentProperties.getValueTypeToken().getRawType())) {
                    return Optional.of((ConfigurationValue<K, E>) this.configPunishmentProperties);
                } else if (key.get().equals("punishment-levels") && typeToken.getRawType()
                        .equals(this.configPunishmentLevels.getValueTypeToken().getRawType())) {
                    return Optional.of((ConfigurationValue<K, E>) this.configPunishmentLevels);
                } else if (key.get().equals("custom-punishments") && typeToken.getRawType()
                        .equals(this.configCustomPunishments.getValueTypeToken().getRawType())) {
                    return Optional.of((ConfigurationValue<K, E>) this.configCustomPunishments);
                } else if (key.get().equals("severity-distribution") && typeToken.getRawType()
                        .equals(this.configSeverityDistribution.getValueTypeToken().getRawType())) {
                    return Optional.of((ConfigurationValue<K, E>) this.configSeverityDistribution);
                }
            }
            return Optional.empty();
        }

        @Override
        public <K, E> void set(ConfigurationValue<K, E> configurationValue) {
            if (configurationValue.getKey().get() instanceof String) {
                if (configurationValue.getKey().get().equals("analysis-time") && configurationValue.getValueTypeToken()
                        .getRawType().equals(this.configAnalysisTime.getValueTypeToken().getRawType())) {
                    this.configAnalysisTime = (ConfigurationValue<String, Double>) configurationValue;
                } else if (configurationValue.getKey().get().equals("minimum-air-time") && configurationValue.getValueTypeToken()
                        .getRawType().equals(this.configMinimumAirTime.getValueTypeToken().getRawType())) {
                    this.configMinimumAirTime = (ConfigurationValue<String, Double>) configurationValue;
                } else if (configurationValue.getKey().get().equals("altitude-maximum") && configurationValue.getValueTypeToken()
                        .getRawType().equals(this.configAltitudeMaximum.getValueTypeToken().getRawType())) {
                    this.configAltitudeMaximum = (ConfigurationValue<String, Double>) configurationValue;
                } else if (configurationValue.getKey().get().equals("tick-bounds") && configurationValue.getValueTypeToken()
                        .getRawType().equals(this.configTickBounds.getValueTypeToken().getRawType())) {
                    this.configTickBounds = (ConfigurationValue<String, Map<String, Double>>) configurationValue;
                } else if (configurationValue.getKey().get().equals("punishment-properties") && configurationValue.getValueTypeToken()
                        .getRawType().equals(this.configPunishmentProperties.getValueTypeToken().getRawType())) {
                    this.configPunishmentProperties = (ConfigurationValue<String, Map<String, String>>) configurationValue;
                } else if (configurationValue.getKey().get().equals("punishment-levels") && configurationValue.getValueTypeToken()
                        .getRawType().equals(this.configPunishmentLevels.getValueTypeToken().getRawType())) {
                    this.configPunishmentLevels = (ConfigurationValue<String, Map<String, Tuple<Double, Double>>>) configurationValue;
                } else if (configurationValue.getKey().get().equals("custom-punishments") && configurationValue.getValueTypeToken()
                        .getRawType().equals(this.configCustomPunishments.getValueTypeToken().getRawType())) {
                    this.configCustomPunishments = (ConfigurationValue<String, Map<String, List<String>>>) configurationValue;
                } else if (configurationValue.getKey().get().equals("severity-distribution") && configurationValue.getValueTypeToken()
                        .getRawType().equals(this.configSeverityDistribution.getValueTypeToken().getRawType())) {
                    this.configSeverityDistribution = (ConfigurationValue<String, Map<String, Double>>) configurationValue;
                }
            }
        }
    }
}
