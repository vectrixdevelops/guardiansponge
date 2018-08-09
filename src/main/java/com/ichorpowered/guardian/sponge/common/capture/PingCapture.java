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

import com.google.common.reflect.TypeToken;
import com.ichorpowered.guardian.api.Guardian;
import com.ichorpowered.guardian.api.game.GameReference;
import com.ichorpowered.guardian.api.game.model.Model;
import com.ichorpowered.guardian.api.game.model.value.key.GameKeys;
import com.ichorpowered.guardian.api.sequence.capture.CaptureValue;
import com.ichorpowered.guardian.api.sequence.process.Process;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.spongepowered.api.entity.living.player.Player;

public class PingCapture implements CaptureValue {

    @Override
    public void apply(@NonNull Process process) {
        final GameReference<Player> gameReference = process.getContext().get("root:player", new TypeToken<GameReference<Player>>() {});
        final Model playerModel = Guardian.getModelRegistry().get(gameReference).orElse(null);
        if (gameReference == null || playerModel == null) return;

        final Player player = gameReference.get();

        // Model Values

        final int joinPing = playerModel.requestFirst("model-geometry", GameKeys.JOIN_PING).map(value -> value.get()).orElse(0);
        final double averagePing = playerModel.requestFirst("model-geometry", GameKeys.AVERAGE_PING).map(value -> value.get()).orElse(0d);

        // Capture Context

        final int ping = player.getConnection().getLatency();

        if (joinPing > ping) {
            playerModel.getComponent("model-geometry").ifPresent(component -> component.set(GameKeys.AVERAGE_PING, (double) joinPing / ping));
        } else {
            playerModel.getComponent("model-geometry").ifPresent(component -> component.set(GameKeys.AVERAGE_PING, joinPing * averagePing));
        }
    }

}
