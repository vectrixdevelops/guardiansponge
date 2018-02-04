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
import com.ichorpowered.guardian.entry.GuardianPlayerEntry;
import com.ichorpowered.guardian.sequence.GuardianSequence;
import com.ichorpowered.guardian.sequence.GuardianSequenceBuilder;
import com.ichorpowered.guardian.sequence.SequenceReport;
import com.ichorpowered.guardian.sequence.capture.GuardianCaptureRegistry;
import com.ichorpowered.guardian.sequence.context.CommonContextKeys;
import com.ichorpowered.guardian.util.ContentUtil;
import com.ichorpowered.guardian.util.WorldUtil;
import com.ichorpowered.guardian.util.entity.BoundingBox;
import com.ichorpowered.guardian.util.item.mutable.GuardianValue;
import com.ichorpowered.guardianapi.content.ContentKeys;
import com.ichorpowered.guardianapi.detection.Detection;
import com.ichorpowered.guardianapi.detection.capture.CaptureContainer;
import com.ichorpowered.guardianapi.detection.check.Check;
import com.ichorpowered.guardianapi.detection.report.Summary;
import com.ichorpowered.guardianapi.event.origin.Origin;
import com.ichorpowered.guardianapi.util.item.value.mutable.Value;
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
        final Double analysisTime = detection.getContentContainer().get(ContentKeys.ANALYSIS_TIME).orElse(GuardianValue.empty())
                .getDirect().orElse(0d) / 0.05;

        final Double analysisIntercept = detection.getContentContainer().get(ContentKeys.ANALYSIS_INTERCEPT).orElse(GuardianValue.empty())
                .getDirect().orElse(0d);

        final Double minimumTickRate = detection.getContentContainer().get(ContentKeys.ANALYSIS_MINIMUM_TICK).orElse(GuardianValue.empty())
                .getDirect().orElse(0d) * analysisTime;

        final Double maximumTickRate = detection.getContentContainer().get(ContentKeys.ANALYSIS_MAXIMUM_TICK).orElse(GuardianValue.empty())
                .getDirect().orElse(0d) * analysisTime;

        return new GuardianSequenceBuilder()

                .capture(new PlayerControlCapture.Common(detection.getPlugin(), detection))
                .capture(new PlayerPositionCapture.RelativeAltitude(detection.getPlugin(), detection))
                .capture(new WorldMaterialCapture(detection.getPlugin(), detection))
                .capture(new PlayerEffectCapture(detection.getPlugin(), detection))

                // Observe : Move Entity Event

                .observe(MoveEntityEvent.class)

                // Pre After

                .after()
                    .delay(10)
                    .expire(20)

                    .condition(sequenceContext -> {
                        final GuardianPlayerEntry<Player> entityEntry = sequenceContext.get(CommonContextKeys.ENTITY_ENTRY);
                        final Summary summary = sequenceContext.get(CommonContextKeys.SUMMARY);

                        summary.set(SequenceReport.class, new SequenceReport(false, Origin.source(sequenceContext.getRoot()).owner(entityEntry).build()));

                        if (!entityEntry.getEntity(Player.class).isPresent()) return false;
                        final Player player = entityEntry.getEntity(Player.class).get();

                        final Value<Double> playerBoxWidth = ContentUtil.getFirst(ContentKeys.BOX_PLAYER_WIDTH, entityEntry, detection.getContentContainer()).orElse(GuardianValue.empty());
                        final Value<Double> playerBoxHeight = ContentUtil.getFirst(ContentKeys.BOX_PLAYER_HEIGHT, entityEntry, detection.getContentContainer()).orElse(GuardianValue.empty());
                        final Value<Double> playerBoxSafety = ContentUtil.getFirst(ContentKeys.BOX_PLAYER_SAFETY, entityEntry, detection.getContentContainer()).orElse(GuardianValue.empty());

                        final double playerWidth = playerBoxWidth.getDirect().orElse(1.25) + playerBoxSafety.getDirect().orElse(0.08);
                        final double playerHeight = playerBoxHeight.getDirect().orElse(1.85) + playerBoxSafety.getDirect().orElse(0.08);

                        final boolean isSneaking = player.get(Keys.IS_SNEAKING).isPresent() && player.get(Keys.IS_SNEAKING).get();
                        final BoundingBox playerBox = WorldUtil.getBoundingBox(playerWidth, isSneaking ? (playerHeight - 0.25) : playerHeight);

                        return !WorldUtil.containsBlocksUnder(player.getLocation(), playerBox, 1.25);
                    }, ConditionType.NORMAL)

                // After

                .after()
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
                        final Player player = entityEntry.getEntity(Player.class).get();

                        /*
                         * Capture Collection
                         */

                        final CaptureContainer captureContainer = captureRegistry.getContainer();

                        final Optional<Location> initial = captureContainer.get(GuardianSequence.INITIAL_LOCATION);
                        final Optional<Double> effectLiftAmplifier = captureContainer.get(PlayerEffectCapture.VERTICAL_SPEED_MODIFIER);
                        final Optional<Double> materialSpeedAmplifier = captureContainer.get(WorldMaterialCapture.SPEED_MODIFIER);
                        final Optional<Double> altitude = captureContainer.get(PlayerPositionCapture.RelativeAltitude.RELATIVE_ALTITUDE);
                        final Optional<Map<String, Integer>> materialStateTicks = captureContainer.get(WorldMaterialCapture.ACTIVE_MATERIAL_TICKS);
                        final Optional<Map<String, Integer>> controlStateTicks = captureContainer.get(PlayerControlCapture.Common.ACTIVE_CONTROL_TICKS);

                        /*
                         * Analysis
                         */

                        if (!initial.isPresent()
                                || !materialSpeedAmplifier.isPresent()
                                || !altitude.isPresent()
                                || !materialStateTicks.isPresent()) return false;

                        final Value<Double> playerBoxWidth = ContentUtil.getFirst(ContentKeys.BOX_PLAYER_WIDTH, entityEntry, detection.getContentContainer()).orElse(GuardianValue.empty());
                        final Value<Double> playerBoxHeight = ContentUtil.getFirst(ContentKeys.BOX_PLAYER_HEIGHT, entityEntry, detection.getContentContainer()).orElse(GuardianValue.empty());
                        final Value<Double> playerBoxSafety = ContentUtil.getFirst(ContentKeys.BOX_PLAYER_SAFETY, entityEntry, detection.getContentContainer()).orElse(GuardianValue.empty());

                        final double playerWidth = playerBoxWidth.getDirect().orElse(1.2) + playerBoxSafety.getDirect().orElse(0.05);
                        final double playerHeight = playerBoxHeight.getDirect().orElse(1.8) + playerBoxSafety.getDirect().orElse(0.05);

                        final boolean isSneaking = player.get(Keys.IS_SNEAKING).isPresent() && player.get(Keys.IS_SNEAKING).get();
                        final BoundingBox playerBox = WorldUtil.getBoundingBox(playerWidth, isSneaking ? (playerHeight - 0.15) : playerHeight);

                        long current = System.currentTimeMillis();

                        // Gets the average time between now and the last action.
                        double averageActionTime = ((current - lastActionTime) / 1000) / 0.05;

                        if (averageActionTime < minimumTickRate) {
                            detection.getLogger().warn("The server may be overloaded. A check could not be completed.");
                            return false;
                        } else if (averageActionTime > maximumTickRate) {
                            return false;
                        }

                        if (player.get(Keys.VEHICLE).isPresent()
                                || (player.get(Keys.IS_FLYING).isPresent() && player.get(Keys.IS_FLYING).get())
                                || player.getLocation().getY() < 1) return false;

                        final double intercept = analysisIntercept + (effectLiftAmplifier.orElse(0d) / analysisTime);

                        // Gets the players vertical displacement in the world.
                        final double verticalDisplacement = ((player.getLocation().getY() - initial.get().getY()) == 0) ? intercept
                                : player.getLocation().getY() - initial.get().getY();

                        // Gets the players relative altitude to the ground.
                        final double averageAltitude = altitude.get() / averageActionTime;

                        // Gets the time the player is on solid ground or a liquid.
                        final int solidMaterialTime = materialStateTicks.get().get(WorldMaterialCapture.SOLID);
                        final int liquidMaterialTime = materialStateTicks.get().get(WorldMaterialCapture.LIQUID);

                        // Gets the time the player is using flight.
                        final int flightControlTime = controlStateTicks.get().get(PlayerControlCapture.FLY);

                        if (verticalDisplacement <= 1
                                || averageAltitude <= 1
                                || WorldUtil.containsBlocksUnder(player.getLocation(), playerBox, 1.25)
                                || solidMaterialTime > 1
                                || liquidMaterialTime > 1
                                || flightControlTime > 1) return false;

                        if (((verticalDisplacement / averageAltitude) + averageAltitude)
                                > intercept) {
                            // ------------------------- DEBUG -----------------------------
                            System.out.println(player.getName() + " has been caught using fly hacks. (" +
                                    ((verticalDisplacement / averageAltitude) + averageAltitude) + ")");
                            // -------------------------------------------------------------

                            SequenceReport report = new SequenceReport(true, Origin.source(sequenceContext.getRoot()).owner(entityEntry).build());
                            report.put("type", "Flight");

                            report.put("information", Collections.singletonList(
                                    "Gained altitude over " + ((verticalDisplacement / averageAltitude) + averageAltitude) + ".")
                            );

                            report.put("initial_location", initial.get());
                            report.put("final_location", player.getLocation());
                            report.put("severity", ((verticalDisplacement / averageAltitude) + averageAltitude)
                                    / (verticalDisplacement + averageAltitude));

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
