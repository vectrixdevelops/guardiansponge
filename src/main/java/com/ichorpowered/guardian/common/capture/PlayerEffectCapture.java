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

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.ichorpowered.guardian.content.transaction.GuardianSingleValue;
import com.ichorpowered.guardian.sequence.GuardianSequence;
import com.ichorpowered.guardian.sequence.capture.AbstractCapture;
import com.ichorpowered.guardianapi.content.ContentKeys;
import com.ichorpowered.guardianapi.detection.Detection;
import com.ichorpowered.guardianapi.detection.capture.CaptureContainer;
import com.ichorpowered.guardianapi.entry.entity.PlayerEntry;
import com.ichorpowered.guardianapi.util.key.NamedTypeKey;
import org.spongepowered.api.data.key.Keys;
import org.spongepowered.api.effect.potion.PotionEffect;
import org.spongepowered.api.entity.living.player.Player;

import java.util.List;
import java.util.Map;

import javax.annotation.Nonnull;

public class PlayerEffectCapture extends AbstractCapture {

    private static final String CLASS_NAME = PlayerEffectCapture.class.getSimpleName().toUpperCase();

    public static NamedTypeKey<Double> SPEED_AMPLIFIER =
            NamedTypeKey.of(CLASS_NAME + "_SPEED_AMPLIFIER", Double.class);

    public static NamedTypeKey<Double> LIFT_AMPLIFIER =
            NamedTypeKey.of(CLASS_NAME + "_LIFT_AMPLIFIER", Double.class);

    private Map<String, Double> effectSpeed;
    private Map<String, Double> effectLift;

    public PlayerEffectCapture(@Nonnull Object plugin, @Nonnull Detection detection) {
        super(plugin, detection);

        // Movement

        this.effectSpeed = (Map<String, Double>) this.getDetection().getContentContainer().get(ContentKeys.MOVEMENT_EFFECT_SPEED)
                .orElse(GuardianSingleValue.empty()).getElement().orElse(Maps.newHashMap());

        // Lift

        this.effectLift = (Map<String, Double>) this.getDetection().getContentContainer().get(ContentKeys.MOVEMENT_EFFECT_LIFT)
                .orElse(GuardianSingleValue.empty()).getElement().orElse(Maps.newHashMap());
    }

    @Override
    public void update(@Nonnull PlayerEntry entry, @Nonnull CaptureContainer captureContainer) {
        if (!entry.getEntity(Player.class).isPresent() || !captureContainer.get(GuardianSequence.INITIAL_LOCATION).isPresent()) return;
        final Player player = entry.getEntity(Player.class).get();

        final List<PotionEffect> potionEffects = player.get(Keys.POTION_EFFECTS).orElse(Lists.newArrayList());

        if (!potionEffects.isEmpty()) {
            for (PotionEffect potionEffect : potionEffects) {
                final String potionName = potionEffect.getType().getName().toLowerCase();

                if (this.effectSpeed.containsKey(potionName)) {
                    final double effectValue = this.effectSpeed.get(potionName);

                    captureContainer.transform(PlayerEffectCapture.SPEED_AMPLIFIER,
                            original -> original + ((potionEffect.getAmplifier() + 1) * effectValue),
                            ((potionEffect.getAmplifier() + 1) * effectValue));
                }

                if (this.effectLift.containsKey(potionName)) {
                    final double effectValue = this.effectLift.get(potionName);

                    captureContainer.transform(PlayerEffectCapture.LIFT_AMPLIFIER,
                            original -> original + ((potionEffect.getAmplifier() + 1) * effectValue),
                            ((potionEffect.getAmplifier() + 1) * effectValue));
                }
            }
        }
    }

}
