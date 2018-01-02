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
package com.ichorpowered.guardian.detection;

import com.google.common.collect.Maps;
import com.ichorpowered.guardian.GuardianPlugin;
import com.ichorpowered.guardianapi.detection.Detection;
import com.ichorpowered.guardianapi.detection.DetectionBuilder;
import com.ichorpowered.guardianapi.detection.DetectionManager;
import com.ichorpowered.guardianapi.detection.stage.model.StageModel;

import java.util.Map;
import java.util.Optional;

public final class GuardianDetectionManager implements DetectionManager {

    private final GuardianPlugin plugin;

    private final Map<String, Class<? extends Detection>> detectionIdMap = Maps.newHashMap();
    private final Map<Class<? extends Detection>, Detection> detectionInstanceMap = Maps.newHashMap();

    private final Map<String, Class<? extends StageModel<?>>> stageModelIdMap = Maps.newHashMap();
    private final Map<Class<? extends StageModel>, StageModel<?>> stageModelInstanceMap = Maps.newHashMap();

    public GuardianDetectionManager(final GuardianPlugin plugin) {
        this.plugin = plugin;
    }

    @Override
    public DetectionManager provideDetection(Class<? extends Detection> detectionClass, Detection detection) {
        this.detectionIdMap.put(detection.getId(), detectionClass);
        this.detectionInstanceMap.put(detectionClass, detection);
        return this;
    }

    @Override
    public DetectionManager provideStageModel(Class<? extends StageModel<?>> stageModelClass, StageModel<?> stageModel) {
        this.stageModelIdMap.put(stageModel.getId(), stageModelClass);
        this.stageModelInstanceMap.put(stageModelClass, stageModel);
        return this;
    }

    @Override
    public Optional<DetectionBuilder> provider(Class<? extends Detection> detectionClass) {
        if (!this.detectionInstanceMap.containsKey(detectionClass)) return Optional.empty();
        return Optional.of(new GuardianDetectionBuilder(this, this.detectionInstanceMap.get(detectionClass)));
    }

    @Override
    public Optional<? extends Detection> getDetection(String id) {
        return Optional.ofNullable(this.detectionInstanceMap.get(this.detectionIdMap.get(id)));
    }

    @Override
    public Optional<? extends Detection> getDetection(Class<? extends Detection> detectionClass) {
        return Optional.ofNullable(this.detectionInstanceMap.get(detectionClass));
    }

    @Override
    public Optional<? extends StageModel<?>> getStageModel(String id) {
        return Optional.ofNullable(this.stageModelInstanceMap.get(this.stageModelIdMap.get(id)));
    }

    @Override
    public Optional<? extends StageModel<?>> getStageModel(Class<? extends StageModel<?>> stageModelClass) {
        return Optional.ofNullable(this.stageModelInstanceMap.get(stageModelClass));
    }
}
