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

import com.me4502.precogs.detection.CommonDetectionTypes;
import io.github.connorhartley.guardian.storage.StorageProvider;
import tech.ferus.util.config.HoconConfigFile;

import java.nio.file.Path;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

public final class DetectionRegistry {

    private static Map<Detection<?, ?>, CommonDetectionTypes.Category> registry = new HashMap<>();

    public DetectionRegistry() {}

    public static <E, F extends StorageProvider<HoconConfigFile, Path>> void register(Detection<E, F> detection, CommonDetectionTypes.Category detectionType) {
        if (!registry.containsKey(detection)) {
            registry.put(detection, detectionType);
        }
    }

    @SuppressWarnings("unchecked")
    public static <E, F extends StorageProvider<HoconConfigFile, Path>> Optional<Detection<E, F>> get(Class<? extends Detection<E, F>> detection) {
        for (Detection<?, ?> detectionObject : registry.keySet()) {
            if (detection.isInstance(detectionObject)) {
                return Optional.of((Detection<E, F>) detectionObject);
            }
        }
        return Optional.empty();
    }

    public static <E, F extends StorageProvider<HoconConfigFile, Path>> Optional<CommonDetectionTypes.Category> getType(Class<? extends Detection<E, F>> detection) {
        for (Detection<?, ?> detectionObject : registry.keySet()) {
            if (detection.isInstance(detectionObject)) {
                return Optional.of(registry.get(detectionObject));
            }
        }
        return Optional.empty();
    }

}
