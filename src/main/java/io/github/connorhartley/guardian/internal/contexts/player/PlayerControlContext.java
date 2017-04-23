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
import io.github.connorhartley.guardian.context.Context;
import io.github.connorhartley.guardian.context.ContextTypes;
import io.github.connorhartley.guardian.context.valuation.ContextValuation;
import io.github.connorhartley.guardian.detection.Detection;
import org.spongepowered.api.data.key.Keys;
import org.spongepowered.api.entity.living.player.Player;

import java.util.HashMap;
import java.util.Map;

public class PlayerControlContext {

    public static class VerticalSpeed extends Context {

        private double flySpeedControl = 1.065;

        private boolean suspended = false;

        public VerticalSpeed(Guardian plugin, Detection detection, ContextValuation contextValuation, Player player) {
            super(plugin, detection, contextValuation, player);

            if (this.getDetection().getConfiguration().get("control-values", new HashMap<String, Double>()).isPresent()) {
                Map<String, Double> storageValueMap = this.getDetection().getConfiguration().get("control-values",
                        new HashMap<String, Double>()).get().getValue();


                this.flySpeedControl = storageValueMap.get("fly");
            }

            this.getContextValuation().set(VerticalSpeed.class, "vertical_control_speed", 1.0);
        }

        @Override
        public void update() {
            if (this.getPlayer().get(Keys.IS_FLYING).isPresent()) {
                if (this.getPlayer().get(Keys.IS_FLYING).get()) {
                    this.getContextValuation().<VerticalSpeed, Double>transform(VerticalSpeed.class, "vertical_control_speed", oldValue -> oldValue * this.flySpeedControl);
                }
            }
            this.getContextValuation().<VerticalSpeed, Integer>transform(VerticalSpeed.class, "update", oldValue -> oldValue + 1);
        }

        @Override
        public void suspend() {
            this.suspended = true;
        }

        @Override
        public boolean isSuspended() {
            return this.suspended;
        }

    }

    public static class HorizontalSpeed extends Context {

        private double sneakSpeedControl = 1.015;
        private double walkSpeedControl = 1.035;
        private double sprintSpeedControl = 1.065;
        private double flySpeedControl = 1.065;

        private double walkSpeedData = 2;
        private double flySpeedData = 2;

        private boolean suspended = false;

        public HorizontalSpeed(Guardian plugin, Detection detection, ContextValuation contextValuation, Player player) {
            super(plugin, detection, contextValuation, player);

            if (this.getDetection().getConfiguration().get("control-values", new HashMap<String, Double>()).isPresent()) {
                Map<String, Double> storageValueMap = this.getDetection().getConfiguration().get("control-values",
                        new HashMap<String, Double>()).get().getValue();

                this.sneakSpeedControl = storageValueMap.get("sneak");
                this.walkSpeedControl = storageValueMap.get("walk");
                this.sprintSpeedControl = storageValueMap.get("sprint");
                this.flySpeedControl = storageValueMap.get("fly");
            }

            if (this.getPlayer().get(Keys.WALKING_SPEED).isPresent()) {
                this.walkSpeedData = this.getPlayer().get(Keys.WALKING_SPEED).get();
            }

            if (this.getPlayer().get(Keys.FLYING_SPEED).isPresent()) {
                this.flySpeedData = this.getPlayer().get(Keys.FLYING_SPEED).get();
            }

            this.getContextValuation().set(HorizontalSpeed.class, "control_modifier", 1.0);
            this.getContextValuation().set(HorizontalSpeed.class, "horizontal_control_speed", 1.0);
            this.getContextValuation().set(HorizontalSpeed.class, "control_speed_state", State.WALKING);
        }

        @Override
        public void update() {
            if (this.getPlayer().get(Keys.IS_SPRINTING).isPresent() && this.getPlayer().get(Keys.IS_SNEAKING).isPresent() &&
                    this.getPlayer().get(Keys.IS_FLYING).isPresent()) {
                if (this.getPlayer().get(Keys.IS_FLYING).get()) {
                    this.getContextValuation().<HorizontalSpeed, Double>transform(
                            HorizontalSpeed.class, "control_modifier", oldValue -> oldValue + (0.05 * this.flySpeedData));

                    this.getContextValuation().<HorizontalSpeed, Double>transform(
                            HorizontalSpeed.class, "horizontal_control_speed", oldValue -> oldValue * this.flySpeedControl);

                    this.getContextValuation().set(HorizontalSpeed.class, "control_speed_state", State.FLYING);
                } else if (this.getPlayer().get(Keys.IS_SPRINTING).get()) {
                    this.getContextValuation().<HorizontalSpeed, Double>transform(
                            HorizontalSpeed.class, "control_modifier", oldValue -> oldValue + (0.05 * this.walkSpeedData));

                    this.getContextValuation().<HorizontalSpeed, Double>transform(
                            HorizontalSpeed.class, "horizontal_control_speed", oldValue -> oldValue * this.sprintSpeedControl);

                    this.getContextValuation().set(HorizontalSpeed.class, "control_speed_state", State.SPRINTING);
                } else if (this.getPlayer().get(Keys.IS_SNEAKING).get()) {
                    this.getContextValuation().<HorizontalSpeed, Double>transform(
                            HorizontalSpeed.class, "horizontal_control_speed", oldValue -> oldValue * this.sneakSpeedControl);

                    this.getContextValuation().set(HorizontalSpeed.class, "control_speed_state", State.SNEAKING);
                } else {
                    this.getContextValuation().<HorizontalSpeed, Double>transform(
                            HorizontalSpeed.class, "control_modifier", oldValue -> oldValue + (0.05 * this.walkSpeedControl));

                    this.getContextValuation().<HorizontalSpeed, Double>transform(
                            HorizontalSpeed.class, "horizontal_control_speed", oldValue -> oldValue * this.walkSpeedControl);

                    this.getContextValuation().set(HorizontalSpeed.class, "control_speed_state", State.WALKING);
                }
            }
            this.getContextValuation().<HorizontalSpeed, Integer>transform(HorizontalSpeed.class, "update", oldValue -> oldValue + 1);
        }

        @Override
        public void suspend() {
            this.suspended = true;
        }

        @Override
        public boolean isSuspended() {
            return this.suspended;
        }

        public enum State {
            SNEAKING,
            WALKING,
            FLYING,
            SPRINTING
        }

    }

}
