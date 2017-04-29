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
package io.github.connorhartley.guardian.detection.check;

import io.github.connorhartley.guardian.Guardian;
import io.github.connorhartley.guardian.event.check.CheckBeginEvent;
import io.github.connorhartley.guardian.event.check.CheckEndEvent;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.entity.living.player.User;
import org.spongepowered.api.event.cause.Cause;
import org.spongepowered.api.event.cause.NamedCause;
import org.spongepowered.api.scheduler.Task;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

public class CheckController {

    private final List<Check> checks = new CopyOnWriteArrayList<>();
    private final Guardian plugin;

    public CheckController(Guardian plugin) {
        this.plugin = plugin;
    }

    public void post(CheckType checkType, User user) {
        Check check = checkType.createInstance(user);

        CheckBeginEvent attempt = new CheckBeginEvent(check, user, Cause.of(NamedCause.source(this.plugin)));
        Sponge.getEventManager().post(attempt);
        if (attempt.isCancelled()) {
            return;
        }

        this.checks.add(check);

        Sponge.getEventManager().registerListeners(this.plugin, check);
    }

    public void tick() {
        this.checks.forEach(Check::update);
    }

    public void cleanup() {
        this.checks.removeIf(check -> {
           if (check.isChecking()) {
               return false;
           }

           CheckEndEvent attempt = new CheckEndEvent(check, Cause.of(NamedCause.source(this.plugin)));
           Sponge.getEventManager().post(attempt);

           Sponge.getEventManager().unregisterListeners(check);
           if (!check.isChecking()) {
               check.finish();
           }

           return true;
        });
    }

    public void end(Check check) {
        if (!check.getUser().isPresent()) return;

        User user = check.getUser().get();

        CheckEndEvent attempt = new CheckEndEvent(check, user, Cause.of(NamedCause.source(this.plugin)));
        Sponge.getEventManager().post(attempt);

        Sponge.getEventManager().unregisterListeners(check);
        check.finish();

        this.checks.remove(check);
    }

    public static class CheckControllerTask {

        private final Guardian plugin;
        private final CheckController checkController;

        private Task.Builder taskBuilder = Task.builder();
        private Task task;

        public CheckControllerTask(Guardian plugin, CheckController checkController) {
            this.plugin = plugin;
            this.checkController = checkController;
        }

        public void start() {
            this.task = this.taskBuilder.execute(() -> {
                this.checkController.cleanup();
                this.checkController.tick();
            }).intervalTicks(1).name("Guardian - Check Controller Task").submit(this.plugin);
        }

        public void stop() {
            if (this.task != null) this.task.cancel();
        }

    }

}
