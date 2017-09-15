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
package io.github.connorhartley.guardian.detection.heuristics;

import com.google.common.collect.BiMap;
import com.google.common.collect.HashBiMap;
import com.ichorpowered.guardian.api.detection.heuristic.Heuristic;
import com.ichorpowered.guardian.api.detection.heuristic.HeuristicRegistry;
import io.github.connorhartley.guardian.GuardianPlugin;
import io.github.connorhartley.guardian.util.ConsoleFormatter;
import org.fusesource.jansi.Ansi;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.Iterator;
import java.util.Set;

public class GuardianHeuristicRegistry implements HeuristicRegistry {

    private final GuardianPlugin plugin;
    private final BiMap<Class<? extends Heuristic>, Heuristic> heuristicRegistry = HashBiMap.create();

    public GuardianHeuristicRegistry(GuardianPlugin plugin) {
        this.plugin = plugin;
    }

    @Override
    public <C> void put(@Nonnull C c, @Nonnull Class<? extends Heuristic> key, @Nonnull Heuristic heuristic) {
        if (this.heuristicRegistry.containsKey(key)) {
            this.plugin.getLogger().warn(ConsoleFormatter.builder()
                    .fg(Ansi.Color.YELLOW,
                            "Attempted to put a heuristic into the registry that already exists!")
                    .build().get()
            );

            return;
        }

        this.heuristicRegistry.put(key, heuristic);
    }

    @Nullable
    @Override
    public Heuristic get(@Nonnull Class<? extends Heuristic> key) {
        return this.heuristicRegistry.get(key);
    }

    @Nullable
    @Override
    public Class<? extends Heuristic> key(@Nonnull Heuristic heuristic) {
        return this.heuristicRegistry.inverse().get(heuristic);
    }

    @Nonnull
    @Override
    public Set<Class<? extends Heuristic>> keySet() {
        return this.heuristicRegistry.keySet();
    }

    @Override
    public Iterator<Heuristic> iterator() {
        return this.heuristicRegistry.values().iterator();
    }

}
