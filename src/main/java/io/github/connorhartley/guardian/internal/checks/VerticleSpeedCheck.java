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

import io.github.connorhartley.guardian.context.ContextBuilder;
import io.github.connorhartley.guardian.detection.Detection;
import io.github.connorhartley.guardian.detection.check.Check;
import io.github.connorhartley.guardian.detection.check.CheckType;
import io.github.connorhartley.guardian.sequence.SequenceBlueprint;
import io.github.connorhartley.guardian.sequence.SequenceBuilder;
import io.github.connorhartley.guardian.sequence.condition.ConditionResult;
import org.spongepowered.api.entity.living.player.User;
import org.spongepowered.api.event.entity.MoveEntityEvent;
import org.spongepowered.api.world.Location;
import org.spongepowered.api.world.World;

public class VerticleSpeedCheck extends Check {

    private CheckType checkType;
    private User user;

    public VerticleSpeedCheck(CheckType checkType, User user) {
        super(checkType, user);
        this.checkType = checkType;
        this.user = user;

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

        private Location<World> previousLocation;

        public Type(Detection detection) {
            this.detection = detection;
        }

        @Override
        public Detection getDetection() {
            return null;
        }

        @Override
        public ContextBuilder getContextTracker() {
            return null;
        }

        @Override
        public SequenceBlueprint getSequence() {
            return new SequenceBuilder()

                    .context(this.getDetection().getContextProvider(), this.getContextTracker())

                    .action(MoveEntityEvent.class)

                    .condition((user, event, contextContainer, sequenceReport, lastAction) -> {
                        if (user.getPlayer().isPresent()) {
                            this.previousLocation = user.getPlayer().get().getLocation();
                            return new ConditionResult(true, sequenceReport);
                        }
                        return new ConditionResult(false, sequenceReport);
                    })

                    .build(this);
        }

        @Override
        public Check createInstance(User user) {
            return null;
        }
    }
}
