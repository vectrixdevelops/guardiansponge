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
package io.github.connorhartley.guardian.context.listener;

import io.github.connorhartley.guardian.context.Context;
import org.spongepowered.api.event.cause.Cause;
import io.github.connorhartley.guardian.sequence.action.Action;

import java.util.ArrayList;
import java.util.List;

/**
 * Context Listener
 *
 * Represents a group of contexts to listen for and forward
 * the appropriate data to the listening {@link Action}.
 */
public class ContextListener {

    private final List<Class<? extends Context>> listeners = new ArrayList<>();

    private ContextListener(Builder builder) {
        this.listeners.addAll(builder.contexts);
    }

    public static Builder builder() {
        return new Builder();
    }

    public List<Class<? extends Context>> getListeners() {
        return this.listeners;
    }

    public static class Builder {

        private List<Class<? extends Context>> contexts = new ArrayList<>();

        public Builder() {}

        public Builder of(ContextListener contextListener) {
            this.contexts.addAll(contextListener.listeners);
            return this;
        }

        public Builder listen(Class<? extends Context> clazz) {
            this.contexts.add(clazz);
            return this;
        }

        public ContextListener build() {
            return new ContextListener(this);
        }

    }

}
