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
package io.ichorpowered.guardian.common.check.movement;

import com.abilityapi.sequenceapi.SequenceBlueprint;
import com.abilityapi.sequenceapi.SequenceContext;
import com.abilityapi.sequenceapi.action.condition.ConditionType;
import com.ichorpowered.guardian.api.detection.Detection;
import com.ichorpowered.guardian.api.detection.DetectionConfiguration;
import com.ichorpowered.guardian.api.detection.check.Check;
import com.ichorpowered.guardian.api.detection.check.CheckBlueprint;
import com.ichorpowered.guardian.api.event.origin.Origin;
import com.ichorpowered.guardian.api.report.Summary;
import com.ichorpowered.guardian.api.sequence.capture.CaptureContainer;
import io.ichorpowered.guardian.GuardianPlugin;
import io.ichorpowered.guardian.entry.GuardianEntityEntry;
import io.ichorpowered.guardian.common.capture.PlayerControlCapture;
import io.ichorpowered.guardian.common.capture.PlayerEffectCapture;
import io.ichorpowered.guardian.common.capture.WorldMaterialCapture;
import io.ichorpowered.guardian.sequence.GuardianSequence;
import io.ichorpowered.guardian.sequence.GuardianSequenceBuilder;
import io.ichorpowered.guardian.sequence.SequenceReport;
import io.ichorpowered.guardian.sequence.capture.GuardianCaptureRegistry;
import io.ichorpowered.guardian.sequence.context.CommonContextKeys;
import io.ichorpowered.guardian.util.MathUtil;
import org.spongepowered.api.data.key.Keys;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.event.entity.MoveEntityEvent;
import org.spongepowered.api.world.Location;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.Collections;
import java.util.Map;
import java.util.Optional;

public class HorizontalSpeedCheck implements Check<GuardianPlugin, DetectionConfiguration> {

    private final CheckBlueprint<GuardianPlugin, DetectionConfiguration> checkBlueprint;
    private final Detection<GuardianPlugin, DetectionConfiguration> detection;

    private double analysisTime = 40;
    private double analysisIntercept = 9.84;
    private double minimumTickRange = 30;
    private double maximumTickRange = 60;

    public HorizontalSpeedCheck(CheckBlueprint<GuardianPlugin, DetectionConfiguration> checkBlueprint,
                              Detection<GuardianPlugin, DetectionConfiguration> detection) {
        this.checkBlueprint = checkBlueprint;
        this.detection = detection;

        this.analysisTime = this.detection.getConfiguration().getStorage().getNode("analysis", "time").getDouble(2d) / 0.05;
        this.minimumTickRange = this.analysisTime * this.detection.getConfiguration().getStorage().getNode("analysis", "range", "minimum").getDouble(0.75);
        this.maximumTickRange = this.analysisTime * this.detection.getConfiguration().getStorage().getNode("analysis", "range", "maximum").getDouble(1.25);
    }

    @Nonnull
    @Override
    public GuardianPlugin getOwner() {
        return this.detection.getOwner();
    }

    @Nonnull
    @Override
    public Detection<GuardianPlugin, DetectionConfiguration> getDetection() {
        return this.detection;
    }

    @Nonnull
    @Override
    public CheckBlueprint<GuardianPlugin, DetectionConfiguration> getCheckBlueprint() {
        return this.checkBlueprint;
    }

    @Nonnull
    @Override
    public SequenceBlueprint getSequence() {
        return new GuardianSequenceBuilder<GuardianPlugin, DetectionConfiguration>()

                .capture(new PlayerControlCapture.Common<>(this.detection.getOwner(), this.detection))
                .capture(new WorldMaterialCapture<>(this.detection.getOwner(), this.detection))
                .capture(new PlayerEffectCapture<>(this.detection.getOwner(), this.detection))

                // Trigger : Move Entity Event

                .observe(MoveEntityEvent.class)

                .observe(MoveEntityEvent.class)
                    .delay(Double.valueOf(this.analysisTime).intValue())
                    .expire(Double.valueOf(this.maximumTickRange).intValue())

                    // TODO: Permission Condition

                    // Analysis Condition

                    .condition(sequenceContext -> {
                        final GuardianEntityEntry<Player> entityEntry = sequenceContext.get(CommonContextKeys.ENTITY_ENTRY);
                        final Summary<GuardianPlugin, DetectionConfiguration> summary = sequenceContext.get(CommonContextKeys.SUMMARY);
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

                        if (averageClockRate < this.minimumTickRange) {
                            this.getOwner().getLogger().warn("The server may be overloaded. A check could not be completed.");
                            return false;
                        } else if (averageClockRate > this.maximumTickRange) {
                            return false;
                        }

                        if (player.get(Keys.VEHICLE).isPresent()) return false;

                        double intercept = this.analysisIntercept + (effectSpeedAmplifier.orElse(0d) / this.analysisTime);

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
                        .owner(this.detection)
                        .root(this)
                        .build());
    }

    @Override
    public final boolean compare(@Nullable final Check<?, ?> check) {
        assert check != null;
        return check.equals(this);
    }

    public static class Blueprint implements CheckBlueprint<GuardianPlugin, DetectionConfiguration> {

        @Override
        public final Check<GuardianPlugin, DetectionConfiguration> create(@Nonnull final Detection<GuardianPlugin, DetectionConfiguration> detection) {
            return new HorizontalSpeedCheck(this, detection);
        }

        @Nonnull
        @Override
        public final Class<? extends Check> getCheckClass() {
            return HorizontalSpeedCheck.class;
        }

    }

}
