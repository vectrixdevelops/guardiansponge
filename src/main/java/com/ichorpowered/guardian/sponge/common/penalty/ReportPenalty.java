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
package com.ichorpowered.guardian.sponge.common.penalty;

import com.google.common.reflect.TypeToken;
import com.ichorpowered.guardian.api.detection.stage.Stage;
import com.ichorpowered.guardian.api.detection.stage.process.Penalty;
import com.ichorpowered.guardian.api.game.GameReference;
import com.ichorpowered.guardian.api.sequence.process.Process;
import com.ichorpowered.guardian.common.detection.stage.type.PenaltyStageImpl;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.text.Text;
import org.spongepowered.api.text.format.TextColors;

public class ReportPenalty implements Penalty {

    @Override
    public boolean test(final @NonNull Process process) {
        final GameReference<Player> gameReference = process.getContext().get("root:player", new TypeToken<GameReference<Player>>() {});
        final Double detectionProbability = process.getContext().get("detection_probability", TypeToken.of(Double.class));
        final String detectionName = process.getContext().get("detection_type", TypeToken.of(String.class));
        if (gameReference == null || detectionName == null || detectionProbability == null) return false;

        final Player target = gameReference.get();

        Sponge.getServer().getOnlinePlayers().forEach(player -> {
            if (!player.hasPermission("guardian.chat.notification")) return;

            player.sendMessage(Text.builder().append(
                    Text.builder("Guardian AntiCheat ").color(TextColors.BLUE).build(),
                    Text.builder("# ").color(TextColors.DARK_GRAY).build(),
                    Text.builder("Caught ").color(TextColors.GRAY).build(),
                    Text.builder(target.getName()).color(TextColors.RED).build(),
                    Text.builder(" triggering the ").color(TextColors.GRAY).build(),
                    Text.builder(detectionName).color(TextColors.RED).build(),
                    Text.builder(" detection, with ").color(TextColors.GRAY).build(),
                    Text.builder(detectionProbability.toString().substring(0, 4)).color(TextColors.RED).build(),
                    Text.builder(" certainty.").color(TextColors.GRAY).build()
            ).build());
        });

        return true;
    }

    @Override
    public @NonNull Class<? extends Stage<?>> getStageType() {
        return PenaltyStageImpl.class;
    }

}
