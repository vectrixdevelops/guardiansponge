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
import io.github.connorhartley.guardian.context.listener.ContextListener;
import io.github.connorhartley.guardian.context.ContextTypes;
import io.github.connorhartley.guardian.context.container.ContextContainer;
import io.github.connorhartley.guardian.detection.Detection;
import io.github.connorhartley.guardian.detection.check.Check;
import io.github.connorhartley.guardian.detection.check.CheckType;
import io.github.connorhartley.guardian.internal.contexts.player.PlayerLocationContext;
import io.github.connorhartley.guardian.internal.contexts.player.PlayerPositionContext;
import io.github.connorhartley.guardian.sequence.SequenceBlueprint;
import io.github.connorhartley.guardian.sequence.SequenceBuilder;
import io.github.connorhartley.guardian.sequence.condition.ConditionResult;
import io.github.connorhartley.guardian.sequence.SequenceReport;
import org.spongepowered.api.data.key.Keys;
import org.spongepowered.api.entity.living.player.User;
import org.spongepowered.api.event.entity.MoveEntityEvent;
import org.spongepowered.api.world.Location;
import org.spongepowered.api.world.World;

import java.util.HashMap;

public class FlyCheck extends Check {

    public FlyCheck(CheckType checkType, User user) {
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
        private double altitudeMaximum = 3.1;
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

            if (this.detection.getConfiguration().get("altitude-maximum", 3.1).isPresent()) {
                this.altitudeMaximum = this.detection.getConfiguration().get("altitude-maximum", 3.1).get().getValue();
            }
        }

        @Override
        public Detection getDetection() {
            return this.detection;
        }

        @Override
        public ContextListener getContextTracker() {
            return ContextListener.builder()
                    .listen(PlayerLocationContext.class)
                    .listen(PlayerPositionContext.Altitude.class)
                    .build();
        }

        @Override
        public SequenceBlueprint getSequence() {
            return new SequenceBuilder()

                    .context(this.getDetection().getContextProvider(), this.getContextTracker())

                    .action(MoveEntityEvent.class)

                    .action(MoveEntityEvent.class)
                    .delay(((Double) this.analysisTime).intValue())
                    .expire(((Double) this.maximumTickRange).intValue())

                    .condition((user, event, contextContainer, sequenceReport, lastAction) -> {
                        if (user.getPlayer().isPresent()) {
                            if (!user.getPlayer().get().hasPermission("guardian.detections.bypass.fly")) {
                                return new ConditionResult(true, sequenceReport);
                            }
                        }
                        return new ConditionResult(false, sequenceReport);
                    })

                    .success((user, event, contextContainers, sequenceReport, lastAction) -> {
                        Guardian plugin = (Guardian) this.detection.getPlugin();

                        Location<World> start = null;
                        Location<World> present = null;

                        long currentTime;
                        long playerAltitudeGainTicks = 0;
                        double playerAltitudeGain = 0;

                        for (ContextContainer contextContainer : contextContainers) {
                            if (contextContainer.get(ContextTypes.START_LOCATION).isPresent()) {
                                start = contextContainer.get(ContextTypes.START_LOCATION).get();
                            }

                            if (contextContainer.get(ContextTypes.PRESENT_LOCATION).isPresent()) {
                                present = contextContainer.get(ContextTypes.PRESENT_LOCATION).get();
                            }

                            if (contextContainer.get(ContextTypes.GAINED_ALTITUDE).isPresent()) {
                                playerAltitudeGainTicks = contextContainer.getContext().updateAmount();
                                playerAltitudeGain = contextContainer.get(ContextTypes.GAINED_ALTITUDE).get();
                            }
                        }

                        if (playerAltitudeGainTicks < this.minimumTickRange) {
                            plugin.getLogger().warn("The server may be overloaded. A detection check has been skipped as it is less than a second and a half behind.");
                            SequenceReport failReport = SequenceReport.builder().of(sequenceReport)
                                    .build(false);

                            return new ConditionResult(false, failReport);
                        } else if (playerAltitudeGainTicks > this.maximumTickRange) {
                            SequenceReport failReport = SequenceReport.builder().of(sequenceReport)
                                    .build(false);

                            return new ConditionResult(false, failReport);
                        }

                        if (user.getPlayer().isPresent() && start != null && present != null) {
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

                            if (user.getPlayer().get().get(Keys.CAN_FLY).isPresent()) {
                                if (user.getPlayer().get().get(Keys.CAN_FLY).get()) {
                                    SequenceReport failReport = SequenceReport.builder().of(sequenceReport)
                                            .build(false);

                                    return new ConditionResult(false, failReport);
                                }
                            }

                            if (user.getPlayer().get().getLocation().getY() < -1.25 || !user.getPlayer().get().isLoaded()) {
                                SequenceReport failReport = SequenceReport.builder().of(sequenceReport)
                                        .build(false);

                                return new ConditionResult(false, failReport);
                            }

                            double sequenceTime = (currentTime - lastAction) / 1000;
                            double contextTime = (playerAltitudeGainTicks + this.analysisTime) / 2;

                            double altitudeDisplacement = Math.abs(present.getY() - start.getY());
                            double travelDisplacement = Math.abs(Math.sqrt((
                                    (present.getX() - start.getX()) *
                                            (present.getX() - start.getX())) +
                                    (present.getZ() - start.getZ()) *
                                            (present.getZ() - start.getZ())));


                            double meanAltitude = playerAltitudeGain / ((contextTime + sequenceTime) / 2);

                            double finalGain = (altitudeDisplacement / meanAltitude) + meanAltitude;

                            // TODO: Clean up the following...

                            SequenceReport.Builder successReportBuilder = SequenceReport.builder().of(sequenceReport);

                            if (altitudeDisplacement < 1 || meanAltitude < 1) {
                                SequenceReport failReport = SequenceReport.builder().of(sequenceReport)
                                        .build(false);

                                return new ConditionResult(false, failReport);
                            }

                            // Possible math duplication over the final gain check. (Clean up later)

                            if (altitudeDisplacement + this.altitudeMaximum > travelDisplacement) {
                                SequenceReport failReport = SequenceReport.builder().of(sequenceReport)
                                        .build(false);

                                return new ConditionResult(false, failReport);
                            }

                            if (finalGain > (this.altitudeMaximum * (this.analysisTime * 0.05))) {
                                successReportBuilder
                                        .information("Result of altitude gain was " + finalGain + ".")
                                        .type("flying")
                                        .severity(finalGain);

                                // TODO : Remove this after testing \/
                                plugin.getLogger().warn(user.getName() + " has triggered the flight check and overshot " +
                                        "the maximum altitude gain by " + finalGain + ".");
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
            return new FlyCheck(this, user);
        }
    }
}
