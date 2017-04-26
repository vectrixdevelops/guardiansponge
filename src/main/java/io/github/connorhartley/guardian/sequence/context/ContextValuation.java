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
package io.github.connorhartley.guardian.sequence.context;

import io.github.connorhartley.guardian.util.Transformer;

import java.util.*;

public class ContextValuation {

    private final List<Context> context;
    private final Map<String, Object> rawMap;

    public ContextValuation() {
        this.context = new ArrayList<>();
        this.rawMap = new HashMap<>();
    }

    public ContextValuation(Context context) {
        this.context = new ArrayList<>();
        this.rawMap = new HashMap<>();

        this.context.add(context);
    }

    public ContextValuation(List<Context> contexts) {
        this.context = new ArrayList<>();
        this.rawMap = new HashMap<>();

        this.context.addAll(contexts);
    }

    public ContextValuation addContext(Context context) {
        this.context.add(context);
        return this;
    }

    public <C, E> Optional<E> get(Class<C> clazz, String name) {
        String combinedId = clazz.getCanonicalName().toLowerCase() + ":" + name.toLowerCase();
        if (this.contains(combinedId)) {
            E value = (E) this.rawMap.get(combinedId);
            if (value != null) {
                return Optional.of(value);
            }
        }
        return Optional.empty();
    }

    public <C, E> ContextValuation set(Class<C> clazz, String name, E value) {
        String combinedId = clazz.getCanonicalName().toLowerCase() + ":" + name.toLowerCase();
        this.rawMap.put(combinedId, value);
        return this;
    }

    public <C, E> ContextValuation transform(Class<C> clazz, String name, Transformer<E> transformer) {
        String combinedId = clazz.getCanonicalName().toLowerCase() + ":" + name.toLowerCase();
        E value = (E) this.rawMap.get(combinedId);
        this.set(clazz, name, transformer.transform(value));
        return this;
    }

    public <C> ContextValuation remove(Class<C> clazz, String name) {
        String combinedId = clazz.getCanonicalName().toLowerCase() + ":" + name.toLowerCase();
        if (this.contains(combinedId)) {
            this.rawMap.remove(combinedId);
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

    public List<Context> getContexts() {
        return this.context;
    }

    public boolean contains(String id) { return this.rawMap.containsKey(id); }

    public void clear() {
        this.context.clear();
        this.rawMap.clear();
    }

    public boolean isEmpty() {
        return this.rawMap.isEmpty();
    }

    @Override
    public ContextValuation clone() {
        ContextValuation shallowClone = new ContextValuation(this.context);
        shallowClone.rawMap.putAll(this.rawMap);
        return shallowClone;
    }

}
