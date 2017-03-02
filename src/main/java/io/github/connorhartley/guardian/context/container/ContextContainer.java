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
package io.github.connorhartley.guardian.context.container;

import io.github.connorhartley.guardian.context.Context;
import io.github.connorhartley.guardian.util.ValueTransform;

import java.util.*;

public class ContextContainer {

    private final List<Context> context = new ArrayList<>();

    private Map<String, Object> rawMap = new HashMap<>();

    public ContextContainer() {}

    public ContextContainer(Context context) {
        this.context.add(context);
    }

    public ContextContainer(Collection<Context> contextCollection) {
        this.context.addAll(contextCollection);
    }

    public <E> Optional<E> get(ContextKey<E> contextKey) {
        if (this.contains(contextKey)) {
            Object value = this.rawMap.get(contextKey.getId());
            if (value != null && contextKey.getValueType().isAssignableFrom(value.getClass()))
                return Optional.of((E) value);
        }
        return Optional.empty();
    }

    public <E> ContextContainer set(ContextKey<E> contextKey) {
        return this.set(contextKey, contextKey.getDefaultValue());
    }

    public <E> ContextContainer set(ContextKey<E> contextKey, E value) {
        if (this.contains(contextKey)) {
            this.rawMap.replace(contextKey.getId(), value);
        } else {
            this.rawMap.put(contextKey.getId(), value);
        }
        return this;
    }

    public <E> ContextContainer transform(ContextKey<E> contextKey, ValueTransform valueTransform) {
        if (this.contains(contextKey)) {
            this.rawMap.replace(contextKey.getId(), valueTransform.transform(this.get(contextKey).get()));
        } else {
            this.rawMap.put(contextKey.getId(), valueTransform.transform(contextKey.getDefaultValue()));
        }
        return this;
    }

    public ContextContainer remove(ContextKey contextKey) {
        if (this.contains(contextKey)) {
            this.rawMap.remove(contextKey.getId());
        }
        return this;
    }

    public Map<String, Object> getValues() {
        Map<String, Object> shallowMap = new HashMap<>();
        shallowMap.putAll(this.rawMap);
        return shallowMap;
    }

    public List<Context> getContext() {
        return this.context;
    }

    public boolean contains(ContextKey contextKey) {
        return this.rawMap.containsKey(contextKey.getId());
    }

    public void clear() {
        this.context.clear();
        this.rawMap.clear();
    }

    public ContextContainer merge(ContextContainer... contextContainers) {
        for (ContextContainer container : contextContainers) {
            this.context.addAll(container.getContext());
            this.rawMap.putAll(container.getValues());
        }
        return this;
    }

    public ContextContainer merge(ContextContainer contextContainer) {
        this.context.addAll(contextContainer.getContext());
        this.rawMap.putAll(contextContainer.getValues());
        return this;
    }

    public ContextContainer cloneMerge(ContextContainer... contextContainers) {
        ContextContainer shallowClone = this.clone();
        for (ContextContainer container : contextContainers) {
            shallowClone.context.addAll(container.getContext());
            shallowClone.rawMap.putAll(container.getValues());
        }
        return shallowClone;
    }

    public ContextContainer cloneMerge(ContextContainer contextContainer) {
        ContextContainer shallowClone = this.clone();
        shallowClone.context.addAll(contextContainer.getContext());
        shallowClone.rawMap.putAll(contextContainer.getValues());
        return shallowClone;
    }

    public boolean isEmpty() {
        return this.rawMap.isEmpty();
    }

    @Override
    public ContextContainer clone() {
        ContextContainer shallowClone = new ContextContainer(this.context);
        shallowClone.rawMap.putAll(this.rawMap);
        return shallowClone;
    }

}
