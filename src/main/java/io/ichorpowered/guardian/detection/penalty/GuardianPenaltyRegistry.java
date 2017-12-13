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
package io.ichorpowered.guardian.detection.penalty;

import com.google.common.collect.BiMap;
import com.google.common.collect.HashBiMap;
import com.ichorpowered.guardian.api.detection.penalty.Penalty;
import com.ichorpowered.guardian.api.detection.penalty.PenaltyRegistry;
import io.ichorpowered.guardian.GuardianPlugin;
import io.ichorpowered.guardian.util.ConsoleUtil;
import org.fusesource.jansi.Ansi;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.Iterator;
import java.util.Set;

public final class GuardianPenaltyRegistry implements PenaltyRegistry {

    private GuardianPlugin plugin;
    private BiMap<Class<? extends Penalty>, Penalty> penaltyRegistry = HashBiMap.create();

    public GuardianPenaltyRegistry(@Nonnull GuardianPlugin plugin) {
        this.plugin = plugin;
    }

    @Override
    public <C> void put(@Nonnull C pluginContainer, @Nonnull Class<? extends Penalty> key, @Nonnull Penalty penalty) {
        if (this.penaltyRegistry.containsKey(key)) {
            this.plugin.getLogger().warn(ConsoleUtil.builder()
                    .add(Ansi.Color.YELLOW, "Attempted to put a penalty into the registry that already exists!")
                    .buildAndGet()
            );

            return;
        }

        this.penaltyRegistry.put(key, penalty);
    }

    @Nullable
    @Override
    public Penalty get(@Nonnull Class<? extends Penalty> key) {
        return this.penaltyRegistry.get(key);
    }

    @Nullable
    @Override
    public Class<? extends Penalty> key(@Nonnull Penalty penalty) {
        return this.penaltyRegistry.inverse().get(penalty);
    }

    @Nonnull
    @Override
    public Set<Class<? extends Penalty>> keySet() {
        return this.penaltyRegistry.keySet();
    }

    @Nonnull
    @Override
    public Iterator<Penalty> iterator() {
        return this.penaltyRegistry.values().iterator();
    }

}
