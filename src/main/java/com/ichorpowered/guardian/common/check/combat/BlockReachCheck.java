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
package com.ichorpowered.guardian.common.check.combat;

import com.abilityapi.sequenceapi.SequenceBlueprint;
import com.abilityapi.sequenceapi.SequenceContext;
import com.abilityapi.sequenceapi.action.condition.ConditionType;
import com.flowpowered.math.vector.Vector2i;
import com.flowpowered.math.vector.Vector3d;
import com.flowpowered.math.vector.Vector3i;
import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import com.ichorpowered.guardian.entry.GuardianPlayerEntry;
import com.ichorpowered.guardian.sequence.GuardianSequenceBuilder;
import com.ichorpowered.guardian.sequence.SequenceReport;
import com.ichorpowered.guardian.sequence.capture.GuardianCaptureRegistry;
import com.ichorpowered.guardian.sequence.context.CommonContextKeys;
import com.ichorpowered.guardian.util.ContentUtil;
import com.ichorpowered.guardian.util.item.mutable.GuardianValue;
import com.ichorpowered.guardian.util.world.RayUtil;
import com.ichorpowered.guardianapi.content.ContentKeys;
import com.ichorpowered.guardianapi.detection.Detection;
import com.ichorpowered.guardianapi.detection.check.Check;
import com.ichorpowered.guardianapi.detection.report.Summary;
import com.ichorpowered.guardianapi.event.origin.Origin;
import com.ichorpowered.guardianapi.util.item.value.mutable.Value;
import org.spongepowered.api.data.key.Keys;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.event.Event;
import org.spongepowered.api.event.block.InteractBlockEvent;
import org.spongepowered.api.util.blockray.BlockRayHit;
import org.spongepowered.api.world.Location;
import org.spongepowered.api.world.World;

import java.util.Collections;
import java.util.Optional;
import java.util.Set;

public class BlockReachCheck implements Check<Event> {

    public BlockReachCheck() {}

    @Override
    public String getId() {
        return "guardian:blockreachcheck";
    }

    @Override
    public String getName() {
        return "Block Reach Check";
    }

    @Override
    public Set<String> getTags() {
        return Sets.newHashSet(
                "guardian",
                "internal",
                "combat",
                "reachcombat",
                "blockreach"
        );
    }

    @Override
    public SequenceBlueprint<Event> getSequence(final Detection detection) {
        final Double analysisIntercept = detection.getContentContainer().get(ContentKeys.ANALYSIS_INTERCEPT).orElse(GuardianValue.empty())
                .getDirect().orElse(0d);

        return new GuardianSequenceBuilder()

                .observe(InteractBlockEvent.class)

                .after()
                    .delay(1)

                    // TODO: Permission check.

                    .condition(sequenceContext -> {
                        final GuardianPlayerEntry<Player> entityEntry = sequenceContext.get(CommonContextKeys.ENTITY_ENTRY);
                        final Summary summary = sequenceContext.get(CommonContextKeys.SUMMARY);
                        final InteractBlockEvent event = sequenceContext.get(CommonContextKeys.TRIGGER_INSTANCE);
                        final GuardianCaptureRegistry captureRegistry = sequenceContext.get(CommonContextKeys.CAPTURE_REGISTRY);
                        final long lastActionTime = sequenceContext.get(CommonContextKeys.LAST_ACTION_TIME);

                        summary.set(SequenceReport.class, new SequenceReport(false, Origin.source(sequenceContext.getRoot()).owner(entityEntry).build()));

                        if (!entityEntry.getEntity(Player.class).isPresent()) return false;
                        final Player player = entityEntry.getEntity(Player.class).get();

                        final Value<Double> playerBoxHeight = ContentUtil.getFirst(ContentKeys.BOX_PLAYER_HEIGHT, entityEntry, detection.getContentContainer()).orElse(GuardianValue.empty());
                        final Value<Double> playerBoxSafety = ContentUtil.getFirst(ContentKeys.BOX_PLAYER_SAFETY, entityEntry, detection.getContentContainer()).orElse(GuardianValue.empty());

                        final boolean isSneaking = player.get(Keys.IS_SNEAKING).isPresent() && player.get(Keys.IS_SNEAKING).get();

                        double playerHeight = playerBoxHeight.getDirect().orElse(1.8) + playerBoxSafety.getDirect().orElse(0.05) - 0.5;
                        playerHeight = isSneaking ? (playerHeight - 0.15) : playerHeight;

                        final Vector3i to = event.getTargetBlock().getPosition();

                        final Location<World> from = player.getLocation().add(0, playerHeight, 0);
                        final Vector3d fromDirection = player.getHeadRotation();

                        final double reach = to.distanceSquared(from.getPosition().toInt());

                        final Optional<BlockRayHit> firstBlock = RayUtil.getFirstBlock(from.getExtent(), from.getPosition(),
                                fromDirection, entityType -> false, RayUtil.TRANSPARENT_FILTER,
                                Double.valueOf(reach + 1).intValue(), false);

                        final double blockReach = firstBlock.map(blockRayHit -> blockRayHit.getPosition().distanceSquared(from.getPosition())).orElse(0d);

                        final double presentReach = reach / 2;
                        final double presentBlockReach = blockReach / 2;

                        if (presentReach != 0d && presentReach > analysisIntercept || presentReach <= presentBlockReach) {
                            // ------------------------- DEBUG -----------------------------
                            System.out.println(player.getName() + " has been caught with block reach hacks. (" +
                                    presentReach + " / " + presentBlockReach + ")");
                            // -------------------------------------------------------------

                            SequenceReport report = new SequenceReport(true, Origin.source(sequenceContext.getRoot()).owner(entityEntry).build());
                            report.put("type", "Reach");

                            if (presentReach > presentBlockReach) {
                                report.put("information", Lists.newArrayList(
                                        "Interacted with a block at impossible reach " + presentReach + ".",
                                        "This location was found to be obstructed by another block or entity at distance " + presentBlockReach + ".")
                                );
                            } else {
                                report.put("information", Lists.newArrayList(
                                        "Interacted with a block at impossible reach " + presentReach + ".")
                                );
                            }

                            report.put("initial_location", from);
                            report.put("final_location", from);
                            report.put("severity", reach / 10);

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
        return InteractBlockEvent.class;
    }

}
