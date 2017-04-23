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
import org.spongepowered.api.entity.living.player.Player;

public class PlayerLocationContext extends Context {

    private boolean suspended = false;

    public PlayerLocationContext(Guardian plugin, Detection detection, ContextValuation contextValuation, Player player) {
        super(plugin, detection, contextValuation, player);

        this.getContextValuation().set(PlayerLocationContext.class, "start_location", this.getPlayer().getLocation());
    }

    @Override
    public void update() {
        this.getContextValuation().set(PlayerLocationContext.class, "present_location", this.getPlayer().getLocation());

        this.getContextValuation().<PlayerLocationContext, Integer>transform(PlayerLocationContext.class, "update", oldValue -> oldValue + 1);
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
