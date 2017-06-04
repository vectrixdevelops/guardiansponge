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
package io.github.connorhartley.guardian.internal.contexts.player;

import io.github.connorhartley.guardian.Guardian;
import io.github.connorhartley.guardian.detection.Detection;
import io.github.connorhartley.guardian.sequence.capture.CaptureContainer;
import io.github.connorhartley.guardian.sequence.capture.CaptureContext;
import io.github.connorhartley.guardian.sequence.capture.CaptureKey;
import io.github.connorhartley.guardian.storage.StorageSupplier;
import org.spongepowered.api.block.BlockTypes;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.world.Location;
import org.spongepowered.api.world.World;

import java.io.File;
import java.util.Optional;

public class PlayerPositionContext {

    public static class Altitude<E, F extends StorageSupplier<File>> extends CaptureContext<E, F> {

        public static CaptureKey<Altitude, Location<World>> depthThreshold =
                new CaptureKey<>(Altitude.class, "depth_threshold", null);

        public static CaptureKey<Altitude, Double> positionAltitude =
                new CaptureKey<>(Altitude.class, "position_altitude", 0.0);

        public static CaptureKey<Altitude, Integer> update =
                new CaptureKey<>(Altitude.class, "update", 0);

        public Altitude(Guardian plugin, Detection<E, F> detection) {
            super(plugin, detection);
        }

        @Override
        public CaptureContainer start(Player player, CaptureContainer valuation) {
            valuation.set(Altitude.depthThreshold);
            valuation.set(Altitude.positionAltitude);
            valuation.set(Altitude.update);

            return valuation;
        }

        @Override
        public CaptureContainer update(Player player, CaptureContainer valuation) {
            Location<World> playerAltitude = null;
            double blockDepth = 0;

            for (int n = 0; n < player.getLocation().getY(); n++) {
                double i = Math.round(0.25 * n);
                Optional<Location<World>> maxDepth = valuation.get(Altitude.depthThreshold);

                if (!player.getLocation().sub(0, i, 0).getBlockType().equals(BlockTypes.AIR)) {
                    Location<World> currentDepth = player.getLocation().sub(0, i, 0);
                    if (maxDepth.isPresent() && maxDepth.get().getY() == currentDepth.getY()) {
                        playerAltitude = currentDepth.add(0, 0.25, 0);
                        blockDepth = 1;
                        break;
                    } else if (maxDepth.isPresent() && maxDepth.get().getY() < currentDepth.getY()) {
                        playerAltitude = currentDepth.add(0, 0.25, 0);
                        blockDepth = (currentDepth.getY() - maxDepth.get().getY()) > -1 ?
                                -1 : currentDepth.getY() - maxDepth.get().getY();
                        break;
                    } else if (maxDepth.isPresent() && maxDepth.get().getY() > currentDepth.getY()) {
                        playerAltitude = currentDepth.add(0, 0.25, 0);
                        blockDepth = (maxDepth.get().getY() - currentDepth.getY()) < 1 ?
                                1 : maxDepth.get().getY() - currentDepth.getY();
                        break;
                    } else if (!maxDepth.isPresent()) {
                        valuation.set(Altitude.depthThreshold, currentDepth);
                        playerAltitude = currentDepth.add(0, 0.25, 0);
                        break;
                    }
                }
            }

            if (!valuation.get(Altitude.depthThreshold).isPresent() || playerAltitude == null) {
                playerAltitude = new Location<>(player.getWorld(), player.getLocation().getX(), 0, player.getLocation().getZ());
            }

            double altitude = (player.getLocation().getY() - playerAltitude.getY()) - Math.abs(blockDepth);

            valuation.transform(Altitude.positionAltitude, oldValue -> oldValue + altitude);

            valuation.transform(Altitude.update, oldValue -> oldValue + 1);

            return valuation;
        }

        @Override
        public CaptureContainer stop(Player player, CaptureContainer valuation) {
            return valuation;
        }
    }
}
