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
package com.ichorpowered.guardian.sponge.sequence;

import com.google.common.collect.Maps;
import com.google.common.reflect.TypeToken;
import com.ichorpowered.guardian.api.sequence.SequenceContext;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.checker.nullness.qual.Nullable;

import java.util.Map;
import java.util.function.Function;

public class SequenceContextImpl implements SequenceContext {

    private Map<String, Object> container = Maps.newHashMap();

    public SequenceContextImpl() {}

    @SuppressWarnings("unchecked")
    @Override
    public <T> @NonNull T set(final @NonNull String key, final @NonNull TypeToken<T> type,
                              final @NonNull T element) {
        this.container.put(key, element);
        return element;
    }

    @Override
    public <T> @NonNull T setOnce(final @NonNull String key, final @NonNull TypeToken<T> type,
                                  final @NonNull T element) {
        if (!this.container.containsKey(key)) {
            this.container.put(key, element);
            return element;
        }

        return (T) this.container.get(key);
    }

    @Override
    public @NonNull <T> SequenceContext add(final @NonNull String key, final @NonNull TypeToken<T> type,
                                            final @NonNull T element) {
        this.container.put(key, element);
        return this;
    }

    @Override
    public @NonNull <T> SequenceContext transform(final @NonNull String key, final @NonNull TypeToken<T> type,
                                                  final @NonNull Function<T, T> function) {
        this.container.put(key, function.apply((T) this.container.get(key)));
        return this;
    }

    @Override
    public @NonNull SequenceContext from(final @NonNull SequenceContext sequenceContext) {
        this.container.putAll(sequenceContext.getAll());
        return this;
    }

    @Override
    public <T> @Nullable T get(final @NonNull String key, final @NonNull TypeToken<T> type) {
        return (T) this.container.get(key);
    }

    @Override
    public @NonNull Map<String, Object> getAll() {
        return Maps.newHashMap(this.container);
    }

    @Override
    public <T> @NonNull T remove(final @NonNull String key, final @NonNull TypeToken<T> type) {
        return (T) this.container.remove(key);
    }

}
