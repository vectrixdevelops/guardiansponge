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

import com.me4502.modularframework.ModuleController;
import io.github.connorhartley.guardian.internal.check.movement.HorizontalSpeedCheck;
import io.github.connorhartley.guardian.internal.check.movement.InvalidCheck;
import io.github.connorhartley.guardian.internal.check.movement.VerticalSpeedCheck;
import io.github.connorhartley.guardian.internal.penalty.ResetPenalty;

public final class GuardianLoader {

    private final GuardianPlugin plugin;

    public GuardianLoader(GuardianPlugin plugin) {
        this.plugin = plugin;
    }

    public void loadModules(ModuleController<GuardianPlugin> moduleController) {
        moduleController.registerModule("io.github.connorhartley.guardian.internal.detection.InvalidMovementDetection");
        moduleController.registerModule("io.github.connorhartley.guardian.internal.detection.MovementSpeedDetection");
    }

    public void loadChecks() {
        this.plugin.getCheckRegistry().put(this.plugin, HorizontalSpeedCheck.Blueprint.class,
                new HorizontalSpeedCheck.Blueprint());
        this.plugin.getCheckRegistry().put(this.plugin, VerticalSpeedCheck.Blueprint.class,
                new VerticalSpeedCheck.Blueprint());
        this.plugin.getCheckRegistry().put(this.plugin, InvalidCheck.Blueprint.class,
                new InvalidCheck.Blueprint());

    }

    public void loadPenalties() {
        this.plugin.getPenaltyRegistry().put(this.plugin, ResetPenalty.class, new ResetPenalty());
    }

}
