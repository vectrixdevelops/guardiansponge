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
package com.ichorpowered.guardian;

import com.ichorpowered.guardian.common.check.movement.HorizontalSpeedCheck;
import com.ichorpowered.guardian.common.check.movement.InvalidCheck;
import com.ichorpowered.guardian.common.check.movement.VerticalSpeedCheck;
import com.ichorpowered.guardian.common.penalty.NotificationPenalty;
import com.ichorpowered.guardian.common.penalty.ResetPenalty;
import com.ichorpowered.guardian.detection.check.GuardianCheckModel;
import com.ichorpowered.guardian.detection.heuristic.GuardianHeuristicModel;
import com.ichorpowered.guardian.detection.penalty.GuardianPenaltyModel;
import com.ichorpowered.guardianapi.detection.check.CheckModel;
import com.ichorpowered.guardianapi.detection.heuristic.HeuristicModel;
import com.ichorpowered.guardianapi.detection.penalty.PenaltyModel;
import com.me4502.modularframework.ModuleController;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.service.permission.PermissionDescription;
import org.spongepowered.api.service.permission.PermissionService;
import org.spongepowered.api.text.Text;

import java.util.Optional;

public final class Common {

    private static final String DETECTION_PATH = "com.ichorpowered.guardian.common.detection.";

    private final GuardianPlugin plugin;

    public Common(final GuardianPlugin plugin) {
        this.plugin = plugin;
    }

    public void loadModules(ModuleController<GuardianPlugin> moduleController) {
        moduleController.registerModule(DETECTION_PATH + "InvalidMovementDetection");
        moduleController.registerModule(DETECTION_PATH + "MovementSpeedDetection");
    }

    public void loadChecks() {
        final GuardianCheckModel checkModel = new GuardianCheckModel();
        this.plugin.getDetectionManager().provideStageModel(CheckModel.class, checkModel);

        // Register Check Stages

        checkModel.register(new HorizontalSpeedCheck());
        checkModel.register(new InvalidCheck());
        checkModel.register(new VerticalSpeedCheck());
    }

    public void loadHeuristics() {
        final GuardianHeuristicModel heuristicModel = new GuardianHeuristicModel();
        this.plugin.getDetectionManager().provideStageModel(HeuristicModel.class, heuristicModel);

        // Register Heuristic Models
    }

    public void loadPenalties() {
        final GuardianPenaltyModel penaltyModel = new GuardianPenaltyModel();
        this.plugin.getDetectionManager().provideStageModel(PenaltyModel.class, penaltyModel);

        // Register Penalty Models

        penaltyModel.register(new NotificationPenalty());
        penaltyModel.register(new ResetPenalty());
    }

    public void registerPermissions() {
        Optional<PermissionService> permissionService = Sponge.getServiceManager().provide(PermissionService.class);

        if (permissionService.isPresent()) {
            permissionService.get().newDescriptionBuilder(this.plugin)
                    .id("guardian.penalty.notifier")
                    .description(Text.of("Allows a player to recieve notifications of detection violations."))
                    .assign(PermissionDescription.ROLE_STAFF, true)
                    .register();

            permissionService.get().newDescriptionBuilder(this.plugin)
                    .id("guardian.penalty.reset-override")
                    .description(Text.of("Exempts a player from receiving the reset penalty."))
                    .assign(PermissionDescription.ROLE_STAFF, true)
                    .register();
        }
    }
}
