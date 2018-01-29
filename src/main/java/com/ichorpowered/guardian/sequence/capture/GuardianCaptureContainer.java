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
import com.ichorpowered.guardianapi.detection.capture.CaptureKey;
import com.ichorpowered.guardianapi.util.item.value.BaseValue;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

import javax.annotation.Nonnull;

public class GuardianCaptureContainer implements CaptureContainer {

    private final HashMap<String, BaseValue<?>> container = Maps.newHashMap();

    public static GuardianCaptureContainer create() {
        return new GuardianCaptureContainer();
    }

    private GuardianCaptureContainer() {}

    @Override
    public <E, V extends BaseValue<E>> void offerIfEmpty(@Nonnull V item) {
        if (this.container.containsKey(item.getKey().getId())) return;
        this.container.put(item.getKey().getId(), item);
    }

    @Override
    public <E, V extends BaseValue<E>> void offer(@Nonnull V item) {
        this.container.remove(item.getKey().getId());
        this.container.put(item.getKey().getId(), item);
    }

    @Nonnull
    @Override
    public CaptureContainer merge(@Nonnull CaptureContainer captureContainer) {
        Set<Map.Entry<String, BaseValue<?>>> entries = ((GuardianCaptureContainer) captureContainer).container.entrySet();

        for (Map.Entry<String, BaseValue<?>> entry : entries) {
            this.container.put(entry.getKey(), entry.getValue());
        }

        return this;
    }

    @Nonnull
    @Override
    public <E, V extends BaseValue<E>> Optional<V> getValue(@Nonnull CaptureKey<V> key) {
        return Optional.ofNullable((V) this.container.get(key.getId()));
    }

    @Override
    public <E, V extends BaseValue<E>> Optional<E> get(@Nonnull CaptureKey<V> key) {
        return Optional.ofNullable(this.container.get(key.getId()) != null ? (E) this.container.get(key.getId()).getDirect().orElse(null) : null);
    }

    @Nonnull
    @Override
    public Set<String> keySet() {
        return this.container.keySet();
    }
}
