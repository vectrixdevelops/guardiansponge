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
package com.ichorpowered.guardian.sequence.capture;

import com.google.common.collect.Maps;
import com.ichorpowered.guardianapi.detection.capture.CaptureContainer;
import com.ichorpowered.guardianapi.util.Transform;
import com.ichorpowered.guardianapi.util.key.NamedTypeKey;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public class GuardianCaptureContainer implements CaptureContainer {

    private final HashMap<String, Object> container = Maps.newHashMap();

    public static GuardianCaptureContainer create() {
        return new GuardianCaptureContainer();
    }

    private GuardianCaptureContainer() {}

    @Override
    public <T> void putOnce(@Nonnull String key, @Nullable T value) {
        if (!this.container.containsKey(key)) this.container.put(key, Object.class.cast(value));
    }

    @Override
    public <T> void putOnce(@Nonnull NamedTypeKey<T> key, @Nullable T value) {
        this.putOnce(key.getName(), value);
    }

    @Override
    public <T> void put(@Nonnull String id, @Nullable T value) {
        this.container.remove(id);
        this.container.put(id, Object.class.cast(value));
    }

    @Override
    public <T> void put(@Nonnull NamedTypeKey<T> key, @Nonnull T value) {
        this.put(key.getName(), value);
    }

    @Nonnull
    @Override
    public CaptureContainer merge(@Nonnull CaptureContainer captureContainer) {
        Set<Map.Entry<String, Object>> entries = ((GuardianCaptureContainer) captureContainer).container.entrySet();

        for (Map.Entry<String, Object> entry : entries) {
            this.container.put(entry.getKey(), entry.getValue());
        }

        return this;
    }

    @Override
    public <T> void transform(@Nonnull String key, @Nullable Transform<T> transform, T defaultValue) {
        if (!this.container.containsKey(key)) {
            this.put(key, transform.transform(defaultValue));
            return;
        }

        this.put(key, transform.transform((T) this.container.get(key)));
    }

    @Override
    public <T> void transform(@Nonnull NamedTypeKey<T> key, @Nonnull Transform<T> transform, T defaultValue) {
        this.transform(key.getName(), transform, defaultValue);
    }

    @Nonnull
    @Override
    public <T> Optional<T> get(@Nonnull String key) {
        return Optional.ofNullable((T) this.container.get(key));
    }

    @Nonnull
    @Override
    public <T> Optional<T> get(@Nonnull NamedTypeKey<T> key) {
        return Optional.ofNullable((T) this.container.get(key.getName()));
    }

    @Nonnull
    @Override
    public Set<String> keySet() {
        return this.container.keySet();
    }
}
