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
package io.github.connorhartley.guardian.detection.heuristic;

import com.google.common.reflect.TypeToken;
import io.github.connorhartley.guardian.Guardian;
import io.github.connorhartley.guardian.PluginInfo;
import io.github.connorhartley.guardian.detection.Detection;
import io.github.connorhartley.guardian.detection.punishment.PunishmentReport;
import io.github.connorhartley.guardian.sequence.SequenceResult;
import io.github.connorhartley.guardian.storage.container.StorageKey;
import org.spongepowered.api.entity.living.player.User;
import org.spongepowered.api.event.cause.Cause;
import org.spongepowered.api.event.cause.NamedCause;

import java.io.File;
import java.time.LocalDateTime;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

public class HeuristicController {

    private final Guardian plugin;

    public HeuristicController(Guardian plugin) {
        this.plugin = plugin;
    }

    public <E, F extends StorageSupplier<File>> Optional<HeuristicReport> analyze(Detection<E, F> detection, User user, SequenceResult sequenceResult) {
        Set<Integer> punishments = this.plugin.getGlobalDatabase().getPunishmentIdByProperties(Integer.valueOf(PluginInfo.DATABASE_VERSION),
                user, sequenceResult.getDetectionType());

        if (punishments.size() > 0) {
            for (Integer punishmentId : punishments) {
                if (this.plugin.getGlobalDatabase().getPunishmentById(punishmentId).isPresent()) {
                    PunishmentReport punishmentReport = this.plugin.getGlobalDatabase().getPunishmentById(punishmentId).get();
                    int punishmentCount = 0;

                    if (this.plugin.getGlobalDatabase().getPunishmentCountById(punishmentId).isPresent()) {
                        punishmentCount = this.plugin.getGlobalDatabase().getPunishmentCountById(punishmentId).get();
                    }

                    if (detection.getConfiguration().isPresent() && detection.getConfiguration().get()
                            .get(new StorageKey<>("heuristic-modifier"), new TypeToken<Map<String, Double>>() {}).isPresent()) {
                        Map<String, Double> modifiers = detection.getConfiguration().get()
                                .get(new StorageKey<>("heuristic-modifier"), new TypeToken<Map<String, Double>>() {}).get().getValue();

                        double divider = modifiers.get("divider-base") - punishmentCount;

                        if (LocalDateTime.now().minusHours(modifiers.get("relevant-punishment-inhours").longValue())
                                .isBefore(punishmentReport.getLocalDateTime())) {

                            return Optional.of(HeuristicReport.builder()
                                    .type(sequenceResult.getDetectionType())
                                    .severity(oldValue -> Math.pow(((oldValue * 100) / divider), modifiers.get("power")) / 100)
                                    .cause(Cause.of(NamedCause.of("punishment_count", punishmentCount)))
                                    .build());
                        }
                    }
                }
            }
        }
        return Optional.empty();
    }

}
