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

import io.github.connorhartley.guardian.Guardian;
import io.github.connorhartley.guardian.detection.Detection;
import io.github.connorhartley.guardian.detection.check.Check;
import io.github.connorhartley.guardian.detection.check.CheckType;
import io.github.connorhartley.guardian.internal.contexts.player.PlayerControlContext;
import io.github.connorhartley.guardian.internal.contexts.player.PlayerLocationContext;
import io.github.connorhartley.guardian.sequence.SequenceBlueprint;
import io.github.connorhartley.guardian.sequence.SequenceBuilder;
import io.github.connorhartley.guardian.sequence.condition.ConditionResult;
import io.github.connorhartley.guardian.sequence.SequenceReport;
import io.github.connorhartley.guardian.util.check.PermissionCheck;
import org.spongepowered.api.data.key.Keys;
import org.spongepowered.api.entity.living.player.User;
import org.spongepowered.api.event.entity.MoveEntityEvent;
import org.spongepowered.api.world.Location;
import org.spongepowered.api.world.World;

import java.util.HashMap;
import java.util.Optional;

public class VerticalSpeedCheck extends Check {

    public VerticalSpeedCheck(CheckType checkType, User user) {
        super(checkType, user);
        this.setChecking(true);
    }

    @Override
    public void update() {}

    @Override
    public void finish() {
        this.setChecking(false);
    }

    public static class Type implements CheckType {

        private final Detection detection;

        private double analysisTime = 40;
        private double minimumTickRange = 30;
        private double maximumTickRange = 50;

        public Type(Detection detection) {
            this.detection = detection;

            if (this.detection.getConfiguration().get("analysis-time", 2.0).isPresent()) {
                this.analysisTime = this.detection.getConfiguration().get("analysis-time", 2.0).get().getValue() / 0.05;
            }

            if (this.detection.getConfiguration().get("tick-bounds", new HashMap<String, Double>()).isPresent()) {
                this.minimumTickRange = this.analysisTime * this.detection.getConfiguration().get("tick-bounds",
                        new HashMap<String, Double>()).get().getValue().get("min");
                this.maximumTickRange = this.analysisTime * this.detection.getConfiguration().get("tick-bounds",
                        new HashMap<String, Double>()).get().getValue().get("max");
            }
        }

        @Override
        public Detection getDetection() {
            return this.detection;
        }

        @Override
        public SequenceBlueprint getSequence() {
            return new SequenceBuilder()

                    .contexts(
                            new PlayerLocationContext((Guardian) this.getDetection().getPlugin(), this.getDetection()),
                            new PlayerControlContext.VerticalSpeed((Guardian) this.getDetection().getPlugin(), this.getDetection())
                    )

                    .action(MoveEntityEvent.class)

                    .action(MoveEntityEvent.class)
                    .delay(((Double) this.analysisTime).intValue())
                    .expire(((Double) this.maximumTickRange).intValue())

                    .condition(new PermissionCheck(this.detection))

                    .success((user, event, contextValuation, sequenceReport, lastAction) -> {
                        Guardian plugin = (Guardian) this.detection.getPlugin();

                        Optional<Location<World>> start = Optional.empty();
                        Optional<Location<World>> present = Optional.empty();

                        long currentTime;
                        long playerControlTicks = 0;
                        double playerControlSpeed = 0;

                        if (contextValuation.<PlayerLocationContext, Location<World>>get(PlayerLocationContext.class, "start_location").isPresent()) {
                            start = Optional.of(contextValuation.<PlayerLocationContext, Location<World>>get(PlayerLocationContext.class, "start_location").get());
                        }

                        if (contextValuation.<PlayerLocationContext, Location<World>>get(PlayerLocationContext.class, "present_location").isPresent()) {
                            present = Optional.of(contextValuation.<PlayerLocationContext, Location<World>>get(PlayerLocationContext.class, "present_location").get());
                        }

                        if (contextValuation.<PlayerControlContext.VerticalSpeed, Double>get(
                                PlayerControlContext.VerticalSpeed.class, "vertical_control_speed").isPresent()) {
                            playerControlSpeed = contextValuation.<PlayerControlContext.VerticalSpeed, Double>get(
                                    PlayerControlContext.VerticalSpeed.class, "vertical_control_speed").get();
                        }

                        if (contextValuation.<PlayerControlContext.VerticalSpeed, Integer>get(
                                PlayerControlContext.VerticalSpeed.class, "update").isPresent()) {
                            playerControlTicks = contextValuation.<PlayerControlContext.VerticalSpeed, Integer>get(
                                    PlayerControlContext.VerticalSpeed.class, "update").get();
                        }

                        if (playerControlTicks < this.minimumTickRange) {
                            plugin.getLogger().warn("The server may be overloaded. A detection check has been skipped as it is less than a second and a half behind.");
                            SequenceReport failReport = SequenceReport.builder().of(sequenceReport)
                                    .build(false);

                            return new ConditionResult(false, failReport);
                        } else if (playerControlTicks > this.maximumTickRange) {
                            SequenceReport failReport = SequenceReport.builder().of(sequenceReport)
                                    .build(false);

                            return new ConditionResult(false, failReport);
                        }

                        if (user.getPlayer().isPresent() && start.isPresent() && present.isPresent()) {
                            // ### For correct movement context ###
                            if (user.getPlayer().get().get(Keys.IS_SITTING).isPresent()) {
                                if (user.getPlayer().get().get(Keys.IS_SITTING).get()) {
                                    SequenceReport failReport = SequenceReport.builder().of(sequenceReport)
                                            .build(false);

                                    return new ConditionResult(false, failReport);
                                }
                            }
                            // ####################################

                            currentTime = System.currentTimeMillis();

                            long contextTime = (1 / playerControlTicks) * ((long) this.analysisTime * 1000);
                            long sequenceTime = (currentTime - lastAction);

                            double travelDisplacement = 0;

                            if (present.get().getY() - start.get().getY() > 0) {
                                travelDisplacement = Math.abs(Math.sqrt(
                                        (present.get().getY() - start.get().getY()) *
                                                (present.get().getY() - start.get().getY())));
                            }

                            double maximumSpeed = (playerControlSpeed * (playerControlSpeed / 0.2)) * (((contextTime + sequenceTime) / 2) / 1000) + 0.01;

                            // TODO: Clean up the following...

                            SequenceReport.Builder successReportBuilder = SequenceReport.builder().of(sequenceReport);

                            if (travelDisplacement > maximumSpeed && travelDisplacement > 0) {
                                successReportBuilder
                                        .information("Overshot maximum speed by " + (travelDisplacement - maximumSpeed) + ".")
                                        .type("vertically overspeeding")
                                        .severity(travelDisplacement - maximumSpeed);

                                // TODO : Remove this after testing \/
                                plugin.getLogger().warn(user.getName() + " has triggered the vertical speed check and overshot " +
                                        "the maximum speed by " + (travelDisplacement - maximumSpeed) + ".");
                            } else {
                                return new ConditionResult(false, successReportBuilder.build(false));
                            }

                            return new ConditionResult(true, successReportBuilder.build(true));
                        }

                        return new ConditionResult(false, sequenceReport);
                    })

                    .build(this);
        }

        @Override
        public Check createInstance(User user) {
            return new VerticalSpeedCheck(this, user);
        }
    }
}
