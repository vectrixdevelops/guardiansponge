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

import com.google.common.collect.Sets;
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
import io.github.connorhartley.guardian.util.check.PermissionCheckCondition;
import org.apache.commons.lang3.StringUtils;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.event.entity.MoveEntityEvent;
import org.spongepowered.api.util.Tuple;
import org.spongepowered.api.world.Location;
import org.spongepowered.api.world.World;
import tech.ferus.util.config.HoconConfigFile;

import java.io.File;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class InvalidMoveCheck<E, F extends StorageProvider<HoconConfigFile, Path>> implements Check<E, F> {

    private final Detection<E, F> detection;

    private double analysisTime = 40;
    private double minimumTickRange = 30;
    private double maximumTickRange = 50;

    public InvalidMoveCheck(Detection<E, F> detection) {
        this.detection = detection;
    }

    @Override
    public void load() {
        this.analysisTime = this.detection.getConfiguration().getStorage().getNode("analysis", "sequence-time").getDouble(2d) / 0.05;
        this.minimumTickRange = this.analysisTime * GuardianConfiguration.GLOBAL_TICK_MIN.get(((Guardian) this.detection.getPlugin()).getGlobalConfiguration().getStorage(), 0.75);
        this.maximumTickRange = this.analysisTime * GuardianConfiguration.GLOBAL_TICK_MAX.get(((Guardian) this.detection.getPlugin()).getGlobalConfiguration().getStorage(), 1.25);
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
                        new PlayerControlContext.InvalidControl<>((Guardian) this.getDetection().getPlugin(), this.getDetection())
                )

                // Trigger : Move Entity Event

                .action(MoveEntityEvent.class)

                // After Analysis Time : Move Entity Event

                .action(MoveEntityEvent.class)
                        .delay(((Double) this.analysisTime).intValue())
                        .expire(((Double) this.maximumTickRange).intValue())

                        // Does the player have permission?
                        .condition(new PermissionCheckCondition(this.getDetection()))

                        .condition((user, event, captureContainer, sequenceReport, lastAction) -> {
                            SequenceResult.Builder report = SequenceResult.builder().of(sequenceReport);

                            Guardian plugin = (Guardian) this.getDetection().getPlugin();

                            Location<World> start;
                            Set<Tuple<String, String>> invalidMoves;

                            long locationTicks;

                            if (user.getPlayer().isPresent()) {
                                Player player = user.getPlayer().get();

                                /*
                                 * Context Collection
                                 */

                                start = captureContainer.<PlayerLocationContext, Location<World>>get(PlayerLocationContext.class, "start_location")
                                        .orElse(player.getLocation());

                                locationTicks = captureContainer.<PlayerLocationContext, Integer>get(PlayerLocationContext.class, "update")
                                        .orElse(0);

                                invalidMoves = captureContainer.get(PlayerControlContext.InvalidControl.invalidMoves)
                                        .orElse(Sets.newHashSet());

                                /*
                                 * Context Analysis
                                 */

                                if (locationTicks < this.minimumTickRange) {
                                    plugin.getLogger().warn("The server may be overloaded. A detection check has been skipped as it is less than a second and a half behind.");
                                    return new ConditionResult(false, report.build(false));
                                } else if (locationTicks > this.maximumTickRange) {
                                    return new ConditionResult(false, report.build(false));
                                }

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
