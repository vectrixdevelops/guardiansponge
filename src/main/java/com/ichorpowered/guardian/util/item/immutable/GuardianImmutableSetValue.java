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

import com.google.common.collect.Sets;
import com.ichorpowered.guardianapi.util.item.key.Key;
import com.ichorpowered.guardianapi.util.item.value.BaseValue;
import com.ichorpowered.guardianapi.util.item.value.immutable.ImmutableSetValue;

import java.util.Optional;
import java.util.Set;
import java.util.function.Function;
import java.util.function.Predicate;

public class GuardianImmutableSetValue<E> implements ImmutableSetValue<E> {

    public static <R> GuardianImmutableSetValue<R> of(Key<? extends BaseValue<Set<R>>> key, final Set<R> defaultValue) {
        return new GuardianImmutableSetValue<>(key, defaultValue);
    }

    public static <R> GuardianImmutableSetValue<R> of(final Key<? extends BaseValue<Set<R>>> key, final Set<R> defaultValue,
                                                      final Set<R> value) {
        return new GuardianImmutableSetValue<>(key, defaultValue, value);
    }

    private final Key<? extends BaseValue<Set<E>>> key;
    private final Set<E> defaultValue;

    private Set<E> value;

    private GuardianImmutableSetValue(Key<? extends BaseValue<Set<E>>> key, Set<E> defaultValue) {
        this(key, defaultValue, null);
    }

    private GuardianImmutableSetValue(Key<? extends BaseValue<Set<E>>> key, Set<E> defaultValue,
                                      final Set<E> value) {
        this.key = key;
        this.defaultValue = defaultValue;
        this.value = value;
    }

    @Override
    public int size() {
        return exists() ? this.value.size() : this.defaultValue.size();
    }

    @Override
    public boolean isEmpty() {
        return exists() ? this.value.isEmpty() : this.defaultValue.isEmpty();
    }

    @Override
    public ImmutableSetValue<E> with(Set<E> collection) {
        final Set<E> value;
        if (exists()) {
            value = Sets.newHashSet(this.value);
        } else {
            value = Sets.newHashSet(this.defaultValue);
        }

        value.addAll(collection);
        return GuardianImmutableSetValue.of(this.key, this.defaultValue, value);
    }

    @Override
    public ImmutableSetValue<E> withElement(E elements) {
        final Set<E> value;
        if (exists()) {
            value = Sets.newHashSet(this.value);
        } else {
            value = Sets.newHashSet(this.defaultValue);
        }

        value.add(elements);
        return GuardianImmutableSetValue.of(this.key, this.defaultValue, value);
    }

    @Override
    public ImmutableSetValue<E> transform(Function<Set<E>, Set<E>> function) {
        return null;
    }

    @Override
    public ImmutableSetValue<E> withAll(Iterable<E> elements) {
        final Set<E> value;
        if (exists()) {
            value = Sets.newHashSet(this.value);
        } else {
            value = Sets.newHashSet(this.defaultValue);
        }

        elements.forEach(value::add);
        return GuardianImmutableSetValue.of(this.key, this.defaultValue, value);
    }

    @Override
    public ImmutableSetValue<E> without(E element) {
        final Set<E> value;
        if (exists()) {
            value = Sets.newHashSet(this.value);
        } else {
            value = Sets.newHashSet(this.defaultValue);
        }

        value.remove(element);
        return GuardianImmutableSetValue.of(this.key, this.defaultValue, value);
    }

    @Override
    public ImmutableSetValue<E> withoutAll(Iterable<E> elements) {
        final Set<E> value;
        if (exists()) {
            value = Sets.newHashSet(this.value);
        } else {
            value = Sets.newHashSet(this.defaultValue);
        }

        elements.forEach(value::remove);
        return GuardianImmutableSetValue.of(this.key, this.defaultValue, value);
    }

    @Override
    public ImmutableSetValue<E> withoutAll(Predicate<E> predicate) {
        final Set<E> value;
        if (exists()) {
            value = Sets.newHashSet(this.value);
        } else {
            value = Sets.newHashSet(this.defaultValue);
        }

        value.removeIf(predicate);
        return GuardianImmutableSetValue.of(this.key, this.defaultValue, value);
    }

    @Override
    public boolean contains(E element) {
        return exists() ? this.value.contains(element) : this.defaultValue.contains(element);
    }

    @Override
    public boolean containsAll(Iterable<E> iterable) {
        final Set<E> compare = Sets.newHashSet();
        iterable.forEach(compare::add);

        return exists() ? compare.stream().allMatch(this.value::contains)
                : compare.stream().allMatch(this.defaultValue::contains);
    }

    @Override
    public Set<E> getAll() {
        return exists() ? Sets.newHashSet(this.value) : Sets.newHashSet(this.defaultValue);
    }

    @Override
    public Set<E> get() {
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
    public Set<E> getDefault() {
        return this.defaultValue;
    }

    @Override
    public Optional<Set<E>> getDirect() {
        return Optional.ofNullable(this.value);
    }

    @Override
    public Key<? extends BaseValue<Set<E>>> getKey() {
        return this.key;
    }
}
