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
package io.github.connorhartley.guardian.context;

import io.github.connorhartley.guardian.Guardian;
import io.github.connorhartley.guardian.context.type.ActionContext;
import org.spongepowered.api.entity.living.player.User;
import org.spongepowered.api.event.Event;
import org.spongepowered.api.event.cause.Cause;
import org.spongepowered.api.event.cause.NamedCause;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * Context Controller
 *
 * A way to register contexts and invoke them on request.
 */
public class ContextController {

    private final Guardian plugin;

    private List<Object> contexts = new ArrayList<>();

    public ContextController(Guardian plugin) {
        this.plugin = plugin;
    }

    public <T> void registerContext(Class<? extends T> context) {
        try {
            this.contexts.add(context.newInstance());
        } catch (InstantiationException | IllegalAccessException e) {
            e.printStackTrace();
        }
    }

    public <T> void unregisterContext(Class<? extends T> context) {
        this.contexts.removeIf(context::isInstance);
    }

    public void unregisterContexts() {
        this.contexts = new ArrayList<>();
    }

    public <T extends ActionContext> Optional<NamedCause> invokeAction(Class<T> context, User user, Event event) {
        for(Object contextObject : this.contexts) {
            if (context.isAssignableFrom(contextObject.getClass())) {
                return Optional.of(((ActionContext) contextObject).invoke(user, event));
            }
        }
        return Optional.empty();
    }

}
