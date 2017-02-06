package io.github.connorhartley.guardian.internal.detections;

import com.me4502.modularframework.module.Module;
import io.github.connorhartley.guardian.detection.Detection;
import io.github.connorhartley.guardian.detection.check.CheckProvider;
import io.github.connorhartley.guardian.util.StorageValue;
import io.github.connorhartley.guardian.util.storage.StorageConsumer;
import ninja.leaping.configurate.ConfigurationNode;
import ninja.leaping.configurate.commented.CommentedConfigurationNode;

import java.util.List;

@Module(moduleId = "speed_detection", moduleName = "Speed Detection", moduleVersion = "0.0.1", onEnable = "onConstruction", onDisable = "onDeconstruction")
public class SpeedDetection implements Detection, StorageConsumer {

    private ConfigurationNode globalConfiguration;

    @Override
    public void onConstruction() {

    }

    @Override
    public void onDeconstruction() {

    }

    @Override
    public List<CheckProvider> getChecks() {
        return null;
    }

    @Override
    public StorageValue[] getStorageNodes() {
        return new StorageValue[0];
    }
}
