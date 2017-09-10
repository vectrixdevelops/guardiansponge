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
package io.github.connorhartley.guardian.detection;

import com.google.common.collect.HashMultimap;
import com.google.common.collect.Multimap;
import com.ichorpowered.guardian.api.detection.DetectionChain;

import java.util.ArrayList;
import java.util.List;
import java.util.NoSuchElementException;

import javax.annotation.Nonnull;

public class GuardianDetectionChain implements DetectionChain {

    private final Multimap<ProcessType, Class<?>> chainRegistry = HashMultimap.create();

    public GuardianDetectionChain() {}

    @Override
    public <C> void add(@Nonnull C pluginContainer, @Nonnull ProcessType processType, @Nonnull Class<?> clazz) {
        if (this.chainRegistry.containsKey(processType) && this.chainRegistry.get(processType).contains(clazz)) return;
        this.chainRegistry.put(processType, clazz);
    }

    @Override
    public <T> List<Class<? extends T>> get(@Nonnull ProcessType processType) {
        if (!this.chainRegistry.containsKey(processType)) throw new NoSuchElementException();

        List<Class<? extends T>> converted = new ArrayList<>();
        this.chainRegistry.get(processType).forEach(clazz -> converted.add((Class<? extends T>) clazz));

        return converted;
    }
}
