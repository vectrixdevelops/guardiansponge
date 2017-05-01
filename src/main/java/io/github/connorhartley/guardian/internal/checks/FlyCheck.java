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
import io.github.connorhartley.guardian.detection.check.CheckType;
import io.github.connorhartley.guardian.internal.contexts.player.PlayerLocationContext;
import io.github.connorhartley.guardian.internal.contexts.player.PlayerPositionContext;
import io.github.connorhartley.guardian.sequence.SequenceBlueprint;
import io.github.connorhartley.guardian.sequence.SequenceBuilder;
import io.github.connorhartley.guardian.sequence.condition.ConditionResult;
import io.github.connorhartley.guardian.sequence.SequenceReport;
import io.github.connorhartley.guardian.storage.container.StorageKey;
import io.github.connorhartley.guardian.util.check.PermissionCheck;
import io.github.nucleuspowered.nucleus.api.events.NucleusTeleportEvent;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.data.key.Keys;
import org.spongepowered.api.entity.living.player.User;
import org.spongepowered.api.event.entity.DestructEntityEvent;
import org.spongepowered.api.event.entity.MoveEntityEvent;
import org.spongepowered.api.world.Location;
import org.spongepowered.api.world.World;

import java.util.Map;

public class FlyCheck extends Check {

    public FlyCheck(CheckType checkType, User user) {
        super(checkType, user);
        this.setChecking(true);
    }

    @Override
    public void update() {}

    @Override
    public void finish() {
        this.setChecking(false);
    }

    public static class Type implements CheckType {

        private final Detection detection;

        private double analysisTime = 40;
        private double altitudeMaximum = 3.1;
        private double minimumTickRange = 30;
        private double maximumTickRange = 50;


        public Type(Detection detection) {
            this.detection = detection;

            if (this.detection.getConfiguration().get(new StorageKey<>("analysis-time"), new TypeToken<Double>(){}).isPresent()) {
                this.analysisTime = this.detection.getConfiguration().get(new StorageKey<>("analysis-time"),
                        new TypeToken<Double>(){}).get().getValue() / 0.05;
            }

            if (this.detection.getConfiguration().get(new StorageKey<>("tick-bounds"), new TypeToken<Map<String, Double>>(){}).isPresent()) {
                this.minimumTickRange = this.analysisTime * this.detection.getConfiguration().get(new StorageKey<>("tick-bounds"),
                        new TypeToken<Map<String, Double>>(){}).get().getValue().get("min");
                this.maximumTickRange = this.analysisTime * this.detection.getConfiguration().get(new StorageKey<>("tick-bounds"),
                        new TypeToken<Map<String, Double>>(){}).get().getValue().get("max");
            }

            if (this.detection.getConfiguration().get(new StorageKey<>("altitude-maximum"), new TypeToken<Double>(){}).isPresent()) {
                this.altitudeMaximum = this.detection.getConfiguration().get(new StorageKey<>("altitude-maximum"),
                        new TypeToken<Double>(){}).get().getValue();
            }
        }

        @Override
        public Detection getDetection() {
            return this.detection;
        }

        @Override
        public SequenceBlueprint getSequence() {
            return new SequenceBuilder()

                    .capture(
                            new PlayerLocationContext((Guardian) this.getDetection().getPlugin(), this.getDetection()),
                            new PlayerPositionContext.Altitude((Guardian) this.getDetection().getPlugin(), this.getDetection())
                    )

                    // Trigger : Move Entity Event

                    .action(MoveEntityEvent.class)

                    // After 2 Seconds : Move Entity Event

                    .action(MoveEntityEvent.class)
                            .delay(((Double) this.analysisTime).intValue())
                            .expire(((Double) this.maximumTickRange).intValue())

                            // Ensures the sequence does not continue if the player dies.

                            .failure((user, event, contextContainer, sequenceReport, lastAction) -> {
                                SequenceReport report = SequenceReport.builder().of(sequenceReport)
                                        .build(false);

                                if (event instanceof DestructEntityEvent.Death) {
                                    return new ConditionResult(true, report);
                                }

                                return new ConditionResult(false, report);
                            })

                            // Ensures the sequence does not continue if the player teleports through Nucleus.

                            .failure((user, event, captureContainer, sequenceReport, lastAction) -> {
                                SequenceReport report = SequenceReport.builder().of(sequenceReport)
                                        .build(false);

                                if (Sponge.getPluginManager().getPlugin("nucleus").isPresent()) {
                                    if (event instanceof NucleusTeleportEvent.AboutToTeleport) {
                                        return new ConditionResult(true, report);
                                    }
                                }

                                return new ConditionResult(false, report);
                            })

                            // Ensures the sequence does not continue if the player teleports.

                            .condition((user, event, contextContainer, sequenceReport, lastAction) -> {
                                SequenceReport report = SequenceReport.builder().of(sequenceReport)
                                        .build(false);

                                if (event instanceof MoveEntityEvent.Teleport) {
                                    return new ConditionResult(false, report);
                                }

                                return new ConditionResult(true, report);
                            })

                            // Does the player have permission?

                            .condition(new PermissionCheck(this.detection))

                            // Logic checks.

                            .condition((user, event, contextValuation, sequenceReport, lastAction) -> {
                                SequenceReport.Builder report = SequenceReport.builder().of(sequenceReport);

                                Guardian plugin = (Guardian) this.detection.getPlugin();

                                Location<World> start = null;
                                Location<World> present = null;

                                long currentTime;
                                long playerAltitudeGainTicks = 0;
                                double playerAltitudeGain = 0;

                                if (contextValuation.<PlayerLocationContext, Location<World>>get(PlayerLocationContext.class, "start_location").isPresent()) {
                                    start = contextValuation.<PlayerLocationContext, Location<World>>get(PlayerLocationContext.class, "start_location").get();
                                }

                                if (contextValuation.<PlayerLocationContext, Location<World>>get(PlayerLocationContext.class, "present_location").isPresent()) {
                                    present = contextValuation.<PlayerLocationContext, Location<World>>get(PlayerLocationContext.class, "present_location").get();
                                }

                                if (contextValuation.<PlayerPositionContext.Altitude, Integer>get(PlayerPositionContext.Altitude.class, "update").isPresent()) {
                                    playerAltitudeGainTicks = contextValuation.<PlayerPositionContext.Altitude, Integer>get(PlayerPositionContext.Altitude.class, "update").get();
                                }

                                if (contextValuation.<PlayerPositionContext.Altitude, Double>get(PlayerPositionContext.Altitude.class, "position_altitude").isPresent()) {
                                    playerAltitudeGain = contextValuation.<PlayerPositionContext.Altitude, Double>get(PlayerPositionContext.Altitude.class, "position_altitude").get();
                                }

                                if (playerAltitudeGainTicks < this.minimumTickRange) {
                                    plugin.getLogger().warn("The server may be overloaded. A detection check has been skipped as it is less than a second and a half behind.");
                                    return new ConditionResult(false, report.build(false));
                                } else if (playerAltitudeGainTicks > this.maximumTickRange) {
                                    return new ConditionResult(false, report.build(false));
                                }

                                if (user.getPlayer().isPresent() && start != null && present != null) {

                                    currentTime = System.currentTimeMillis();

                                    if (user.getPlayer().get().get(Keys.VEHICLE).isPresent()) {
                                        return new ConditionResult(false, report.build(false));
                                    }

                                    if (user.getPlayer().get().get(Keys.CAN_FLY).isPresent()) {
                                        if (user.getPlayer().get().get(Keys.CAN_FLY).get()) {
                                            return new ConditionResult(false, report.build(false));
                                        }
                                    }

                                    if (user.getPlayer().get().getLocation().getY() < -1.25 || !user.getPlayer().get().isLoaded()) {
                                        return new ConditionResult(false, report.build(false));
                                    }

                                    double altitudeDisplacement = Math.abs(present.getY() - start.getY());
                                    double travelDisplacement = Math.abs(Math.sqrt((
                                            (present.getX() - start.getX()) *
                                                    (present.getX() - start.getX())) +
                                            (present.getZ() - start.getZ()) *
                                                    (present.getZ() - start.getZ())));


                                    double meanAltitude = playerAltitudeGain / ((
                                            ((playerAltitudeGainTicks + this.analysisTime) / 2) +
                                                    ((currentTime - lastAction) / 1000)) / 2);

                                    if (altitudeDisplacement < 1 || meanAltitude < 1 ||
                                            altitudeDisplacement + this.altitudeMaximum > travelDisplacement) {
                                        return new ConditionResult(false, report.build(false));
                                    }

                                    if (((altitudeDisplacement / meanAltitude) + meanAltitude) > (this.altitudeMaximum *
                                            (this.analysisTime * 0.05))) {
                                        report
                                                .information("Result of altitude gain was " + ((altitudeDisplacement / meanAltitude) + meanAltitude) + ".")
                                                .type("flying")
                                                .severity(((altitudeDisplacement / meanAltitude) + meanAltitude));

                                        // TODO : Remove this after testing \/
                                        plugin.getLogger().warn(user.getName() + " has triggered the flight check and overshot " +
                                                "the maximum altitude gain by " + ((altitudeDisplacement / meanAltitude) + meanAltitude) + ".");

                                        return new ConditionResult(true, report.build(true));
                                    }
                                }
                                return new ConditionResult(false, sequenceReport);
                            })

                    .build(this);
        }

        @Override
        public Check createInstance(User user) {
            return new FlyCheck(this, user);
        }
    }
}
