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
package io.github.connorhartley.guardian.context;

import io.github.connorhartley.guardian.Guardian;
import io.github.connorhartley.guardian.context.valuation.ContextValuation;
import io.github.connorhartley.guardian.detection.Detection;
import org.spongepowered.api.entity.living.player.Player;

public abstract class Context {

    private final Guardian plugin;
    private final Detection detection;
    private final ContextValuation contextValuation;
    private final Player player;

    public Context(Guardian plugin, Detection detection, ContextValuation contextValuation, Player player) {
        this.plugin = plugin;
        this.detection = detection;
        this.contextValuation = contextValuation;
        this.player = player;

        this.contextValuation.addContext(this);
    }

    public Guardian getPlugin() {
        return this.plugin;
    }

    public Detection getDetection() {
        return this.detection;
    }

    public ContextValuation getContextValuation() {
        return this.contextValuation;
    }

    public Player getPlayer() {
        return this.player;
    }

    public abstract void update();

    public abstract void suspend();

    public abstract boolean isSuspended();

}
