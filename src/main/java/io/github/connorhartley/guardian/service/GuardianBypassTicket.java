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
package io.github.connorhartley.guardian.service;

import com.me4502.precogs.detection.DetectionType;
import com.me4502.precogs.service.BypassTicket;
import io.github.connorhartley.guardian.detection.Detection;
import org.spongepowered.api.entity.living.player.Player;

import java.util.List;

public class GuardianBypassTicket implements BypassTicket {

    private final Object plugin;
    private final Player player;
    private final List<DetectionType> detectionTypes;

    private boolean isClosed = false;

    public GuardianBypassTicket(Player player, List<DetectionType> detectionTypes, Object plugin) {
        this.plugin = plugin;
        this.player = player;
        this.detectionTypes = detectionTypes;

        this.detectionTypes.forEach(detectionType -> ((Detection) detectionType).setPunish(false));
    }

    @Override
    public Player getPlayer() {
        return this.player;
    }

    @Override
    public List<DetectionType> getDetectionTypes() {
        return this.detectionTypes;
    }

    @Override
    public boolean isClosed() {
        return this.isClosed;
    }

    @Override
    public void close() {
        this.detectionTypes.forEach(detectionType -> ((Detection) detectionType).setPunish(true));

        this.isClosed = true;
    }
}
