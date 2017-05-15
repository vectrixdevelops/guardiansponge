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
package io.github.connorhartley.guardian.sequence.capture;

import io.github.connorhartley.guardian.util.Transformer;

import javax.annotation.Nullable;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

/**
 * CaptureContext Container
 *
 * A storage for all capture data.
 */
public class CaptureContainer {

    private final Map<String, Object> rawMap;

    public CaptureContainer() {
        this.rawMap = new HashMap<>();
    }

    /**
     * Get
     *
     * <p>Returns data stored under the {@link CaptureContext} path name
     * and data name. If the data does not exist it will return empty.</p>
     *
     * @param clazz The class used for the path name
     * @param name The name used for the data name
     * @param <C> The class type
     * @param <E> The data value type
     * @return The data value if it exists
     */
    public <C, E> Optional<E> get(Class<C> clazz, String name) {
        return this.get(new CaptureKey<C, E>(clazz, name));
    }

    /**
     * Get
     *
     * <p>Returns data stored under the {@link CaptureContext} path name
     * and data name. If the data does not exist it will return empty.</p>
     *
     * @param captureKey The capture key for path name and value type
     * @param <C> The class type
     * @param <E> The data value type
     * @return The data value if it exists
     */
    public <C, E> Optional<E> get(CaptureKey<C, E> captureKey) {
        if (this.contains(captureKey.getId())) {
            return Optional.ofNullable(captureKey.transformValue(this.rawMap.get(captureKey.getId())));
        }
        return Optional.empty();
    }

    /**
     * Set
     *
     * <p>Sets a data value stored under the {@link CaptureContext} path name
     * and data name. If the data already exists it will be overridden.</p>
     *
     * @param clazz The class used for the path name
     * @param name The name used for the data name
     * @param value The value to store
     * @param <C> The class type
     * @param <E> The data value type
     * @return This capture container
     */
    public <C, E> CaptureContainer set(Class<C> clazz, String name, E value) {
        return this.set(new CaptureKey<>(clazz, name), value);
    }

    /**
     * Set
     *
     * <p>Sets a data value stored under the {@link CaptureContext} path name
     * and data name. If the data already exists it will be overridden.</p>
     *
     * @param captureKey The capture key for path name and value type
     * @param <C> The class type
     * @param <E> The data value type
     * @return This capture container
     */
    public <C, E> CaptureContainer set(CaptureKey<C, E> captureKey) {
        return this.set(captureKey, null);
    }

    /**
     * Set
     *
     * <p>Sets a data value stored under the {@link CaptureContext} path name
     * and data name. If the data already exists it will be overridden.</p>
     *
     * @param captureKey The capture key for path name and value type
     * @param value The value to store
     * @param <C> The class type
     * @param <E> The data value type
     * @return This capture container
     */
    public <C, E> CaptureContainer set(CaptureKey<C, E> captureKey, @Nullable E value) {
        this.rawMap.put(captureKey.getId(), captureKey.transformValue(value));
        return this;
    }

    /**
     * Transform
     *
     * <p>Transform uses a lambda function to transform the existing data value
     * stored under the {@link CaptureContext} path name and data name.</p>
     *
     * @param clazz The class used for the path name
     * @param name The name used for the data name
     * @param transformer Lambda transform function
     * @param <C> The class type
     * @param <E> The data value type
     * @return This capture container
     */
    public <C, E> CaptureContainer transform(Class<C> clazz, String name, Transformer<E> transformer) {
        return this.transform(new CaptureKey<>(clazz, name), transformer);
    }

    /**
     * Transform
     *
     * <p>Transform uses a lambda function to transform the existing data value
     * stored under the {@link CaptureContext} path name and data name.</p>
     *
     * @param captureKey The capture key for path name and value type
     * @param transformer Lambda transform function
     * @param <C> The class type
     * @param <E> The data value type
     * @return This capture container
     */
    public <C, E> CaptureContainer transform(CaptureKey<C, E> captureKey, Transformer<E> transformer) {
        this.set(captureKey, transformer.transform(captureKey.transformValue(this.rawMap.get(captureKey.getId()))));
        return this;
    }

    /**
     * Remove
     *
     * <p>Removes a data value stored under the {@link CaptureContext} path name
     * and data name.</p>
     *
     * @param clazz The class used for the path name
     * @param name The name used for the data name
     * @param <C> The class type
     * @param <E> The data value type
     * @return This capture container
     */
    public <C, E> CaptureContainer remove(Class<C> clazz, String name) {
        return this.remove(new CaptureKey<C, E>(clazz, name));
    }

    /**
     * Remove
     *
     * <p>Removes a data value stored under the {@link CaptureContext} path name
     * and data name.</p>
     *
     * @param captureKey The capture key for path name and value type
     * @param <C> The class type
     * @param <E> The data value type
     * @return This capture container
     */
    public <C, E> CaptureContainer remove(CaptureKey<C, E> captureKey) {
        if (this.contains(captureKey.getId())) {
            this.rawMap.remove(captureKey.getId());
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
    public CaptureContainer clone() {
        CaptureContainer shallowClone = new CaptureContainer();
        shallowClone.rawMap.putAll(this.rawMap);
        return shallowClone;
    }

}
