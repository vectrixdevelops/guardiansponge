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
package com.ichorpowered.guardian.common.capture.player;

import com.google.common.collect.Maps;
import com.google.common.reflect.TypeToken;
import com.ichorpowered.guardian.sequence.GuardianSequence;
import com.ichorpowered.guardian.sequence.capture.AbstractCapture;
import com.ichorpowered.guardian.sequence.capture.GuardianCaptureKey;
import com.ichorpowered.guardian.util.item.mutable.GuardianMapValue;
import com.ichorpowered.guardian.util.item.mutable.GuardianValue;
import com.ichorpowered.guardianapi.content.ContentKeys;
import com.ichorpowered.guardianapi.detection.Detection;
import com.ichorpowered.guardianapi.detection.capture.CaptureContainer;
import com.ichorpowered.guardianapi.detection.capture.CaptureKey;
import com.ichorpowered.guardianapi.entry.entity.PlayerEntry;
import com.ichorpowered.guardianapi.util.item.value.mutable.MapValue;
import com.ichorpowered.guardianapi.util.item.value.mutable.Value;
import org.spongepowered.api.data.key.Keys;
import org.spongepowered.api.entity.living.player.Player;

import java.util.Map;

import javax.annotation.Nonnull;

public class ControlCapture extends AbstractCapture {

    private static final String CLASS_NAME = ControlCapture.class.getSimpleName().toUpperCase();

    public static final CaptureKey<Value<Double>> VERTICAL_DISTANCE = GuardianCaptureKey.<Value<Double>>builder()
            .id(CLASS_NAME + ":verticalDistance")
            .name("VerticalDistance")
            .type(GuardianValue.empty(), TypeToken.of(Double.class))
            .build();

    public static final CaptureKey<Value<Double>> HORIZONTAL_DISTANCE = GuardianCaptureKey.<Value<Double>>builder()
            .id(CLASS_NAME + ":horizontalDistance")
            .name("HorizontalDistance")
            .type(GuardianValue.empty(), TypeToken.of(Double.class))
            .build();

    public static final CaptureKey<MapValue<String, Integer>> ACTIVE_CONTROL_TICKS = GuardianCaptureKey.<MapValue<String, Integer>>builder()
            .id(CLASS_NAME + ":activeControlTicks")
            .name("ActiveControlTicks")
            .type(GuardianMapValue.empty(), TypeToken.of(Map.class))
            .build();

    public static String FLY = "fly";
    public static String SNEAK = "sneak";
    public static String SPRINT = "sprint";
    public static String WALK = "walk";

    private double liftOffset;

    private double sneakOffset;
    private double walkOffset;
    private double sprintOffset;
    private double flyOffset;

    public ControlCapture(@Nonnull Object plugin, @Nonnull Detection detection) {
        super(plugin, detection);

        this.liftOffset = this.getDetection().getContentContainer().get(ContentKeys.MOVEMENT_LIFT_SPEED)
                .orElse(GuardianValue.empty()).getDirect().orElse(2.012);

        this.sneakOffset = this.getDetection().getContentContainer().get(ContentKeys.MOVEMENT_SNEAK_SPEED)
                .orElse(GuardianValue.empty()).getDirect().orElse(1.068);

        this.walkOffset = this.getDetection().getContentContainer().get(ContentKeys.MOVEMENT_WALK_SPEED)
                .orElse(GuardianValue.empty()).getDirect().orElse(1.094);

        this.sprintOffset = this.getDetection().getContentContainer().get(ContentKeys.MOVEMENT_SPRINT_SPEED)
                .orElse(GuardianValue.empty()).getDirect().orElse(1.124);

        this.flyOffset = this.getDetection().getContentContainer().get(ContentKeys.MOVEMENT_FLY_SPEED)
                .orElse(GuardianValue.empty()).getDirect().orElse(1.218);
    }

    @Override
    public void update(@Nonnull PlayerEntry entry, @Nonnull CaptureContainer captureContainer) {
        if (!entry.getEntity(Player.class).isPresent() || !captureContainer.get(GuardianSequence.INITIAL_LOCATION).isPresent()) return;
        final Player player = entry.getEntity(Player.class).get();

        final MapValue<String, Integer> activeControls = GuardianMapValue.builder(ControlCapture.ACTIVE_CONTROL_TICKS)
                .defaultElement(Maps.newHashMap())
                .element(Maps.newHashMap())
                .create();

        activeControls.put(FLY, 0);
        activeControls.put(WALK, 0);
        activeControls.put(SNEAK, 0);
        activeControls.put(SPRINT, 0);

        captureContainer.offerIfEmpty(activeControls);

        captureContainer.offerIfEmpty(GuardianValue.builder(ControlCapture.HORIZONTAL_DISTANCE)
                .defaultElement(1d)
                .element(1d)
                .create());

        captureContainer.offerIfEmpty(GuardianValue.builder(ControlCapture.VERTICAL_DISTANCE)
                .defaultElement(1d)
                .element(1d)
                .create());

        double walkSpeedData = player.get(Keys.WALKING_SPEED).orElse(1d) * 10;
        double flySpeedData = player.get(Keys.FLYING_SPEED).orElse(0.5) * 10;

        if (player.getLocation().getY() != captureContainer.get(GuardianSequence.INITIAL_LOCATION).get().getY()) {
            captureContainer.getValue(ControlCapture.VERTICAL_DISTANCE).ifPresent(value -> value.transform(original -> original * this.liftOffset));
        }

        if ((player.get(Keys.IS_FLYING).isPresent() && player.get(Keys.IS_FLYING).get())) {
            captureContainer.getValue(ControlCapture.HORIZONTAL_DISTANCE).ifPresent(value -> value.transform(original -> original * (this.flyOffset * flySpeedData)));

            captureContainer.getValue(ControlCapture.ACTIVE_CONTROL_TICKS).ifPresent(value -> value.put(FLY, value.get().get(FLY) + 1));
        } else if (player.get(Keys.IS_SPRINTING).isPresent() && player.get(Keys.IS_SPRINTING).get()) {
            captureContainer.getValue(ControlCapture.HORIZONTAL_DISTANCE).ifPresent(value -> value.transform(original -> original * (this.sprintOffset * walkSpeedData)));

            captureContainer.getValue(ControlCapture.ACTIVE_CONTROL_TICKS).ifPresent(value -> value.put(SPRINT, value.get().get(SPRINT) + 1));
        } else if (player.get(Keys.IS_SNEAKING).isPresent() && player.get(Keys.IS_SNEAKING).get()) {
            captureContainer.getValue(ControlCapture.HORIZONTAL_DISTANCE).ifPresent(value -> value.transform(original -> original * (this.sneakOffset * walkSpeedData)));

            captureContainer.getValue(ControlCapture.ACTIVE_CONTROL_TICKS).ifPresent(value -> value.put(SNEAK, value.get().get(SNEAK) + 1));
        } else {
            captureContainer.getValue(ControlCapture.HORIZONTAL_DISTANCE).ifPresent(value -> value.transform(original -> original * (this.walkOffset * walkSpeedData)));

            captureContainer.getValue(ControlCapture.ACTIVE_CONTROL_TICKS).ifPresent(value -> value.put(WALK, value.get().get(WALK) + 1));
        }
    }
}
