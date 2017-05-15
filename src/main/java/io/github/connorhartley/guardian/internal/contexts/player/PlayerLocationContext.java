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
import io.github.connorhartley.guardian.detection.Detection;
import io.github.connorhartley.guardian.sequence.capture.CaptureContainer;
import io.github.connorhartley.guardian.sequence.capture.CaptureContext;
import org.spongepowered.api.entity.living.player.Player;

public class PlayerLocationContext extends CaptureContext {

    public PlayerLocationContext(Guardian plugin, Detection detection) {
        super(plugin, detection);
    }

    @Override
    public CaptureContainer start(Player player, CaptureContainer valuation) {
        valuation.set(PlayerLocationContext.class, "start_location", player.getLocation());
        valuation.set(PlayerLocationContext.class, "update", 0);

        return valuation;
    }

    @Override
    public CaptureContainer update(Player player, CaptureContainer valuation) {
        valuation.set(PlayerLocationContext.class, "present_location", player.getLocation());
        valuation.<PlayerLocationContext, Integer>transform(PlayerLocationContext.class, "update", oldValue -> oldValue + 1);

        return valuation;
    }

    @Override
    public CaptureContainer stop(Player player, CaptureContainer valuation) {
        return valuation;
    }
}
