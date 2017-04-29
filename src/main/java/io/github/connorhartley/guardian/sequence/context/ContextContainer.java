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

/**
 * Context Container
 *
 * A storage for all context data.
 */
public class ContextContainer {

    private final Map<String, Object> rawMap;

    public ContextContainer() {
        this.rawMap = new HashMap<>();
    }

    /**
     * Get
     *
     * <p>Returns data stored under the {@link Context} path name
     * and data name. If the data does not exist it will return empty.</p>
     *
     * @param clazz The class used for the path name
     * @param name The name used for the data name
     * @param <C> The class type
     * @param <E> The data value type
     * @return The data value if it exists
     */
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

    /**
     * Set
     *
     * <p>Sets a data value stored under the {@link Context} path name
     * and data name. If the data already exists it will be overridden.</p>
     *
     * @param clazz The class used for the path name
     * @param name The name used for the data name
     * @param value The value to store
     * @param <C> The class type
     * @param <E> The data value type
     * @return This context container
     */
    public <C, E> ContextContainer set(Class<C> clazz, String name, E value) {
        String combinedId = clazz.getCanonicalName().toLowerCase() + ":" + name.toLowerCase();
        this.rawMap.put(combinedId, value);
        return this;
    }

    /**
     * Transform
     *
     * <p>Transform uses a lambda function to transform the existing data value
     * stored under the {@link Context} path name and data name.</p>
     *
     * @param clazz The class used for the path name
     * @param name The name used for the data name
     * @param transformer Lambda transform function
     * @param <C> The class type
     * @param <E> The data value type
     * @return This context container
     */
    public <C, E> ContextContainer transform(Class<C> clazz, String name, Transformer<E> transformer) {
        String combinedId = clazz.getCanonicalName().toLowerCase() + ":" + name.toLowerCase();
        E value = (E) this.rawMap.get(combinedId);
        this.set(clazz, name, transformer.transform(value));
        return this;
    }

    /**
     * Remove
     *
     * <p>Removes a data value stored under the {@link Context} path name
     * and data name.</p>
     *
     * @param clazz The class used for the path name
     * @param name The name used for the data name
     * @param <C> The class type
     * @return This context container
     */
    public <C> ContextContainer remove(Class<C> clazz, String name) {
        String combinedId = clazz.getCanonicalName().toLowerCase() + ":" + name.toLowerCase();
        if (this.contains(combinedId)) {
            this.rawMap.remove(combinedId);
        }
        return this;
    }

    /**
     * Get All
     *
     * <p>Returns the entire map of data entries.</p>
     *
     * @return Map of data entries
     */
    public Map<String, Object> getAll() {
        return this.rawMap;
    }

    /**
     * Contains
     *
     * <p>Returns true if this container contains a data value
     * under the specified id.</p>
     *
     * @param id The id to look for
     * @return True if it exists
     */
    public boolean contains(String id) { return this.rawMap.containsKey(id); }

    /**
     * Clear
     *
     * <p>Clears all the data entries from the map.</p>
     */
    public void clear() {
        this.rawMap.clear();
    }

    /**
     * Is Empty
     *
     * <p>Returns true if the map is entry.</p>
     *
     * @return True if the map is entry
     */
    public boolean isEmpty() {
        return this.rawMap.isEmpty();
    }

    @Override
    public ContextContainer clone() {
        ContextContainer shallowClone = new ContextContainer();
        shallowClone.rawMap.putAll(this.rawMap);
        return shallowClone;
    }

}
