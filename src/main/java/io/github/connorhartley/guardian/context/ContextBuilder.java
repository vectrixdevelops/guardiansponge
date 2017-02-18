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
public class ContextBuilder {

    private final List<String> contexts;

    private ContextBuilder(Builder builder) {
        this.contexts = builder.contexts;
    }

    public static Builder builder() {
        return new Builder();
    }

    public List<String> getContexts() {
        return this.contexts;
    }

    public static class Builder {

        private List<String> contexts = new ArrayList<>();

        public Builder() {}

        public Builder of(ContextBuilder contextBuilder) {
            this.contexts = contextBuilder.contexts;
            return this;
        }

        public Builder append(String id) {
            this.contexts.add(id);
            return this;
        }

        public ContextBuilder build() {
            return new ContextBuilder(this);
        }

    }

}
