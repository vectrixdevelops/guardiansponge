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

import io.github.connorhartley.guardian.detection.Detection;
import io.github.connorhartley.guardian.storage.StorageProvider;
import org.spongepowered.api.entity.living.player.User;
import tech.ferus.util.config.HoconConfigFile;

import java.nio.file.Path;
import java.util.Optional;

/**
 * PunishmentReport Type
 *
 * Represents a punishment handler.
 */
public interface Punishment {

    /**
     * Get Name
     *
     * <p>Returns the name of this punishment handler.</p>
     *
     * @return The name
     */
    String getName();

    /**
     * Handle
     *
     * <p>Executed each time to handleFinish a punishmentReport. Returns
     * true if the punishmentReport was handled.</p>
     *
     * @param detection The detection to handle for
     * @param args Misc arguments
     * @param user The user to be punished
     * @param punishmentReport Information about this punishmentReport
     * @return True if the punishmentReport was handled
     */
    <E, F extends StorageProvider<HoconConfigFile, Path>> boolean handle(Detection<E, F> detection, String[] args, User user, PunishmentReport punishmentReport);

}
