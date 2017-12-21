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

import com.google.common.collect.Lists;
import com.google.common.reflect.TypeToken;
import com.ichorpowered.guardian.detection.stage.GuardianStageCycle;
import com.ichorpowered.guardian.util.property.PropertyInjectorFactory;
import com.ichorpowered.guardianapi.detection.Detection;
import com.ichorpowered.guardianapi.detection.DetectionBuilder;
import com.ichorpowered.guardianapi.detection.DetectionContentLoader;
import com.ichorpowered.guardianapi.detection.DetectionManager;
import com.ichorpowered.guardianapi.detection.stage.Stage;
import com.ichorpowered.guardianapi.detection.stage.StageCycle;
import com.ichorpowered.guardianapi.detection.stage.model.StageModel;
import com.ichorpowered.guardianapi.detection.stage.model.StageModelArchetype;
import com.ichorpowered.guardianapi.detection.stage.model.StageModelBuilder;

import java.util.List;

public class GuardianDetectionBuilder implements DetectionBuilder {

    private final GuardianDetectionManager detectionManager;
    private final Detection detection;
    private final List<StageModelArchetype<?>> stageModelArchetypes = Lists.newArrayList();

    private String id;
    private String name;
    private DetectionContentLoader detectionContentLoader;

    public GuardianDetectionBuilder(final GuardianDetectionManager detectionManager, final Detection detection) {
        this.detectionManager = detectionManager;
        this.detection = detection;
    }

    @Override
    public DetectionBuilder id(String id) {
        this.id = id;
        return this;
    }

    @Override
    public DetectionBuilder name(String name) {
        this.name = name;
        return this;
    }

    @Override
    public <T extends Stage> StageModelBuilder<T> stage(Class<? extends StageModel<T>> stageModelClass) {
        return null;
    }

    @Override
    public DetectionBuilder stage(StageModelArchetype stageModelArchetype) {
        this.stageModelArchetypes.add(stageModelArchetype);
        return this;
    }

    @Override
    public DetectionBuilder contentLoader(DetectionContentLoader contentLoader) {
        this.detectionContentLoader = contentLoader;
        return this;
    }

    @Override
    public DetectionManager submit(Object plugin) {
        PropertyInjectorFactory.create(this.detection)
                .inject(TypeToken.of(String.class), "id", this.id)
                .inject(TypeToken.of(String.class), "name", this.name)
                .inject(TypeToken.of(StageCycle.class), "stageCycle", new GuardianStageCycle(this.stageModelArchetypes))
                .inject(TypeToken.of(DetectionContentLoader.class), "contentLoader", this.detectionContentLoader);

        return this.detectionManager;
    }
}
