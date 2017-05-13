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

import com.google.common.collect.Sets;
import com.google.common.reflect.TypeToken;
import io.github.connorhartley.guardian.Guardian;
import io.github.connorhartley.guardian.detection.Detection;
import io.github.connorhartley.guardian.sequence.context.CaptureContainer;
import io.github.connorhartley.guardian.sequence.context.CaptureContext;
import io.github.connorhartley.guardian.sequence.context.CaptureKey;
import io.github.connorhartley.guardian.storage.container.StorageKey;
import org.spongepowered.api.data.key.Keys;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.util.Tuple;

import java.util.Map;
import java.util.Set;

public class PlayerControlContext {

    public static class InvalidControl extends CaptureContext {

        public static CaptureKey<InvalidControl, Set<Tuple<String, String>>> invalidMoves =
                new CaptureKey<>(InvalidControl.class, "invalid_movements", Sets.newHashSet());

        public InvalidControl(Guardian plugin, Detection detection) {
            super(plugin, detection);
        }

        @Override
        public CaptureContainer start(Player player, CaptureContainer valuation) {
            valuation.set(InvalidControl.invalidMoves, null);

            return valuation;
        }

        @Override
        public CaptureContainer update(Player player, CaptureContainer valuation) {
            if (player.get(Keys.IS_SNEAKING).isPresent() && player.get(Keys.IS_SNEAKING).get()) {
                if (player.get(Keys.IS_SPRINTING).isPresent() && player.get(Keys.IS_SPRINTING).get()) {
                    valuation.transform(InvalidControl.invalidMoves, oldValue -> {
                        if (!oldValue.contains(Tuple.of("sneaking", "sprinting"))) {
                            oldValue.add(Tuple.of("sneaking", "sprinting"));
                            return oldValue;
                        }
                        return oldValue;
                    });
                }
            } else if (player.get(Keys.IS_SITTING).isPresent() && player.get(Keys.IS_SITTING).get()) {
                if (player.get(Keys.IS_SPRINTING).isPresent() && player.get(Keys.IS_SPRINTING).get()) {
                    valuation.transform(InvalidControl.invalidMoves, oldValue -> {
                        if (!oldValue.contains(Tuple.of("sitting", "sprinting"))) {
                            oldValue.add(Tuple.of("sitting", "sprinting"));
                            return oldValue;
                        }
                        return oldValue;
                    });
                } else if (player.get(Keys.IS_FLYING).isPresent() && player.get(Keys.IS_FLYING).get()) {
                    valuation.transform(InvalidControl.invalidMoves, oldValue -> {
                        if (!oldValue.contains(Tuple.of("sitting", "flying"))) {
                            oldValue.add(Tuple.of("sitting", "flying"));
                            return oldValue;
                        }
                        return oldValue;
                    });
                }
            }

            return valuation;
        }

        @Override
        public CaptureContainer stop(Player player, CaptureContainer valuation) {
            return valuation;
        }
    }

    public static class VerticalSpeed extends CaptureContext {

        private double flySpeedControl = 1.065;

        public VerticalSpeed(Guardian plugin, Detection detection) {
            super(plugin, detection);

            if (this.getDetection().getConfiguration().get(new StorageKey<>("control-values"), new TypeToken<Map<String, Double>>(){}).isPresent()) {
                Map<String, Double> storageValueMap = this.getDetection().getConfiguration().get(new StorageKey<>("control-values"),
                        new TypeToken<Map<String, Double>>(){}).get().getValue();


                this.flySpeedControl = storageValueMap.get("fly");
            }
        }

        @Override
        public CaptureContainer start(Player player, CaptureContainer valuation) {
            valuation.set(PlayerControlContext.VerticalSpeed.class, "vertical_control_speed", 1.0);
            valuation.set(PlayerControlContext.VerticalSpeed.class, "update", 0);

            return valuation;
        }

        @Override
        public CaptureContainer update(Player player, CaptureContainer valuation) {
            if (player.get(Keys.IS_FLYING).isPresent()) {
                if (player.get(Keys.IS_FLYING).get()) {
                    valuation.<PlayerControlContext.VerticalSpeed, Double>transform(
                            PlayerControlContext.VerticalSpeed.class, "vertical_control_speed", oldValue -> oldValue * this.flySpeedControl);
                }
            }
            valuation.<PlayerControlContext.VerticalSpeed, Integer>transform(
                    PlayerControlContext.VerticalSpeed.class, "update", oldValue -> oldValue + 1);

            return valuation;
        }

        @Override
        public CaptureContainer stop(Player player, CaptureContainer valuation) {
            return valuation;
        }
    }

    public static class HorizontalSpeed extends CaptureContext {

        private double sneakSpeedControl = 1.015;
        private double walkSpeedControl = 1.035;
        private double sprintSpeedControl = 1.065;
        private double flySpeedControl = 1.065;

        private double walkSpeedData = 2;
        private double flySpeedData = 2;

        public HorizontalSpeed(Guardian plugin, Detection detection) {
            super(plugin, detection);

            if (this.getDetection().getConfiguration().get(new StorageKey<>("control-values"), new TypeToken<Map<String, Double>>(){}).isPresent()) {
                Map<String, Double> storageValueMap = this.getDetection().getConfiguration().get(new StorageKey<>("control-values"),
                        new TypeToken<Map<String, Double>>(){}).get().getValue();

                this.sneakSpeedControl = storageValueMap.get("sneak");
                this.walkSpeedControl = storageValueMap.get("walk");
                this.sprintSpeedControl = storageValueMap.get("sprint");
                this.flySpeedControl = storageValueMap.get("fly");
            }
        }

        @Override
        public CaptureContainer start(Player player, CaptureContainer valuation) {
            if (player.get(Keys.WALKING_SPEED).isPresent()) {
                this.walkSpeedData = player.get(Keys.WALKING_SPEED).get();
            }

            if (player.get(Keys.FLYING_SPEED).isPresent()) {
                this.flySpeedData = player.get(Keys.FLYING_SPEED).get();
            }

            valuation.set(PlayerControlContext.HorizontalSpeed.class, "control_modifier", 1.0);
            valuation.set(PlayerControlContext.HorizontalSpeed.class, "horizontal_control_speed", 1.0);
            valuation.set(PlayerControlContext.HorizontalSpeed.class, "control_speed_state", State.WALKING);
            valuation.set(PlayerControlContext.HorizontalSpeed.class, "update", 0);

            return valuation;
        }

        @Override
        public CaptureContainer update(Player player, CaptureContainer valuation) {
            if (player.get(Keys.IS_SPRINTING).isPresent() && player.get(Keys.IS_SNEAKING).isPresent() &&
                    player.get(Keys.IS_FLYING).isPresent()) {
                if (player.get(Keys.IS_FLYING).get()) {
                    valuation.<PlayerControlContext.HorizontalSpeed, Double>transform(
                            PlayerControlContext.HorizontalSpeed.class, "control_modifier", oldValue -> oldValue + (0.05 * this.flySpeedData));

                    valuation.<PlayerControlContext.HorizontalSpeed, Double>transform(
                            PlayerControlContext.HorizontalSpeed.class, "horizontal_control_speed", oldValue -> oldValue * this.flySpeedControl);

                    valuation.set(PlayerControlContext.HorizontalSpeed.class, "control_speed_state", State.FLYING);
                } else if (player.get(Keys.IS_SPRINTING).get()) {
                    valuation.<PlayerControlContext.HorizontalSpeed, Double>transform(
                            PlayerControlContext.HorizontalSpeed.class, "control_modifier", oldValue -> oldValue + (0.05 * this.walkSpeedData));

                    valuation.<PlayerControlContext.HorizontalSpeed, Double>transform(
                            PlayerControlContext.HorizontalSpeed.class, "horizontal_control_speed", oldValue -> oldValue * this.sprintSpeedControl);

                    valuation.set(PlayerControlContext.HorizontalSpeed.class, "control_speed_state", State.SPRINTING);
                } else if (player.get(Keys.IS_SNEAKING).get()) {
                    valuation.<PlayerControlContext.HorizontalSpeed, Double>transform(
                            PlayerControlContext.HorizontalSpeed.class, "horizontal_control_speed", oldValue -> oldValue * this.sneakSpeedControl);

                    valuation.set(PlayerControlContext.HorizontalSpeed.class, "control_speed_state", State.SNEAKING);
                } else {
                    valuation.<PlayerControlContext.HorizontalSpeed, Double>transform(
                            HorizontalSpeed.class, "control_modifier", oldValue -> oldValue + (0.05 * this.walkSpeedControl));

                    valuation.<PlayerControlContext.HorizontalSpeed, Double>transform(
                            PlayerControlContext.HorizontalSpeed.class, "horizontal_control_speed", oldValue -> oldValue * this.walkSpeedControl);

                    valuation.set(PlayerControlContext.HorizontalSpeed.class, "control_speed_state", State.WALKING);
                }
            }

            valuation.<PlayerControlContext.HorizontalSpeed, Integer>transform(
                    PlayerControlContext.HorizontalSpeed.class, "update", oldValue -> oldValue + 1);

            return valuation;
        }

        @Override
        public CaptureContainer stop(Player player, CaptureContainer valuation) {
            return valuation;
        }

        public enum State {
            SNEAKING,
            WALKING,
            FLYING,
            SPRINTING
        }

    }

}
