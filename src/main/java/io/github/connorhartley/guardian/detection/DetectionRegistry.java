package io.github.connorhartley.guardian.detection;

import com.me4502.precogs.detection.CommonDetectionTypes;
import io.github.connorhartley.guardian.storage.StorageSupplier;

import java.io.File;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

public final class DetectionRegistry {

    private static Map<Detection<?, ?>, CommonDetectionTypes.Category> registry = new HashMap<>();

    public DetectionRegistry() {}

    public static <E, F extends StorageSupplier<File>> void register(Detection<E, F> detection, CommonDetectionTypes.Category detectionType) {
        if (!registry.containsKey(detection)) {
            registry.put(detection, detectionType);
        }
    }

    @SuppressWarnings("unchecked")
    public static <E, F extends StorageSupplier<File>> Optional<Detection<E, F>> get(Class<? extends Detection<E, F>> detection) {
        for (Detection<?, ?> detectionObject : registry.keySet()) {
            if (detection.isInstance(detectionObject)) {
                return Optional.of((Detection<E, F>) detectionObject);
            }
        }
        return Optional.empty();
    }

    public static <E, F extends StorageSupplier<File>> Optional<CommonDetectionTypes.Category> getType(Class<? extends Detection<E, F>> detection) {
        for (Detection<?, ?> detectionObject : registry.keySet()) {
            if (detection.isInstance(detectionObject)) {
                return Optional.of(registry.get(detectionObject));
            }
        }
        return Optional.empty();
    }

}
