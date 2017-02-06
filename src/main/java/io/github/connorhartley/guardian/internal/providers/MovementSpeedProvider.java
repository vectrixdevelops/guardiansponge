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
package io.github.connorhartley.guardian.internal.providers;

import io.github.connorhartley.guardian.detection.check.Check;
import io.github.connorhartley.guardian.detection.check.CheckController;
import io.github.connorhartley.guardian.detection.check.CheckProvider;
import io.github.connorhartley.guardian.sequence.Sequence;
import io.github.connorhartley.guardian.sequence.SequenceBlueprint;
import io.github.connorhartley.guardian.sequence.SequenceBuilder;
import io.github.connorhartley.guardian.sequence.report.SequencePoint;
import org.spongepowered.api.entity.living.player.User;
import org.spongepowered.api.event.entity.MoveEntityEvent;

public class MovementSpeedProvider implements CheckProvider {

    @Override
    public SequenceBlueprint getSequence() {
        return new SequenceBuilder()
                .action(MoveEntityEvent.class)
                    .delay(20 * 2)
                    .condition((user, event, sequenceResult) -> {
                        if (user.hasPermission("guardian.detection.movementspeed")) {
                            if (user.hasPermission("guardian.detection.movementspeed.exempt")) {
                                return new SequencePoint.Builder().setPass(false).build();
                            }
                            return new SequencePoint.Builder().setPass(true).build();
                        }
                        return new SequencePoint.Builder().setPass(false).build();
                    })
                .action(MoveEntityEvent.class)
                    .condition((user, event, sequenceResult) -> {

                        // TODO: Juice'y movement speed check here.

                        return new SequencePoint.Builder().build();
                    })
                .build(this);
    }

    @Override
    public Check createInstance(CheckController checkController, Sequence sequence, User user) {
        return null;
    }

}
