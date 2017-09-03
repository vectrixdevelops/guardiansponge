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

import com.google.common.collect.BiMap;
import com.google.common.collect.HashBiMap;
import com.ichorpowered.guardian.api.sequence.capture.CaptureContainer;
import com.ichorpowered.guardian.api.util.IdentifierKey;
import com.ichorpowered.guardian.api.util.Transform;

import java.util.Set;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public class GuardianCaptureContainer implements CaptureContainer {

    private final BiMap<String, Object> container = HashBiMap.create();

    public static GuardianCaptureContainer create() {
        return new GuardianCaptureContainer();
    }

    public GuardianCaptureContainer() {}

    @Override
    public <T> void put(@Nonnull String id, @Nullable T value) {
        this.container.put(id, value);
    }

    @Override
    public <T> void put(@Nonnull IdentifierKey<String> key, @Nonnull T value) {
        this.container.put(key.get(), value);
    }

    @Override
    public <T> void transform(@Nonnull String key, @Nullable Transform<T> transform) {
        this.container.put(key, transform.transform((T) this.container.get(key)));
    }

    @Override
    public <T> void transform(@Nonnull IdentifierKey<String> key, @Nonnull Transform<T> transform) {
        this.container.put(key.get(), transform.transform((T) this.container.get(key.get())));
    }

    @Nullable
    @Override
    @SuppressWarnings("unchecked")
    public <T> T get(@Nonnull String id) {
        return (T) this.container.get(id);
    }

    @Nullable
    @Override
    @SuppressWarnings("unchecked")
    public <T> T get(@Nonnull IdentifierKey<String> key) {
        return (T) this.container.get(key.get());
    }

    @Nullable
    @Override
    public <T> String key(T value) {
        return this.container.inverse().get(value);
    }

    @Override
    public Set<String> keySet() {
        return this.container.keySet();
    }
}
