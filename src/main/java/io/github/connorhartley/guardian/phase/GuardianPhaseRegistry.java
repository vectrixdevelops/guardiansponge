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
package io.github.connorhartley.guardian.phase;

import com.google.common.collect.BiMap;
import com.google.common.collect.HashBiMap;
import com.ichorpowered.guardian.api.phase.PhaseRegistry;
import com.ichorpowered.guardian.api.phase.PhaseViewer;
import com.ichorpowered.guardian.api.util.key.NamedTypeKey;
import io.github.connorhartley.guardian.GuardianPlugin;
import io.github.connorhartley.guardian.util.ConsoleFormatter;
import org.fusesource.jansi.Ansi;

import java.util.NoSuchElementException;
import java.util.Set;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public class GuardianPhaseRegistry implements PhaseRegistry {

    private final GuardianPlugin plugin;
    private final BiMap<NamedTypeKey, PhaseViewer> phaseRegistry = HashBiMap.create();

    public GuardianPhaseRegistry(GuardianPlugin plugin) {
        this.plugin = plugin;
    }

    @Override
    public <C, T> void put(@Nonnull C pluginContainer, @Nonnull NamedTypeKey<T> key, @Nonnull PhaseViewer<T> phaseViewer) {
        if (this.phaseRegistry.containsKey(key)) {
            this.plugin.getLogger().warn(ConsoleFormatter.builder()
                    .fg(Ansi.Color.YELLOW,
                            "Attempted to put a phase viewer into the registry that already exists!")
                    .buildAndGet()
            );

            return;
        }

        this.phaseRegistry.put(key, phaseViewer);
    }

    @Nonnull
    @Override
    @SuppressWarnings("unchecked")
    public <T> PhaseViewer<T> expect(@Nonnull NamedTypeKey<T> key) {
        if (!this.phaseRegistry.containsKey(key)) throw new NoSuchElementException();
        return (PhaseViewer<T>) this.phaseRegistry.get(key);
    }

    @Nullable
    @Override
    public PhaseViewer get(@Nonnull NamedTypeKey key) {
        return this.phaseRegistry.get(key);
    }

    @Nullable
    @Override
    public NamedTypeKey key(@Nonnull PhaseViewer phaseViewer) {
        return this.phaseRegistry.inverse().get(phaseViewer);
    }

    @Nonnull
    @Override
    public Set<NamedTypeKey> keySet() {
        return this.phaseRegistry.keySet();
    }

}
