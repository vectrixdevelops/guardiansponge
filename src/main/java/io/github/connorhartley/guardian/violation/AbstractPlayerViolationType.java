/*
 * MIT License
 *
 * Copyright (c) 2016 Connor Hartley
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
package io.github.connorhartley.guardian.violation;

import org.spongepowered.api.entity.living.player.Player;

import java.util.ArrayList;
import java.util.Collection;

public abstract class AbstractPlayerViolationType<P> implements ViolationType<P> {

    private final P provider;

    private ArrayList<Player> players = new ArrayList<>();

    public AbstractPlayerViolationType(P provider, Player player) {
        this.provider = provider;
    }

    @Override
    public void handleConnect(Player player) {
        if (this.players.contains(player)) return;

        this.players.add(player);
        this.update();
    }

    @Override
    public void handleDisconnect(Player player) {
        if (!this.players.contains(player)) return;

        this.players.remove(player);
        this.update();
    }

    @Override
    public P getProvider() {
        return this.provider;
    }

    public Collection<Player> getPlayers() {
        return this.players;
    }

}
