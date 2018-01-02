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
package com.ichorpowered.guardian.common.check.movement;

import com.abilityapi.sequenceapi.SequenceBlueprint;
import com.abilityapi.sequenceapi.SequenceContext;
import com.abilityapi.sequenceapi.action.condition.ConditionType;
import com.google.common.collect.Sets;
import com.ichorpowered.guardian.common.capture.PlayerControlCapture;
import com.ichorpowered.guardian.common.capture.PlayerEffectCapture;
import com.ichorpowered.guardian.common.capture.WorldMaterialCapture;
import com.ichorpowered.guardian.content.transaction.GuardianSingleValue;
import com.ichorpowered.guardian.entry.GuardianPlayerEntry;
import com.ichorpowered.guardian.sequence.GuardianSequence;
import com.ichorpowered.guardian.sequence.GuardianSequenceBuilder;
import com.ichorpowered.guardian.sequence.SequenceReport;
import com.ichorpowered.guardian.sequence.capture.GuardianCaptureRegistry;
import com.ichorpowered.guardian.sequence.context.CommonContextKeys;
import com.ichorpowered.guardian.util.MathUtil;
import com.ichorpowered.guardianapi.content.ContentKeys;
import com.ichorpowered.guardianapi.detection.Detection;
import com.ichorpowered.guardianapi.detection.capture.CaptureContainer;
import com.ichorpowered.guardianapi.detection.check.Check;
import com.ichorpowered.guardianapi.detection.report.Summary;
import com.ichorpowered.guardianapi.event.origin.Origin;
import org.spongepowered.api.data.key.Keys;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.event.Event;
import org.spongepowered.api.event.entity.MoveEntityEvent;
import org.spongepowered.api.world.Location;

import java.util.Collections;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

public class HorizontalSpeedCheck implements Check<Event> {

    public HorizontalSpeedCheck() {}

    @Override
    public String getId() {
        return "guardian:horizontalspeedcheck";
    }

    @Override
    public String getName() {
        return "Horizontal Speed Check";
    }

    @Override
    public Set<String> getTags() {
        return Sets.newHashSet(
                "guardian",
                "internal",
                "movement",
                "movementspeed",
                "horizontalspeed"
        );
    }

    @Override
    public SequenceBlueprint<Event> getSequence(final Detection detection) {
        final Double analysisTime = detection.getContentContainer().get(ContentKeys.ANALYSIS_TIME).orElse(GuardianSingleValue.empty())
                .getElement().orElse(0d);

        final Double analysisIntercept = detection.getContentContainer().get(ContentKeys.ANALYSIS_INTERCEPT).orElse(GuardianSingleValue.empty())
                .getElement().orElse(0d);

        final Double minimumTickRate = detection.getContentContainer().get(ContentKeys.ANALYSIS_MINIMUM_TICK).orElse(GuardianSingleValue.empty())
                .getElement().orElse(0d);

        final Double maximumTickRate = detection.getContentContainer().get(ContentKeys.ANALYSIS_MAXIMUM_TICK).orElse(GuardianSingleValue.empty())
                .getElement().orElse(0d);

        return new GuardianSequenceBuilder()

                .capture(new PlayerControlCapture.Common(detection.getPlugin(), detection))
                .capture(new WorldMaterialCapture(detection.getPlugin(), detection))
                .capture(new PlayerEffectCapture(detection.getPlugin(), detection))

                // Trigger : Move Entity Event

                .observe(MoveEntityEvent.class)

                .observe(MoveEntityEvent.class)
                .delay(analysisTime.intValue())
                .expire(maximumTickRate.intValue())

                // TODO: Permission Condition

                // Analysis Condition

                .condition(sequenceContext -> {
                    final GuardianPlayerEntry<Player> entityEntry = sequenceContext.get(CommonContextKeys.ENTITY_ENTRY);
                    final Summary summary = sequenceContext.get(CommonContextKeys.SUMMARY);
                    final GuardianCaptureRegistry captureRegistry = sequenceContext.get(CommonContextKeys.CAPTURE_REGISTRY);
                    final long lastActionTime = sequenceContext.get(CommonContextKeys.LAST_ACTION_TIME);

                    summary.set(SequenceReport.class, new SequenceReport(false, Origin.source(sequenceContext.getRoot()).owner(entityEntry).build()));

                    if (!entityEntry.getEntity(Player.class).isPresent()) return false;
                    Player player = entityEntry.getEntity(Player.class).get();

                    /*
                     * Capture Collection
                     */

                    final CaptureContainer captureContainer = captureRegistry.getContainer();

                    Optional<Location> initial = captureContainer.get(GuardianSequence.INITIAL_LOCATION);
                    Optional<Double> effectSpeedAmplifier = captureContainer.get(PlayerEffectCapture.SPEED_AMPLIFIER);
                    Optional<Double> materialSpeedAmplifier = captureContainer.get(WorldMaterialCapture.SPEED_AMPLIFIER);
                    Optional<Double> horizontalOffset = captureContainer.get(PlayerControlCapture.Common.HORIZONTAL_OFFSET);
                    Optional<Map> controlStateTicks = captureContainer.get(PlayerControlCapture.Common.CONTROL_STATE_TICKS);

                    /*
                     * Analysis
                     */

                    if (!initial.isPresent()
                            || !effectSpeedAmplifier.isPresent()
                            || !materialSpeedAmplifier.isPresent()
                            || !horizontalOffset.isPresent()
                            || !controlStateTicks.isPresent()) return false;

                    long current = System.currentTimeMillis();

                    // Finds the average between now and the last action.
                    double averageClockRate = ((current - lastActionTime) / 1000) / 0.05;

                    if (averageClockRate < minimumTickRate) {
                        detection.getLogger().warn("The server may be overloaded. A check could not be completed.");
                        return false;
                    } else if (averageClockRate > maximumTickRate) {
                        return false;
                    }

                    if (player.get(Keys.VEHICLE).isPresent()) return false;

                    double intercept = analysisIntercept + (effectSpeedAmplifier.orElse(0d) / analysisTime);

                    // Finds the players displacement x and z in the world.
                    double horizontalDisplacement = MathUtil.truncateDownTo(Math.abs((player.getLocation().getX() -
                            initial.get().getX()) + (player.getLocation().getZ() - initial.get().getZ())), 4);

                    // Finds the players maximum displacement for the context of their controls.
                    double horizontalPlacement = MathUtil.truncateDownTo((((horizontalOffset.get() * materialSpeedAmplifier.get()) / 2)
                            / averageClockRate) + intercept, 4);

                    if (horizontalDisplacement <= 1 || horizontalPlacement <= 1) return false;

                    if (horizontalDisplacement > horizontalPlacement) {
                        // ------------------------- DEBUG -----------------------------
                        System.out.println(player.getName() + " has been caught using horizontal speed hacks. (" +
                                (horizontalDisplacement - horizontalPlacement) + ")");
                        // -------------------------------------------------------------

                        SequenceReport report = new SequenceReport(true, Origin.source(sequenceContext.getRoot()).owner(entityEntry).build());
                        report.put("type", "Horizontal Speed");

                        report.put("information", Collections.singletonList(
                                "Overshot maximum movement by " + (horizontalDisplacement - horizontalPlacement) + ".")
                        );

                        report.put("initial_location", initial);
                        report.put("final_location", player.getLocation());
                        report.put("severity", (horizontalDisplacement - horizontalPlacement) / horizontalDisplacement);

                        summary.set(SequenceReport.class, report);

                        return true;
                    }

                    return false;
                }, ConditionType.NORMAL)

                .build(SequenceContext.builder()
                        .owner(detection)
                        .root(this)
                        .build());
    }
}
