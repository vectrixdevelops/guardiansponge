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
package io.github.connorhartley.guardian.internal.capture;

import com.google.common.collect.Sets;
import com.google.common.reflect.TypeToken;
import com.ichorpowered.guardian.api.detection.Detection;
import com.ichorpowered.guardian.api.detection.DetectionConfiguration;
import com.ichorpowered.guardian.api.entry.EntityEntry;
import com.ichorpowered.guardian.api.sequence.capture.CaptureContainer;
import com.ichorpowered.guardian.api.util.IdentifierKey;
import io.github.connorhartley.guardian.sequence.GuardianSequence;
import io.github.connorhartley.guardian.sequence.capture.AbstractCapture;
import org.spongepowered.api.data.key.Keys;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.world.Location;
import org.spongepowered.api.world.World;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

public class PlayerControlCapture {

    public static String FLY = "fly";
    public static String SNEAK = "sneak";
    public static String SPRINT = "sprint";
    public static String WALK = "walk";

    public static class Invalid<E, F extends DetectionConfiguration> extends AbstractCapture<E, F> {

        public static IdentifierKey<String> INVALID_MOVEMENT =
                IdentifierKey.of(Invalid.class.getCanonicalName().toUpperCase() + "_INVALID_MOVEMENT");

        public Invalid(E owner, Detection<E, F> detection) {
            super(owner, detection);
        }

        @Override
        public void start(EntityEntry entry, CaptureContainer captureContainer) {
            captureContainer.put(Invalid.INVALID_MOVEMENT, Sets.newHashSet());
        }

        @Override
        public void update(EntityEntry entry, CaptureContainer captureContainer) {
            if (!entry.getEntity(TypeToken.of(Player.class)).isPresent()) return;
            Player player = entry.getEntity(TypeToken.of(Player.class)).get();

            Set<String> cap = captureContainer.get(Invalid.INVALID_MOVEMENT);
            if (player.get(Keys.IS_SNEAKING).isPresent() && player.get(Keys.IS_SNEAKING).get()) {
                if (player.get(Keys.IS_SPRINTING).isPresent() && player.get(Keys.IS_SPRINTING).get()) {
                    assert cap != null;
                    if (!cap.contains("sneaking") || !cap.contains("sprinting")) {
                        cap.addAll(Arrays.asList("sneaking", "sprinting"));
                    }
                }
            } else if (player.get(Keys.IS_SLEEPING).isPresent() || player.get(Keys.IS_SLEEPING).get() ||
                    player.get(Keys.VEHICLE).isPresent()) {
                if (player.get(Keys.IS_SPRINTING).isPresent() && player.get(Keys.IS_SPRINTING).get()) {
                    assert cap != null;
                    if (!cap.contains("sitting") || !cap.contains("sprinting")) {
                        cap.addAll(Arrays.asList("sitting", "sprinting"));
                    }
                } else if (player.get(Keys.IS_FLYING).isPresent() && player.get(Keys.IS_FLYING).get()) {
                    assert cap != null;
                    if (!cap.contains("sitting") || !cap.contains("flying")) {
                        cap.addAll(Arrays.asList("sitting", "flying"));
                    }
                }
            }

            assert cap != null;
            captureContainer.put(Invalid.INVALID_MOVEMENT, cap);
        }

        @Override
        public void stop(EntityEntry entry, CaptureContainer captureContainer) {}

    }

    public static class Common<E, F extends DetectionConfiguration> extends AbstractCapture<E, F> {

        public static IdentifierKey<String> VERTICAL_OFFSET =
                IdentifierKey.of(Common.class.getCanonicalName().toUpperCase() + "_VERTICAL_OFFSET");

        public static IdentifierKey<String> HORIZONTAL_OFFSET =
                IdentifierKey.of(Common.class.getCanonicalName().toUpperCase() + "_HORIZONTAL_OFFSET");

        public static IdentifierKey<String> CONTROL_STATE_TICKS =
                IdentifierKey.of(Common.class.getCanonicalName().toUpperCase() + "_CONTROL_STATE_TICKS");

        public static IdentifierKey<String> UPDATE =
                IdentifierKey.of(Common.class.getCanonicalName().toUpperCase() + "_UPDATE");

        private double liftOffset = 0;

        private double sneakOffset = 0;
        private double walkOffset = 0;
        private double sprintOffset = 0;
        private double flyOffset = 0;

        public Common(E owner, Detection<E, F> detection) {
            super(owner, detection);

            this.liftOffset = this.getDetection().getConfiguration().getStorage().getNode("analysis", "control-values", "lift")
                    .getDouble(0d);

            this.sneakOffset = this.getDetection().getConfiguration().getStorage().getNode("analysis", "control-values", SNEAK)
                    .getDouble(0d);

            this.walkOffset = this.getDetection().getConfiguration().getStorage().getNode("analysis", "control-values", WALK)
                    .getDouble(0d);

            this.sprintOffset = this.getDetection().getConfiguration().getStorage().getNode("analysis", "control-values", SPRINT)
                    .getDouble(0d);

            this.flyOffset = this.getDetection().getConfiguration().getStorage().getNode("analysis", "control-values", FLY)
                    .getDouble(0d);
        }

        @Override
        public void start(EntityEntry entry, CaptureContainer captureContainer) {
            Map<String, Integer> controlState = new HashMap<>();
            controlState.put(FLY, 0);
            controlState.put(WALK, 0);
            controlState.put(SNEAK, 0);
            controlState.put(SPRINT, 0);

            captureContainer.put(Common.VERTICAL_OFFSET, 1.0);
            captureContainer.put(Common.HORIZONTAL_OFFSET, 1.0);
            captureContainer.put(Common.CONTROL_STATE_TICKS, controlState);
            captureContainer.put(Common.UPDATE, 0);
        }

        @Override
        public void update(EntityEntry entry, CaptureContainer captureContainer) {
            if (!entry.getEntity(TypeToken.of(Player.class)).isPresent() || captureContainer.<Location<World>>get(GuardianSequence.INITIAL_LOCATION) == null) return;
            Player player = entry.getEntity(TypeToken.of(Player.class)).get();

            if ((player.get(Keys.IS_FLYING).isPresent() && player.get(Keys.IS_FLYING).get()) ||
                    captureContainer.<Location>get(GuardianSequence.INITIAL_LOCATION).getY() != player.getLocation().getY()) {
                captureContainer.<Double>transform(Common.VERTICAL_OFFSET, original -> original * this.liftOffset);

                captureContainer.<Double>transform(Common.HORIZONTAL_OFFSET, original -> original * this.flyOffset);

                captureContainer.transform(Common.CONTROL_STATE_TICKS, (Map<String, Integer> original) -> {
                    original.put(FLY, original.get(FLY) + 1);
                    return original;
                });
            } else if (player.get(Keys.IS_SPRINTING).isPresent() && player.get(Keys.IS_SPRINTING).get()) {
                captureContainer.<Double>transform(Common.HORIZONTAL_OFFSET, original -> original * this.sprintOffset);

                captureContainer.transform(Common.CONTROL_STATE_TICKS, (Map<String, Integer> original) -> {
                    original.put(SPRINT, original.get(SPRINT) + 1);
                    return original;
                });
            } else if (player.get(Keys.IS_SNEAKING).isPresent() && player.get(Keys.IS_SNEAKING).get()) {
                captureContainer.<Double>transform(Common.HORIZONTAL_OFFSET, original -> original * this.sneakOffset);

                captureContainer.transform(Common.CONTROL_STATE_TICKS, (Map<String, Integer> original) -> {
                    original.put(SNEAK, original.get(SNEAK) + 1);
                    return original;
                });
            } else {
                captureContainer.<Double>transform(Common.HORIZONTAL_OFFSET, original -> original * this.walkOffset);

                captureContainer.transform(Common.CONTROL_STATE_TICKS, (Map<String, Integer> original) -> {
                    original.put(WALK, original.get(WALK) + 1);
                    return original;
                });
            }

            captureContainer.<Integer>transform(Common.UPDATE, original -> original + 1);
        }

        @Override
        public void stop(EntityEntry entry, CaptureContainer captureContainer) {}

    }

}
