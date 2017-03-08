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
import io.github.connorhartley.guardian.detection.Detection;
import io.github.connorhartley.guardian.storage.StorageConsumer;
import io.github.connorhartley.guardian.storage.StorageProvider;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.scheduler.Task;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.*;

public class ContextController {

    private final Guardian plugin;
    private final List<Class<? extends Context>> contextRegistry = new LinkedList<>();
    private final HashMap<Player, List<Context>> runningContexts = new LinkedHashMap<>();

    public ContextController(Guardian plugin) {
        this.plugin = plugin;
    }

    public Optional<Context> construct(Detection detection, Player player, Class<? extends Context> context) {
        for (Class<? extends Context> contextClass : this.contextRegistry) {
            if (contextClass.equals(context)) {
                if (this.runningContexts.get(player) == null) {
                    List<Context> contexts = new ArrayList<>();

                    try {
                        Constructor<?> ctor = contextClass.getConstructor(Guardian.class, Detection.class, Player.class);
                        Context newContext = (Context) ctor.newInstance(this.plugin, detection, player);

                        contexts.add(newContext);
                        this.runningContexts.put(player, contexts);

                        return Optional.of(newContext);
                    } catch (NoSuchMethodException | InstantiationException | IllegalAccessException | InvocationTargetException e) {
                        e.printStackTrace();
                    }
                } else {
                    boolean exists = false;
                    for (Context contextSearch : this.runningContexts.get(player)) {
                        if (contextSearch.getClass().equals(context)) {
                            exists = true;
                            break;
                        }
                    }

                    if (!exists) {
                        List<Context> contexts = new ArrayList<>();
                        contexts.addAll(this.runningContexts.get(player));

                        try {
                            Constructor<?> ctor = contextClass.getConstructor(Guardian.class, Detection.class, Player.class);
                            Context newContext = (Context) ctor.newInstance(this.plugin, detection, player);

                            contexts.add(newContext);
                            this.runningContexts.put(player, contexts);

                            return Optional.of(newContext);
                        } catch (NoSuchMethodException | InstantiationException | IllegalAccessException | InvocationTargetException e) {
                            e.printStackTrace();
                        }
                    }
                }
            }
        }
        return Optional.empty();
    }

    public void suspend(Player player, Context context) {
        context.getContainer().clear();
        context.suspend();
        this.runningContexts.get(player).remove(context);
    }

    public void suspendFor(Player player) {
        if (this.runningContexts.get(player) != null) {
            this.runningContexts.get(player).forEach(context -> {
                context.getContainer().clear();
                context.suspend();
            });
        }
        this.runningContexts.remove(player);
    }

    public void updateAll() {
        this.runningContexts.values()
                .forEach(contexts -> contexts.forEach(context -> {
                    if (!context.isSuspended()) {
                        context.update();
                    }
                }));
    }

    public void register(Class<? extends Context> clazz) {
        if (this.contextRegistry.contains(clazz)) return;
        this.contextRegistry.add(clazz);
    }

    public List<Class<? extends Context>> getContextRegistry() {
        return this.contextRegistry;
    }

    public void unregister(Class<? extends Context> clazz) {
        this.contextRegistry.remove(clazz);
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
            this.task = this.taskBuilder.execute(this.contextController::updateAll)
                    .intervalTicks(1).name("Guardian - Context Controller Task").submit(this.plugin);
        }

        public void stop() {
            if (this.task != null) this.task.cancel();
        }

    }

}
