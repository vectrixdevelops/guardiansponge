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
package com.ichorpowered.guardian.sponge.sequence;

import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import com.ichorpowered.guardian.api.sequence.SequenceBlueprint;
import com.ichorpowered.guardian.api.sequence.SequenceRegistry;
import org.checkerframework.checker.nullness.qual.NonNull;

import java.util.Iterator;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

@Singleton
public final class SequenceRegistryImpl implements SequenceRegistry {

    private final Map<Class<?>, SequenceBlueprint<?>> registry = Maps.newHashMap();

    @Inject
    public SequenceRegistryImpl() {}

    @Override
    public void set(final @NonNull Class<?> sequenceType, final @NonNull SequenceBlueprint<?> sequenceBlueprint) {
        this.registry.put(sequenceType, sequenceBlueprint);
    }

    @Override
    public @NonNull Optional<SequenceBlueprint<?>> get(final @NonNull Class<?> sequenceType) {
        return Optional.ofNullable(this.registry.get(sequenceType));
    }

    @Override
    public @NonNull Set<Class<?>> keys() {
        return this.registry.keySet();
    }

    @Override
    public @NonNull Set<SequenceBlueprint<?>> values() {
        return Sets.newHashSet(this.registry.values());
    }

    @Override
    public Iterator<SequenceBlueprint<?>> iterator() {
        return this.registry.values().iterator();
    }

}
