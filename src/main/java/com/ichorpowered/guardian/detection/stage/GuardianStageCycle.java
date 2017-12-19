package com.ichorpowered.guardian.detection.stage;

import com.ichorpowered.guardianapi.detection.stage.Stage;
import com.ichorpowered.guardianapi.detection.stage.cycle.StageCycle;
import com.ichorpowered.guardianapi.detection.stage.model.StageModel;

import java.util.List;

public class GuardianStageCycle implements StageCycle {

    public GuardianStageCycle() {

    }

    @Override
    public void next() {

    }

    @Override
    public boolean hasNext() {
        return false;
    }

    @Override
    public String getModelId() {
        return null;
    }

    @Override
    public <T extends Stage> StageModel<T> getModel() {
        return null;
    }

    @Override
    public String getStageId() {
        return null;
    }

    @Override
    public <T extends Stage> T getStage() {
        return null;
    }

    @Override
    public int size() {
        return 0;
    }

    @Override
    public List<Class<? extends StageModel<?>>> getAll() {
        return null;
    }

    @Override
    public int totalSize() {
        return 0;
    }
}
