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
package com.ichorpowered.guardian.detection.stage;

import com.google.common.collect.HashMultimap;
import com.google.common.collect.Lists;
import com.google.common.collect.Multimap;
import com.ichorpowered.guardianapi.detection.DetectionManager;
import com.ichorpowered.guardianapi.detection.stage.Stage;
import com.ichorpowered.guardianapi.detection.stage.StageCycle;
import com.ichorpowered.guardianapi.detection.stage.model.StageModel;
import com.ichorpowered.guardianapi.detection.stage.model.StageModelArchetype;

import java.util.Iterator;
import java.util.List;
import java.util.Optional;
import java.util.function.Predicate;

public class GuardianStageCycle implements StageCycle {

    private final DetectionManager detectionManager;
    private final List<StageModelArchetype<?>> modelArchetypes;
    private final Multimap<StageModel<?>, Stage> filteredStages = HashMultimap.create();

    private Iterator<StageModel<?>> modelIterator;
    private Iterator<? extends Stage> stageIterator;
    private StageModel<?> presentStageModel;
    private Stage presentStage;

    public GuardianStageCycle(final DetectionManager detectionManager,
                              final List<StageModelArchetype<?>> modelArchetypes) {
        this.detectionManager = detectionManager;
        this.modelArchetypes = modelArchetypes;

        this.evaluateCycle();
    }

    @Override
    @SuppressWarnings("unchecked")
    public boolean next() {
        final Boolean cycleInit = this.initializeCycle();
        if (cycleInit != null) {
            return cycleInit;
        }

        if (!this.stageIterator.hasNext()) {
            if (this.modelIterator.hasNext()) {
                this.presentStageModel = this.modelIterator.next();

                // Setup Stage Iterator
                this.stageIterator = this.presentStageModel.iterator();

                if (this.stageIterator.hasNext()) {
                    this.presentStage = this.stageIterator.next();
                } else {
                    // Shouldn't reach this point but in case it does.
                    this.next();
                }
            } else {
                // finished
                this.modelIterator = null;
                this.stageIterator = null;

                return false;
            }
        } else {
            this.presentStage = this.stageIterator.next();
        }

        return true;
    }

    @Override
    public boolean nextModel() {
        final Boolean cycleInit = this.initializeCycle();
        if (cycleInit != null) {
            return cycleInit;
        }

        if (this.modelIterator.hasNext()) {
            this.presentStageModel = this.modelIterator.next();

            // Setup Stage Iterator
            this.stageIterator = this.presentStageModel.iterator();

            if (this.stageIterator.hasNext()) {
                this.presentStage = this.stageIterator.next();
            } else {
                // Shouldn't reach this point but in case it does.
                this.nextModel();
            }
        } else {
            // finished
            this.modelIterator = null;
            this.stageIterator = null;

            return false;
        }

        return true;
    }

    @Override
    public boolean hasNext() {
        return this.stageIterator.hasNext() || this.modelIterator.hasNext();
    }

    @Override
    public Optional<String> getModelId() {
        return Optional.ofNullable(this.presentStageModel.getId());
    }

    @Override
    public <T extends Stage> Optional<StageModel<T>> getModel() {
        return Optional.ofNullable((StageModel<T>) this.presentStageModel);
    }

    @Override
    public Optional<String> getStageId() {
        return Optional.ofNullable(this.presentStage.getId());
    }

    @Override
    public <T extends Stage> Optional<T> getStage() {
        return Optional.ofNullable((T) this.presentStage);
    }

    @Override
    public int size() {
        if (this.presentStageModel == null) return 0;
        return this.filteredStages.get(this.presentStageModel).size();
    }

    @Override
    @SuppressWarnings("unchecked")
    public List<Class<? extends StageModel<?>>> getAll() {
        return Lists.newArrayList((Class<? extends StageModel<?>>[]) this.modelArchetypes.stream()
                .map(stageModelArchetype -> stageModelArchetype.getModelClass())
                .toArray());
    }

    @Override
    public int sizeFor(StageModel<?> stageModel) {
        return this.filteredStages.get(stageModel).size();
    }

    @Override
    public int totalSize() {
        return this.filteredStages.size();
    }

    private Boolean initializeCycle() {
        if (this.modelIterator == null && this.stageIterator == null) {
            this.modelIterator = this.filteredStages.keys().iterator();

            if (this.modelIterator.hasNext()) {
                this.presentStageModel = this.modelIterator.next();

                // Setup Stage Iterator
                this.stageIterator = this.presentStageModel.iterator();

                if (this.stageIterator.hasNext()) {
                    this.presentStage = this.stageIterator.next();
                }
            } else {
                // finished
                return false;
            }

            return true;
        }

        return null;
    }

    private void evaluateCycle() {
        for (StageModelArchetype<?> stageModelArchetype : this.modelArchetypes) {
            final StageModel<?> stageModel = this.detectionManager.getStageModel(stageModelArchetype.getModelClass()).orElse(null);
            final Predicate<Stage> currentFilter = (Predicate<Stage>) stageModelArchetype.getModelFilter();

            if (stageModel == null) continue;

            for (Stage stage : stageModel) {
                if (currentFilter.test(stage)) this.filteredStages.put(stageModel, stage);
            }
        }
    }
}
