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
package io.github.connorhartley.guardian.detection.check;

import com.google.common.collect.BiMap;
import com.google.common.collect.HashBiMap;
import com.ichorpowered.guardian.api.detection.DetectionConfiguration;
import com.ichorpowered.guardian.api.detection.check.CheckBlueprint;
import com.ichorpowered.guardian.api.detection.check.CheckRegistry;
import io.github.connorhartley.guardian.GuardianPlugin;
import io.github.connorhartley.guardian.util.ConsoleFormatter;
import org.fusesource.jansi.Ansi;

import java.util.Iterator;
import java.util.NoSuchElementException;
import java.util.Set;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public final class GuardianCheckRegistry implements CheckRegistry {

    private final GuardianPlugin plugin;
    private final BiMap<Class<? extends CheckBlueprint>, CheckBlueprint> checkRegistry = HashBiMap.create();

    public GuardianCheckRegistry(GuardianPlugin plugin) {
        this.plugin = plugin;
    }

    @Override
    public <C> void put(@Nonnull C pluginContainer, @Nonnull Class<? extends CheckBlueprint> key, @Nonnull CheckBlueprint check) {
        if (this.checkRegistry.containsKey(key)) {
            this.plugin.getLogger().warn(ConsoleFormatter.builder()
                    .fg(Ansi.Color.YELLOW,
                            "Attempted to put a check into the registry that already exists!")
                    .build().get()
            );

            return;
        }

        this.checkRegistry.put(key, check);
    }

    @Nonnull
    @Override
    @SuppressWarnings("unchecked")
    public <E, F extends DetectionConfiguration> CheckBlueprint<E, F> expect(@Nonnull Class<? extends CheckBlueprint<E, F>> key) throws NoSuchElementException {
        if (!this.checkRegistry.containsKey(key)) throw new NoSuchElementException();
        return (CheckBlueprint<E, F>) this.checkRegistry.get(key);
    }

    @Nullable
    @Override
    public CheckBlueprint get(@Nonnull Class<? extends CheckBlueprint> key) {
        return this.checkRegistry.get(key);
    }

    @Nullable
    @Override
    public Class<? extends CheckBlueprint> key(@Nonnull CheckBlueprint check) {
        return this.checkRegistry.inverse().get(check);
    }

    @Nonnull
    @Override
    public Set<Class<? extends CheckBlueprint>> keySet() {
        return this.checkRegistry.keySet();
    }

    @Nonnull
    @Override
    public Iterator<CheckBlueprint> iterator() {
        return this.checkRegistry.values().iterator();
    }
}
