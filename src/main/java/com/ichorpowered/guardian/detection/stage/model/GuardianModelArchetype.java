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
package com.ichorpowered.guardian.detection.stage.model;

import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import com.ichorpowered.guardianapi.detection.DetectionBuilder;
import com.ichorpowered.guardianapi.detection.stage.Stage;
import com.ichorpowered.guardianapi.detection.stage.model.StageModel;
import com.ichorpowered.guardianapi.detection.stage.model.StageModelArchetype;
import com.ichorpowered.guardianapi.detection.stage.model.StageModelBuilder;

import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.function.Predicate;

public class GuardianModelArchetype<T extends Stage> implements StageModelArchetype<T> {

    private final Class<? extends StageModel<T>> stageModelClass;
    private final List<Class<? extends T>> includes;
    private final Set<Class<? extends T>> excludes;
    private final Set<Predicate<T>> filters;
    private final int minimum;
    private final int maximum;

    private final Predicate<T> modelFilter;

    public GuardianModelArchetype(final Builder<T> builder) {
        this.stageModelClass = builder.stageModelClass;
        this.includes = builder.includes;
        this.excludes = builder.excludes;
        this.filters = builder.filters;
        this.minimum = builder.minimum;
        this.maximum = builder.maximum;

        // Model Filter

        this.modelFilter = t -> {
            if (this.excludes.contains(t.getClass())) return false;
            if (this.includes.contains(t.getClass())) return true;

            for (Predicate<T> predicate : this.filters) {
                if (predicate.test(t)) return true;
            }

            return false;
        };
    }

    @Override
    public Class<? extends StageModel<T>> getModelClass() {
        return this.stageModelClass;
    }

    @Override
    public Predicate<T> getModelFilter() {
        return this.modelFilter;
    }

    @Override
    public List<Class<? extends T>> getIncludes() {
        return this.includes;
    }

    @Override
    public Set<Class<? extends T>> getExcludes() {
        return this.excludes;
    }

    @Override
    public Set<Predicate<T>> getFilters() {
        return this.filters;
    }

    @Override
    public int getMinimum() {
        return this.minimum;
    }

    @Override
    public int getMaximum() {
        return this.maximum;
    }

    public static class Builder<T extends Stage> implements StageModelBuilder<T> {

        private final DetectionBuilder detectionBuilder;
        private final Class<? extends StageModel<T>> stageModelClass;

        private final List<Class<? extends T>> includes = Lists.newArrayList();
        private final Set<Class<? extends T>> excludes = Sets.newHashSet();
        private final Set<Predicate<T>> filters = Sets.newHashSet();
        private int minimum = 0;
        private int maximum = Integer.MAX_VALUE;

        public Builder(final DetectionBuilder detectionBuilder,
                                    final Class<? extends StageModel<T>> stageModelClass) {
            this.detectionBuilder = detectionBuilder;
            this.stageModelClass = stageModelClass;
        }

        @Override
        public StageModelBuilder<T> include(final Class<? extends T> stageClass) {
            if (!this.includes.contains(stageClass)) {
                this.includes.add(stageClass);
            }
            return this;
        }

        @Override
        public StageModelBuilder<T> exclude(final Class<? extends T> stageClass) {
            if (!this.excludes.contains(stageClass)) {
                this.excludes.add(stageClass);
            }
            return this;
        }

        @Override
        public StageModelBuilder<T> filter(final Predicate<T> stagePredicate) {
            if (!this.filters.contains(stagePredicate)) {
                this.filters.add(stagePredicate);
            }
            return this;
        }

        @Override
        public StageModelBuilder<T> min(final int minimum) {
            this.minimum = minimum;
            return this;
        }

        @Override
        public StageModelBuilder<T> max(final int maximum) {
            this.maximum = maximum;
            return this;
        }

        @Override
        public DetectionBuilder append() {
            this.detectionBuilder.stage(new GuardianModelArchetype<>(this));

            return this.detectionBuilder;
        }
    }
}
