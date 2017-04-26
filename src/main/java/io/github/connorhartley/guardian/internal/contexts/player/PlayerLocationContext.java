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
import io.github.connorhartley.guardian.sequence.context.Context;
import io.github.connorhartley.guardian.sequence.context.ContextValuation;
import io.github.connorhartley.guardian.detection.Detection;

public class PlayerLocationContext extends Context {

    private ContextValuation valuation;
    private boolean stopped = false;

    public PlayerLocationContext(Guardian plugin, Detection detection) {
        super(plugin, detection);
    }

    @Override
    public ContextValuation getValuation() {
        return this.valuation;
    }

    @Override
    public void start(ContextValuation valuation) {
        this.valuation = valuation;

        this.getValuation().set(PlayerLocationContext.class, "start_location", this.getPlayer().getLocation());
        this.getValuation().set(PlayerLocationContext.class, "update", 0);
    }

    @Override
    public void update(ContextValuation valuation) {
        this.valuation = valuation;

        this.getValuation().set(PlayerLocationContext.class, "present_location", this.getPlayer().getLocation());

        this.getValuation().<PlayerLocationContext, Integer>transform(PlayerLocationContext.class, "update", oldValue -> oldValue + 1);
    }

    @Override
    public void stop(ContextValuation valuation) {
        this.valuation = valuation;

        this.stopped = true;
    }

    @Override
    public boolean hasStopped() {
        return this.stopped;
    }
}
