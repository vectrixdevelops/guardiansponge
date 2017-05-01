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
import io.github.connorhartley.guardian.sequence.context.CaptureContext;
import io.github.connorhartley.guardian.sequence.context.CaptureContainer;
import io.github.connorhartley.guardian.detection.Detection;
import org.spongepowered.api.block.BlockTypes;
import org.spongepowered.api.world.Location;
import org.spongepowered.api.world.World;

public class PlayerPositionContext {

    public static class Altitude extends CaptureContext {

        private Location<World> depthThreshold;

        private CaptureContainer valuation;
        private boolean stopped = false;

        public Altitude(Guardian plugin, Detection detection) {
            super(plugin, detection);
        }

        @Override
        public CaptureContainer getContainer() {
            return this.valuation;
        }

        @Override
        public void start(CaptureContainer valuation) {
            this.valuation = valuation;
            this.stopped = false;

            this.depthThreshold = null;

            this.getContainer().set(PlayerPositionContext.Altitude.class, "position_altitude", 0.0);
            this.getContainer().set(PlayerPositionContext.Altitude.class, "update", 0);
        }

        @Override
        public void update(CaptureContainer valuation) {
            this.valuation = valuation;

            Location<World> playerAltitude = null;
            double blockDepth = 0;

            for (int n = 0; n < this.getPlayer().getLocation().getY(); n++) {
                double i = Math.round(0.25 * n);

                if (!this.getPlayer().getLocation().sub(0, i, 0).getBlockType().equals(BlockTypes.AIR)) {
                    Location<World> currentDepth = this.getPlayer().getLocation().sub(0, i, 0);
                    if (this.depthThreshold != null && this.depthThreshold.getY() == currentDepth.getY()) {
                        playerAltitude = currentDepth.add(0, 1, 0);
                        blockDepth = 1;
                    } else if (this.depthThreshold != null && this.depthThreshold.getY() < currentDepth.getY()) {
                        playerAltitude = currentDepth.add(0, 1, 0);
                        blockDepth = currentDepth.getY() - this.depthThreshold.getY();
                    } else if (this.depthThreshold != null && this.depthThreshold.getY() > currentDepth.getY()) {
                        playerAltitude = currentDepth.add(0, 1, 0);
                        blockDepth = this.depthThreshold.getY() - currentDepth.getY();
                    } else if (this.depthThreshold == null) {
                        this.depthThreshold = currentDepth;
                        playerAltitude = currentDepth.add(0, 1, 0);
                    }
                }
            }

            if (this.depthThreshold == null || playerAltitude == null) {
                playerAltitude = new Location<>(this.getPlayer().getWorld(), this.getPlayer().getLocation().getX(), 0, this.getPlayer().getLocation().getZ());
            }

            double altitude = (this.getPlayer().getLocation().getY() - playerAltitude.getY()) - blockDepth;

            if (altitude < 0) {
                this.getContainer().<PlayerPositionContext.Altitude, Double>transform(
                        PlayerPositionContext.Altitude.class, "position_altitude", oldValue -> oldValue);
            } else {
                this.getContainer().<PlayerPositionContext.Altitude, Double>transform(
                        PlayerPositionContext.Altitude.class, "position_altitude", oldValue -> oldValue + altitude);
            }

            this.getContainer().<PlayerPositionContext.Altitude, Integer>transform(
                    PlayerPositionContext.Altitude.class, "update", oldValue -> oldValue + 1);
        }

        @Override
        public void stop(CaptureContainer valuation) {
            this.valuation = valuation;

            this.stopped = true;
        }

        @Override
        public boolean hasStopped() {
            return this.stopped;
        }
    }
}
