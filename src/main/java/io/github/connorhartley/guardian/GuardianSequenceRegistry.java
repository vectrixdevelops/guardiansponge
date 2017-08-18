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
package io.github.connorhartley.guardian;

import com.google.common.collect.BiMap;
import com.google.common.collect.HashBiMap;
import com.ichorpowered.guardian.api.sequence.SequenceBlueprint;
import com.ichorpowered.guardian.api.sequence.SequenceRegistry;
import io.github.connorhartley.guardian.util.ConsoleFormatter;
import org.fusesource.jansi.Ansi;

import java.util.Iterator;
import java.util.NoSuchElementException;
import java.util.Set;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public class GuardianSequenceRegistry implements SequenceRegistry {

    private final GuardianPlugin plugin;
    private final BiMap<Class<? extends SequenceBlueprint>, SequenceBlueprint> blueprintRegistry = HashBiMap.create();

    public GuardianSequenceRegistry(GuardianPlugin plugin) {
        this.plugin = plugin;
    }

    @Override
    public <C> void put(@Nonnull C pluginContainer, @Nonnull Class<? extends SequenceBlueprint> key, @Nonnull SequenceBlueprint sequenceBlueprint) {
        if (this.blueprintRegistry.containsKey(key)) {
            this.plugin.getLogger().warn(ConsoleFormatter.builder()
                    .fg(Ansi.Color.YELLOW,
                            "Attempted to put a blueprint into the registry that already exists!")
                    .build().get()
            );

            return;
        }

        this.blueprintRegistry.put(key, sequenceBlueprint);
    }

    @Nonnull
    @Override
    @SuppressWarnings("unchecked")
    public <E, F> SequenceBlueprint<E, F> expect(@Nonnull Class<? extends SequenceBlueprint> key) throws NoSuchElementException {
        if (!this.blueprintRegistry.containsKey(key)) throw new NoSuchElementException();
        return (SequenceBlueprint<E, F>) this.blueprintRegistry.get(key);
    }

    @Nullable
    @Override
    public SequenceBlueprint get(@Nonnull Class<? extends SequenceBlueprint> key) {
        return this.blueprintRegistry.get(key);
    }

    @Nullable
    @Override
    public Class<? extends SequenceBlueprint> key(@Nonnull SequenceBlueprint sequenceBlueprint) {
        return this.blueprintRegistry.inverse().get(sequenceBlueprint);
    }

    @Nonnull
    @Override
    public Set<Class<? extends SequenceBlueprint>> keySet() {
        return this.blueprintRegistry.keySet();
    }

    @Override
    public Iterator<SequenceBlueprint> iterator() {
        return this.blueprintRegistry.values().iterator();
    }

}

