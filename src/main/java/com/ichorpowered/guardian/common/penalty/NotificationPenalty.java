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
package com.ichorpowered.guardian.common.penalty;

import com.ichorpowered.guardian.api.detection.DetectionConfiguration;
import com.ichorpowered.guardian.api.detection.penalty.Penalty;
import com.ichorpowered.guardian.api.detection.penalty.PenaltyPredicate;
import com.me4502.modularframework.module.Module;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.text.Text;
import org.spongepowered.api.text.format.TextColors;

import javax.annotation.Nonnull;

public class NotificationPenalty implements Penalty {

    @Nonnull
    @Override
    public String getId() {
        return "notification";
    }

    @Nonnull
    @Override
    public <E, F extends DetectionConfiguration> PenaltyPredicate<E, F> getPredicate() {
        return (entityEntry, detection, summary) -> {
            Player reported = entityEntry.getEntity(Player.class).get();

            Sponge.getServer().getOnlinePlayers().forEach(player -> {
                if (!player.hasPermission("guardian.report.notifier")) return;

                String detectionName = detection.getClass().getAnnotation(Module.class).name();

                player.sendMessage(
                        Text.builder()
                                .color(TextColors.BLUE).append(Text.of("Guardian AntiCheat » "))
                                .color(TextColors.GRAY).append(Text.of("The player "))
                                .color(TextColors.DARK_AQUA).append(Text.of(reported.getName()))
                                .color(TextColors.GRAY).append(Text.of(" has been flagged by the "))
                                .color(TextColors.DARK_AQUA).append(Text.of(detectionName))
                                .color(TextColors.GRAY).append(Text.of(" module."))
                        .build()
                );
            });

            return true;
        };
    }

}
