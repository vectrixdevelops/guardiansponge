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
import io.github.connorhartley.guardian.context.ContextBuilder;
import io.github.connorhartley.guardian.context.ContextTypes;
import io.github.connorhartley.guardian.context.container.ContextContainer;
import io.github.connorhartley.guardian.detection.Detection;
import io.github.connorhartley.guardian.detection.check.Check;
import io.github.connorhartley.guardian.detection.check.CheckType;
import io.github.connorhartley.guardian.internal.contexts.player.PlayerLocationContext;
import io.github.connorhartley.guardian.internal.contexts.world.MaterialSpeedContext;
import io.github.connorhartley.guardian.internal.contexts.player.PlayerControlContext;
import io.github.connorhartley.guardian.sequence.SequenceBlueprint;
import io.github.connorhartley.guardian.sequence.SequenceBuilder;
import io.github.connorhartley.guardian.sequence.condition.ConditionResult;
import io.github.connorhartley.guardian.sequence.report.ReportType;
import io.github.connorhartley.guardian.sequence.report.SequenceReport;
import org.spongepowered.api.data.key.Keys;
import org.spongepowered.api.entity.living.player.User;
import org.spongepowered.api.event.entity.MoveEntityEvent;
import org.spongepowered.api.world.Location;
import org.spongepowered.api.world.World;

import java.util.HashMap;

public class HorizontalSpeedCheck extends Check {

    HorizontalSpeedCheck(CheckType checkType, User user) {
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
        public ContextBuilder getContextTracker() {
            return ContextBuilder.builder()
                    .append(PlayerLocationContext.class)
                    .append(PlayerControlContext.HorizontalSpeed.class)
                    .append(MaterialSpeedContext.class)
                    .build();
        }

        @Override
        public SequenceBlueprint getSequence() {
            return new SequenceBuilder()

                    .context(this.getDetection().getContextProvider(), this.getContextTracker())

                    // Trigger : Move Entity Event

                    .action(MoveEntityEvent.class)

                    // After 2 Seconds : Move Entity Event

                    .action(MoveEntityEvent.class)
                    .delay(((Double) this.analysisTime).intValue())
                    .expire(((Double) this.maximumTickRange).intValue())

                    .condition((user, event, contextContainers, sequenceReport, lastAction) -> {
                        if (!user.hasPermission("guardian.detection.speed.exempt")) {
                            return new ConditionResult(true, sequenceReport);
                        }
                        return new ConditionResult(false, sequenceReport);
                    })

                    .success((user, event, contextContainers, sequenceResult, lastAction) -> {
                        Guardian plugin = (Guardian) this.getDetection().getPlugin();

                        Location<World> start = null;
                        Location<World> present = null;

                        long playerControlTicks = 0;
                        long blockModifierTicks = 0;

                        double playerControlSpeed = 1.0;
                        double blockModifier = 1.0;
                        double playerControlModifier = 4.0;

                        PlayerControlContext.HorizontalSpeed.State playerControlState = PlayerControlContext.HorizontalSpeed.State.WALKING;

                        long currentTime;

                        for (ContextContainer contextContainer : contextContainers) {
                            if (contextContainer.get(ContextTypes.START_LOCATION).isPresent()) {
                                start = contextContainer.get(ContextTypes.START_LOCATION).get();
                            }

                            if (contextContainer.get(ContextTypes.PRESENT_LOCATION).isPresent()) {
                                present = contextContainer.get(ContextTypes.PRESENT_LOCATION).get();
                            }

                            if (contextContainer.get(ContextTypes.HORIZONTAL_CONTROL_SPEED).isPresent()) {
                                playerControlTicks = contextContainer.getContext().updateAmount();
                                playerControlSpeed = contextContainer.get(ContextTypes.HORIZONTAL_CONTROL_SPEED).get();
                            }

                            if (contextContainer.get(ContextTypes.SPEED_AMPLIFIER).isPresent()) {
                                blockModifierTicks = contextContainer.getContext().updateAmount();
                                blockModifier = contextContainer.get(ContextTypes.SPEED_AMPLIFIER).get();
                            }

                            if (contextContainer.get(ContextTypes.CONTROL_MODIFIER).isPresent()) {
                                playerControlModifier = contextContainer.get(ContextTypes.CONTROL_MODIFIER).get();
                            }

                            if (contextContainer.get(ContextTypes.CONTROL_SPEED_STATE).isPresent()) {
                                playerControlState = contextContainer.get(ContextTypes.CONTROL_SPEED_STATE).get();
                            }
                        }

                        if (playerControlTicks < this.minimumTickRange || blockModifierTicks < this.minimumTickRange) {
                            plugin.getLogger().warn("The server may be overloaded. A detection check has been skipped as it is less than a second and a half behind.");
                            SequenceReport failReport = SequenceReport.of(sequenceResult)
                                    .append(ReportType.TEST, false)
                                    .build();

                            return new ConditionResult(false, failReport);
                        } else if (playerControlTicks > this.maximumTickRange || blockModifierTicks > this.maximumTickRange) {
                            SequenceReport failReport = SequenceReport.of(sequenceResult)
                                    .append(ReportType.TEST, false)
                                    .build();

                            return new ConditionResult(false, failReport);
                        }

                        if (user.getPlayer().isPresent() && start != null && present != null) {
                            // ### For correct movement context ###
                            if (user.getPlayer().get().get(Keys.IS_SITTING).isPresent()) {
                                if (user.getPlayer().get().get(Keys.IS_SITTING).get()) {
                                    SequenceReport failReport = SequenceReport.of(sequenceResult)
                                            .append(ReportType.TEST, false)
                                            .build();

                                    return new ConditionResult(false, failReport);
                                }
                            }
                            // ####################################

                            currentTime = System.currentTimeMillis();

                            long contextTime = (1 / ((playerControlTicks + blockModifierTicks) / 2)) * ((long) this.analysisTime * 1000);
                            long sequenceTime = (currentTime - lastAction);

                            double travelDisplacement = Math.abs(Math.sqrt((
                                    (present.getX() - start.getX()) *
                                            (present.getX() - start.getX())) +
                                    (present.getZ() - start.getZ()) *
                                            (present.getZ() - start.getZ())));

                            travelDisplacement += playerControlModifier / 2;

                            double maximumSpeed = playerControlSpeed * blockModifier * (((contextTime + sequenceTime) / 2) / 1000);

                            SequenceReport.Builder successReportBuilder = SequenceReport.of(sequenceResult)
                                    .append(ReportType.INFORMATION, "Horizontal travel speed should be less than " +
                                            maximumSpeed + " while they're " + playerControlState.name() + ".");

                            if (travelDisplacement > maximumSpeed) {
                                successReportBuilder.append(ReportType.TEST, true)
                                        .append(ReportType.INFORMATION, "Overshot maximum speed by " +
                                                (travelDisplacement - maximumSpeed) + ".")
                                        .append(ReportType.SEVERITY, travelDisplacement - maximumSpeed);

                                // TODO : Remove this after testing \/
                                plugin.getLogger().warn(user.getName() + " has triggered the horizontal speed check and overshot " +
                                        "the maximum speed by " + (travelDisplacement - maximumSpeed) + ".");
                            } else {
                                successReportBuilder.append(ReportType.TEST, false);
                            }

                            return new ConditionResult(true, successReportBuilder.build());
                        }

                        return new ConditionResult(false, sequenceResult);
                    })

                    .build(this);
        }

        @Override
        public Check createInstance(User user) {
            return new HorizontalSpeedCheck(this, user);
        }
    }

}