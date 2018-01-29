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

import com.google.common.collect.ImmutableCollection;
import com.google.common.collect.ImmutableSet;
import com.google.common.reflect.TypeToken;
import com.ichorpowered.guardianapi.util.item.ValueBuilder;
import com.ichorpowered.guardianapi.util.item.ValueHolder;
import com.ichorpowered.guardianapi.util.item.key.Key;
import com.ichorpowered.guardianapi.util.item.value.BaseValue;
import com.ichorpowered.guardianapi.util.item.value.mutable.MapValue;
import com.ichorpowered.guardianapi.util.item.value.mutable.Value;

import java.util.Map;
import java.util.Optional;
import java.util.function.Function;
import java.util.function.Predicate;

import javax.annotation.Nonnull;

public class GuardianMapValue<K, V> implements MapValue<K, V>, ValueHolder<MapValue> {

    public static <R, S> GuardianMapValue<R, S> empty() {
        return new GuardianMapValue<>(null, null, null);
    }

    public static <R, S> Builder<R, S> builder(Key<MapValue<R, S>> key) {
        return new Builder<>(key);
    }

    private final Key<? extends BaseValue<Map<K, V>>> key;
    private final Map<K, V> defaultValue;

    private Map<K, V> value;

    private GuardianMapValue(Key<? extends BaseValue<Map<K, V>>> key, Map<K, V> defaultValue,
                             final Map<K, V> value) {
        this.key = key;
        this.defaultValue = defaultValue;
        this.value = value;
    }

    @Override
    public int size() {
        return exists() ? this.value.size() : this.defaultValue.size();
    }

    @Override
    public MapValue<K, V> put(K key, V value) {
        if (exists()) {
            this.value.put(key, value);
        }

        return this;
    }

    @Override
    public MapValue<K, V> putAll(Map<K, V> map) {
        if (exists()) {
            this.value.putAll(map);
        }

        return this;
    }

    @Override
    public MapValue<K, V> remove(K key) {
        if (exists()) {
            this.value.remove(key);
        }

        return this;
    }

    @Override
    public MapValue<K, V> removeAll(Iterable<K> keys) {
        if (exists()) {
            keys.forEach(this.value::remove);
        }

        return this;
    }

    @Override
    public MapValue<K, V> removeAll(Predicate<Map.Entry<K, V>> predicate) {
        if (exists()) {
            this.value.entrySet().removeIf(predicate);
        }

        return this;
    }

    @Override
    public boolean containsKey(K key) {
        return exists() ? this.value.containsKey(key) : this.defaultValue.containsKey(key);
    }

    @Override
    public boolean containsValue(V value) {
        return exists() ? this.value.containsValue(value) : this.defaultValue.containsValue(value);
    }

    @Override
    public ImmutableSet<K> keySet() {
        return exists() ? ImmutableSet.copyOf(this.value.keySet()) : ImmutableSet.copyOf(this.defaultValue.keySet());
    }

    @Override
    public ImmutableSet<Map.Entry<K, V>> entrySet() {
        return exists() ? ImmutableSet.copyOf(this.value.entrySet()) : ImmutableSet.copyOf(this.defaultValue.entrySet());
    }

    @Override
    public ImmutableCollection<V> values() {
        return exists() ? ImmutableSet.copyOf(this.value.values()) : ImmutableSet.copyOf(this.defaultValue.values());
    }

    @Override
    public Value<Map<K, V>> set(Map<K, V> value) {
        this.value = value;
        return this;
    }

    @Override
    public MapValue<K, V> transform(Function<Map<K, V>, Map<K, V>> function) {
        this.value = function.apply(this.value);
        return this;
    }

    @Override
    public Map<K, V> get() {
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
    public Map<K, V> getDefault() {
        return this.defaultValue;
    }

    @Override
    public Optional<Map<K, V>> getDirect() {
        return Optional.ofNullable(this.value);
    }

    @Override
    public Key<? extends BaseValue<Map<K, V>>> getKey() {
        return this.key;
    }

    @Nonnull
    @Override
    public TypeToken<MapValue> type() {
        return TypeToken.of(MapValue.class);
    }

    public static class Builder<K, V> implements ValueBuilder<Map<K, V>, MapValue<K, V>> {

        private final Key<MapValue<K, V>> key;
        private Map<K, V> defaultElement;
        private Map<K, V> element;

        public Builder(Key<MapValue<K, V>> key) {
            this.key = key;
        }

        @Override
        public ValueBuilder<Map<K, V>, MapValue<K, V>> with(MapValue<K, V> otherValue) {
            this.defaultElement = otherValue.getDefault();
            this.element = otherValue.get();
            return this;
        }

        @Override
        public ValueBuilder<Map<K, V>, MapValue<K, V>> defaultElement(Map<K, V> element) {
            this.defaultElement = element;
            return this;
        }

        @Override
        public ValueBuilder<Map<K, V>, MapValue<K, V>> element(Map<K, V> element) {
            this.element = element;
            return this;
        }

        @Override
        public MapValue<K, V> create() {
            return new GuardianMapValue<>(this.key, this.defaultElement, this.element);
        }
    }
}
