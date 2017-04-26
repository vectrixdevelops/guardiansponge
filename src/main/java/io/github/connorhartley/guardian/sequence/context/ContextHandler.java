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
package io.github.connorhartley.guardian.sequence.context;

import org.spongepowered.api.entity.living.player.Player;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;

public class ContextHandler {

    private final Player player;
    private final List<Context> contexts = new ArrayList<>();

    private ContextValuation contextValuation;

    public ContextHandler(Player player) {
        this.player = player;
    }

    public ContextHandler(Player player, Context... contexts) {
        this.player = player;
        this.contexts.addAll(Arrays.asList(contexts));
    }

    public void addContext(Context context) {
        this.contexts.add(context);
    }

    public List<Context> getContexts() {
        return this.contexts;
    }

    public void setValuation(ContextValuation contextValuation) {
        this.contextValuation = contextValuation;
    }

    public ContextValuation getValuation() {
        return this.contextValuation;
    }

    public ContextValuation start() {
        Iterator<Context> iterator = this.contexts.iterator();

        while(iterator.hasNext()) {
            Context context = iterator.next();

            if (!context.hasStopped()) {
                context.setPlayer(this.player);
                context.start(this.contextValuation);
                this.contextValuation = context.getValuation();
            }
        }

        return this.contextValuation;
    }

    public ContextValuation update() {
        Iterator<Context> iterator = this.contexts.iterator();

        while(iterator.hasNext()) {
            Context context = iterator.next();

            if (!context.hasStopped()) {
                context.update(this.contextValuation);
                this.contextValuation = context.getValuation();
            }
        }

        return this.contextValuation;
    }

    public void stop() {
        for (Context context : this.contexts) {
            if (!context.hasStopped()) {
                context.stop(this.contextValuation);
                this.contextValuation = context.getValuation();
            }
        }
    }

}
