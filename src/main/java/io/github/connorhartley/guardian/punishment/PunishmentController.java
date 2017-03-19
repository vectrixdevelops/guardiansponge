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
import io.github.connorhartley.guardian.data.DataKeys;
import io.github.connorhartley.guardian.detection.Detection;
import io.github.connorhartley.guardian.detection.Offense;
import io.github.connorhartley.guardian.sequence.report.ReportType;
import io.github.connorhartley.guardian.sequence.report.SequenceReport;
import org.spongepowered.api.entity.living.player.User;
import org.spongepowered.api.event.filter.data.Has;

import java.util.*;
import java.util.function.BiPredicate;

public class PunishmentController {

    private final Guardian plugin;
    private final PunishmentAction punishmentAction;
    private final HashMap<Detection, HashMap<String, Double>> detectionPunishments = new HashMap<>();
    private final HashMap<Detection, HashMap<String, String>> detectionProperties = new HashMap<>();

    public PunishmentController(Guardian plugin) {
        this.plugin = plugin;
        this.punishmentAction = new PunishmentAction();
    }

    public void analyse(User user, Offense offense) {
        this.detectionPunishments.get(offense.getDetection()).forEach((punishmentType, value) -> {
            if (value >= offense.getSeverity() && value != -1) {
                this.post(user, punishmentType, offense);
            }
        });
    }

    public void post(User user, String punishmentType, Offense offense) {
        PunishmentType punishmentTypeObject = PunishmentType.valueOf(punishmentType);
        List<PunishmentType> punishmentTypeList = new ArrayList<>();

        if (user.get(DataKeys.GUARDIAN_PUNISHMENT_TAG).isPresent()) {
            punishmentTypeList = user.get(DataKeys.GUARDIAN_PUNISHMENT_TAG).get();
        }

        if (!punishmentTypeList.contains(punishmentTypeObject)) {
            punishmentTypeList.add(punishmentTypeObject);

            user.offer(DataKeys.GUARDIAN_PUNISHMENT_TAG, punishmentTypeList);

            Detection detection = offense.getDetection();

            String[] output = punishmentTypeObject.getText().split("(?:\\w\\d*)+");

            HashMap<String, Integer> outputTypeMap = new HashMap<>();

            for (int i = 1; i < output.length; i++) {
                switch (output[i]) {
                    case "p": outputTypeMap.put("player", i + 1);
                    case "b": outputTypeMap.put("releasetime", i + 1);
                    case "t": outputTypeMap.put("time", i + 1);
                    case "c": outputTypeMap.put("channel", i + 1);
                    case "r": outputTypeMap.put("report", i + 1);
                }
            }

            switch (output[0]) {
                case "warn": {
                    if (outputTypeMap.containsKey("player")) {
                        this.punishmentAction.warn(detection, user, offense.getReport());
                    } else {
                        this.plugin.getLogger().warn("Unable to execute a " + output[0] + " as it's properties does not contain a player.");
                    }
                }
                case "flag": {
                    if (outputTypeMap.containsKey("player")) {
                        this.punishmentAction.flag(detection, user, offense.getDateAndTime().toString(), offense.getReport());
                    } else {
                        this.plugin.getLogger().warn("Unable to execute a " + output[0] + " as it's properties does not contain a player.");
                    }
                }
                case "report": {
                    if (outputTypeMap.containsKey("player")) {
                        this.punishmentAction.report(detection, user, offense.getDateAndTime().toString(),
                                this.detectionProperties.get(detection).get("channel"), offense.getReport());
                    } else {
                        this.plugin.getLogger().warn("Unable to execute a " + output[0] + " as it's properties does not contain a player.");
                    }
                }
                case "kick": {
                    if (outputTypeMap.containsKey("player")) {
                        this.punishmentAction.kick(detection, user, offense.getDateAndTime().toString(),
                                this.detectionProperties.get(detection).get("channel"), offense.getReport());
                    } else {
                        this.plugin.getLogger().warn("Unable to execute a " + output[0] + " as it's properties does not contain a player.");
                    }
                }
                case "tempban": {
                    if (outputTypeMap.containsKey("player")) {
                        this.punishmentAction.tempban(detection, user,
                                this.detectionProperties.get(detection).get("releasetime"), offense.getDateAndTime().toString(),
                                this.detectionProperties.get(detection).get("channel"), offense.getReport());
                    }
                }
                case "ban": {
                    if (outputTypeMap.containsKey("player")) {
                        this.punishmentAction.ban(detection, user,
                                offense.getDateAndTime().toString(),
                                this.detectionProperties.get(detection).get("channel"), offense.getReport());
                    }
                }
            }
        }
    }

    public void register(Detection detection) {
        if (detection.getConfiguration().get("punishment-levels", new HashMap<String, Double>()).isPresent()) {
            this.detectionPunishments.put(detection, detection.getConfiguration()
                    .get("punishment-levels", new HashMap<String, Double>()).get().getValue());
        }

        if (detection.getConfiguration().get("punishment-properties", new HashMap<String, String>()).isPresent()) {
            this.detectionProperties.put(detection, detection.getConfiguration()
                    .get("punishment-properties", new HashMap<String, String>()).get().getValue());
        }
    }

    public void unregister(Detection detection) {
        if (this.detectionPunishments.containsKey(detection)) {
            this.detectionPunishments.remove(detection);
        }
    }

}
