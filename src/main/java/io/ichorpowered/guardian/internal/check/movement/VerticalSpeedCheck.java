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
package io.ichorpowered.guardian.internal.check.movement;

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
import io.ichorpowered.guardian.internal.capture.PlayerControlCapture;
import io.ichorpowered.guardian.internal.capture.PlayerEffectCapture;
import io.ichorpowered.guardian.sequence.GuardianSequence;
import io.ichorpowered.guardian.sequence.GuardianSequenceBuilder;
import io.ichorpowered.guardian.sequence.SequenceReport;
import io.ichorpowered.guardian.sequence.capture.GuardianCaptureRegistry;
import io.ichorpowered.guardian.sequence.context.CommonContextKeys;
import io.ichorpowered.guardian.util.MathUtil;
import org.spongepowered.api.data.key.Keys;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.event.Event;
import org.spongepowered.api.event.entity.MoveEntityEvent;
import org.spongepowered.api.world.Location;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.Collections;
import java.util.Map;
import java.util.Optional;

public class VerticalSpeedCheck implements Check<GuardianPlugin, DetectionConfiguration> {

    private final CheckBlueprint<GuardianPlugin, DetectionConfiguration> checkBlueprint;
    private final Detection<GuardianPlugin, DetectionConfiguration> detection;

    private double analysisTime = 40;
    private double minimumTickRange = 30;
    private double maximumTickRange = 50;

    public VerticalSpeedCheck(CheckBlueprint<GuardianPlugin, DetectionConfiguration> checkBlueprint,
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
    public SequenceBlueprint<Event> getSequence() {
        return new GuardianSequenceBuilder<GuardianPlugin, DetectionConfiguration>()

                .capture(new PlayerControlCapture.Common<>(this.detection.getOwner(), this.detection))
                .capture(new PlayerEffectCapture<>(this.detection.getOwner(), this.detection))

                // Trigger : Move Entity Event

                .observe(MoveEntityEvent.class)

                // After : Move Entity Event

                .observe(MoveEntityEvent.class)
                .delay(Double.valueOf(this.analysisTime).intValue())
                .expire(Double.valueOf(this.maximumTickRange).intValue())

                // TODO: Permission check.

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
                    Optional<Map> controlStateTicks = captureContainer.get(PlayerControlCapture.Common.CONTROL_STATE_TICKS);
                    Optional<Double> verticalOffset = captureContainer.get(PlayerControlCapture.Common.VERTICAL_OFFSET);

                    /*
                     * Analysis
                     */

                    if (!initial.isPresent()
                            || !controlStateTicks.isPresent()
                            || !verticalOffset.isPresent()) return false;

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

                    double verticalDisplacement = MathUtil.truncateDownTo(player.getLocation().getY() - initial.get().getY(), 4);

                    double verticalPlacement = MathUtil.truncateDownTo((verticalOffset.get() * (verticalOffset.get() / 0.2))
                            / averageClockRate + 0.1, 4);

                    if (verticalDisplacement < 1 || verticalOffset.get() < 1) return false;

                    if (verticalDisplacement > verticalPlacement) {
                        // ------------------------- DEBUG -----------------------------
                        System.out.println(player.getName() + " has been caught using vertical speed hacks. (" +
                                (verticalDisplacement - verticalPlacement) + ")");
                        // -------------------------------------------------------------

                        SequenceReport report = new SequenceReport(true, Origin.source(sequenceContext.getRoot()).owner(entityEntry).build());
                        report.put("type", "Vertical Speed");

                        report.put("information", Collections.singletonList(
                                "Overshot maximum movement by " + (verticalDisplacement - verticalPlacement) + ".")
                        );

                        report.put("initial_location", initial);
                        report.put("final_location", player.getLocation());
                        report.put("severity", (verticalDisplacement - verticalPlacement) / verticalDisplacement);

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
    public boolean compare(@Nullable Check<?, ?> check) {
        assert check != null;
        return check.equals(this);
    }

    public static class Blueprint implements CheckBlueprint<GuardianPlugin, DetectionConfiguration> {

        @Override
        public Check<GuardianPlugin, DetectionConfiguration> create(Detection<GuardianPlugin, DetectionConfiguration> detection) {
            return new VerticalSpeedCheck(this, detection);
        }

        @Override
        public Class<? extends Check> getCheckClass() {
            return VerticalSpeedCheck.class;
        }

    }

}
