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
package io.ichorpowered.guardian.common.capture;

import com.google.common.collect.Maps;
import com.ichorpowered.guardian.api.detection.Detection;
import com.ichorpowered.guardian.api.detection.DetectionConfiguration;
import com.ichorpowered.guardian.api.entry.EntityEntry;
import com.ichorpowered.guardian.api.sequence.capture.CaptureContainer;
import com.ichorpowered.guardian.api.util.key.NamedTypeKey;
import io.ichorpowered.guardian.sequence.GuardianSequence;
import io.ichorpowered.guardian.sequence.capture.AbstractCapture;
import org.spongepowered.api.data.key.Keys;
import org.spongepowered.api.entity.living.player.Player;

import javax.annotation.Nonnull;
import java.util.*;

public class PlayerControlCapture {

    public static String LIFT = "lift";
    public static String FLY = "fly";
    public static String SNEAK = "sneak";
    public static String SPRINT = "sprint";
    public static String WALK = "walk";

    public static class Invalid<E, F extends DetectionConfiguration> extends AbstractCapture<E, F> {

        private static final String CLASS_NAME = Invalid.class.getSimpleName().toUpperCase();

        public static NamedTypeKey<Set> INVALID_MOVEMENT =
                NamedTypeKey.of(CLASS_NAME + "_INVALID_MOVEMENT", Set.class);

        public Invalid(@Nonnull E owner, @Nonnull Detection<E, F> detection) {
            super(owner, detection);
        }

        @Override
        public void update(@Nonnull EntityEntry entry, @Nonnull CaptureContainer captureContainer) {
            if (!entry.getEntity(Player.class).isPresent()) return;
            Player player = entry.getEntity(Player.class).get();

            Set<String> cap = captureContainer.get(Invalid.INVALID_MOVEMENT).orElse(new HashSet<>());
            if (player.get(Keys.IS_SNEAKING).isPresent() && player.get(Keys.IS_SNEAKING).get()) {
                if (player.get(Keys.IS_SPRINTING).isPresent() && player.get(Keys.IS_SPRINTING).get()) {
                    if (!cap.contains("sneaking") || !cap.contains("sprinting")) {
                        cap.addAll(Arrays.asList("sneaking", "sprinting"));
                    }
                }
            } else if (player.get(Keys.IS_SLEEPING).isPresent() || player.get(Keys.IS_SLEEPING).get() ||
                    player.get(Keys.VEHICLE).isPresent()) {
                if (player.get(Keys.IS_SPRINTING).isPresent() && player.get(Keys.IS_SPRINTING).get()) {
                    if (!cap.contains("sitting") || !cap.contains("sprinting")) {
                        cap.addAll(Arrays.asList("sitting", "sprinting"));
                    }
                } else if (player.get(Keys.IS_FLYING).isPresent() && player.get(Keys.IS_FLYING).get()) {
                    if (!cap.contains("sitting") || !cap.contains("flying")) {
                        cap.addAll(Arrays.asList("sitting", "flying"));
                    }
                }
            }

            captureContainer.put(Invalid.INVALID_MOVEMENT, cap);
        }

    }

    public static class Common<E, F extends DetectionConfiguration> extends AbstractCapture<E, F> {

        private static final String CLASS_NAME = Common.class.getSimpleName().toUpperCase();

        public static NamedTypeKey<Double> VERTICAL_OFFSET =
                NamedTypeKey.of(CLASS_NAME + "_VERTICAL_OFFSET", Double.class);

        public static NamedTypeKey<Double> HORIZONTAL_OFFSET =
                NamedTypeKey.of(CLASS_NAME + "_HORIZONTAL_OFFSET", Double.class);

        public static NamedTypeKey<Map> CONTROL_STATE_TICKS =
                NamedTypeKey.of(CLASS_NAME + "_CONTROL_STATE_TICKS", Map.class);

        private double liftOffset = 2.012;

        private double sneakOffset = 1.068;
        private double walkOffset = 1.094;
        private double sprintOffset = 1.124;
        private double flyOffset = 1.218;

        public Common(@Nonnull E owner, @Nonnull Detection<E, F> detection) {
            super(owner, detection);

            this.liftOffset = this.getDetection().getConfiguration().getStorage().getNode("analysis", "control-values", LIFT)
                    .getDouble(this.liftOffset);

            this.sneakOffset = this.getDetection().getConfiguration().getStorage().getNode("analysis", "control-values", SNEAK)
                    .getDouble(this.sneakOffset);

            this.walkOffset = this.getDetection().getConfiguration().getStorage().getNode("analysis", "control-values", WALK)
                    .getDouble(this.walkOffset);

            this.sprintOffset = this.getDetection().getConfiguration().getStorage().getNode("analysis", "control-values", SPRINT)
                    .getDouble(this.sprintOffset);

            this.flyOffset = this.getDetection().getConfiguration().getStorage().getNode("analysis", "control-values", FLY)
                    .getDouble(this.flyOffset);
        }

        @Override
        public void update(@Nonnull EntityEntry entry, @Nonnull CaptureContainer captureContainer) {
            if (!entry.getEntity(Player.class).isPresent() || !captureContainer.get(GuardianSequence.INITIAL_LOCATION).isPresent()) return;

            Player player = entry.getEntity(Player.class).get();

            Map<String, Integer> controlState = new HashMap<>();
            controlState.put(FLY, 0);
            controlState.put(WALK, 0);
            controlState.put(SNEAK, 0);
            controlState.put(SPRINT, 0);
            captureContainer.putOnce(Common.CONTROL_STATE_TICKS, controlState);

            if (player.getLocation().getY() != captureContainer.get(GuardianSequence.INITIAL_LOCATION).get().getY()) {
                captureContainer.transform(Common.VERTICAL_OFFSET, original -> original * this.liftOffset, this.liftOffset);
            }

            if ((player.get(Keys.IS_FLYING).isPresent() && player.get(Keys.IS_FLYING).get())) {
                captureContainer.transform(Common.HORIZONTAL_OFFSET, original -> original * this.flyOffset, this.flyOffset);

                captureContainer.transform(Common.CONTROL_STATE_TICKS, original -> {
                    ((Map<String, Integer>) original).put(FLY, ((Map<String, Integer>) original).get(FLY) + 1);
                    return (Map<String, Integer>) original;
                }, Maps.newHashMap());
            } else if (player.get(Keys.IS_SPRINTING).isPresent() && player.get(Keys.IS_SPRINTING).get()) {
                captureContainer.transform(Common.HORIZONTAL_OFFSET, original -> original * this.sprintOffset, this.sprintOffset);

                captureContainer.transform(Common.CONTROL_STATE_TICKS, original -> {
                    ((Map<String, Integer>) original).put(SPRINT, ((Map<String, Integer>) original).get(SPRINT) + 1);
                    return (Map<String, Integer>) original;
                }, Maps.newHashMap());
            } else if (player.get(Keys.IS_SNEAKING).isPresent() && player.get(Keys.IS_SNEAKING).get()) {
                captureContainer.transform(Common.HORIZONTAL_OFFSET, original -> original * this.sneakOffset, this.sneakOffset);

                captureContainer.transform(Common.CONTROL_STATE_TICKS, original -> {
                    ((Map<String, Integer>) original).put(SNEAK, ((Map<String, Integer>) original).get(SPRINT) + 1);
                    return (Map<String, Integer>) original;
                }, Maps.newHashMap());
            } else {
                captureContainer.transform(Common.HORIZONTAL_OFFSET, original -> original * this.walkOffset, this.walkOffset);

                captureContainer.transform(Common.CONTROL_STATE_TICKS, original -> {
                    ((Map<String, Integer>) original).put(WALK, ((Map<String, Integer>) original).get(WALK) + 1);
                    return (Map<String, Integer>) original;
                }, Maps.newHashMap());
            }
        }
    }
}
