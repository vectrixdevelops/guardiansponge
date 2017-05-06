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
package io.github.connorhartley.guardian.util.check;

import io.github.connorhartley.guardian.detection.Detection;
import io.github.connorhartley.guardian.sequence.SequenceReport;
import io.github.connorhartley.guardian.sequence.condition.Condition;
import io.github.connorhartley.guardian.sequence.condition.ConditionResult;
import io.github.connorhartley.guardian.sequence.context.CaptureContainer;
import io.github.nucleuspowered.nucleus.api.events.NucleusTeleportEvent;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.entity.living.player.User;
import org.spongepowered.api.event.Event;
import org.spongepowered.api.event.entity.DestructEntityEvent;
import org.spongepowered.api.event.entity.MoveEntityEvent;
import org.spongepowered.api.event.entity.RideEntityEvent;

/**
 * Common Movement Conditions
 *
 * Commonly used movement based conditions for
 * small compatibility or optimization fixes.
 */
public class CommonMovementConditions {

    /**
     * Death Condition
     *
     * Base: Failure
     *
     * Cancels a sequence if the player being tracked dies.
     */
    public static class DeathCondition implements Condition {

        private final Detection detection;

        public DeathCondition(Detection detection) {
            this.detection = detection;
        }

        @Override
        public ConditionResult test(User user, Event event, CaptureContainer captureContainer, SequenceReport sequenceReport, long lastAction) {
            SequenceReport report = SequenceReport.builder().of(sequenceReport)
                    .build(false);

            if (event instanceof DestructEntityEvent.Death) {
                return new ConditionResult(true, report);
            }

            return new ConditionResult(false, report);
        }
    }

    /**
     * Vehicle Mount Condition
     *
     * Base: Failure
     *
     * Cancels a sequence if the player being tracked mounts or dismounts a vehicle.
     */
    public static class VehicleMountCondition implements Condition {

        private final Detection detection;

        public VehicleMountCondition(Detection detection) {
            this.detection = detection;
        }

        @Override
        public ConditionResult test(User user, Event event, CaptureContainer captureContainer, SequenceReport sequenceReport, long lastAction) {
            SequenceReport report = SequenceReport.builder().of(sequenceReport)
                    .build(false);

            if (event instanceof RideEntityEvent) {
                return new ConditionResult(true, report);
            }

            return new ConditionResult(false, report);
        }
    }

    /**
     * Teleportation Condition
     *
     * Base: Condition
     *
     * Cancels a sequence if the player being tracked teleports.
     */
    public static class TeleportCondition implements Condition {
        private final Detection detection;

        public TeleportCondition(Detection detection) {
            this.detection = detection;
        }

        @Override
        public ConditionResult test(User user, Event event, CaptureContainer captureContainer, SequenceReport sequenceReport, long lastAction) {
            SequenceReport report = SequenceReport.builder().of(sequenceReport)
                    .build(false);

            if (event instanceof MoveEntityEvent.Teleport) {
                return new ConditionResult(false, report);
            }

            return new ConditionResult(true, report);
        }
    }

    /**
     * Nucleus Teleportation Condition
     *
     * Base: Failure
     *
     * Cancels a sequence if the player being tracked teleports through Nucleus.
     *
     * NOTE: This is only temporary.
     */
    public static class NucleusTeleportCondition implements Condition {

        private final Detection detection;

        public NucleusTeleportCondition(Detection detection) {
            this.detection = detection;
        }

        @Override
        public ConditionResult test(User user, Event event, CaptureContainer captureContainer, SequenceReport sequenceReport, long lastAction) {
            SequenceReport report = SequenceReport.builder().of(sequenceReport)
                    .build(false);

            if (Sponge.getPluginManager().getPlugin("nucleus").isPresent()) {
                if (event instanceof NucleusTeleportEvent.AboutToTeleport) {
                    return new ConditionResult(true, report);
                }
            }

            return new ConditionResult(false, report);
        }
    }
}
