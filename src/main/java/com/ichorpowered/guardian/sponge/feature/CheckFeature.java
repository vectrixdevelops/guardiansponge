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
package com.ichorpowered.guardian.sponge.feature;

import com.google.common.collect.ImmutableList;
import com.ichorpowered.guardian.api.detection.DetectionController;
import com.ichorpowered.guardian.api.sequence.SequenceRegistry;
import com.ichorpowered.guardian.common.detection.stage.type.CheckStageImpl;
import com.ichorpowered.guardian.common.detection.stage.type.HeuristicStageImpl;
import com.ichorpowered.guardian.common.detection.stage.type.PenaltyStageImpl;
import com.ichorpowered.guardian.sponge.GuardianPlugin;
import com.ichorpowered.guardian.sponge.common.check.FlightCheck;
import com.ichorpowered.guardian.sponge.common.check.HorizontalCheck;
import com.ichorpowered.guardian.sponge.common.check.VerticalCheck;
import com.ichorpowered.guardian.sponge.common.heuristic.DistributionHeuristic;
import com.ichorpowered.guardian.sponge.common.penalty.ReportPenalty;

import java.util.stream.StreamSupport;

public class CheckFeature {

    private final GuardianPlugin plugin;
    private final DetectionController detectionController;
    private final SequenceRegistry sequenceRegistry;

    public CheckFeature(final GuardianPlugin plugin, final DetectionController detectionController, final SequenceRegistry sequenceRegistry) {
        this.plugin = plugin;
        this.detectionController = detectionController;
        this.sequenceRegistry = sequenceRegistry;
    }

    public void create() {
        this.detectionController.builder("flight_detection")
                .stage(CheckStageImpl.class)
                .add(FlightCheck.class)
                .max(10)
                .submit()
                .stage(HeuristicStageImpl.class)
                .add(DistributionHeuristic.class)
                .max(10)
                .submit()
                .stage(PenaltyStageImpl.class)
                .add(ReportPenalty.class)
                .max(10)
                .submit()
                .register("Flight Detection", this.plugin);

        this.detectionController.builder("speed_detection")
                .stage(CheckStageImpl.class)
                .add(HorizontalCheck.class)
                .add(VerticalCheck.class)
                .max(10)
                .submit()
                .stage(HeuristicStageImpl.class)
                .add(DistributionHeuristic.class)
                .max(10)
                .submit()
                .stage(PenaltyStageImpl.class)
                .add(ReportPenalty.class)
                .max(10)
                .submit()
                .register("Speed Detection", this.plugin);
    }

    public void register() {
        this.detectionController.forEach(detection -> {
            if (!detection.getConfiguration().getNode("enable").getBoolean(false)) {
                this.detectionController.removeDetection(detection.getId());
                return;
            }

            detection.getStageCycle().getFor(CheckStageImpl.class).forEach(stageProcess ->
                    this.sequenceRegistry.set(stageProcess.getClass(), stageProcess.getSequence(detection)));
        });
    }

    public void unregister() {
        ImmutableList.copyOf(this.detectionController).forEach(detection -> this.detectionController.removeDetection(detection.getId()));
    }

    public void categorize() {

    }

}
