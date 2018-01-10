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
package com.ichorpowered.guardian.common.capture;

import com.flowpowered.math.vector.Vector3d;
import com.ichorpowered.guardian.content.transaction.GuardianSingleValue;
import com.ichorpowered.guardian.sequence.GuardianSequence;
import com.ichorpowered.guardian.sequence.capture.AbstractCapture;
import com.ichorpowered.guardian.util.ContentUtil;
import com.ichorpowered.guardian.util.WorldUtil;
import com.ichorpowered.guardian.util.entity.BoundingBox;
import com.ichorpowered.guardianapi.content.ContentContainer;
import com.ichorpowered.guardianapi.content.ContentKeys;
import com.ichorpowered.guardianapi.content.transaction.result.SingleValue;
import com.ichorpowered.guardianapi.detection.Detection;
import com.ichorpowered.guardianapi.detection.capture.CaptureContainer;
import com.ichorpowered.guardianapi.entry.entity.PlayerEntry;
import com.ichorpowered.guardianapi.util.key.NamedTypeKey;
import org.spongepowered.api.data.key.Keys;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.world.Location;

import java.util.Optional;

import javax.annotation.Nonnull;

public class PlayerPositionCapture {

    public static class RelativeAltitude extends AbstractCapture {

        private static final String CLASS_NAME = RelativeAltitude.class.getSimpleName().toUpperCase();

        public static NamedTypeKey<Location> INITIAL_DEPTH =
                NamedTypeKey.of(CLASS_NAME + ":initialDepth", Location.class);

        public static NamedTypeKey<Double> RELATIVE_ALTITUDE =
                NamedTypeKey.of(CLASS_NAME + ":relativeAltitude", Double.class);

        private final double amount;
        private final boolean liftOnly;

        public RelativeAltitude(@Nonnull Object plugin, @Nonnull Detection detection) {
            this(plugin, detection, 0.25);
        }

        public RelativeAltitude(@Nonnull Object plugin, @Nonnull Detection detection,
                                double amount) {
            this(plugin, detection, amount, false);
        }

        public RelativeAltitude(@Nonnull Object plugin, @Nonnull Detection detection,
                                double amount, boolean liftOnly) {
            super(plugin, detection);

            this.amount = amount;
            this.liftOnly = liftOnly;
        }

        @Override
        public void update(PlayerEntry entry, CaptureContainer captureContainer) {
            if (!entry.getEntity(Player.class).isPresent() || !captureContainer.get(GuardianSequence.INITIAL_LOCATION).isPresent()) return;
            final Player player = entry.getEntity(Player.class).get();

            final SingleValue<Double> playerBoxWidth = ContentUtil.getFirst(ContentKeys.BOX_PLAYER_WIDTH, entry, (ContentContainer) this.getDetection()).orElse(GuardianSingleValue.empty());
            final SingleValue<Double> playerBoxHeight = ContentUtil.getFirst(ContentKeys.BOX_PLAYER_HEIGHT, entry, (ContentContainer) this.getDetection()).orElse(GuardianSingleValue.empty());
            final SingleValue<Double> playerBoxSafety = ContentUtil.getFirst(ContentKeys.BOX_PLAYER_SAFETY, entry, (ContentContainer) this.getDetection()).orElse(GuardianSingleValue.empty());

            final double playerWidth = playerBoxWidth.getElement().orElse(1.0) + playerBoxSafety.getElement().orElse(0.08);
            final double playerHeight = playerBoxHeight.getElement().orElse(1.75) + playerBoxSafety.getElement().orElse(0.08);

            final boolean isSneaking = player.get(Keys.IS_SNEAKING).isPresent() && player.get(Keys.IS_SNEAKING).get();
            final BoundingBox playerBox = WorldUtil.getBoundingBox(playerWidth, isSneaking ? (playerHeight - 0.25) : playerHeight);

            final Location location = player.getLocation();

            Location relativeAltitude = null;
            double blockDepthOffset = 0;

            for (int n = 0; n < location.getY(); n++) {
                double i = this.amount * n;
                Optional<Location> maximumDepth = captureContainer.get(RelativeAltitude.INITIAL_DEPTH);

                if (!WorldUtil.isEmptyAtDepth(location, playerBox, i)) {
                    Location currentDepth = location.sub(0, i, 0);

                    if (maximumDepth.isPresent() && maximumDepth.get().getY() == currentDepth.getY()) {
                        relativeAltitude = currentDepth.add(0, this.amount, 0);
                        blockDepthOffset = 1;
                        break;
                    } else if (maximumDepth.isPresent() && maximumDepth.get().getY() < currentDepth.getY()) {
                        relativeAltitude = currentDepth.add(0, this.amount, 0);
                        blockDepthOffset = (currentDepth.getY() - maximumDepth.get().getY()) > -1 ?
                                -1 : currentDepth.getY() - maximumDepth.get().getY();
                        break;
                    } else if (maximumDepth.isPresent() && maximumDepth.get().getY() > currentDepth.getY()) {
                        relativeAltitude = currentDepth.add(0, this.amount, 0);
                        blockDepthOffset = (maximumDepth.get().getY() - currentDepth.getY()) < 1 ?
                                1 : maximumDepth.get().getY() - currentDepth.getY();
                        break;
                    } else if (!maximumDepth.isPresent()) {
                        captureContainer.put(RelativeAltitude.INITIAL_DEPTH, currentDepth);
                        relativeAltitude = currentDepth.add(0, this.amount, 0);
                        break;
                    }
                }
            }

            if (!captureContainer.get(RelativeAltitude.INITIAL_DEPTH).isPresent() || relativeAltitude == null) {
                relativeAltitude = player.getLocation().setPosition(new Vector3d(player.getLocation().getX(), 0, player.getLocation().getZ()));
            }

            double relativeAltitudeOffset = (player.getLocation().getY() - relativeAltitude.getY()) - Math.abs(blockDepthOffset);

            if (this.liftOnly && relativeAltitudeOffset < 0) return;

            captureContainer.transform(RelativeAltitude.RELATIVE_ALTITUDE, original -> original + relativeAltitudeOffset, 1.0);
        }

    }

}
