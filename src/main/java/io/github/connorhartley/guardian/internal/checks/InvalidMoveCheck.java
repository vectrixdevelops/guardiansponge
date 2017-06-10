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
import io.github.connorhartley.guardian.detection.Detection;
import io.github.connorhartley.guardian.detection.check.Check;
import io.github.connorhartley.guardian.internal.contexts.player.PlayerControlContext;
import io.github.connorhartley.guardian.internal.contexts.player.PlayerLocationContext;
import io.github.connorhartley.guardian.sequence.SequenceBlueprint;
import io.github.connorhartley.guardian.sequence.SequenceBuilder;
import io.github.connorhartley.guardian.sequence.SequenceResult;
import io.github.connorhartley.guardian.sequence.condition.ConditionResult;
import io.github.connorhartley.guardian.storage.StorageSupplier;
import io.github.connorhartley.guardian.storage.container.StorageKey;
import io.github.connorhartley.guardian.util.check.PermissionCheckCondition;
import org.apache.commons.lang3.StringUtils;
import org.spongepowered.api.event.entity.MoveEntityEvent;
import org.spongepowered.api.util.Tuple;
import org.spongepowered.api.world.Location;
import org.spongepowered.api.world.World;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class InvalidMoveCheck<E, F extends StorageSupplier<File>> implements Check<E, F> {

    private final Detection<E, F> detection;

    private double analysisTime = 40;
    private double minimumTickRange = 30;
    private double maximumTickRange = 50;

    public InvalidMoveCheck(Detection<E, F> detection) {
        this.detection = detection;

        if (this.detection.getConfiguration().get().get(new StorageKey<>("analysis-time"), new TypeToken<Double>(){}).isPresent()) {
            this.analysisTime = this.detection.getConfiguration().get().get(new StorageKey<>("analysis-time"),
                    new TypeToken<Double>(){}).get().getValue() / 0.05;
        }

        if (this.detection.getConfiguration().get().get(new StorageKey<>("tick-bounds"), new TypeToken<Map<String, Double>>(){}).isPresent()) {
            this.minimumTickRange = this.analysisTime * this.detection.getConfiguration().get().get(new StorageKey<>("tick-bounds"),
                    new TypeToken<Map<String, Double>>(){}).get().getValue().get("min");
            this.maximumTickRange = this.analysisTime * this.detection.getConfiguration().get().get(new StorageKey<>("tick-bounds"),
                    new TypeToken<Map<String, Double>>(){}).get().getValue().get("max");
        }
    }

    @Override
    public Detection<E, F> getDetection() {
        return this.detection;
    }

    @Override
    public SequenceBlueprint getSequence() {
        return new SequenceBuilder()

                .capture(
                        new PlayerLocationContext((Guardian) this.getDetection().getPlugin(), this.getDetection()),
                        new PlayerControlContext.InvalidControl((Guardian) this.getDetection().getPlugin(), this.getDetection())
                )

                // Trigger : Move Entity Event

                .action(MoveEntityEvent.class)

                // After Analysis Time : Move Entity Event

                .action(MoveEntityEvent.class)
                        .delay(((Double) this.analysisTime).intValue())
                        .expire(((Double) this.maximumTickRange).intValue())

                        // Does the player have permission?
                        .condition(new PermissionCheckCondition(this.detection))

                        .condition((user, event, captureContainer, sequenceReport, lastAction) -> {
                            SequenceResult.Builder report = SequenceResult.builder().of(sequenceReport);

                            Guardian plugin = (Guardian) this.getDetection().getPlugin();

                            int locationUpdate = 0;
                            Location<World> start = null;
                            Location<World> present = null;
                            Set<Tuple<String, String>> invalidMoves = null;

                            if (captureContainer.<PlayerLocationContext, Location<World>>get(PlayerLocationContext.class, "start_location").isPresent()) {
                                start = captureContainer.<PlayerLocationContext, Location<World>>get(PlayerLocationContext.class, "start_location").get();
                            }

                            if (captureContainer.<PlayerLocationContext, Location<World>>get(PlayerLocationContext.class, "present_location").isPresent()) {
                                present = captureContainer.<PlayerLocationContext, Location<World>>get(PlayerLocationContext.class, "present_location").get();
                            }

                            if (captureContainer.<PlayerLocationContext, Integer>get(PlayerLocationContext.class, "update").isPresent()) {
                                locationUpdate = captureContainer.<PlayerLocationContext, Integer>get(PlayerLocationContext.class, "update").get();
                            }

                            if (captureContainer.get(PlayerControlContext.InvalidControl.invalidMoves).isPresent()) {
                                invalidMoves = captureContainer.get(PlayerControlContext.InvalidControl.invalidMoves).get();
                            }

                            if (locationUpdate < this.minimumTickRange) {
                                plugin.getLogger().warn("The server may be overloaded. A detection check has been skipped as it is less than a second and a half behind.");
                                return new ConditionResult(false, report.build(false));
                            } else if (locationUpdate > this.maximumTickRange) {
                                return new ConditionResult(false, report.build(false));
                            }

                            if (user.getPlayer().isPresent() && invalidMoves != null && start != null && present != null) {
                                if (invalidMoves.isEmpty()) {
                                    return new ConditionResult(false, report.build(false));
                                }

                                List<String> invalidControls = new ArrayList<>();

                                for (Tuple<String, String> invalidMove : invalidMoves) {
                                    invalidControls.add(invalidMove.getFirst());
                                    invalidControls.add(invalidMove.getSecond());
                                }

                                if (!invalidControls.isEmpty()) {
                                    report
                                            .information("Recieved invalid controls of " + StringUtils.join(invalidControls, ", ") + ".")
                                            .type("using invalid controls of " + StringUtils.join(invalidControls, ", "))
                                            .initialLocation(start.copy())
                                            .severity(100);

                                    return new ConditionResult(true, report.build(true));
                                }
                            }
                            return new ConditionResult(false, sequenceReport);
                        })

                .build(this);
    }
}
