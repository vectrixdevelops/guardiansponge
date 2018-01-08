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
package com.ichorpowered.guardian.report;

import com.google.common.collect.BiMap;
import com.google.common.collect.HashBiMap;
import com.ichorpowered.guardianapi.detection.Detection;
import com.ichorpowered.guardianapi.detection.report.Report;
import com.ichorpowered.guardianapi.detection.report.Summary;
import com.ichorpowered.guardianapi.entry.entity.PlayerEntry;
import com.ichorpowered.guardianapi.event.origin.Origin;

import java.util.Iterator;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public class GuardianSummary implements Summary {

    private final Object plugin;
    private final PlayerEntry entry;
    private final Detection detection;
    private final BiMap<Class<?>, Report> reportRegistry = HashBiMap.create();
    private final Origin origin;

    public GuardianSummary(@Nonnull Object plugin, @Nonnull Detection detection,
                           @Nonnull PlayerEntry entry, @Nonnull Origin origin) {
        this.plugin = plugin;
        this.entry = entry;
        this.detection = detection;
        this.origin = origin;
    }

    @Nonnull
    @Override
    public Object getPlugin() {
        return this.plugin;
    }

    @Nonnull
    @Override
    public PlayerEntry getPlayerEntry() {
        return this.entry;
    }

    @Nonnull
    @Override
    public Detection getDetection() {
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
