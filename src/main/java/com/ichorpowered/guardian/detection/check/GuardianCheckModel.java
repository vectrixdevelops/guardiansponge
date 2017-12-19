package com.ichorpowered.guardian.detection.check;

import com.google.common.collect.Maps;
import com.ichorpowered.guardianapi.detection.check.Check;
import com.ichorpowered.guardianapi.detection.check.CheckModel;
import com.ichorpowered.guardianapi.detection.stage.model.StageModel;

import java.util.Iterator;
import java.util.Map;
import java.util.Optional;

public class GuardianCheckModel implements CheckModel {

    private Map<Class<? extends Check>, Check> checkMap = Maps.newHashMap();

    @Override
    public String getId() {
        return "guardian:model:check";
    }

    @Override
    public String getName() {
        return "Guardian Check Model";
    }

    @Override
    public StageModel register(Check stage) {
        this.checkMap.put(stage.getClass(), stage);
        return this;
    }

    @Override
    public StageModel unregister(Class<? extends Check> stageClass) {
        this.checkMap.remove(stageClass);
        return this;
    }

    @Override
    public StageModel unregister(Check stage) {
        this.checkMap.remove(stage.getClass(), stage);
        return this;
    }

    @Override
    public Optional<? extends Check> get(Class<? extends Check> stageClass) {
        return Optional.ofNullable(this.checkMap.get(stageClass));
    }

    @Override
    public Iterator<Check> iterator() {
        return this.checkMap.values().iterator();
    }
}
