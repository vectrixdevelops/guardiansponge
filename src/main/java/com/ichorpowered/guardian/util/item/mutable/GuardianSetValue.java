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

import com.google.common.collect.Sets;
import com.google.common.reflect.TypeToken;
import com.ichorpowered.guardianapi.util.item.ValueBuilder;
import com.ichorpowered.guardianapi.util.item.ValueHolder;
import com.ichorpowered.guardianapi.util.item.key.Key;
import com.ichorpowered.guardianapi.util.item.value.BaseValue;
import com.ichorpowered.guardianapi.util.item.value.mutable.SetValue;

import java.util.Collection;
import java.util.Iterator;
import java.util.Optional;
import java.util.Set;
import java.util.function.Function;
import java.util.function.Predicate;

import javax.annotation.Nonnull;

public class GuardianSetValue<E> implements SetValue<E>, ValueHolder<SetValue> {

    public static <R> GuardianSetValue<R> empty() {
        return new GuardianSetValue<>(null, null, null);
    }

    public static <T> Builder<T> builder(Key<SetValue<T>> key) {
        return new Builder<>(key);
    }

    private final Key<SetValue<E>> key;
    private final Set<E> defaultValue;

    private Set<E> value;

    private GuardianSetValue(Key<SetValue<E>> key, Set<E> defaultValue,
                             final Set<E> value) {
        this.key = key;
        this.defaultValue = defaultValue;
        this.value = value;
    }

    @Override
    public SetValue<E> set(Set<E> value) {
        this.value = value;
        return this;
    }

    @Override
    public SetValue<E> transform(Function<Set<E>, Set<E>> function) {
        this.value = function.apply(this.value);
        return this;
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
    public SetValue<E> add(E element) {
        if (exists()) {
            this.value.add(element);
        }

        return this;
    }

    @Override
    public SetValue<E> addAll(Iterable<E> elements) {
        if (exists()) {
            elements.forEach(this.value::add);
        }

        return this;
    }

    @Override
    public SetValue<E> remove(E element) {
        if (exists()) {
            this.value.remove(element);
        }

        return this;
    }

    @Override
    public SetValue<E> removeAll(Iterable<E> elements) {
        if (exists()) {
            elements.forEach(this.value::remove);
        }

        return this;
    }

    @Override
    public SetValue<E> removeAll(Predicate<E> predicate) {
        if (exists()) {
            this.value.removeIf(predicate);
        }

        return this;
    }

    @Override
    public boolean contains(E element) {
        return exists() ? this.value.contains(element) : this.defaultValue.contains(element);
    }

    @Override
    public boolean containsAll(Collection<E> iterable) {
        return exists() ? iterable.stream().allMatch(this.value::contains) : iterable.stream().allMatch(this.defaultValue::contains);
    }

    @Override
    public SetValue<E> filter(Predicate<? super E> predicate) {
        final Set<E> set = Sets.newHashSet(this.value);
        if (exists()) {
            set.removeIf(e -> !predicate.test(e));
        }

        return new GuardianSetValue<>(this.key, this.defaultValue, this.value);
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
        return false;
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

    @Nonnull
    @Override
    public Iterator<E> iterator() {
        return exists() ? this.value.iterator() : this.defaultValue.iterator();
    }

    @Nonnull
    @Override
    public TypeToken<SetValue> type() {
        return TypeToken.of(SetValue.class);
    }

    public static class Builder<E> implements ValueBuilder<Set<E>, SetValue<E>> {

        private final Key<SetValue<E>> key;
        private Set<E> defaultElement;
        private Set<E> element;

        public Builder(Key<SetValue<E>> key) {
            this.key = key;
        }

        @Override
        public ValueBuilder<Set<E>, SetValue<E>> with(SetValue<E> otherValue) {
            this.defaultElement = otherValue.getDefault();
            this.element = otherValue.get();
            return this;
        }

        @Override
        public ValueBuilder<Set<E>, SetValue<E>> defaultElement(Set<E> element) {
            this.defaultElement = element;
            return this;
        }

        @Override
        public ValueBuilder<Set<E>, SetValue<E>> element(Set<E> element) {
            this.element = element;
            return this;
        }

        @Override
        public SetValue<E> create() {
            return new GuardianSetValue<>(this.key, this.defaultElement, this.element);
        }
    }
}
