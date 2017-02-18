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
import org.spongepowered.api.entity.living.player.User;
import org.spongepowered.api.event.Event;
import org.spongepowered.api.scheduler.Task;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Optional;

/**
 * Context Controller
 *
 * A way to register contexts and invoke them on request.
 */
public class ContextController {

    private final Guardian plugin;

    private HashMap<String, Class> registry = new HashMap<>();
    private HashMap<User, List<Class>> runningContexts = new HashMap<>();
    private List<Object> contexts = new ArrayList<>();

    public ContextController(Guardian plugin) {
        this.plugin = plugin;
    }

    public void registerContext(String id, Class<? extends Context> context) {
        this.registry.put(id, context);
    }

    public void unregisterContext(String id) {
        this.registry.remove(id);
    }

    public void unregisterContexts() {
        this.registry.clear();
    }

    public Optional<Object> construct(String id, User user, Event event) throws IllegalAccessException, InstantiationException {
        if (this.registry.containsKey(id)) {
            Object context = this.registry.get(id).newInstance();
            if (context != null && this.runningContexts.get(user) == null) {
                List<Class> contextClasses = new ArrayList<>();
                contextClasses.add(context.getClass());
                this.runningContexts.put(user, contextClasses);
                this.contexts.add(context);
                return Optional.of(context);
            } else if (context != null && !this.runningContexts.get(user).contains(context.getClass())) {
                List<Class> contextClasses = this.runningContexts.get(user);
                contextClasses.add(context.getClass());
                this.runningContexts.replace(user, contextClasses);
                this.contexts.add(context);
                return Optional.of(context);
            }
        }
        return Optional.empty();
    }

    public void updateAll() {
        this.contexts.stream()
                .filter(context -> ((Context) context).asTimed().isPresent())
                .filter(context -> !((TimeContext) context).isReady())
                .forEach(context -> ((TimeContext) context).update());
    }

    public void suspend(Context context) {
        if (context.getClass().isAssignableFrom(TimeContext.class)) {
            ((TimeContext) context).stop();
        }
    }

    public static class ContextControllerTask {

        private final Guardian plugin;
        private final ContextController contextController;

        private Task.Builder taskBuilder = Task.builder();
        private Task task;

        public ContextControllerTask(Guardian plugin, ContextController contextController) {
            this.plugin = plugin;
            this.contextController = contextController;
        }

        public void start() {
            this.task = this.taskBuilder.execute(this.contextController::updateAll).intervalTicks(1)
                    .name("Guardian - Context Controller Task").submit(this.plugin);
        }

        public void stop() {
            if (this.task != null) this.task.cancel();
        }

    }

}
