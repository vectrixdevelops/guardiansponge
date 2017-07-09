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
package io.github.connorhartley.guardian.internal.punishment;

import io.github.connorhartley.guardian.data.DataKeys;
import io.github.connorhartley.guardian.detection.Detection;
import io.github.connorhartley.guardian.detection.punishment.PunishmentMixin;
import io.github.connorhartley.guardian.detection.punishment.PunishmentReport;
import io.github.connorhartley.guardian.storage.StorageProvider;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.entity.living.player.User;
import tech.ferus.util.config.HoconConfigFile;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

public class ResetPunishment implements PunishmentMixin {

    public ResetPunishment() {}

    @Override
    public String getName() {
        return "@reset";
    }

    @Override
    public <E, F extends StorageProvider<HoconConfigFile, Path>> boolean handle(Detection<E, F> detection, String[] args, User user, PunishmentReport punishmentReport) {
        List<PunishmentMixin> punishmentTypes = new ArrayList<>();
        if (user.get(DataKeys.GUARDIAN_PUNISHMENT_TAG).isPresent()) {
            punishmentTypes.addAll(user.get(DataKeys.GUARDIAN_PUNISHMENT_TAG).get());
        }

        punishmentTypes.add(this);

        user.offer(DataKeys.GUARDIAN_PUNISHMENT_TAG, punishmentTypes);

        if (user.getPlayer().isPresent()) {
            Player player = user.getPlayer().get();

            if (punishmentReport.getSequenceResult().getInitialLocation().isPresent()) {
                Sponge.getTeleportHelper()
                        .getSafeLocation(punishmentReport.getSequenceResult().getInitialLocation().get(), 64, 5)
                        .ifPresent(player::setLocationSafely);
                return true;
            }
        }

        return false;
    }
}
