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
package io.github.connorhartley.guardian.internal.checks;

import com.google.common.reflect.TypeToken;
import io.github.connorhartley.guardian.Guardian;
import io.github.connorhartley.guardian.GuardianConfiguration;
import io.github.connorhartley.guardian.detection.Detection;
import io.github.connorhartley.guardian.detection.check.Check;
import io.github.connorhartley.guardian.internal.contexts.player.PlayerControlContext;
import io.github.connorhartley.guardian.internal.contexts.player.PlayerLocationContext;
import io.github.connorhartley.guardian.sequence.SequenceBlueprint;
import io.github.connorhartley.guardian.sequence.SequenceBuilder;
import io.github.connorhartley.guardian.sequence.SequenceResult;
import io.github.connorhartley.guardian.sequence.condition.ConditionResult;
import io.github.connorhartley.guardian.storage.StorageProvider;
import io.github.connorhartley.guardian.storage.container.StorageKey;
import io.github.connorhartley.guardian.util.check.CommonMovementConditions;
import io.github.connorhartley.guardian.util.check.PermissionCheckCondition;
import org.spongepowered.api.data.key.Keys;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.event.entity.MoveEntityEvent;
import org.spongepowered.api.world.Location;
import org.spongepowered.api.world.World;
import tech.ferus.util.config.HoconConfigFile;

import java.io.File;
import java.nio.file.Path;
import java.util.Map;

public class VerticalSpeedCheck<E, F extends StorageProvider<HoconConfigFile, Path>> implements Check<E, F> {

    private final Detection<E, F> detection;

    private double analysisTime = 40;
    private double minimumTickRange = 30;
    private double maximumTickRange = 50;

    public VerticalSpeedCheck(Detection<E, F> detection) {
        this.detection = detection;

        this.analysisTime = this.detection.getConfiguration().getStorage().getNode("analysis", "sequence-time").getDouble(2d) / 0.05;

        this.minimumTickRange = this.analysisTime * GuardianConfiguration.GLOBAL_TICK_MIN.get(this.detection.getConfiguration().getStorage(), 0.75);

        this.maximumTickRange = this.analysisTime * GuardianConfiguration.GLOBAL_TICK_MAX.get(this.detection.getConfiguration().getStorage(), 1.25);
    }

    @Override
    public Detection<E, F> getDetection() {
        return this.detection;
    }

    @Override
    public SequenceBlueprint getSequence() {
        return new SequenceBuilder<E, F>()

                .capture(
                        new PlayerLocationContext<>((Guardian) this.getDetection().getPlugin(), this.getDetection()),
                        new PlayerControlContext.VerticalSpeed<>((Guardian) this.getDetection().getPlugin(), this.getDetection())
                )

                // Trigger : Move Entity Event

                .action(MoveEntityEvent.class)

                // After 2 Seconds : Move Entity Event

                .action(MoveEntityEvent.class)
                        .delay(((Double) this.analysisTime).intValue())
                        .expire(((Double) this.maximumTickRange).intValue())

                        /*
                         * Cancels the sequence if the player being tracked, dies, teleports,
                         * teleports through Nucleus and mounts or dismounts a vehicle. This
                         * is due to the location comparison at the beginning and end of a check
                         * which these events change the behaviour of.
                         */
                        .failure(new CommonMovementConditions.DeathCondition(this.detection))
                        .failure(new CommonMovementConditions.NucleusTeleportCondition(this.detection))
                        .failure(new CommonMovementConditions.VehicleMountCondition(this.detection))
                        .condition(new CommonMovementConditions.TeleportCondition(this.detection))

                        // Does the player have permission?
                        .condition(new PermissionCheckCondition(this.detection))

                        .condition((user, event, contextValuation, sequenceReport, lastAction) -> {
                            SequenceResult.Builder report = SequenceResult.builder().of(sequenceReport);

                            Guardian plugin = (Guardian) this.detection.getPlugin();

                            Location<World> start;
                            Location<World> present;

                            long currentTime;
                            long playerControlTicks;
                            double playerControlSpeed;

                            if (user.getPlayer().isPresent()) {
                                Player player = user.getPlayer().get();

                                /*
                                 * Context Collection
                                 */

                                start = contextValuation.<PlayerLocationContext, Location<World>>get(PlayerLocationContext.class, "start_location")
                                        .orElse(player.getLocation());

                                present = contextValuation.<PlayerLocationContext, Location<World>>get(PlayerLocationContext.class, "present_location")
                                        .orElse(player.getLocation());

                                playerControlTicks = contextValuation.<PlayerControlContext.HorizontalSpeed, Integer>get(
                                        PlayerControlContext.HorizontalSpeed.class, "update").orElse(0);

                                playerControlSpeed = contextValuation.<PlayerControlContext.HorizontalSpeed, Double>get(
                                        PlayerControlContext.HorizontalSpeed.class, "vertical_control_speed").orElse(0d);

                                /*
                                 * Context Analysis
                                 */

                                if (playerControlTicks < this.minimumTickRange) {
                                    plugin.getLogger().warn("The server may be overloaded. A detection check has been skipped as it is less than a second and a half behind.");
                                    return new ConditionResult(false, report.build(false));
                                } else if (playerControlTicks > this.maximumTickRange) {
                                    return new ConditionResult(false, report.build(false));
                                }

                                currentTime = System.currentTimeMillis();

                                if (player.get(Keys.VEHICLE).isPresent()) {
                                    return new ConditionResult(false, report.build(false));
                                }

                                double travelDisplacement = Math.abs(Math.sqrt(
                                        (present.getY() - start.getY()) *
                                                (present.getY() - start.getY())));

                                double maximumSpeed = (playerControlSpeed * (playerControlSpeed / 0.2)) *
                                        (((((1 / playerControlTicks) * ((long) this.analysisTime * 1000)) +
                                                (currentTime - lastAction)) / 2) / 1000) + 0.01;

                                report
                                        .information("Vertical travel speed should be less than " + maximumSpeed + ".");

                                if (travelDisplacement > maximumSpeed && present.getY() - start.getY() > 0) {
                                    report
                                            .information("Overshot maximum speed by " + (travelDisplacement - maximumSpeed) + ".")
                                            .type("vertically overspeeding")
                                            .initialLocation(start.copy())
                                            .severity(travelDisplacement - maximumSpeed);

                                    // TODO : Remove this after testing \/
                                    plugin.getLogger().warn(user.getName() + " has triggered the vertical speed check and overshot " +
                                            "the maximum speed by " + (travelDisplacement - maximumSpeed) + ".");

                                    return new ConditionResult(true, report.build(true));
                                }
                            }
                            return new ConditionResult(false, sequenceReport);
                        })

                .build(this);
    }
}
