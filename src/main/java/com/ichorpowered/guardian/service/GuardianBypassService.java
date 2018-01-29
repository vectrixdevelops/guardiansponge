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
package com.ichorpowered.guardian.service;

import com.ichorpowered.guardian.GuardianPlugin;
import com.me4502.precogs.detection.DetectionType;
import com.me4502.precogs.service.AntiCheatService;
import com.me4502.precogs.service.BypassTicket;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.entity.living.player.User;

import java.util.List;
import java.util.Optional;

public class GuardianBypassService implements AntiCheatService {

    private final GuardianPlugin plugin;

    public GuardianBypassService(GuardianPlugin plugin) {
        this.plugin = plugin;
    }

    @Override
    public Optional<BypassTicket> requestBypassTicket(Player player, List<DetectionType> detectionTypes, Object owner) {
        return Optional.of(new GuardianBypassTicket(this.plugin, player, detectionTypes, owner));
    }

    @Override
    public double getViolationLevel(User user, DetectionType detectionType) {
        return 0;
    }

    @Override
    public void logViolation(User user, DetectionType detectionType, double value) {

    }

    @Override
    public void logViolation(User user, DetectionType detectionType, double value, String reason) {

    }
}
