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
package io.github.connorhartley.guardian.internal.capture;

import com.google.common.reflect.TypeToken;
import com.ichorpowered.guardian.api.detection.Detection;
import com.ichorpowered.guardian.api.detection.DetectionConfiguration;
import com.ichorpowered.guardian.api.entry.EntityEntry;
import com.ichorpowered.guardian.api.sequence.capture.CaptureContainer;
import com.ichorpowered.guardian.api.util.key.NamedKey;
import io.github.connorhartley.guardian.sequence.capture.AbstractCapture;
import org.spongepowered.api.entity.Entity;

import javax.annotation.Nonnull;

public class PlayerLocationCapture<E, F extends DetectionConfiguration> extends AbstractCapture<E, F> {

    public static NamedKey INITIAL_LOCATION =
            NamedKey.of(PlayerLocationCapture.class.getCanonicalName().toUpperCase() + "_INITIAL_LOCATION");

    public static NamedKey PRESET_LOCATION =
            NamedKey.of(PlayerLocationCapture.class.getCanonicalName().toUpperCase() + "_PRESENT_LOCATION");

    public static NamedKey UPDATE =
            NamedKey.of(PlayerLocationCapture.class.getCanonicalName().toUpperCase() + "_UPDATE");

    public PlayerLocationCapture(@Nonnull E owner, @Nonnull Detection<E, F> detection) {
        super(owner, detection);
    }

    @Override
    public void start(@Nonnull EntityEntry entry, @Nonnull CaptureContainer captureContainer) {
        if (!entry.getEntity(TypeToken.of(Entity.class)).isPresent()) return;
        Entity entity = entry.getEntity(TypeToken.of(Entity.class)).get();

        captureContainer.put(PlayerLocationCapture.INITIAL_LOCATION, entity.getLocation());
        captureContainer.put(PlayerLocationCapture.PRESET_LOCATION, entity.getLocation());
        captureContainer.put(PlayerLocationCapture.UPDATE, 0);
    }

    @Override
    public void update(@Nonnull EntityEntry entry, @Nonnull CaptureContainer captureContainer) {
        if (!entry.getEntity(TypeToken.of(Entity.class)).isPresent()) return;
        Entity entity = entry.getEntity(TypeToken.of(Entity.class)).get();

        captureContainer.put(PlayerLocationCapture.PRESET_LOCATION, entity.getLocation());
        captureContainer.<Integer>transform(PlayerLocationCapture.UPDATE, old -> old + 1);
    }

    @Override
    public void stop(@Nonnull EntityEntry entry, @Nonnull CaptureContainer captureContainer) {}

}
