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

import com.google.common.collect.ImmutableCollection;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Maps;
import com.ichorpowered.guardianapi.util.item.key.Key;
import com.ichorpowered.guardianapi.util.item.value.BaseValue;
import com.ichorpowered.guardianapi.util.item.value.immutable.ImmutableMapValue;

import java.util.Map;
import java.util.Optional;
import java.util.function.Function;
import java.util.function.Predicate;

public class GuardianImmutableMapValue<K, V> implements ImmutableMapValue<K, V> {

    private final Key<ImmutableMapValue<K, V>> key;
    private final Map<K, V> defaultValue;

    private Map<K, V> value;

    private GuardianImmutableMapValue(Key<ImmutableMapValue<K, V>> key, Map<K, V> defaultValue,
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
    public ImmutableMapValue<K, V> with(K key, V value) {
        final Map<K, V> map;
        if (exists()) {
            map = Maps.newHashMap(this.value);
        } else {
            map = Maps.newHashMap(this.defaultValue);
        }

        map.put(key, value);
        return new GuardianImmutableMapValue<>(this.key, this.defaultValue, map);
    }

    @Override
    public ImmutableMapValue<K, V> with(Map<K, V> value) {
        return this.withAll(value);
    }

    @Override
    public ImmutableMapValue<K, V> withAll(Map<K, V> map) {
        final Map<K, V> value;
        if (exists()) {
            value = Maps.newHashMap(this.value);
        } else {
            value = Maps.newHashMap(this.defaultValue);
        }

        value.putAll(map);
        return new GuardianImmutableMapValue<>(this.key, this.defaultValue, value);
    }

    @Override
    public ImmutableMapValue<K, V> without(K key) {
        final Map<K, V> value;
        if (exists()) {
            value = Maps.newHashMap(this.value);
        } else {
            value = Maps.newHashMap(this.defaultValue);
        }

        value.remove(key);
        return new GuardianImmutableMapValue<>(this.key, this.defaultValue, value);
    }

    @Override
    public ImmutableMapValue<K, V> withoutAll(Iterable<K> keys) {
        final Map<K, V> value;
        if (exists()) {
            value = Maps.newHashMap(this.value);
        } else {
            value = Maps.newHashMap(this.defaultValue);
        }

        keys.forEach(value::remove);
        return new GuardianImmutableMapValue<>(this.key, this.defaultValue, value);
    }

    @Override
    public ImmutableMapValue<K, V> withoutAll(Predicate<Map.Entry<K, V>> predicate) {
        final Map<K, V> value;
        if (exists()) {
            value = Maps.newHashMap(this.value);
        } else {
            value = Maps.newHashMap(this.defaultValue);
        }

        value.entrySet().removeIf(predicate);
        return new GuardianImmutableMapValue<>(this.key, this.defaultValue, value);
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
    public ImmutableMapValue<K, V> transform(Function<Map<K, V>, Map<K, V>> function) {
        final Map<K, V> value;
        if (exists()) {
            value = Maps.newHashMap(this.value);
        } else {
            value = Maps.newHashMap(this.defaultValue);
        }

        return new GuardianImmutableMapValue<>(this.key, this.defaultValue, Maps.newHashMap(function.apply(value)));
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
        return true;
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
}
