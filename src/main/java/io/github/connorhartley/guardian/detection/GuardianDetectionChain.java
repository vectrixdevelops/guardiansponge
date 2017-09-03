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
package io.github.connorhartley.guardian.detection;

import com.ichorpowered.guardian.api.detection.DetectionChain;
import com.ichorpowered.guardian.api.detection.DetectionConfiguration;
import com.ichorpowered.guardian.api.detection.check.CheckBlueprint;
import com.ichorpowered.guardian.api.detection.heuristic.Heuristic;

import java.util.Collections;
import java.util.List;
import java.util.Set;

public class GuardianDetectionChain<E, F extends DetectionConfiguration> implements DetectionChain<E, F> {

    private final Set<Class<? extends CheckBlueprint<E, F>>> checks = Collections.emptySet();
    private final Set<Class<? extends Heuristic>> heuristics = Collections.emptySet();

    public GuardianDetectionChain(Builder<E, F> builder) {
        this.checks.addAll(builder.checks);
        this.heuristics.addAll(builder.heuristics);
    }

    @Override
    public Set<Class<? extends CheckBlueprint<E, F>>> checkKeys() {
        return this.checks;
    }

    @Override
    public Set<Class<? extends Heuristic>> heuristicKeys() {
        return this.heuristics;
    }

    public static class Builder<E, F extends DetectionConfiguration> implements DetectionChain.Builder<E, F> {

        private final List<Class<? extends CheckBlueprint<E, F>>> checks = Collections.emptyList();
        private final List<Class<? extends Heuristic>> heuristics = Collections.emptyList();

        public Builder() {}

        @Override
        public DetectionChain.Builder<E, F> check(Class<? extends CheckBlueprint<E, F>> aClass) {
            this.checks.add(aClass);
            return this;
        }

        @Override
        public DetectionChain.Builder<E, F> heuristic(Class<? extends Heuristic> aClass) {
            this.heuristics.add(aClass);
            return this;
        }

        @Override
        public DetectionChain<E, F> build() {
            return new GuardianDetectionChain<>(this);
        }

    }

}
