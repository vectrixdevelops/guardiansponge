package com.ichorpowered.guardian.detection;

import com.google.common.reflect.TypeToken;
import com.ichorpowered.guardian.util.property.PropertyInjector;
import com.ichorpowered.guardian.util.property.PropertyInjectorFactory;
import com.ichorpowered.guardianapi.detection.Detection;
import com.ichorpowered.guardianapi.detection.DetectionBuilder;
import com.ichorpowered.guardianapi.detection.DetectionContentLoader;
import com.ichorpowered.guardianapi.detection.DetectionManager;
import com.ichorpowered.guardianapi.detection.stage.Stage;
import com.ichorpowered.guardianapi.detection.stage.model.StageModel;
import com.ichorpowered.guardianapi.detection.stage.model.StageModelArchetype;
import com.ichorpowered.guardianapi.detection.stage.model.StageModelBuilder;

import java.util.List;

public class GuardianDetectionBuilder implements DetectionBuilder {

    private final GuardianDetectionManager detectionManager;
    private final Detection detection;

    private String id;
    private String name;
    private List<StageModelArchetype> stageModelArchetypes;
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
        PropertyInjector propertyInjector = PropertyInjectorFactory.create(this.detection);

        propertyInjector
                .inject(TypeToken.of(String.class), "id", this.id)
                .inject(TypeToken.of(String.class), "name", this.name)
                // TODO: Stage model.
                .inject("contentLoader", this.detectionContentLoader);

        return this.detectionManager;
    }
}
