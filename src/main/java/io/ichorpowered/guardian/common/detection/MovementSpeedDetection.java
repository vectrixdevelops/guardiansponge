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
package io.ichorpowered.guardian.common.detection;

import com.google.inject.Inject;
import com.ichorpowered.guardian.api.detection.DetectionConfiguration;
import com.ichorpowered.guardian.api.detection.DetectionPhase;
import com.me4502.modularframework.module.Module;
import com.me4502.modularframework.module.guice.ModuleContainer;
import io.ichorpowered.guardian.GuardianPlugin;
import io.ichorpowered.guardian.detection.AbstractDetection;
import io.ichorpowered.guardian.detection.GuardianDetectionPhase;
import io.ichorpowered.guardian.common.check.movement.HorizontalSpeedCheck;
import io.ichorpowered.guardian.common.check.movement.VerticalSpeedCheck;
import io.ichorpowered.guardian.common.penalty.ResetPenalty;
import io.ichorpowered.guardian.phase.GuardianPhaseFilter;
import ninja.leaping.configurate.commented.CommentedConfigurationNode;
import org.spongepowered.api.plugin.PluginContainer;
import tech.ferus.util.config.ConfigFile;

import javax.annotation.Nonnull;
import java.io.IOException;
import java.nio.file.Path;

@Module(id = "movementspeed",
        name = "Movement Speed Detection",
        authors = { "Connor Hartley (vectrix)" },
        version = "0.2.0",
        onEnable = "onConstruction",
        onDisable = "onDeconstruction"
)
public class MovementSpeedDetection extends AbstractDetection {

    private State state = State.UNDEFINED;
    private MovementSpeedConfiguration detectionConfiguration;
    private GuardianDetectionPhase<GuardianPlugin, DetectionConfiguration> phaseManipulator;

    @Inject
    public MovementSpeedDetection(@ModuleContainer PluginContainer moduleContainer) {
        super(GuardianPlugin.class.cast(moduleContainer.getInstance().get()), "movementspeed", "Movement Speed Detection");
    }

    @Override
    public void onConstruction() {
        this.detectionConfiguration = new MovementSpeedConfiguration(this, this.getOwner().getConfigDirectory());
        this.detectionConfiguration.load();

        this.phaseManipulator = new GuardianDetectionPhase<>(this.getOwner(), this, new GuardianPhaseFilter()
                // Check
                .include(HorizontalSpeedCheck.class)
                .include(VerticalSpeedCheck.class)

                // Penalty
                .include(ResetPenalty.class)
        );
    }

    @Override
    public void onDeconstruction() {

    }

    @Nonnull
    @Override
    public String getPermission(@Nonnull String permissionTarget) {
        return null;
    }

    @Nonnull
    @Override
    public DetectionConfiguration getConfiguration() {
        return this.detectionConfiguration;
    }

    @Nonnull
    @Override
    public State getState() {
        return this.state;
    }

    @Nonnull
    @Override
    public DetectionPhase<GuardianPlugin, DetectionConfiguration> getPhaseManipulator() {
        return this.phaseManipulator;
    }

    public static class MovementSpeedConfiguration implements DetectionConfiguration {

        private static final String FILE_NAME = "movementspeed.conf";

        private final MovementSpeedDetection detection;
        private final Path configDir;

        private ConfigFile<CommentedConfigurationNode> configFile;

        MovementSpeedConfiguration(@Nonnull MovementSpeedDetection detection,
                                   @Nonnull Path configDir) {
            this.detection = detection;
            this.configDir = configDir;
        }

        @Override
        public void load() {
            try {
                this.configFile = ConfigFile.loadHocon(this.configDir.resolve("detection").resolve(FILE_NAME),
                        "/detection/" + FILE_NAME, false, false);
            } catch (IOException e) {
                this.detection.getOwner().getLogger().error("A problem occurred attempting to load the " +
                        "guardian movement speed detection configuration!", e);
            }
        }

        @Nonnull
        @Override
        public ConfigFile<CommentedConfigurationNode> getStorage() {
            return this.configFile;
        }

        @Nonnull
        @Override
        public Path getLocation() {
            return this.configDir;
        }

        public boolean exists() {
            return this.configDir.resolve("detection").resolve(FILE_NAME).toFile().exists();
        }

    }

}
