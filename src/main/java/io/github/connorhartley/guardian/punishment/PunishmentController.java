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
package io.github.connorhartley.guardian.punishment;

import io.github.connorhartley.guardian.Guardian;
import io.github.connorhartley.guardian.detection.Detection;
import io.github.connorhartley.guardian.detection.Offense;
import io.github.connorhartley.guardian.sequence.report.ReportType;
import org.spongepowered.api.entity.living.player.User;
import org.spongepowered.api.event.filter.data.Has;

import java.util.*;
import java.util.function.BiPredicate;

public class PunishmentController {

    private final Guardian plugin;
    private final HashMap<Detection, HashMap<PunishmentType, Double>> detectionPunishments = new HashMap<>();

    public PunishmentController(Guardian plugin) {
        this.plugin = plugin;
    }

    public void analyse(User user, Offense offense) {
        this.detectionPunishments.get(offense.getDetection()).forEach((punishmentType, value) -> {
            if (value >= offense.getSeverity()) {
                this.post(user, punishmentType, offense);
            }
        });
    }

    public void post(User user, PunishmentType punishmentType, Offense offense) {
        // TODO: Do the punishment.
    }

    public void register(Detection detection) {
        if (detection.getConfiguration().get("punishment-levels", new HashMap<PunishmentType, Double>()).isPresent()) {
            this.detectionPunishments.put(detection, detection.getConfiguration()
                    .get("punishment-levels", new HashMap<PunishmentType, Double>()).get().getValue());
        }
    }

    public void unregister(Detection detection) {
        if (this.detectionPunishments.containsKey(detection)) {
            this.detectionPunishments.remove(detection);
        }
    }

}
