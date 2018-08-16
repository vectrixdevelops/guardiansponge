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
package com.ichorpowered.guardian.sponge.common.check;

import com.google.common.collect.Lists;
import com.google.common.reflect.TypeToken;
import com.ichorpowered.guardian.api.Guardian;
import com.ichorpowered.guardian.api.detection.Detection;
import com.ichorpowered.guardian.api.detection.stage.Stage;
import com.ichorpowered.guardian.api.detection.stage.process.Check;
import com.ichorpowered.guardian.api.game.GameReference;
import com.ichorpowered.guardian.api.game.model.Model;
import com.ichorpowered.guardian.api.game.model.value.key.GameKeys;
import com.ichorpowered.guardian.api.sequence.SequenceBlueprint;
import com.ichorpowered.guardian.api.sequence.capture.CaptureKeys;
import com.ichorpowered.guardian.common.detection.stage.type.CheckStageImpl;
import com.ichorpowered.guardian.common.util.ConsoleUtil;
import com.ichorpowered.guardian.sponge.GuardianPlugin;
import com.ichorpowered.guardian.sponge.common.capture.ControlCapture;
import com.ichorpowered.guardian.sponge.common.capture.EffectCapture;
import com.ichorpowered.guardian.sponge.common.capture.MaterialCapture;
import com.ichorpowered.guardian.sponge.common.capture.TickCapture;
import com.ichorpowered.guardian.sponge.sequence.SequenceBuilderImpl;
import com.ichorpowered.guardian.sponge.sequence.SequenceContextImpl;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.fusesource.jansi.Ansi;
import org.spongepowered.api.data.key.Keys;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.event.Event;
import org.spongepowered.api.event.entity.MoveEntityEvent;
import org.spongepowered.api.world.Location;
import org.spongepowered.api.world.World;

import java.util.Comparator;
import java.util.List;
import java.util.Map;

public class HorizontalCheck implements Check<Event> {

    @Override
    public @NonNull SequenceBlueprint<MoveEntityEvent> getSequence(final @NonNull Detection detection) {
        final int collectionTime = (detection.getConfiguration().getNode("options", "collection-time").getInt() * 20) * 2;
        final double minimumCollectionTime = (detection.getConfiguration().getNode("options", "minimum-collection").getDouble() * collectionTime);

        return new SequenceBuilderImpl<MoveEntityEvent>()
                .captures(
                        CaptureKeys.MOVEMENT_CONTROL_CAPTURE,
                        CaptureKeys.MOVEMENT_EFFECT_CAPTURE,
                        CaptureKeys.MOVEMENT_MATERIAL_CAPTURE,
                        CaptureKeys.CLIENT_PING_CAPTURE,
                        CaptureKeys.CLIENT_TICK_CAPTURE
                )

                // Observe : Player Move Event
                .observe(MoveEntityEvent.class)

                // After
                .after()
                    .delay(collectionTime)

                    .condition(process -> {
                        final GameReference<Player> gameReference = process.getContext().get("root:player", new TypeToken<GameReference<Player>>() {});
                        final Location<World> location = process.getContext().get("root:player_location", TypeToken.of(Location.class));
                        final long stepTime = process.getContext().get("root:step_time", TypeToken.of(Long.class));

                        final Model playerModel = Guardian.getModelRegistry().get(gameReference).orElse(null);

                        if (gameReference == null || playerModel == null) return process.end();

                        final Player player = gameReference.get();
                        final double averagePing = playerModel.requestFirst(GameKeys.AVERAGE_PING).map(value -> value.get()).orElse(0d);

                        if (player.hasPermission("guardian.admin.detection.bypass") || player.hasPermission("guardian.admin.detection." + detection.getId() + ".bypass")) return process.end();

                        // Capture Context

                        final int ticks = process.getContext().get(TickCapture.TICKS, TypeToken.of(Integer.class));
                        final double controlHorizontalDistance = process.getContext().get(ControlCapture.HORIZONTAL_DISTANCE, TypeToken.of(Double.class));
                        final double effectHorizontalDistance = process.getContext().get(EffectCapture.HORIZONTAL_DISTANCE, TypeToken.of(Double.class));
                        final double materialHorizontalDistance = process.getContext().get(MaterialCapture.HORIZONTAL_DISTANCE, TypeToken.of(Double.class));
                        final Map<String, Integer> matterTime = process.getContext().get(MaterialCapture.MATTER_TIME, new TypeToken<Map<String, Integer>>() {});
                        final Map<String, Integer> controlTime = process.getContext().get(ControlCapture.CONTROL_TIME, new TypeToken<Map<String, Integer>>() {});

                        // Analysis

                        final long timePassed = ((System.currentTimeMillis() - stepTime) / 1000) * 20;
                        if (timePassed < minimumCollectionTime) {
                            ((GuardianPlugin) detection.getPlugin()).getLogger().warn("The server may be overloaded. A check could not be completed.");
                            return process.end();
                        }

                        if (player.get(Keys.VEHICLE).isPresent() || player.get(Keys.IS_ELYTRA_FLYING).orElse(false)) return process.end();

                        // Find average tick time for captured ticks.
                        int averageTicks = 1;
                        if (collectionTime >= ticks) {
                            averageTicks = (collectionTime / ticks) / 2;
                        }

                        // Gets the players possible horizontal displacement in the world.
                        double possibility = (controlHorizontalDistance
                                + effectHorizontalDistance
                                + materialHorizontalDistance)
                                / 3;

                        final double horizontalDisplacement = Math.abs((player.getLocation().getX() -
                                location.getX()) + (player.getLocation().getZ() - location.getZ()));

                        if (averageTicks > 1) possibility *= averageTicks;
                        if (averagePing > 1) possibility *= averagePing;

                        // Gets the most frequent matter type the player was on.
                        final String matterType = matterTime.entrySet().stream().max(Comparator.comparingInt(Map.Entry::getValue)).map(Map.Entry::getKey).orElse("Unknown");

                        // Gets the most frequent control type the player was using.
                        final String controlType = controlTime.entrySet().stream().max(Comparator.comparingInt(Map.Entry::getValue)).map(Map.Entry::getKey).orElse("Unknown");

                        if (horizontalDisplacement <= 1
                                || possibility <= 1) return process.end();

                        if (horizontalDisplacement > possibility) {
                            ((GuardianPlugin) detection.getPlugin()).getLogger().info(ConsoleUtil.of(Ansi.Color.RED, false, "{} has been caught " +
                                    "using a horizontal speed cheat. ({})", player.getName(), String.valueOf(horizontalDisplacement - possibility)
                            ));

                            process.getContext().add("detection", TypeToken.of(Detection.class), detection);
                            process.getContext().add("detection_type", TypeToken.of(String.class), "Horizontal Speed");
                            process.getContext().add("detection_type_id", TypeToken.of(Integer.class), 1);

                            process.getContext().add("detection_information", new TypeToken<List<String>>() {}, Lists.newArrayList(
                                    "Gained horizontal speed by " + (horizontalDisplacement - possibility) + ".",
                                    "Matter type traveling on was " + matterType + ".",
                                    "Control type used was " + controlType + "."
                            ));

                            process.getContext().add("detection_severity", TypeToken.of(Double.class), (horizontalDisplacement - possibility) / horizontalDisplacement);

                            return process.next();
                        }

                        return process.end();
                    })

                .build(new SequenceContextImpl()
                        .add("root:owner", TypeToken.of(Detection.class), detection)
                        .add("root:event_type", new TypeToken<Class<? extends Event>>() {}, this.getEventType())
                );
    }

    @Override
    public @NonNull Class<? extends Event> getEventType() {
        return MoveEntityEvent.class;
    }

    @Override
    public @NonNull Class<? extends Stage<?>> getStageType() {
        return CheckStageImpl.class;
    }

}
