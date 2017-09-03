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

import com.google.common.collect.BiMap;
import com.google.common.collect.HashBiMap;
import com.ichorpowered.guardian.api.detection.Detection;
import com.ichorpowered.guardian.api.detection.DetectionConfiguration;
import com.ichorpowered.guardian.api.detection.DetectionRegistry;
import io.github.connorhartley.guardian.GuardianPlugin;
import io.github.connorhartley.guardian.util.ConsoleFormatter;
import org.fusesource.jansi.Ansi;

import java.util.Iterator;
import java.util.NoSuchElementException;
import java.util.Set;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public class GuardianDetectionRegistry implements DetectionRegistry {

    private final GuardianPlugin plugin;
    private final BiMap<Class<? extends Detection>, Detection> detectionRegistry = HashBiMap.create();

    public GuardianDetectionRegistry(GuardianPlugin plugin) {
        this.plugin = plugin;
    }

    @Override
    public <C> void put(@Nonnull C plugin, @Nonnull Class<? extends Detection> aClass, @Nonnull Detection detection) {
        if (this.detectionRegistry.containsKey(aClass)) {
            this.plugin.getLogger().warn(ConsoleFormatter.builder()
                    .fg(Ansi.Color.YELLOW,
                            "Attempted to put a detection into the registry that already exists!")
                    .build().get()
            );

            return;
        }

        this.detectionRegistry.put(aClass, detection);
    }

    @Nonnull
    @Override
    @SuppressWarnings("unchecked")
    public <E, F extends DetectionConfiguration> Detection<E, F> expect(@Nonnull Class<? extends Detection<E, F>> aClass) throws NoSuchElementException {
        if (!this.detectionRegistry.containsKey(aClass)) throw new NoSuchElementException();
        return (Detection<E, F>) this.detectionRegistry.get(aClass);
    }

    @Nullable
    @Override
    public Detection get(@Nonnull Class<? extends Detection> aClass) {
        return this.detectionRegistry.get(aClass);
    }

    @Nullable
    @Override
    public Class<? extends Detection> key(@Nonnull Detection detection) {
        return this.detectionRegistry.inverse().get(detection);
    }

    @Nonnull
    @Override
    public Set<Class<? extends Detection>> keySet() {
        return this.detectionRegistry.keySet();
    }

    @Override
    public Iterator<Detection> iterator() {
        return this.detectionRegistry.values().iterator();
    }

}
