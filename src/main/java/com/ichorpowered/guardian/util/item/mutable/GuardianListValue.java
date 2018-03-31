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

import com.google.common.collect.Lists;
import com.google.common.reflect.TypeToken;
import com.ichorpowered.guardianapi.util.item.ValueBuilder;
import com.ichorpowered.guardianapi.util.item.ValueHolder;
import com.ichorpowered.guardianapi.util.item.key.Key;
import com.ichorpowered.guardianapi.util.item.value.BaseValue;
import com.ichorpowered.guardianapi.util.item.value.mutable.ListValue;

import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.Optional;
import java.util.function.Function;
import java.util.function.Predicate;

import javax.annotation.Nonnull;

public class GuardianListValue<E> implements ListValue<E>, ValueHolder<ListValue> {

    public static <R> GuardianListValue<R> empty() {
        return new GuardianListValue<>(null, null, null);
    }

    public static <T> Builder<T> builder(final Key<ListValue<T>> key) {
        return new Builder<>(key);
    }

    private final Key<ListValue<E>> key;
    private final List<E> defaultValue;

    private List<E> value;

    private GuardianListValue(final Key<ListValue<E>> key, final List<E> defaultValue,
                              final List<E> value) {
        this.key = key;
        this.defaultValue = defaultValue;
        this.value = value;
    }

    @Override
    public E get(final int index) {
        return this.exists() ? this.value.get(index) : this.defaultValue.get(index);
    }

    @Override
    public ListValue<E> add(final int index, final E value) {
        if (this.exists()) {
            this.value.add(index, value);
        }

        return this;
    }

    @Override
    public ListValue<E> add(int index, final Iterable<E> values) {
        if (this.exists()) {
            final int i = index++;
            values.forEach(e -> this.value.add(i, e));
        }

        return this;
    }

    @Override
    public ListValue<E> remove(final int index) {
        if (this.exists()) {
            this.value.remove(index);
        }

        return this;
    }

    @Override
    public ListValue<E> set(final int index, final E element) {
        if (this.exists()) {
            this.value.set(index, element);
        }

        return this;
    }

    @Override
    public int indexOf(final E element) {
        return this.exists() ? this.value.indexOf(element) : this.defaultValue.indexOf(element);
    }

    @Override
    public ListValue<E> set(final List<E> value) {
        this.value = value;
        return this;
    }

    @Override
    public ListValue<E> transform(final Function<List<E>, List<E>> function) {
        this.value = function.apply(this.value);
        return this;
    }

    @Override
    public int size() {
        return this.exists() ? this.value.size() : this.defaultValue.size();
    }

    @Override
    public boolean isEmpty() {
        return this.exists() ? this.value.isEmpty() : this.defaultValue.isEmpty();
    }

    @Override
    public ListValue<E> add(final E element) {
        if (this.exists()) {
            this.value.add(element);
        }

        return this;
    }

    @Override
    public ListValue<E> addAll(final Iterable<E> elements) {
        if (this.exists()) {
            elements.forEach(this.value::add);
        }

        return this;
    }

    @Override
    public ListValue<E> remove(final E element) {
        if (this.exists()) {
            this.value.remove(element);
        }
        return this;
    }

    @Override
    public ListValue<E> removeAll(final Iterable<E> elements) {
        if (this.exists()) {
            elements.forEach(this.value::remove);
        }

        return this;
    }

    @Override
    public ListValue<E> removeAll(final Predicate<E> predicate) {
        if (this.exists()) {
            this.value.removeIf(predicate);
        }

        return this;
    }

    @Override
    public boolean contains(final E element) {
        return this.exists() ? this.value.contains(element) : this.defaultValue.contains(element);
    }

    @Override
    public boolean containsAll(final Collection<E> iterable) {
        return this.exists() ? this.value.containsAll(iterable) : this.defaultValue.containsAll(iterable);
    }

    @Override
    public ListValue<E> filter(final Predicate<? super E> predicate) {
        final List<E> list = Lists.newArrayList(this.value);
        if (this.exists()) {
            list.removeIf(e -> !predicate.test(e));
        }

        return new GuardianListValue<>(this.key, this.defaultValue, list);
    }

    @Override
    public List<E> getAll() {
        return this.exists() ? Lists.newArrayList(this.value) : Lists.newArrayList(this.defaultValue);
    }

    @Override
    public List<E> get() {
        return this.exists() ? this.value : this.defaultValue;
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
    public List<E> getDefault() {
        return this.defaultValue;
    }

    @Override
    public Optional<List<E>> getDirect() {
        return Optional.ofNullable(this.value);
    }

    @Override
    public Key<? extends BaseValue<List<E>>> getKey() {
        return this.key;
    }

    @Nonnull
    @Override
    public Iterator<E> iterator() {
        return this.exists() ? this.value.iterator() : this.defaultValue.iterator();
    }

    @Nonnull
    @Override
    public TypeToken<ListValue> type() {
        return TypeToken.of(ListValue.class);
    }

    public static class Builder<E> implements ValueBuilder<List<E>, ListValue<E>> {

        private final Key<ListValue<E>> key;
        private List<E> defaultElement;
        private List<E> element;

        public Builder(final Key<ListValue<E>> key) {
            this.key = key;
        }

        @Override
        public ValueBuilder<List<E>, ListValue<E>> with(final ListValue<E> otherValue) {
            this.defaultElement = otherValue.getDefault();
            this.element = otherValue.get();
            return this;
        }

        @Override
        public ValueBuilder<List<E>, ListValue<E>> defaultElement(final List<E> element) {
            this.defaultElement = element;
            return this;
        }

        @Override
        public ValueBuilder<List<E>, ListValue<E>> element(final List<E> element) {
            this.element = element;
            return this;
        }

        @Override
        public ListValue<E> create() {
            return new GuardianListValue<>(this.key, this.defaultElement, this.element);
        }
    }
}
