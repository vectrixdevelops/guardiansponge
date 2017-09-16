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
package io.github.connorhartley.guardian;

import com.ichorpowered.guardian.api.phase.type.PhaseTypes;
import com.me4502.modularframework.ModuleController;
import io.github.connorhartley.guardian.detection.check.GuardianCheckViewer;
import io.github.connorhartley.guardian.detection.heuristics.GuardianHeuristicViewer;
import io.github.connorhartley.guardian.detection.penalty.GuardianPenaltyViewer;
import io.github.connorhartley.guardian.internal.check.movement.HorizontalSpeedCheck;
import io.github.connorhartley.guardian.internal.check.movement.InvalidCheck;
import io.github.connorhartley.guardian.internal.check.movement.VerticalSpeedCheck;
import io.github.connorhartley.guardian.internal.penalty.ResetPenalty;

import javax.annotation.Nonnull;

final class GuardianLoader {

    private final GuardianPlugin plugin;

    GuardianLoader(@Nonnull GuardianPlugin plugin) {
        this.plugin = plugin;
    }

    void loadModules(ModuleController<GuardianPlugin> moduleController) {
        moduleController.registerModule("io.github.connorhartley.guardian.internal.detection.InvalidMovementDetection");
        moduleController.registerModule("io.github.connorhartley.guardian.internal.detection.MovementSpeedDetection");
    }

    void loadChecks() {
        if (this.plugin.getCheckRegistry() == null) return;

        this.plugin.getCheckRegistry().put(this.plugin, HorizontalSpeedCheck.Blueprint.class,
                new HorizontalSpeedCheck.Blueprint());
        this.plugin.getCheckRegistry().put(this.plugin, VerticalSpeedCheck.Blueprint.class,
                new VerticalSpeedCheck.Blueprint());
        this.plugin.getCheckRegistry().put(this.plugin, InvalidCheck.Blueprint.class,
                new InvalidCheck.Blueprint());

    }

    void loadPenalties() {
        if (this.plugin.getPenaltyRegistry() == null) return;

        this.plugin.getPenaltyRegistry().put(this.plugin, ResetPenalty.class, new ResetPenalty());
    }

    void loadPhases() {
        if (this.plugin.getPhaseRegistry() == null) return;

        this.plugin.getPhaseRegistry().put(this.plugin, PhaseTypes.CHECK, new GuardianCheckViewer(this.plugin));
        this.plugin.getPhaseRegistry().put(this.plugin, PhaseTypes.HEURISTIC, new GuardianHeuristicViewer(this.plugin));
        this.plugin.getPhaseRegistry().put(this.plugin, PhaseTypes.PENALTY, new GuardianPenaltyViewer(this.plugin));
    }

}
