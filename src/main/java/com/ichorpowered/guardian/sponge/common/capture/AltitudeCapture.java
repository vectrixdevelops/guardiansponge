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

import com.google.common.reflect.TypeToken;
import com.ichorpowered.guardian.api.Guardian;
import com.ichorpowered.guardian.api.game.GameReference;
import com.ichorpowered.guardian.api.game.model.Model;
import com.ichorpowered.guardian.api.game.model.value.key.GameKeys;
import com.ichorpowered.guardian.api.sequence.capture.CaptureValue;
import com.ichorpowered.guardian.api.sequence.process.Process;
import com.ichorpowered.guardian.sponge.util.WorldUtil;
import com.ichorpowered.guardian.sponge.util.entity.BoundingBox;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.spongepowered.api.data.key.Keys;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.world.Location;
import org.spongepowered.api.world.World;

public class AltitudeCapture implements CaptureValue {

    public static String RELATIVE_ALTITUDE = asKey(AltitudeCapture.class, "relative_altitude");
    public static String INITIAL_DEPTH = asKey(AltitudeCapture.class, "initial_depth");
    public static String LAST_ALTITUDE = asKey(AltitudeCapture.class, "last_altitude");

    @Override
    public void apply(final @NonNull Process process) {
        final GameReference<Player> gameReference = process.getContext().get("root:player", new TypeToken<GameReference<Player>>() {});
        final Location<World> location = process.getContext().get("root:player_location", TypeToken.of(Location.class));
        final Model playerModel = Guardian.getModelRegistry().get(gameReference).orElse(null);
        if (gameReference == null || playerModel == null) return;

        final Player player = gameReference.get();

        // Model Values

        final double playerWidth = playerModel.requestFirst(GameKeys.PLAYER_WIDTH).map(value -> value.get()).orElse(0.6);
        final double playerHeight = playerModel.requestFirst(GameKeys.PLAYER_HEIGHT).map(value -> value.get()).orElse(1.8);
        final double amount = playerModel.requestFirst(GameKeys.RAY_TRACE_STEP).map(value -> value.get()).orElse(0.25);

        // Capture Context

        final BoundingBox playerBox = WorldUtil.getBoundingBox(playerWidth, player.get(Keys.IS_SNEAKING).orElse(false) ? (playerHeight - 0.15) : playerHeight);

        Location<World> relativeAltitude = null;
        double blockDepthOffset = 0;

        process.getContext().setOnce(AltitudeCapture.RELATIVE_ALTITUDE, TypeToken.of(Double.class), 0d);

        for (int n = 0; n < Math.abs(location.getY()); n++) {
            double i = amount * n;
            Location<World> maximumDepth = process.getContext().get(AltitudeCapture.INITIAL_DEPTH, TypeToken.of(Location.class));

            Location<World> currentDepth = location.sub(0, i, 0);

            if (!WorldUtil.isEmptyAtDepth(location, playerBox, i)) {
                if (maximumDepth != null && maximumDepth.getY() == currentDepth.getY()) {
                    relativeAltitude = currentDepth.add(0, amount, 0);
                    blockDepthOffset = 1;
                    break;
                } else if (maximumDepth != null && maximumDepth.getY() < currentDepth.getY()) {
                    relativeAltitude = currentDepth.add(0, amount, 0);
                    blockDepthOffset = (currentDepth.getY() - maximumDepth.getY()) > -1 ?
                            -1 : currentDepth.getY() - maximumDepth.getY();
                    break;
                } else if (maximumDepth != null && maximumDepth.getY() > currentDepth.getY()) {
                    relativeAltitude = currentDepth.add(0, amount, 0);
                    blockDepthOffset = (maximumDepth.getY() - currentDepth.getY()) < 1 ?
                            1 : maximumDepth.getY() - currentDepth.getY();
                    break;
                } else if (maximumDepth == null) {
                    process.getContext().set(AltitudeCapture.INITIAL_DEPTH, TypeToken.of(Location.class), currentDepth);

                    relativeAltitude = currentDepth.add(0, amount, 0);
                    break;
                }
            } else if ((currentDepth.getY() - 1) < 0) {
                if (maximumDepth == null) {
                    Location<World> modifiedLocation = currentDepth.sub(0, 256, 0);

                    process.getContext().set(AltitudeCapture.INITIAL_DEPTH, TypeToken.of(Location.class), modifiedLocation);
                }

                if (maximumDepth != null && maximumDepth.getY() == currentDepth.getY()) {
                    relativeAltitude = currentDepth.add(0, amount, 0);
                    blockDepthOffset = -1;
                    break;
                }
            }
        }

        if (process.getContext().get(AltitudeCapture.INITIAL_DEPTH, TypeToken.of(Location.class)) == null || relativeAltitude == null) {
            Location cloneLocation = player.getLocation().sub(0, player.getLocation().getY(), 0);
            relativeAltitude = cloneLocation;
        }

        final double offset = (player.getLocation().getY() - relativeAltitude.getY()) - Math.abs(blockDepthOffset);

        process.getContext().transform(AltitudeCapture.RELATIVE_ALTITUDE, TypeToken.of(Double.class), value -> value + offset);
        process.getContext().set(AltitudeCapture.LAST_ALTITUDE, TypeToken.of(Double.class), offset);
    }

}
