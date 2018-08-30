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
import com.google.common.collect.Lists;
import com.ichorpowered.guardian.api.detection.DetectionController;
import com.ichorpowered.guardian.api.sequence.SequenceRegistry;
import com.ichorpowered.guardian.common.detection.stage.type.CheckStageImpl;
import com.ichorpowered.guardian.common.detection.stage.type.HeuristicStageImpl;
import com.ichorpowered.guardian.common.detection.stage.type.PenaltyStageImpl;
import com.ichorpowered.guardian.sponge.GuardianDetections;
import com.ichorpowered.guardian.sponge.GuardianPlugin;
import com.ichorpowered.guardian.sponge.common.check.FlightCheck;
import com.ichorpowered.guardian.sponge.common.check.HorizontalCheck;
import com.ichorpowered.guardian.sponge.common.check.VerticalCheck;
import com.ichorpowered.guardian.sponge.common.heuristic.DistributionHeuristic;
import com.ichorpowered.guardian.sponge.common.penalty.ReportPenalty;
import com.ichorpowered.guardian.sponge.detection.DetectionProviderImpl;
import com.me4502.precogs.detection.CommonDetectionTypes;
import com.me4502.precogs.detection.DetectionType;
import org.spongepowered.api.Sponge;

public class CheckFeature {

    private final GuardianPlugin plugin;
    private final DetectionController detectionController;
    private final SequenceRegistry sequenceRegistry;

    public CheckFeature(final GuardianPlugin plugin, final DetectionController detectionController, final SequenceRegistry sequenceRegistry) {
        this.plugin = plugin;
        this.detectionController = detectionController;
        this.sequenceRegistry = sequenceRegistry;
    }

    @SuppressWarnings("unchecked")
    public void create() {
        this.detectionController.builder()
                .stage(CheckStageImpl.class, FlightCheck.class)
                .stage(HeuristicStageImpl.class, DistributionHeuristic.class)
                .stage(PenaltyStageImpl.class, ReportPenalty.class)
                .register(GuardianDetections.FLIGHT_DETECTION, this.plugin);

        this.detectionController.builder()
                .stage(CheckStageImpl.class, HorizontalCheck.class, VerticalCheck.class)
                .stage(HeuristicStageImpl.class, DistributionHeuristic.class)
                .stage(PenaltyStageImpl.class, ReportPenalty.class)
                .register(GuardianDetections.SPEED_DETECTION, this.plugin);
    }

    public void register() {
        ImmutableList.copyOf(this.detectionController).forEach(detection -> {
            if (!detection.getConfiguration().getNode("enable").getBoolean(false)) {
                this.detectionController.removeDetection(detection.getId());
                return;
            }

            detection.getStageCycle().getFor(CheckStageImpl.class).forEach(stageProcess ->
                    this.sequenceRegistry.set(stageProcess.getClass(), stageProcess.getSequence(detection)));

            Sponge.getRegistry().getType(DetectionType.class, detection.getId()).ifPresent(detectionType -> {
                if (detectionType instanceof DetectionProviderImpl) ((DetectionProviderImpl) detectionType).setProvider(detection);
            });
        });
    }

    public void unregister() {
        ImmutableList.copyOf(this.detectionController).forEach(detection -> this.detectionController.removeDetection(detection.getId()));
    }

    public void categorize() {
        Sponge.getRegistry().getType(DetectionType.class, GuardianDetections.FLIGHT_DETECTION.getId()).ifPresent(detectionType ->
                CommonDetectionTypes.provideDetectionTypesFor(CommonDetectionTypes.Category.MOVEMENT, Lists.newArrayList(detectionType)));

        Sponge.getRegistry().getType(DetectionType.class, GuardianDetections.SPEED_DETECTION.getId()).ifPresent(detectionType ->
                CommonDetectionTypes.provideDetectionTypesFor(CommonDetectionTypes.Category.MOVEMENT, Lists.newArrayList(detectionType)));
    }

}
