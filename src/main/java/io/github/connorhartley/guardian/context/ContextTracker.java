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

import org.spongepowered.api.event.cause.Cause;
import io.github.connorhartley.guardian.context.type.ActionContext;
import io.github.connorhartley.guardian.sequence.action.Action;

import java.util.ArrayList;
import java.util.List;

/**
 * Context Tracker
 *
 * Provides a way to represent a group of {@link Cause}s that
 * have been analysed by the added contexts to ensure validity in
 * {@link Action}s and represent how it happened.
 */
public class ContextTracker<T> {

    private final List<Class<T>> actionContexts;

    private ContextTracker(Builder builder) {
        this.actionContexts = builder.actionContexts;
    }

    public static Builder builder() {
        return new Builder();
    }

    public List<Class<T>> getContexts() {
        return this.actionContexts;
    }

    public static class Builder<T> {

        private List<Class<T>> actionContexts = new ArrayList<>();

        public Builder() {}

        public Builder of(ContextTracker contextTracker) {
            this.actionContexts = contextTracker.actionContexts;
            return this;
        }

        public Builder append(Class<T> context) {
            this.actionContexts.add(context);
            return this;
        }

        public ContextTracker build() {
            return new ContextTracker(this);
        }

    }

}
