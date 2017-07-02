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
import com.me4502.modularframework.module.guice.ModuleContainer;
import com.me4502.precogs.detection.CommonDetectionTypes;
import io.github.connorhartley.guardian.Guardian;
import io.github.connorhartley.guardian.detection.Detection;
import io.github.connorhartley.guardian.detection.DetectionRegistry;
import io.github.connorhartley.guardian.event.sequence.SequenceFinishEvent;
import io.github.connorhartley.guardian.internal.checks.FlyCheck;
import io.github.connorhartley.guardian.internal.punishments.CustomPunishment;
import io.github.connorhartley.guardian.internal.punishments.KickPunishment;
import io.github.connorhartley.guardian.internal.punishments.ReportPunishment;
import io.github.connorhartley.guardian.internal.punishments.ResetPunishment;
import io.github.connorhartley.guardian.internal.punishments.WarningPunishment;
import io.github.connorhartley.guardian.storage.StorageProvider;
import io.github.connorhartley.guardian.util.HoconLoaderPatch;
import org.apache.commons.math3.distribution.NormalDistribution;
import org.spongepowered.api.event.Listener;
import org.spongepowered.api.event.game.GameReloadEvent;
import org.spongepowered.api.plugin.PluginContainer;
import org.spongepowered.api.util.Tuple;
import tech.ferus.util.config.ConfigKey;
import tech.ferus.util.config.HoconConfigFile;

import javax.annotation.Nonnull;
import java.io.IOException;
import java.nio.file.Path;
import java.util.Collections;

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
                () -> Collections.singletonList(new FlyCheck<>(this)),
                () -> new Configuration(this, this.getConfigLocation().orElseThrow(Error::new)),
                CustomPunishment.class, ResetPunishment.class,
                WarningPunishment.class, KickPunishment.class,
                ReportPunishment.class
        );

        DetectionRegistry.register(this, CommonDetectionTypes.Category.MOVEMENT);
        this.setReady(true);
    }

    @Override
    public void onDeconstruction() {
        this.setReady(false);
    }

    @Listener
    public void onReload(GameReloadEvent event) {
        getChecks().forEach(check -> this.getPlugin().getSequenceController().unregister(check));
        this.setChecks(null);

        this.getConfiguration().load();

        this.setChecks(this.getCheckSupplier().create());
        getChecks().forEach(check -> this.getPlugin().getSequenceController().register(check));
    }

    @Listener
    public void onSequenceFinish(SequenceFinishEvent event) {
        try {
            this.handleFinish(this, event, () -> {
                double lower = Configuration.SEVERITY_LOWER.get(this.getConfiguration().getStorage(), 0d);
                double mean = Configuration.SEVERITY_MEAN.get(this.getConfiguration().getStorage(), 10d);
                double standardDeviation = Configuration.SEVERITY_STANDARD_DEVIATION.get(this.getConfiguration().getStorage(), 5d);

                return Tuple.of(new NormalDistribution(mean, standardDeviation), lower);
            });
        } catch (Throwable throwable) {
            this.getPlugin().getLogger().error("Error occurred handling sequence heuristic.", throwable);
        }
    }

    @Override
    public CommonDetectionTypes.Category getCategory() {
        return CommonDetectionTypes.Category.MOVEMENT;
    }

    public static class Configuration implements StorageProvider<HoconConfigFile, Path> {

        private static final String ROOT = "fly.conf";

        private final FlyDetection detection;
        private final Path configDir;

        private HoconConfigFile configFile;

        public static final ConfigKey<Double> ANALYSIS_TIME = ConfigKey.of("analysis", "sequence-time");
        public static final ConfigKey<Double> MINIMUM_AIR_TIME = ConfigKey.of("analysis", "minimum-air-time");
        public static final ConfigKey<Double> SEVERITY_LOWER = ConfigKey.of("punishment", "severity", "lower");
        public static final ConfigKey<Double> SEVERITY_MEAN = ConfigKey.of("punishment", "severity", "mean");
        public static final ConfigKey<Double> SEVERITY_STANDARD_DEVIATION = ConfigKey.of("punishment", "severity", "standard-deviation");
        public static final ConfigKey<Double> HEURISTIC_DIVIDER_BASE = ConfigKey.of("heuristic", "divider-base");
        public static final ConfigKey<Double> HEURISTIC_POWER = ConfigKey.of("heuristic", "power");
        public static final ConfigKey<Double> HEURISTIC_RELEVANCY_PERIOD = ConfigKey.of("heuristic", "relevancy-period");

        Configuration(@Nonnull FlyDetection detection,
                      @Nonnull Path configDir) {
            this.detection = detection;
            this.configDir = configDir;
        }

        @Override
        public void load() {
            final Path path = this.configDir.resolve(ROOT);

            try {
                this.configFile = HoconLoaderPatch.load(path, "/detection/" + ROOT, !this.exists());
            } catch (final IOException e) {
                this.detection.getPlugin().getLogger().error("A problem occurred attempting to load Guardians global configuration!", e);
            }
        }

        @Override
        public HoconConfigFile getStorage() {
            return this.configFile;
        }

        @Override
        public Path getLocation() {
            return this.configDir;
        }

        @Override
        public boolean exists() {
            return this.configDir.resolve(ROOT).toFile().exists();
        }
    }
}
