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
import org.spongepowered.api.entity.living.player.User;

import java.util.Optional;

/**
 * Punishment Type
 *
 * Represents a punishment handler.
 */
public interface PunishmentType {

    /**
     * Get Name
     *
     * <p>Returns the name of this punishment handler.</p>
     *
     * @return The name
     */
    String getName();

    /**
     * Get Detection
     *
     * <p>Returns the detection this punishment was created by.</p>
     *
     * @return The detection
     */
    Optional<Detection<?, ?>> getDetection();

    /**
     * Handle
     *
     * <p>Executed each time to handle a punishment. Returns
     * true if the punishment was handled.</p>
     *
     * @param args Misc arguments
     * @param user The user to be punished
     * @param punishment Information about this punishment
     * @return True if the punishment was handled
     */
    boolean handle(String[] args, User user, Punishment punishment);

}
