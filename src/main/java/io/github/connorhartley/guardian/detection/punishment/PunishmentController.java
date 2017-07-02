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

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.reflect.TypeToken;
import io.github.connorhartley.guardian.Guardian;
import io.github.connorhartley.guardian.detection.Detection;
import io.github.connorhartley.guardian.event.punishment.PunishmentExecuteEvent;
import io.github.connorhartley.guardian.storage.StorageProvider;
import io.github.connorhartley.guardian.storage.container.StorageKey;
import org.apache.commons.lang3.StringUtils;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.entity.living.player.User;
import org.spongepowered.api.event.cause.Cause;
import org.spongepowered.api.event.cause.NamedCause;
import org.spongepowered.api.util.Tuple;
import tech.ferus.util.config.HoconConfigFile;

import java.io.File;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * PunishmentReport Controller
 *
 * Controls punishment execution and registration.
 */
public class PunishmentController {

    private final Guardian plugin;
    private final HashMap<String, Punishment> definitionRegistry = new HashMap<>();
    private final HashMap<String, List<Level>> detectionLevelRegistry = new HashMap<>();
    private final HashMap<String, HashMap<String, String[]>> detectionDefinitionRegistry = new HashMap<>();

    private final HashMap<Detection, List<Punishment>> registry = new HashMap<>();

    public PunishmentController(Guardian plugin) {
        this.plugin = plugin;
    }

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
                    if (this.detectionDefinitionRegistry.containsKey(action) || this.detectionDefinitionRegistry.get("_global").containsKey(action)) {
                        if (this.definitionRegistry.containsKey(action)) {
                            this.definitionRegistry.get(action).handle(detection, new String[]{}, user, punishmentReport);
                        }
                    }
                }
            }
        }

        return true;
    }

    public void bind(String id, PunishmentProvider punishmentProvider) {
         this.detectionDefinitionRegistry.computeIfAbsent(id, k -> Maps.newHashMap());
         this.detectionLevelRegistry.computeIfAbsent(id, k -> punishmentProvider.getLevels());

         for (Map.Entry<String, String[]> entry : punishmentProvider.getPunishments().entrySet()) {
             if (entry.getKey().equals("_global")) {
                 this.detectionDefinitionRegistry.compute(id, (detect, def) -> {
                     for (String value : entry.getValue()) {
                         def.computeIfAbsent(value, v -> this.detectionDefinitionRegistry.get("_global").get(v));
                     }

                     return def;
                 });
             } else {
                 this.detectionDefinitionRegistry.compute(id, (detect, def) -> {
                     def.computeIfAbsent(entry.getKey(), k -> entry.getValue());

                     return def;
                 });
             }
         }
    }

    public void register(String punishmentName, Class<? extends Punishment> punishmentClass) {
         if (this.definitionRegistry.get(punishmentName) == null) {
             try {
                 Constructor<?> ctor = punishmentClass.getConstructor(Guardian.class);
                 Punishment punishment = (Punishment) ctor.newInstance(this.plugin);

                 this.definitionRegistry.put(punishmentName, punishment);
             } catch (NoSuchMethodException | InstantiationException | InvocationTargetException | IllegalAccessException e) {
                 e.printStackTrace();
             }
         }
    }

    /**
     * Bind
     *
     * <p>Registers a punishment handler to a {@link Detection}.</p>
     *
     * @param punishmentClass The punishment handler
     * @param detection The detection
     */
    public void bind(Class<? extends Punishment> punishmentClass, Detection detection) {
        if (this.registry.get(detection) == null) {
            List<Punishment> punishments = new ArrayList<>();

            try {
                Constructor<?> ctor = punishmentClass.getConstructor(Guardian.class, Detection.class);
                Punishment newPunishment = (Punishment) ctor.newInstance(this.plugin, detection);

                punishments.add(newPunishment);
                this.registry.put(detection, punishments);
            } catch (NoSuchMethodException | InstantiationException | InvocationTargetException | IllegalAccessException e) {
                e.printStackTrace();
            }
        } else {
            boolean exists = false;
            for (Punishment punishmentSearch : this.registry.get(detection)) {
                if (punishmentSearch.getClass().equals(punishmentClass)) {
                    exists = true;
                    break;
                }
            }

            if (!exists) {
                List<Punishment> punishments = new ArrayList<>();
                punishments.addAll(this.registry.get(detection));

                try {
                    Constructor<?> ctor = punishmentClass.getConstructor(Guardian.class, Detection.class);
                    Punishment newPunishment = (Punishment) ctor.newInstance(this.plugin, detection);

                    punishments.add(newPunishment);
                    this.registry.put(detection, punishments);
                } catch (NoSuchMethodException | InstantiationException | InvocationTargetException | IllegalAccessException e) {
                    e.printStackTrace();
                }
            }
        }
    }

}
