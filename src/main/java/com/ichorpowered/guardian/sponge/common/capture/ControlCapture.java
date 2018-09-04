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
package com.ichorpowered.guardian.sponge.common.capture;

import static com.ichorpowered.guardian.api.sequence.capture.CaptureValue.asKey;

import com.google.common.collect.Maps;
import com.google.common.reflect.TypeToken;
import com.ichorpowered.guardian.api.Guardian;
import com.ichorpowered.guardian.api.game.GameReference;
import com.ichorpowered.guardian.api.game.model.Model;
import com.ichorpowered.guardian.api.game.model.value.key.GameKeys;
import com.ichorpowered.guardian.api.sequence.capture.CaptureValue;
import com.ichorpowered.guardian.api.sequence.process.Process;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.spongepowered.api.data.key.Keys;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.entity.living.player.gamemode.GameModes;
import org.spongepowered.api.world.Location;
import org.spongepowered.api.world.World;

import java.util.Map;

public class ControlCapture implements CaptureValue {

    public static String CONTROL_TIME = asKey(ControlCapture.class, "control_time");
    public static String VERTICAL_DISTANCE = asKey(ControlCapture.class, "vertical_distance");
    public static String HORIZONTAL_DISTANCE = asKey(ControlCapture.class, "horizontal_distance");

    @Override
    public void apply(final @NonNull Process process) {
        final GameReference<Player> gameReference = process.getContext().get("root:player", new TypeToken<GameReference<Player>>() {});
        final Model playerModel = Guardian.getModelRegistry().get(gameReference).orElse(null);
        if (gameReference == null || playerModel == null) return;

        final Player player = gameReference.get();

        // Model Values

        final double lift = playerModel.requestFirst(GameKeys.LIFT_SPEED).map(value -> value.get()).orElse(2.012);
        final double sneak = playerModel.requestFirst(GameKeys.SNEAK_SPEED).map(value -> value.get()).orElse(1.086);
        final double walk = playerModel.requestFirst(GameKeys.WALK_SPEED).map(value -> value.get()).orElse(1.218);
        final double sprint = playerModel.requestFirst(GameKeys.SPRINT_SPEED).map(value -> value.get()).orElse(1.236);
        final double flight = playerModel.requestFirst(GameKeys.FLIGHT_SPEED).map(value -> value.get()).orElse(2.418);

        // Capture Context

        final Location<World> initialLocation = process.getContext().get("root:initial_location", TypeToken.of(Location.class));
        final Map<String, Integer> controls = process.getContext().setOnce(ControlCapture.CONTROL_TIME, new TypeToken<Map<String, Integer>>() {}, Maps.newHashMap());
        process.getContext().setOnce(ControlCapture.VERTICAL_DISTANCE, TypeToken.of(Double.class), 1d);
        process.getContext().setOnce(ControlCapture.HORIZONTAL_DISTANCE, TypeToken.of(Double.class), 1d);

        if (controls.isEmpty()) {
            controls.put("flight", 0);
            controls.put("sprint", 0);
            controls.put("sneak", 0);
            controls.put("walk", 0);
        }

        double walkSpeed = player.get(Keys.WALKING_SPEED).orElse(1d) * 10;
        double flySpeed = player.get(Keys.FLYING_SPEED).orElse(0.5) * 5;

        if (initialLocation != null && player.getLocation().getY() != initialLocation.getY()) {
            process.getContext().transform(ControlCapture.VERTICAL_DISTANCE, TypeToken.of(Double.class), value -> value * lift);
        }

        if (player.get(Keys.IS_FLYING).orElse(false)
                || player.get(Keys.IS_ELYTRA_FLYING).orElse(false)
                || player.get(Keys.GAME_MODE).map(gameMode -> gameMode.equals(GameModes.SPECTATOR)).orElse(false)) {
            process.getContext().transform(ControlCapture.HORIZONTAL_DISTANCE, TypeToken.of(Double.class), value -> value * (flight * flySpeed));

            controls.put("flight", controls.get("flight") + 1);
        }

        if (player.get(Keys.IS_SPRINTING).orElse(false)) {
            process.getContext().transform(ControlCapture.HORIZONTAL_DISTANCE, TypeToken.of(Double.class), value -> value * (sprint * walkSpeed));

            controls.put("sprint", controls.get("sprint") + 1);
        }

        if (player.get(Keys.IS_SNEAKING).orElse(false)) {
            process.getContext().transform(ControlCapture.HORIZONTAL_DISTANCE, TypeToken.of(Double.class), value -> value * (sneak * walkSpeed));

            controls.put("sneak", controls.get("sneak") + 1);
        } else {
            process.getContext().transform(ControlCapture.HORIZONTAL_DISTANCE, TypeToken.of(Double.class), value -> value * (walk * walkSpeed));

            controls.put("walk", controls.get("walk") + 1);
        }
    }

}
