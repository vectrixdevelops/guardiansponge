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
package com.ichorpowered.guardian.sponge.common.heuristic;

import com.google.common.reflect.TypeToken;
import com.ichorpowered.guardian.api.detection.Detection;
import com.ichorpowered.guardian.api.detection.stage.Stage;
import com.ichorpowered.guardian.api.detection.stage.process.Heuristic;
import com.ichorpowered.guardian.api.sequence.process.Process;
import com.ichorpowered.guardian.common.detection.stage.type.HeuristicStageImpl;
import org.checkerframework.checker.nullness.qual.NonNull;

public class DistributionHeuristic implements Heuristic {

    @Override
    public boolean test(final @NonNull Process process) {
        final Detection detection = process.getContext().get("detection", TypeToken.of(Detection.class));
        final Double severity = process.getContext().get("detection_severity", TypeToken.of(Double.class));

        if (detection == null || severity == null) return false;

        final double minimumRange = detection.getConfiguration().getNode("options", "heuristic-range-minimum").getDouble(1);
        final double maximumRange = detection.getConfiguration().getNode("options", "heuristic-range-maximum").getDouble(100);

        double probability = (severity / maximumRange) * 100;
        if (probability > maximumRange) probability = 100;
        if (probability > minimumRange) process.getContext().set("detection_probability", TypeToken.of(Double.class), probability);

        return true;
    }

    @Override
    public @NonNull Class<? extends Stage<?>> getStageType() {
        return HeuristicStageImpl.class;
    }

}
