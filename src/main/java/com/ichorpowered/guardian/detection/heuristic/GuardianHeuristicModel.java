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
