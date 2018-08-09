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
package com.ichorpowered.guardian.sponge.sequence.capture;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import com.ichorpowered.guardian.api.Guardian;
import com.ichorpowered.guardian.api.game.GameReference;
import com.ichorpowered.guardian.api.game.model.value.GameValue;
import com.ichorpowered.guardian.api.game.model.value.key.GameKey;
import com.ichorpowered.guardian.api.sequence.capture.CaptureRegistry;
import com.ichorpowered.guardian.api.sequence.capture.CaptureValue;
import org.checkerframework.checker.nullness.qual.NonNull;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

public class CaptureRegistryImpl implements CaptureRegistry {

    private GameReference<?> gameReference;
    private Map<GameKey<CaptureValue>, GameValue<CaptureValue>> captures = Maps.newHashMap();
    private List<GameKey<CaptureValue>> defaults = Lists.newArrayList();

    public CaptureRegistryImpl(final GameReference<?> gameReference, final List<GameKey<CaptureValue>> defaults) {
        this.gameReference = gameReference;

        Guardian.getModelRegistry().get(gameReference).ifPresent(model -> defaults.forEach(key -> model.requestFirst(key).ifPresent(value -> this.captures.put(key, value))));

        this.defaults.addAll(defaults);
    }


    @Override
    public @NonNull GameReference<?> getGameReference() {
        return this.gameReference;
    }

    @Override
    public @NonNull Optional<GameValue<CaptureValue>> get(final @NonNull GameKey<CaptureValue> key) {
        return Optional.ofNullable(this.captures.get(key));
    }

    @Override
    public @NonNull GameValue<CaptureValue> set(final @NonNull GameKey<CaptureValue> key, final @NonNull GameValue<CaptureValue> element) {
        final GameValue<CaptureValue> captureValue = this.captures.put(key, element);
        return captureValue;
    }

    @Override
    public @NonNull Set<GameValue<CaptureValue>> values() {
        return Sets.newHashSet(this.captures.values());
    }

    @Override
    public @NonNull Set<GameKey<CaptureValue>> keys() {
        return this.captures.keySet();
    }

    @Override
    public @NonNull Set<GameKey<CaptureValue>> defaultKeys() {
        return Sets.newHashSet(this.defaults);
    }
}
