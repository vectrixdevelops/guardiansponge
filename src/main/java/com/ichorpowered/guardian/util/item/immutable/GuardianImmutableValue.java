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
package com.ichorpowered.guardian.util.item.immutable;

import com.ichorpowered.guardianapi.util.item.key.Key;
import com.ichorpowered.guardianapi.util.item.value.BaseValue;
import com.ichorpowered.guardianapi.util.item.value.immutable.ImmutableValue;

import java.util.Optional;
import java.util.function.Function;

public class GuardianImmutableValue<E> implements ImmutableValue<E> {

    public static <U> GuardianImmutableValue<U> of(Key<? extends BaseValue<U>> key, final U defaultValue) {
        return new GuardianImmutableValue<>(key, defaultValue);
    }

    public static <U> GuardianImmutableValue<U> of(final Key<? extends BaseValue<U>> key, final U defaultValue,
                                                   final U value) {
        return new GuardianImmutableValue<>(key, defaultValue, value);
    }

    private final Key<? extends BaseValue<E>> key;
    private final E defaultValue;

    private E value;

    private GuardianImmutableValue(final Key<? extends BaseValue<E>> key, final E defaultValue) {
        this(key, defaultValue, null);
    }

    private GuardianImmutableValue(final Key<? extends BaseValue<E>> key, final E defaultValue,
                                   final E value) {
        this.key = key;
        this.defaultValue = defaultValue;
        this.value = value;
    }

    @Override
    public ImmutableValue<E> with(E value) {
        return GuardianImmutableValue.of(this.key, this.defaultValue, value);
    }

    @Override
    public ImmutableValue<E> transform(Function<E, E> function) {
        return GuardianImmutableValue.of(this.key, this.defaultValue, function.apply(this.value));
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
        return true;
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
}
