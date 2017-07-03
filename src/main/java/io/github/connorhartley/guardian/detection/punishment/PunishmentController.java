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
package io.github.connorhartley.guardian.detection.punishment;

import com.google.common.collect.Maps;
import io.github.connorhartley.guardian.detection.Detection;
import io.github.connorhartley.guardian.event.punishment.PunishmentExecuteEvent;
import io.github.connorhartley.guardian.storage.StorageProvider;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.entity.living.player.User;
import org.spongepowered.api.event.cause.Cause;
import org.spongepowered.api.event.cause.NamedCause;
import tech.ferus.util.config.HoconConfigFile;

import java.nio.file.Path;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * PunishmentReport Controller
 *
 * Controls punishment execution and registration.
 */
public final class PunishmentController {

    private final HashMap<String, List<Level>> detectionLevelRegistry = new HashMap<>();
    private final HashMap<String, HashMap<String, String[]>> detectionDefinitionRegistry = new HashMap<>();

    public PunishmentController() {}

    /**
     * Execute
     *
     * <p>Returns true if a punishmentReport is successfully handled by the
     * appropriate {@link Punishment}, returns false if it was cancelled.</p>
     *
     * @param detection The detection
     * @param user The user
     * @param punishmentReport Review on this punishmentReport
     * @param <E> The plugin type
     * @param <F> The detection configuration type
     * @return true if the punishmentReport was handled, false if it was not
     */
    public <E, F extends StorageProvider<HoconConfigFile, Path>> boolean execute(Detection<E, F> detection, User user, PunishmentReport punishmentReport) {
        PunishmentExecuteEvent punishmentExecuteEvent = new PunishmentExecuteEvent(punishmentReport, user, Cause.of(NamedCause.of("detection", detection)));
        Sponge.getEventManager().post(punishmentExecuteEvent);
        if (punishmentExecuteEvent.isCancelled()) {
            return false;
        }

        for (Level level : this.detectionLevelRegistry.get(detection.getId())) {
            if (punishmentReport.getSeverityTransformer().transform(0d) >= level.getRange().getFirst() &&
                    punishmentReport.getSeverityTransformer().transform(0d) <= level.getRange().getSecond() &&
                    level.getRange().getFirst() != -1 && level.getRange().getSecond() != -1) {
                for (String action : level.getActions()) {
                    if (this.detectionDefinitionRegistry.get(detection.getId()).containsKey(action)) {
                        for (String command : this.detectionDefinitionRegistry.get(detection.getId()).get(action)) {
                            String commandModified = command.replace("%player%", user.getName())
                                    .replace("%datetime%", punishmentReport.getLocalDateTime().toString())
                                    .replace("%probability%", punishmentReport.getSeverityTransformer().toString())
                                    .replace("%detection-name%", detection.getName())
                                    .replace("%detection-id%", detection.getId());

                            Sponge.getCommandManager().process(Sponge.getServer().getConsole(), commandModified);
                        }
                    }
                }
            }
        }

        return true;
    }

    /**
     * Register
     *
     * <p>Registers a punishment provider under a specific id.</p>
     *
     * @param providerId The provider id
     * @param punishmentProvider The provider
     */
    public void register(String providerId, PunishmentProvider punishmentProvider) {
         this.detectionDefinitionRegistry.computeIfAbsent(providerId, k -> Maps.newHashMap());
         this.detectionLevelRegistry.computeIfAbsent(providerId, k -> punishmentProvider.getLevels());

         for (Map.Entry<String, String[]> entry : punishmentProvider.getPunishments().entrySet()) {
             if (entry.getKey().equals("_global")) {
                 this.detectionDefinitionRegistry.compute(providerId, (detect, def) -> {
                     def.computeIfAbsent(entry.getKey(), k -> this.detectionDefinitionRegistry.get("_global").get(k));
                     return def;
                 });
             } else {
                 this.detectionDefinitionRegistry.compute(providerId, (detect, def) -> {
                     def.compute(entry.getKey(), (k, lev) -> entry.getValue());
                     return def;
                 });
             }
         }

         for (Level level : punishmentProvider.getLevels()) {
             this.detectionLevelRegistry.compute(providerId, (detect, lev) -> {
                if (!lev.contains(level)) lev.add(level);
                return lev;
             });
         }
    }

}
