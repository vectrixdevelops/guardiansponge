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
import io.github.connorhartley.guardian.sequence.context.CaptureContainer;
import io.github.connorhartley.guardian.sequence.context.CaptureContext;
import org.spongepowered.api.block.BlockTypes;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.world.Location;
import org.spongepowered.api.world.World;

public class PlayerPositionContext {

    public static class Altitude extends CaptureContext {

        private Location<World> depthThreshold;

        private boolean stopped = false;

        public Altitude(Guardian plugin, Detection detection) {
            super(plugin, detection);
        }

        @Override
        public CaptureContainer start(Player player, CaptureContainer valuation) {
            this.stopped = false;

            this.depthThreshold = null;

            valuation.set(PlayerPositionContext.Altitude.class, "position_altitude", 0.0);
            valuation.set(PlayerPositionContext.Altitude.class, "update", 0);

            return valuation;
        }

        @Override
        public CaptureContainer update(Player player, CaptureContainer valuation) {
            Location<World> playerAltitude = null;
            double blockDepth = 0;

            for (int n = 0; n < player.getLocation().getY(); n++) {
                double i = Math.round(0.25 * n);

                if (!player.getLocation().sub(0, i, 0).getBlockType().equals(BlockTypes.AIR)) {
                    Location<World> currentDepth = player.getLocation().sub(0, i, 0);
                    if (this.depthThreshold != null && this.depthThreshold.getY() == currentDepth.getY()) {
                        playerAltitude = currentDepth.add(0, 0.25, 0);
                        blockDepth = 1;
                        break;
                    } else if (this.depthThreshold != null && this.depthThreshold.getY() < currentDepth.getY()) {
                        playerAltitude = currentDepth.add(0, 0.25, 0);
                        blockDepth = (currentDepth.getY() - this.depthThreshold.getY()) < 1 ?
                                1 : currentDepth.getY() - this.depthThreshold.getY();
                        break;
                    } else if (this.depthThreshold != null && this.depthThreshold.getY() > currentDepth.getY()) {
                        playerAltitude = currentDepth.add(0, 0.25, 0);
                        blockDepth = (this.depthThreshold.getY() - currentDepth.getY()) < 1 ?
                                1 : this.depthThreshold.getY() - currentDepth.getY();
                        break;
                    } else if (this.depthThreshold == null) {
                        this.depthThreshold = currentDepth;
                        playerAltitude = currentDepth.add(0, 0.25, 0);
                        break;
                    }
                }
            }

            if (this.depthThreshold == null || playerAltitude == null) {
                playerAltitude = new Location<>(player.getWorld(), player.getLocation().getX(), 0, player.getLocation().getZ());
            }

            double altitude = (player.getLocation().getY() - playerAltitude.getY()) - blockDepth;

            if (altitude < 0) {
                valuation.<PlayerPositionContext.Altitude, Double>transform(
                        PlayerPositionContext.Altitude.class, "position_altitude", oldValue -> oldValue);
            } else {
                valuation.<PlayerPositionContext.Altitude, Double>transform(
                        PlayerPositionContext.Altitude.class, "position_altitude", oldValue -> oldValue + altitude);
            }

            valuation.<PlayerPositionContext.Altitude, Integer>transform(
                    PlayerPositionContext.Altitude.class, "update", oldValue -> oldValue + 1);

            return valuation;
        }

        @Override
        public CaptureContainer stop(Player player, CaptureContainer valuation) {
            this.stopped = true;

            return valuation;
        }

        @Override
        public boolean hasStopped() {
            return this.stopped;
        }
    }
}
