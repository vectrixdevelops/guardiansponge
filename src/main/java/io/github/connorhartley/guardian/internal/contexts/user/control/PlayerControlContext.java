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
package io.github.connorhartley.guardian.internal.contexts.user.control;

import io.github.connorhartley.guardian.Guardian;
import io.github.connorhartley.guardian.context.Context;
import io.github.connorhartley.guardian.context.TimeContext;
import io.github.connorhartley.guardian.util.ContextValue;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.entity.living.player.User;
import org.spongepowered.api.event.Event;

import java.util.HashMap;
import java.util.Optional;

public class PlayerControlContext extends Context implements TimeContext {

    // TODO: Check for weird movement and control based things.

    private Player player;
    private HashMap<String, ContextValue> values = new HashMap<>();

    private boolean ready = false;
    
    public PlayerControlContext(Guardian plugin, String id) {
        super(plugin, id);
    }

    @Override
    public void start(User user, Event event) {

    }

    @Override
    public void update() {

    }

    @Override
    public void stop() {

    }

    @Override
    public boolean isReady() {
        return this.ready;
    }

    @Override
    public HashMap<String, ContextValue> getValues() {
        return null;
    }

    @Override
    public Optional<TimeContext> asTimed() {
        return null;
    }
}
