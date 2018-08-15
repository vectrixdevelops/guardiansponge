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
package com.ichorpowered.guardian.sponge.service;

import com.ichorpowered.guardian.api.detection.DetectionController;
import com.ichorpowered.guardian.api.sequence.SequenceController;
import com.ichorpowered.guardian.sponge.GuardianPlugin;
import com.ichorpowered.guardian.sponge.detection.DetectionProviderImpl;
import com.me4502.precogs.detection.DetectionType;
import com.me4502.precogs.service.AntiCheatService;
import com.me4502.precogs.service.BypassTicket;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.entity.living.player.User;

import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;

public class BypassServiceImpl implements AntiCheatService {

    private final DetectionController detectionController;
    private final SequenceController sequenceController;
    private final GuardianPlugin plugin;

    public BypassServiceImpl(final DetectionController detectionController, final SequenceController sequenceController,
                             final GuardianPlugin plugin) {
        this.detectionController = detectionController;
        this.sequenceController = sequenceController;
        this.plugin = plugin;
    }

    @Override
    public Optional<BypassTicket> requestBypassTicket(final Player player, final List<DetectionType> detectionTypes, Object owner) {
        return Optional.of(new BypassTicketImpl(this.detectionController, this.sequenceController, this.plugin, player,
                detectionTypes.stream()
                        .map(detectionType -> {
                            if (detectionType instanceof DetectionProviderImpl) return (DetectionProviderImpl) detectionType;
                            else return null;
                        })
                        .filter(Objects::nonNull).collect(Collectors.toList())
        , owner));
    }

    @Override
    public double getViolationLevel(User user, DetectionType detectionType) {
        return 0;
    }

    @Override
    public void logViolation(User user, DetectionType detectionType, double v) {

    }

    @Override
    public void logViolation(User user, DetectionType detectionType, double v, String s) {

    }

}
