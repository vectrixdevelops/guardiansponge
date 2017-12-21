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
