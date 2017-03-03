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
package io.github.connorhartley.guardian.internal.detections;

import com.google.inject.Inject;
import com.me4502.modularframework.module.Module;
import com.me4502.modularframework.module.guice.ModuleContainer;
import com.me4502.precogs.detection.CommonDetectionTypes;
import io.github.connorhartley.guardian.Guardian;
import io.github.connorhartley.guardian.context.ContextProvider;
import io.github.connorhartley.guardian.detection.Detection;
import io.github.connorhartley.guardian.detection.DetectionTypes;
import io.github.connorhartley.guardian.detection.check.CheckProvider;
import io.github.connorhartley.guardian.internal.checks.HorizontalSpeedCheck;
import io.github.connorhartley.guardian.storage.container.StorageValue;
import io.github.connorhartley.guardian.storage.StorageConsumer;
import ninja.leaping.configurate.ConfigurationNode;
import org.spongepowered.api.plugin.PluginContainer;

import java.util.Collections;
import java.util.List;
import java.util.Optional;

@Module(id = "speed_detection",
        name = "HorizontalSpeed Detection",
        authors = { "Connor Hartley (vectrix)" },
        version = "0.0.2",
        onEnable = "onConstruction",
        onDisable = "onDeconstruction")
public class SpeedDetection extends Detection implements StorageConsumer {

    @Inject
    @ModuleContainer
    private PluginContainer moduleContainer;

    private Guardian plugin;
    private ConfigurationNode globalConfigurationNode;
    private boolean ready = false;

    private static final Module moduleAnnotation = SpeedDetection.class.getAnnotation(Module.class);

    public SpeedDetection() {
        super(moduleAnnotation.id(), moduleAnnotation.name());
    }

    @Override
    public void onConstruction() {
        if (!moduleContainer.getInstance().isPresent());
        this.plugin = (Guardian) moduleContainer.getInstance().get();

        this.globalConfigurationNode = this.plugin.getGlobalConfiguration().getConfigurationNode();

        DetectionTypes.SPEED_DETECTION = Optional.of(this);

        this.ready = true;
    }

    @Override
    public void onDeconstruction() {
        this.ready = false;
    }

    @Override
    public ContextProvider getContextProvider() {
        return this.plugin;
    }

    @Override
    public Object getPlugin() {
        return this.plugin;
    }

    @Override
    public boolean isReady() {
        return this.ready;
    }

    @Override
    public List<CheckProvider> getChecks() {
        return Collections.singletonList(new HorizontalSpeedCheck.Provider(this));
    }

    @Override
    public CommonDetectionTypes.Category getCategory() {
        return CommonDetectionTypes.Category.MOVEMENT;
    }

    @Override
    public StorageValue<?, ?>[] getStorageNodes() {
        return new StorageValue<?, ?>[0];
    }
}
