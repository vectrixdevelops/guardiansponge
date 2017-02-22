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

import io.github.connorhartley.guardian.context.Context;
import io.github.connorhartley.guardian.context.ContextBuilder;
import io.github.connorhartley.guardian.context.ContextKeys;
import io.github.connorhartley.guardian.context.ContextTypes;
import io.github.connorhartley.guardian.detection.Detection;
import io.github.connorhartley.guardian.detection.check.Check;
import io.github.connorhartley.guardian.detection.check.CheckController;
import io.github.connorhartley.guardian.detection.check.CheckProvider;
import io.github.connorhartley.guardian.internal.contexts.user.control.PlayerControlSpeedContext;
import io.github.connorhartley.guardian.sequence.Sequence;
import io.github.connorhartley.guardian.sequence.SequenceBlueprint;
import io.github.connorhartley.guardian.sequence.SequenceBuilder;
import io.github.connorhartley.guardian.sequence.condition.ConditionResult;
import io.github.connorhartley.guardian.sequence.report.ReportType;
import io.github.connorhartley.guardian.sequence.report.SequenceReport;
import io.github.connorhartley.guardian.util.ContextValue;
import org.spongepowered.api.data.key.Keys;
import org.spongepowered.api.data.value.mutable.Value;
import org.spongepowered.api.entity.living.player.User;
import org.spongepowered.api.event.entity.MoveEntityEvent;
import org.spongepowered.api.world.Location;
import org.spongepowered.api.world.World;

public class MovementSpeedCheck extends Check {

    public MovementSpeedCheck(CheckProvider checkProvider, User user) {
        super(checkProvider, user);
    }

    @Override
    public void update() {}

    @Override
    public void finish() {}

    public static class Provider implements CheckProvider {

        private final Detection detection;

        private Location<World> previousLocation;
        private Location<World> presentLocation;

        public Provider(Detection detection) {
            this.detection = detection;
        }

        @Override
        public Detection getDetection() {
            return this.detection;
        }

        @Override
        public ContextBuilder getContextTracker() {
            return ContextBuilder.builder().append(ContextTypes.PLAYER_CONTROL_SPEED)
                    .append(ContextTypes.BLOCK_SPEED).build();
        }

        @Override
        public SequenceBlueprint getSequence() {
            return new SequenceBuilder()

                    .context(this.getContextTracker())

                    // Trigger : Move Entity Event

                    .action(MoveEntityEvent.class)

                    .condition((user, event, contexts, sequenceResult, lastAction) -> {
                        if (!user.hasPermission("guardian.detection.movementspeed.exempt")) {
                            if (user.getPlayer().isPresent()) {
                                this.previousLocation = user.getPlayer().get().getLocation();
                            }

                            return new ConditionResult(true, sequenceResult);
                        }
                        return new ConditionResult(false, sequenceResult);
                    })

                    // After 2 Seconds : Move Entity Event

                    .action(MoveEntityEvent.class)
                    .delay(20 * 2)
                    .expire(20 * 3)

                    .suspend(ContextTypes.PLAYER_CONTROL_SPEED, ContextTypes.BLOCK_SPEED)

                    .success((user, event, contexts, sequenceResult, lastAction) -> {
                        double playerControlSpeed = 0.0;
                        double blockModifier = 0.0;

                        long currentTime = 0;

                        PlayerControlSpeedContext.State playerControlState = PlayerControlSpeedContext.State.WALKING;

                        for (Context context : contexts) {
                            if (context.getName().equals(ContextTypes.PLAYER_CONTROL_SPEED)) {
                                ContextValue value = context.getValues().get(ContextKeys.CONTROL_SPEED);
                                playerControlSpeed = value.<Double>get();
                            }

                            if (context.getName().equals(ContextTypes.BLOCK_SPEED)) {
                                ContextValue value = context.getValues().get(ContextKeys.SPEED_AMPLIFIER);
                                blockModifier = value.<Double>get();
                            }
                        }

                        if (user.getPlayer().isPresent()) {
                            if (user.getPlayer().get().get(Keys.IS_SITTING).isPresent()) {
                                if (user.getPlayer().get().get(Keys.IS_SITTING).get()) {
                                    SequenceReport failReport = SequenceReport.of(sequenceResult)
                                            .append(ReportType.TEST, false)
                                            .build();

                                    return new ConditionResult(false, failReport);
                                }
                            }

                            currentTime = System.currentTimeMillis();

                            this.presentLocation = user.getPlayer().get().getLocation();

                            double travelDisplacement = Math.abs(Math.sqrt((
                                    (this.presentLocation.getX() - this.previousLocation.getX()) *
                                            (this.presentLocation.getX() - this.previousLocation.getX())) +
                                    (this.presentLocation.getZ() - this.previousLocation.getZ()) *
                                            (this.presentLocation.getZ() - this.previousLocation.getZ())) +
                                    (this.presentLocation.getY() - this.previousLocation.getY()) *
                                            (this.presentLocation.getY() - this.previousLocation.getY()));

                            double maximumSpeed = playerControlSpeed * blockModifier * ((currentTime - lastAction) * 1000);

                            SequenceReport.Builder successReportBuilder = SequenceReport.of(sequenceResult)
                                    .append(ReportType.INFORMATION, "Travel speed should be less than " +
                                            maximumSpeed + " while they're " + playerControlState.name() + ".");

                            if (travelDisplacement > maximumSpeed) {
                                successReportBuilder.append(ReportType.TEST, true)
                                        .append(ReportType.INFORMATION, "Overshot maximum speed by " +
                                                (travelDisplacement - maximumSpeed) + ".");
                            } else {
                                successReportBuilder.append(ReportType.TEST, false);
                            }

                            return new ConditionResult(true, successReportBuilder.build());
                        }

                        return new ConditionResult(false, sequenceResult);
                    })

                    .build(this, this.getDetection().getContextProvider());
        }

        @Override
        public Check createInstance(User user) {
            return new MovementSpeedCheck(this, user);
        }
    }

}
