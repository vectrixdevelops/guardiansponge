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
package io.github.connorhartley.guardian.heuristic;

import io.github.connorhartley.guardian.Guardian;
import io.github.connorhartley.guardian.PluginInfo;
import io.github.connorhartley.guardian.detection.Detection;
import io.github.connorhartley.guardian.punishment.Punishment;
import io.github.connorhartley.guardian.report.HeuristicReport;
import io.github.connorhartley.guardian.report.SequenceReport;
import io.github.connorhartley.guardian.storage.StorageSupplier;
import org.spongepowered.api.entity.living.player.User;

import java.io.File;
import java.util.Optional;
import java.util.Set;

public class HeuristicController {

    private final Guardian plugin;

    public HeuristicController(Guardian plugin) {
        this.plugin = plugin;
    }

    public <E, F extends StorageSupplier<File>> Optional<HeuristicReport> analyze(Detection<E, F> detection, User user, SequenceReport sequenceReport) {
        Set<Integer> punishments = this.plugin.getGlobalDatabase().getPunishmentIdByProperties(Integer.valueOf(PluginInfo.DATABASE_VERSION),
                user, sequenceReport.getDetectionType());

        if (punishments.size() > 0) {
            for (Integer punishmentId : punishments) {
                if (this.plugin.getGlobalDatabase().getPunishmentById(punishmentId).isPresent()) {
                    int punishmentCount = 0;

                    if (this.plugin.getGlobalDatabase().getPunishmentCountById(punishmentId).isPresent()) {
                        punishmentCount = this.plugin.getGlobalDatabase().getPunishmentCountById(punishmentId).get();
                    }

                    // TODO: More here.
                }
            }
        }
        return Optional.empty();
    }

}
