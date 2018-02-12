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

import com.google.common.collect.Sets;
import com.google.common.reflect.TypeToken;
import com.ichorpowered.guardian.sequence.capture.AbstractCapture;
import com.ichorpowered.guardian.sequence.capture.GuardianCaptureKey;
import com.ichorpowered.guardian.util.item.mutable.GuardianSetValue;
import com.ichorpowered.guardianapi.detection.Detection;
import com.ichorpowered.guardianapi.detection.capture.CaptureContainer;
import com.ichorpowered.guardianapi.detection.capture.CaptureKey;
import com.ichorpowered.guardianapi.entry.entity.PlayerEntry;
import com.ichorpowered.guardianapi.util.item.value.mutable.SetValue;
import org.spongepowered.api.data.key.Keys;
import org.spongepowered.api.entity.living.player.Player;

import java.util.Arrays;
import java.util.Set;

import javax.annotation.Nonnull;

public class InvalidControlCapture extends AbstractCapture {

    private static final String CLASS_NAME = InvalidControlCapture.class.getSimpleName().toUpperCase();

    public static final CaptureKey<SetValue<String>> INVALID_CONTROLS = GuardianCaptureKey.<SetValue<String>>builder()
            .id(CLASS_NAME + ":invalidControls")
            .name("InvalidControls")
            .type(GuardianSetValue.empty(), TypeToken.of(String.class))
            .build();

    public InvalidControlCapture(@Nonnull Object plugin, @Nonnull Detection detection) {
        super(plugin, detection);
    }

    @Override
    public void update(@Nonnull PlayerEntry entry, @Nonnull CaptureContainer captureContainer) {
        if (!entry.getEntity(Player.class).isPresent()) return;
        Player player = entry.getEntity(Player.class).get();

        final Set<String> controls = captureContainer.get(InvalidControlCapture.INVALID_CONTROLS)
                .orElse(Sets.newHashSet());

        if (player.get(Keys.IS_SNEAKING).isPresent() && player.get(Keys.IS_SNEAKING).get()) {
            if (player.get(Keys.IS_SPRINTING).isPresent() && player.get(Keys.IS_SPRINTING).get()) {
                if (!controls.contains("sneaking") || !controls.contains("sprinting")) {
                    controls.addAll(Arrays.asList("sneaking", "sprinting"));
                }
            }
        } else if (player.get(Keys.IS_SLEEPING).isPresent() && player.get(Keys.IS_SLEEPING).get() ||
                player.get(Keys.VEHICLE).isPresent()) {
            if (player.get(Keys.IS_SPRINTING).isPresent() && player.get(Keys.IS_SPRINTING).get()) {
                if (!controls.contains("sitting") || !controls.contains("sprinting")) {
                    controls.addAll(Arrays.asList("sitting", "sprinting"));
                }
            } else if (player.get(Keys.IS_FLYING).isPresent() && player.get(Keys.IS_FLYING).get()) {
                if (!controls.contains("sitting") || !controls.contains("flying")) {
                    controls.addAll(Arrays.asList("sitting", "flying"));
                }
            }
        }

        captureContainer.offer(GuardianSetValue.builder(InvalidControlCapture.INVALID_CONTROLS)
                .defaultElement(Sets.newHashSet())
                .element(controls)
                .create());
    }
}
