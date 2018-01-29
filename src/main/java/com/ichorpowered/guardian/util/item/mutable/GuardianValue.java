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
package com.ichorpowered.guardian.util.item.mutable;

import com.google.common.reflect.TypeToken;
import com.ichorpowered.guardianapi.util.item.ValueBuilder;
import com.ichorpowered.guardianapi.util.item.ValueHolder;
import com.ichorpowered.guardianapi.util.item.key.Key;
import com.ichorpowered.guardianapi.util.item.value.BaseValue;
import com.ichorpowered.guardianapi.util.item.value.mutable.Value;

import java.util.Optional;
import java.util.function.Function;

import javax.annotation.Nonnull;

public class GuardianValue<E> implements Value<E>, ValueHolder<Value> {

    public static <U> GuardianValue<U> empty() {
        return new GuardianValue<>(null, null, null);
    }

    public static <T> Builder<T> builder(Key<Value<T>> key) {
        return new Builder<>(key);
    }

    private final Key<? extends BaseValue<E>> key;
    private final E defaultValue;

    private E value;

    GuardianValue(final Key<? extends BaseValue<E>> key, final E defaultValue,
                  final E value) {
        this.key = key;
        this.defaultValue = defaultValue;
        this.value = value;
    }

    @Override
    public Value<E> set(E value) {
        this.value = value;
        return this;
    }

    @Override
    public Value<E> transform(Function<E, E> function) {
        this.value = function.apply(exists() ? this.value : this.defaultValue);
        return this;
    }

    @Override
    public E get() {
        return exists() ? this.value : this.defaultValue;
    }

    @Override
    public boolean exists() {
        return this.value != null;
    }

    @Override
    public boolean isImmutable() {
        return false;
    }

    @Override
    public E getDefault() {
        return this.defaultValue;
    }

    @Override
    public Optional<E> getDirect() {
        return Optional.ofNullable(this.value);
    }

    @Override
    public Key<? extends BaseValue<E>> getKey() {
        return this.key;
    }

    @Nonnull
    @Override
    public TypeToken<Value> type() {
        return TypeToken.of(Value.class);
    }

    public static class Builder<E> implements ValueBuilder<E, Value<E>> {

        private final Key<Value<E>> key;
        private E defaultElement;
        private E element;

        public Builder(Key<Value<E>> key) {
            this.key = key;
        }

        @Override
        public ValueBuilder<E, Value<E>> with(Value<E> otherValue) {
            this.defaultElement = otherValue.getDefault();
            this.element = otherValue.get();
            return this;
        }

        @Override
        public ValueBuilder<E, Value<E>> defaultElement(E element) {
            this.defaultElement = element;
            return this;
        }

        @Override
        public ValueBuilder<E, Value<E>> element(E element) {
            this.element = element;
            return this;
        }

        @Override
        public Value<E> create() {
            return new GuardianValue<>(this.key, this.defaultElement, this.element);
        }
    }
}
