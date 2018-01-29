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
package com.ichorpowered.guardian.content;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import com.ichorpowered.guardianapi.content.ContentContainer;
import com.ichorpowered.guardianapi.content.ContentLoader;
import com.ichorpowered.guardianapi.content.key.ContentKey;
import com.ichorpowered.guardianapi.util.item.value.BaseValue;

import java.util.Iterator;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

public abstract class AbstractContentContainer implements ContentContainer {

    private final ContentLoader contentLoader;
    private final Set<ContentKey<?>> keySet = Sets.newHashSet();
    private final Map<String, BaseValue<?>> container = Maps.newHashMap();

    public AbstractContentContainer() {
        this(null);
    }

    public AbstractContentContainer(final ContentLoader contentLoader) {
        this.contentLoader = contentLoader;
    }

    @Override
    public <E extends BaseValue<?>> void offer(ContentKey<E> key, E value) {
        if (!this.keySet.contains(key)) this.keySet.add(key);
        if (!this.container.containsKey(key.getId())) this.container.put(key.getId(), value);
    }

    @Override
    public void attempt(ContentKey key, BaseValue<?> value) {
        if (!this.keySet.contains(key)) this.keySet.add(key);
        if (!this.container.containsKey(key.getId())) this.container.put(key.getId(), value);
    }

    @Override
    public <E extends BaseValue<?>> Optional<E> get(ContentKey<E> key) {
        if (!this.keySet.contains(key)) return Optional.empty();
        if (!this.container.containsKey(key.getId())) return Optional.empty();

        return Optional.ofNullable((E) this.container.get(key.getId()));
    }

    @Override
    public ImmutableMap<ContentKey<?>, BaseValue<?>> getMap() {
        final Map<ContentKey<?>, BaseValue<?>> map = Maps.newHashMap();
        for (Map.Entry<String, BaseValue<?>> entry : this.container.entrySet()) {
            final Optional<ContentKey<?>> key = this.keySet.stream().filter(contentKey -> contentKey.getId().equals(entry.getKey())).findFirst();

            if (!key.isPresent()) continue;
            map.put(key.get(), entry.getValue());
        }

        return ImmutableMap.copyOf(map);
    }

    @Override
    public Set<ContentKey<?>> getKeys() {
        return this.keySet;
    }

    @Override
    public Set<?> getValues() {
        return Sets.newHashSet(this.container.values());
    }

    @Override
    public Optional<ContentLoader> getContentLoader() {
        return Optional.ofNullable(this.contentLoader);
    }

    @Override
    public Iterator<BaseValue<?>> iterator() {
        return this.container.values().iterator();
    }
}
