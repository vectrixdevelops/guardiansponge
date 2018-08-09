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
package com.ichorpowered.guardian.sponge.common.capture;

import static com.ichorpowered.guardian.api.sequence.capture.CaptureValue.asKey;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.reflect.TypeToken;
import com.ichorpowered.guardian.api.Guardian;
import com.ichorpowered.guardian.api.game.GameReference;
import com.ichorpowered.guardian.api.game.model.Model;
import com.ichorpowered.guardian.api.game.model.value.key.GameKeys;
import com.ichorpowered.guardian.api.sequence.capture.CaptureValue;
import com.ichorpowered.guardian.api.sequence.process.Process;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.spongepowered.api.data.key.Keys;
import org.spongepowered.api.effect.potion.PotionEffect;
import org.spongepowered.api.entity.living.player.Player;

import java.util.List;
import java.util.Map;

public class EffectCapture implements CaptureValue {

    public static String HORIZONTAL_DISTANCE = asKey(EffectCapture.class, "horizontal_distance");
    public static String VERTICAL_DISTANCE = asKey(EffectCapture.class, "vertical_distance");

    @Override
    public void apply(final @NonNull Process process) {
        final GameReference<Player> gameReference = process.getContext().get("root:player", new TypeToken<GameReference<Player>>() {});
        final Model playerModel = Guardian.getModelRegistry().get(gameReference).orElse(null);
        if (gameReference == null || playerModel == null) return;

        final Player player = gameReference.get();

        // Model Values
        final Map<String, Double> horizontal = playerModel.requestFirst(GameKeys.EFFECT_HORIZONTAL_DISTANCE).map(value -> value.get()).orElse(Maps.newHashMap());
        final Map<String, Double> vertical = playerModel.requestFirst(GameKeys.EFFECT_VERTICAL_DISTANCE).map(value -> value.get()).orElse(Maps.newHashMap());

        // Capture Context

        process.getContext().setOnce(EffectCapture.HORIZONTAL_DISTANCE, TypeToken.of(Double.class), 1d);
        process.getContext().setOnce(EffectCapture.VERTICAL_DISTANCE, TypeToken.of(Double.class), 1d);

        final List<PotionEffect> potionEffects = player.get(Keys.POTION_EFFECTS).orElse(Lists.newArrayList());

        if (!potionEffects.isEmpty()) {
            for (PotionEffect potionEffect : potionEffects) {
                final String potionName = potionEffect.getType().getName().toLowerCase();

                if (horizontal.containsKey(potionName)) {
                    final double effectValue = horizontal.get(potionName);

                    process.getContext().transform(EffectCapture.HORIZONTAL_DISTANCE, TypeToken.of(Double.class), value -> value + ((potionEffect.getAmplifier() + 1) * effectValue));
                }

                if (vertical.containsKey(potionName)) {
                    final double effectValue = vertical.get(potionName);

                    process.getContext().transform(EffectCapture.VERTICAL_DISTANCE, TypeToken.of(Double.class), value -> value + ((potionEffect.getAmplifier() + 1) * effectValue));
                }
            }
        }
    }

}
