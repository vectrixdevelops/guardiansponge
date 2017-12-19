package com.ichorpowered.guardian.detection.heuristic;

import com.google.common.collect.Maps;
import com.ichorpowered.guardianapi.detection.heuristic.Heuristic;
import com.ichorpowered.guardianapi.detection.heuristic.HeuristicModel;
import com.ichorpowered.guardianapi.detection.stage.model.StageModel;

import java.util.Iterator;
import java.util.Map;
import java.util.Optional;

public class GuardianHeuristicModel implements HeuristicModel {

    private Map<Class<? extends Heuristic>, Heuristic> heuristicMap = Maps.newHashMap();

    @Override
    public String getId() {
        return "guardian:model:heuristic";
    }

    @Override
    public String getName() {
        return "Guardian Heuristic Model";
    }

    @Override
    public StageModel register(Heuristic stage) {
        this.heuristicMap.put(stage.getClass(), stage);
        return this;
    }

    @Override
    public StageModel unregister(Class<? extends Heuristic> stageClass) {
        this.heuristicMap.remove(stageClass);
        return this;
    }

    @Override
    public StageModel unregister(Heuristic stage) {
        this.heuristicMap.remove(stage.getClass(), stage);
        return this;
    }

    @Override
    public Optional<? extends Heuristic> get(Class<? extends Heuristic> stageClass) {
        return Optional.ofNullable(this.heuristicMap.get(stageClass));
    }

    @Override
    public Iterator<Heuristic> iterator() {
        return this.heuristicMap.values().iterator();
    }
}
