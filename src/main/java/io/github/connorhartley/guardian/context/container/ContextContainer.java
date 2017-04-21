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
import io.github.connorhartley.guardian.util.Transformer;

import java.util.*;

public class ContextContainer {

    private Context context;
    private Map<String, Object> rawMap = new HashMap<>();

    public ContextContainer() {}

    public ContextContainer(Context context) {
        this.context = context;
    }

    public <E> Optional<E> get(ContextKey<E> contextKey) {
        if (this.contains(contextKey)) {
            Object value = this.rawMap.get(contextKey.getId());
            if (value != null)
                return Optional.of((E) value);
        }
        return Optional.empty();
    }

    public <E> Optional<E> get(Class<? extends Context> clazz, String name) {
        String combinedId = clazz.getName().toLowerCase() + ":" + name.toLowerCase();
        if (this.contains(combinedId)) {
            Object value = this.rawMap.get(combinedId);
            if (value != null)
                return Optional.of((E) value);
        }
        return Optional.empty();
    }

    public <E> ContextContainer set(ContextKey<E> contextKey) {
        return this.set(contextKey, contextKey.getDefaultValue());
    }

    public <E> ContextContainer set(Class<? extends Context> clazz, String name, E value) {
        String combinedId = clazz.getName().toLowerCase() + ":" + name.toLowerCase();
        this.rawMap.put(combinedId, value);
        return this;
    }

    public <E> ContextContainer set(ContextKey<E> contextKey, E value) {
        this.rawMap.put(contextKey.getId(), value);
        return this;
    }

    public <E> ContextContainer transform(Class<? extends Context> clazz, String name, Transformer<E> transformer) {
        String combinedId = clazz.getName().toLowerCase() + ":" + name.toLowerCase();
        E value = (E) this.rawMap.get(combinedId);
        this.set(clazz, name, transformer.transform(value));
        return this;
    }

    public <E> ContextContainer transform(ContextKey<E> contextKey, Transformer<E> transformer) {
        E value = (E) this.rawMap.get(contextKey.getId());
        this.set(contextKey, transformer.transform(value));
        return this;
    }

    public <E> ContextContainer remove(ContextKey<E> contextKey) {
        if (this.contains(contextKey)) {
            this.rawMap.remove(contextKey.getId());
        }
        return this;
    }

    public Map<String, Object> getAll() {
        return this.rawMap;
    }

    public Map<String, Object> getAllShallow() {
        Map<String, Object> shallowMap = new HashMap<>();
        shallowMap.putAll(this.rawMap);
        return shallowMap;
    }

    public Context getContext() {
        return this.context;
    }

    public <E> boolean contains(ContextKey<E> contextKey) {
        return this.rawMap.containsKey(contextKey.getId());
    }

    public boolean contains(String id) { return this.rawMap.containsKey(id); }

    public void clear() {
        this.context = null;
        this.rawMap.clear();
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
