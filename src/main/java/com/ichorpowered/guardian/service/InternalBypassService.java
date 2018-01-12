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
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.event.Event;
import org.spongepowered.api.scheduler.Task;

import java.util.Optional;

public class InternalBypassService {

    private final GuardianPlugin plugin;

    public InternalBypassService(GuardianPlugin plugin) {
        this.plugin = plugin;
    }

    public Optional<InternalBypassTicket> requestTimeBypassTicket(Player player, Class<? extends Event> eventClass, Object plugin, long delay) {
        Optional<InternalBypassTicket> bypassTicket = this.requestBypassTicket(player, eventClass, plugin);
        bypassTicket.ifPresent((ticket) -> {
            Task.builder()
                    .name("Guardian Bypass - " + eventClass.getSimpleName() + " for " + player.getName())
                    .execute(() -> {
                        if (!ticket.isClosed()) {
                            ticket.close();
                        }
                    })
                    .delayTicks(delay)
                    .submit(this.plugin);
        });
        return bypassTicket;
    }

    public Optional<InternalBypassTicket> requestBypassTicket(Player player, Class<? extends Event> eventClass, Object plugin) {
        return Optional.of(new InternalBypassTicket(this.plugin, player, eventClass, plugin));
    }

}
