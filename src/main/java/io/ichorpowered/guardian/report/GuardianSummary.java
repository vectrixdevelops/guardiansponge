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
package io.ichorpowered.guardian.report;

import com.google.common.collect.BiMap;
import com.google.common.collect.HashBiMap;
import com.ichorpowered.guardian.api.detection.Detection;
import com.ichorpowered.guardian.api.detection.DetectionConfiguration;
import com.ichorpowered.guardian.api.entry.EntityEntry;
import com.ichorpowered.guardian.api.event.origin.Origin;
import com.ichorpowered.guardian.api.report.Report;
import com.ichorpowered.guardian.api.report.Summary;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.Iterator;

public class GuardianSummary<E, F extends DetectionConfiguration> implements Summary<E, F> {

    private final E owner;
    private final EntityEntry entry;
    private final Detection<E, F> detection;
    private final BiMap<Class<?>, Report> reportRegistry = HashBiMap.create();
    private final Origin origin;

    public GuardianSummary(@Nonnull E owner, @Nonnull Detection<E, F> detection,
                           @Nonnull EntityEntry entry, @Nonnull Origin origin) {
        this.owner = owner;
        this.entry = entry;
        this.detection = detection;
        this.origin = origin;
    }

    @Nonnull
    @Override
    public E getOwner() {
        return this.owner;
    }

    @Nonnull
    @Override
    public EntityEntry getEntityEntry() {
        return this.entry;
    }

    @Nonnull
    @Override
    public Detection<E, F> getDetection() {
        return this.detection;
    }

    @Override
    public <T extends Report> void set(@Nonnull Class<T> key, @Nonnull Report report) {
        this.reportRegistry.put(key, report);
    }

    @Nullable
    @Override
    public <T extends Report> T view(@Nonnull Class<T> key) {
        return (T) this.reportRegistry.get(key);
    }

    @Nonnull
    @Override
    public Origin getOrigin() {
        return this.origin;
    }

    @Nonnull
    @Override
    public Iterator<Report> iterator() {
        return this.reportRegistry.values().iterator();
    }

}
