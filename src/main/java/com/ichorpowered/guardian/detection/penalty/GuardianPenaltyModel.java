package com.ichorpowered.guardian.detection.penalty;

import com.google.common.collect.Maps;
import com.ichorpowered.guardianapi.detection.penalty.Penalty;
import com.ichorpowered.guardianapi.detection.penalty.PenaltyModel;
import com.ichorpowered.guardianapi.detection.stage.model.StageModel;

import java.util.Iterator;
import java.util.Map;
import java.util.Optional;

public class GuardianPenaltyModel implements PenaltyModel {

    private Map<Class<? extends Penalty>, Penalty> penaltyMap = Maps.newHashMap();

    @Override
    public String getId() {
        return "guardian:model:penalty";
    }

    @Override
    public String getName() {
        return "Guardian Penalty Model";
    }

    @Override
    public StageModel register(Penalty stage) {
        this.penaltyMap.put(stage.getClass(), stage);
        return this;
    }

    @Override
    public StageModel unregister(Class<? extends Penalty> stageClass) {
        this.penaltyMap.remove(stageClass);
        return this;
    }

    @Override
    public StageModel unregister(Penalty stage) {
        this.penaltyMap.remove(stage.getClass(), stage);
        return this;
    }

    @Override
    public Optional<? extends Penalty> get(Class<? extends Penalty> stageClass) {
        return Optional.ofNullable(this.penaltyMap.get(stageClass));
    }

    @Override
    public Iterator<Penalty> iterator() {
        return this.penaltyMap.values().iterator();
    }
}
