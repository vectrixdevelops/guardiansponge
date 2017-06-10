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

import com.google.common.reflect.TypeToken;
import io.github.connorhartley.guardian.Guardian;
import io.github.connorhartley.guardian.detection.Detection;
import io.github.connorhartley.guardian.event.punishment.PunishmentExecuteEvent;
import io.github.connorhartley.guardian.storage.StorageSupplier;
import io.github.connorhartley.guardian.storage.container.StorageKey;
import org.apache.commons.lang3.StringUtils;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.entity.living.player.User;
import org.spongepowered.api.event.cause.Cause;
import org.spongepowered.api.event.cause.NamedCause;
import org.spongepowered.api.util.Tuple;

import java.io.File;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
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
     public <E, F extends StorageSupplier<File>> boolean execute(Detection<E, F> detection, User user, PunishmentReport punishmentReport) {
         Map<String, Tuple<Double, Double>> detectionPunishLevels = new HashMap<>();
         List<String> currentPunishLevels = new ArrayList<>();

         PunishmentExecuteEvent punishmentExecuteEvent = new PunishmentExecuteEvent(punishmentReport, user, Cause.of(NamedCause.of("detection", detection)));
         Sponge.getEventManager().post(punishmentExecuteEvent);
         if (punishmentExecuteEvent.isCancelled()) {
             return false;
         }

         if (detection.getConfiguration().get().get(new StorageKey<>("punishment-levels"),
                 new TypeToken<Map<String, Tuple<Double, Double>>>(){}).isPresent()) {
             detectionPunishLevels = detection.getConfiguration().get().get(new StorageKey<>("punishment-levels"),
                     new TypeToken<Map<String, Tuple<Double, Double>>>(){}).get().getValue();
         }

         for (Map.Entry<String, Tuple<Double, Double>> entry : detectionPunishLevels.entrySet()) {
             if (punishmentReport.getSeverityTransformer().transform(0d) >= entry.getValue().getFirst() &&
                     punishmentReport.getSeverityTransformer().transform(0d) <= entry.getValue().getSecond() &&
                     entry.getValue().getFirst() != -1 && entry.getValue().getSecond() != -1) {
                 currentPunishLevels.add(entry.getKey());
             }
         }

         if (this.registry.get(detection) != null) {
             for (Punishment punishment : this.registry.get(detection)) {
                 currentPunishLevels.forEach(currentPunishLevel -> {
                     String[] level = StringUtils.split(currentPunishLevel, "&");

                     if (punishment.getName().equals(level[0])) {
                         punishment.handle(new String[]{ level[1] }, user, punishmentReport);
                     }
                 });
             }
         }
         return true;
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

    /**
     * Unbind
     *
     * <p>Unregisters a punishment handler to a {@link Detection}.</p>
     *
     * @param punishmentClass The punishment handler
     * @param detection The detection
     */
    public void unbind(Class<? extends Punishment> punishmentClass, Detection detection) {
        if (this.registry.get(detection) != null) {
            this.registry.get(detection).removeIf(punishmentType -> punishmentType.getClass().equals(punishmentClass));
        }
    }

}
