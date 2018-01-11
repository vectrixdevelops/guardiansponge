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
import com.ichorpowered.guardian.common.capture.PlayerPositionCapture;
import com.ichorpowered.guardian.common.capture.WorldMaterialCapture;
import com.ichorpowered.guardian.content.transaction.GuardianSingleValue;
import com.ichorpowered.guardian.entry.GuardianPlayerEntry;
import com.ichorpowered.guardian.sequence.GuardianSequence;
import com.ichorpowered.guardian.sequence.GuardianSequenceBuilder;
import com.ichorpowered.guardian.sequence.SequenceReport;
import com.ichorpowered.guardian.sequence.capture.GuardianCaptureRegistry;
import com.ichorpowered.guardian.sequence.context.CommonContextKeys;
import com.ichorpowered.guardian.util.ContentUtil;
import com.ichorpowered.guardian.util.WorldUtil;
import com.ichorpowered.guardian.util.entity.BoundingBox;
import com.ichorpowered.guardianapi.content.ContentKeys;
import com.ichorpowered.guardianapi.content.transaction.result.SingleValue;
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

public class FlightCheck implements Check<Event> {

    public FlightCheck() {}

    @Override
    public String getId() {
        return "guardian:flightcheck";
    }

    @Override
    public String getName() {
        return "Flight Check";
    }

    @Override
    public Set<String> getTags() {
        return Sets.newHashSet(
                "guardian",
                "internal",
                "movement",
                "movementflight",
                "flight"
        );
    }

    @Override
    public SequenceBlueprint<Event> getSequence(final Detection detection) {
        final Double analysisTime = detection.getContentContainer().get(ContentKeys.ANALYSIS_TIME).orElse(GuardianSingleValue.empty())
                .getElement().orElse(0d) / 0.05;

        final Double analysisIntercept = detection.getContentContainer().get(ContentKeys.ANALYSIS_INTERCEPT).orElse(GuardianSingleValue.empty())
                .getElement().orElse(0d);

        final Double minimumTickRate = detection.getContentContainer().get(ContentKeys.ANALYSIS_MINIMUM_TICK).orElse(GuardianSingleValue.empty())
                .getElement().orElse(0d) * analysisTime;

        final Double maximumTickRate = detection.getContentContainer().get(ContentKeys.ANALYSIS_MAXIMUM_TICK).orElse(GuardianSingleValue.empty())
                .getElement().orElse(0d) * analysisTime;

        return new GuardianSequenceBuilder()

                .capture(new PlayerControlCapture.Common(detection.getPlugin(), detection))
                .capture(new PlayerPositionCapture.RelativeAltitude(detection.getPlugin(), detection))
                .capture(new WorldMaterialCapture(detection.getPlugin(), detection))
                .capture(new PlayerEffectCapture(detection.getPlugin(), detection))

                // Trigger : Move Entity Event

                .observe(MoveEntityEvent.class)

                .observe(MoveEntityEvent.class)
                    .delay(analysisTime.intValue())
                    .expire(maximumTickRate.intValue())

                    // TODO: Permission check.

                    .condition(sequenceContext -> {
                        final GuardianPlayerEntry<Player> entityEntry = sequenceContext.get(CommonContextKeys.ENTITY_ENTRY);
                        final Summary summary = sequenceContext.get(CommonContextKeys.SUMMARY);
                        final GuardianCaptureRegistry captureRegistry = sequenceContext.get(CommonContextKeys.CAPTURE_REGISTRY);
                        final long lastActionTime = sequenceContext.get(CommonContextKeys.LAST_ACTION_TIME);

                        summary.set(SequenceReport.class, new SequenceReport(false, Origin.source(sequenceContext.getRoot()).owner(entityEntry).build()));

                        if (!entityEntry.getEntity(Player.class).isPresent()) return false;
                        Player player = entityEntry.getEntity(Player.class).get();

                        final SingleValue<Double> playerBoxWidth = ContentUtil.getFirst(ContentKeys.BOX_PLAYER_WIDTH, entityEntry, detection.getContentContainer()).orElse(GuardianSingleValue.empty());
                        final SingleValue<Double> playerBoxHeight = ContentUtil.getFirst(ContentKeys.BOX_PLAYER_HEIGHT, entityEntry, detection.getContentContainer()).orElse(GuardianSingleValue.empty());
                        final SingleValue<Double> playerBoxSafety = ContentUtil.getFirst(ContentKeys.BOX_PLAYER_SAFETY, entityEntry, detection.getContentContainer()).orElse(GuardianSingleValue.empty());

                        final double playerWidth = playerBoxWidth.getElement().orElse(1.0) + playerBoxSafety.getElement().orElse(0.08);
                        final double playerHeight = playerBoxHeight.getElement().orElse(1.75) + playerBoxSafety.getElement().orElse(0.08);

                        final boolean isSneaking = player.get(Keys.IS_SNEAKING).isPresent() && player.get(Keys.IS_SNEAKING).get();
                        final BoundingBox playerBox = WorldUtil.getBoundingBox(playerWidth, isSneaking ? (playerHeight - 0.25) : playerHeight);

                        /*
                         * Capture Collection
                         */

                        final CaptureContainer captureContainer = captureRegistry.getContainer();

                        Optional<Location> initial = captureContainer.get(GuardianSequence.INITIAL_LOCATION);
                        Optional<Double> effectLiftAmplifier = captureContainer.get(PlayerEffectCapture.VERTICAL_SPEED_MODIFIER);
                        Optional<Double> materialSpeedAmplifier = captureContainer.get(WorldMaterialCapture.SPEED_MODIFIER);
                        Optional<Double> altitude = captureContainer.get(PlayerPositionCapture.RelativeAltitude.RELATIVE_ALTITUDE);
                        Optional<Map> materialStateTicks = captureContainer.get(WorldMaterialCapture.ACTIVE_MATERIAL_TICKS);
                        Optional<Map> controlStateTicks = captureContainer.get(PlayerControlCapture.Common.ACTIVE_CONTROL_TICKS);

                        /*
                         * Analysis
                         */

                        if (!initial.isPresent()
                                || !materialSpeedAmplifier.isPresent()
                                || !altitude.isPresent()
                                || !materialStateTicks.isPresent()) return false;

                        long current = System.currentTimeMillis();

                        // Finds the average between now and the last action.
                        double averageClockRate = ((current - lastActionTime) / 1000) / 0.05;

                        if (averageClockRate < minimumTickRate) {
                            detection.getLogger().warn("The server may be overloaded. A check could not be completed.");
                            return false;
                        } else if (averageClockRate > maximumTickRate) {
                            return false;
                        }

                        if (player.get(Keys.VEHICLE).isPresent()
                                || (player.get(Keys.IS_FLYING).isPresent() && player.get(Keys.IS_FLYING).get())
                                || player.getLocation().getY() < 1) return false;

                        double intercept = analysisIntercept + (effectLiftAmplifier.orElse(0d) / analysisTime);

                        // Finds the players displacement y in the world.
                        double altitudeDisplacement = ((player.getLocation().getY() - initial.get().getY()) == 0) ? intercept
                                : player.getLocation().getY() - initial.get().getY();

                        // Finds the players relative altitude to the ground.
                        double altitudePlacement = altitude.get() / averageClockRate;

                        // Finds the time the player is on solid ground or a liquid.
                        int groundTime = ((Map<String, Integer>) materialStateTicks.get())
                                .get(WorldMaterialCapture.SOLID);

                        int liquidTime = ((Map<String, Integer>) materialStateTicks.get())
                                .get(WorldMaterialCapture.LIQUID);

                        // Finds the players fly time time in ticks.
                        double flyTime = ((Map<String, Integer>) controlStateTicks.get())
                                .get(PlayerControlCapture.FLY).doubleValue() * 0.05;

                        if (altitudeDisplacement <= 1 || altitudePlacement <= 1
                                || !WorldUtil.isEmptyUnder(player, playerBox, playerHeight)
                                || groundTime > 1
                                || liquidTime > 1) return false;

                        if (((altitudeDisplacement / altitudePlacement) + altitudePlacement)
                                > (intercept * (analysisTime * 0.05))
                                && flyTime == 0d) {
                            // ------------------------- DEBUG -----------------------------
                            System.out.println(player.getName() + " has been caught using fly hacks. (" +
                                    ((altitudeDisplacement / altitudePlacement) + altitudePlacement) + ")");
                            // -------------------------------------------------------------

                            SequenceReport report = new SequenceReport(true, Origin.source(sequenceContext.getRoot()).owner(entityEntry).build());
                            report.put("type", "Flight");

                            report.put("information", Collections.singletonList(
                                    "Gained altitude over " + ((altitudeDisplacement / altitudePlacement) + altitudePlacement) + ".")
                            );

                            report.put("initial_location", initial.get());
                            report.put("final_location", player.getLocation());
                            report.put("severity", ((altitudeDisplacement / altitudePlacement) + altitudePlacement)
                                    / (altitudeDisplacement + altitudePlacement));

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

    @Override
    public Class<? extends Event> getSequenceTrigger() {
        return MoveEntityEvent.class;
    }
}
