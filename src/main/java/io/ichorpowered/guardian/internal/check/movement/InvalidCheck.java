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
import io.ichorpowered.guardian.sequence.GuardianSequence;
import io.ichorpowered.guardian.sequence.GuardianSequenceBuilder;
import io.ichorpowered.guardian.sequence.SequenceReport;
import io.ichorpowered.guardian.sequence.capture.GuardianCaptureRegistry;
import io.ichorpowered.guardian.sequence.context.CommonContextKeys;
import org.apache.commons.lang3.StringUtils;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.event.entity.MoveEntityEvent;
import org.spongepowered.api.world.Location;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.Collections;
import java.util.Optional;
import java.util.Set;

public class InvalidCheck implements Check<GuardianPlugin, DetectionConfiguration> {

    private final CheckBlueprint<GuardianPlugin, DetectionConfiguration> checkBlueprint;
    private final Detection<GuardianPlugin, DetectionConfiguration> detection;

    private double analysisTime = 40;
    private double minimumTickRange = 30;
    private double maximumTickRange = 50;

    public InvalidCheck(CheckBlueprint<GuardianPlugin, DetectionConfiguration> checkBlueprint,
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

                .capture(new PlayerControlCapture.Invalid<>(this.detection.getOwner(), this.detection))

                // Trigger : Move Entity Event

                .observe(MoveEntityEvent.class)

                // Analysis : Move Entity Event

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
                        Optional<Set> invalidControls = captureContainer.get(PlayerControlCapture.Invalid.INVALID_MOVEMENT);

                        /*
                         * Analysis
                         */

                        if (!initial.isPresent() || !invalidControls.isPresent()) return false;

                        long current = System.currentTimeMillis();

                        // Finds the average between now and the last action.
                        double averageClockRate = ((current - lastActionTime) / 1000) / 0.05;

                        if (averageClockRate < this.minimumTickRange) {
                            this.getOwner().getLogger().warn("The server may be overloaded. A check could not be completed.");
                            return false;
                        } else if (averageClockRate > this.maximumTickRange) {
                            return false;
                        }

                        if (invalidControls.get().isEmpty()) return false;

                        // ------------------------- DEBUG -----------------------------
                        System.out.println(player.getName() + " has been caught using invalid movement hacks.");
                        // -------------------------------------------------------------

                        SequenceReport report = new SequenceReport(true, Origin.source(sequenceContext.getRoot()).owner(entityEntry).build());
                        report.put("type", "Invalid Movement");

                        report.put("information", Collections.singletonList(
                                "Received invalid controls of " + StringUtils.join((Set<String>) invalidControls.get(), ", ") + ".")
                        );

                        report.put("initial_location", initial);
                        report.put("final_location", player.getLocation());
                        report.put("severity", 1d);

                        summary.set(SequenceReport.class, report);

                        return true;
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
            return new InvalidCheck(this, detection);
        }

        @Override
        public Class<? extends Check> getCheckClass() {
            return InvalidCheck.class;
        }

    }

}
