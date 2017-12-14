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
package io.ichorpowered.guardian.sequence.capture;

import com.google.common.collect.BiMap;
import com.google.common.collect.HashBiMap;
import com.ichorpowered.guardian.api.detection.DetectionConfiguration;
import com.ichorpowered.guardian.api.entry.EntityEntry;
import com.ichorpowered.guardian.api.sequence.capture.Capture;
import com.ichorpowered.guardian.api.sequence.capture.CaptureContainer;
import com.ichorpowered.guardian.api.sequence.capture.CaptureRegistry;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.Iterator;
import java.util.NoSuchElementException;
import java.util.Set;

public class GuardianCaptureRegistry implements CaptureRegistry {

    private final EntityEntry entry;
    private final BiMap<Class<? extends Capture>, Capture> captureRegistry;
    private final CaptureContainer captureContainer;

    public GuardianCaptureRegistry(@Nonnull EntityEntry entry) {
        this.entry = entry;
        this.captureRegistry = HashBiMap.create();
        this.captureContainer = GuardianCaptureContainer.create();
    }

    @Override
    public <C> void put(@Nonnull C pluginContainer, @Nonnull Class<? extends Capture> aClass, @Nullable Capture capture) {
        this.captureRegistry.put(aClass, capture);
    }

    @Nonnull
    @Override
    @SuppressWarnings("unchecked")
    public <E, F extends DetectionConfiguration> Capture<E, F> expect(@Nonnull Class<? extends Capture<E, F>> aClass) throws NoSuchElementException {
        if (!this.captureRegistry.containsKey(aClass)) throw new NoSuchElementException();
        return (Capture<E, F>) this.captureRegistry.get(aClass);
    }

    @Nullable
    @Override
    public Capture get(@Nonnull Class<? extends Capture> aClass) {
        return this.captureRegistry.get(aClass);
    }

    @Nullable
    @Override
    public Class<? extends Capture> key(@Nonnull Capture capture) {
        return this.captureRegistry.inverse().get(capture);
    }

    @Override
    public Set<Class<? extends Capture>> keySet() {
        return this.captureRegistry.keySet();
    }

    @Override
    public EntityEntry getEntityEntry() {
        return this.entry;
    }

    @Override
    public CaptureContainer getContainer() {
        return this.captureContainer;
    }

    @Override
    public Iterator<Capture> iterator() {
        return this.captureRegistry.values().iterator();
    }

}
