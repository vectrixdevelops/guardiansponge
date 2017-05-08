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
package io.github.connorhartley.guardian.sequence;

import io.github.connorhartley.guardian.Guardian;
import io.github.connorhartley.guardian.detection.check.Check;
import io.github.connorhartley.guardian.detection.check.CheckController;
import io.github.connorhartley.guardian.detection.check.CheckType;
import io.github.connorhartley.guardian.event.sequence.SequenceBeginEvent;
import io.github.connorhartley.guardian.event.sequence.SequenceFinishEvent;
import io.github.connorhartley.guardian.util.compatibility.NucleusSequenceListener;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.entity.living.player.User;
import org.spongepowered.api.event.Event;
import org.spongepowered.api.event.cause.Cause;
import org.spongepowered.api.event.cause.NamedCause;
import org.spongepowered.api.scheduler.Task;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

/**
 * Sequence Controller
 *
 * Controls all the sequences for each {@link Check}.
 */
public class SequenceController implements SequenceInvoker {

    private final Guardian plugin;
    private final CheckController checkController;
    private final List<SequenceBlueprint> blueprints = new ArrayList<>();

    private final HashMap<Player, List<Sequence>> runningSequences = new HashMap<>();

    public SequenceController(Guardian plugin, CheckController checkController) {
        this.plugin = plugin;
        this.checkController = checkController;
    }

    @Override
    public void invoke(Player player, Event event) {
        List<Sequence> currentlyExecuting;

        if (!this.runningSequences.containsKey(player)) {
            currentlyExecuting = new ArrayList<>();
            runningSequences.put(player, currentlyExecuting);
        } else {
            currentlyExecuting = this.runningSequences.get(player);
        }

        currentlyExecuting.forEach(sequence -> sequence.check(player, event));
        currentlyExecuting.removeIf(Sequence::isCancelled);
        currentlyExecuting.removeIf(Sequence::hasExpired);
        currentlyExecuting.removeIf(sequence -> {
            if (!sequence.isFinished()) {
                return false;
            }

            if (sequence.hasStarted()) {
                sequence.getCaptureHandler().stop();
            }

            SequenceFinishEvent attempt = new SequenceFinishEvent(sequence, player, sequence.getSequenceReport(),
                    Cause.of(NamedCause.source(this.plugin), NamedCause.of("CONTEXT", sequence.getCaptureContainer())));
            Sponge.getEventManager().post(attempt);
            if (attempt.isCancelled()) {
                return true;
            }

            CheckType checkType = sequence.getProvider();
            this.checkController.post(checkType, player);
            return true;
        });
        this.runningSequences.put(player, currentlyExecuting);

        this.blueprints.stream()
                .filter(blueprint -> !currentlyExecuting.contains(blueprint))
                .forEach(blueprint -> {
                    Sequence sequence = blueprint.create(player);

                    SequenceBeginEvent attempt = new SequenceBeginEvent(sequence, player, sequence.getSequenceReport(),
                            Cause.of(NamedCause.source(this.plugin), NamedCause.of("CONTEXT", sequence.getCaptureContainer())));
                    Sponge.getEventManager().post(attempt);
                    if (attempt.isCancelled()) {
                        return;
                    }

                    if (sequence.check(player, event)) {
                        if (sequence.isCancelled()) {
                            return;
                        }

                        if (sequence.isFinished()) {
                            CheckType checkType = sequence.getProvider();
                            this.checkController.post(checkType, player);
                            return;
                        }

                        currentlyExecuting.add(sequence);
                    }
                });

        this.runningSequences.put(player, currentlyExecuting);
    }

    public void update() {
        Sponge.getServer().getOnlinePlayers().forEach(player -> {
            if (this.runningSequences.get(player) == null || this.runningSequences.get(player).isEmpty()) return;
            this.runningSequences.get(player).forEach(sequence -> {
                if (sequence.hasStarted()) {
                    sequence.getCaptureHandler().setContainer(sequence.getCaptureHandler().update());
                }
            });
        });
    }

    /**
     * Clean Up
     *
     * <p>Removes any {@link Sequence}s that have expired from the {@link User}s running sequences.</p>
     */
    public void cleanup() {
        Sponge.getServer().getOnlinePlayers().forEach(player -> {
            if (this.runningSequences.get(player) == null || this.runningSequences.get(player).isEmpty()) return;
            this.runningSequences.get(player).removeIf(Sequence::hasExpired);
        });
    }

    /**
     * Force Clean Up
     *
     * <p>Removes the {@link User}'s data from the running sequences.</p>
     *
     * @param player {@link Player} to remove data from
     */
    public void forceCleanup(Player player) {
        this.runningSequences.remove(player);
    }

    /**
     * Force Clean Up
     *
     * <p>Removes running sequences from all of the players online.</p>
     */
    public void forceCleanup() {
        this.runningSequences.clear();
    }

    /**
     * Register
     *
     * <p>Registers a {@link Sequence} from a {@link CheckType}.</p>
     *
     * @param checkType Type of a {@link Sequence}
     */
    public void register(CheckType checkType) {
        this.blueprints.add(checkType.getSequence());
    }

    /**
     * Unregister
     *
     * <p>Unregisters a {@link Sequence} from a {@link CheckType}.</p>
     *
     * @param checkType Type of a {@link Sequence}
     */
    public void unregister(CheckType checkType) {
        this.blueprints.removeIf(blueprint -> blueprint.getCheckProvider().equals(checkType));
    }

    public static class SequenceControllerTask {

        private final Guardian plugin;
        private final SequenceController sequenceController;
        private final SequenceListener sequenceListener;

        private NucleusSequenceListener nucleusSequenceListener;
        private Task.Builder taskBuilder = Task.builder();
        private Task cleanTask;
        private Task updateTask;

        public SequenceControllerTask(Guardian plugin, SequenceController sequenceController) {
            this.plugin = plugin;
            this.sequenceController = sequenceController;
            this.sequenceListener = new SequenceListener(this.sequenceController);

            if (Sponge.getPluginManager().getPlugin("nucleus").isPresent()) {
                this.nucleusSequenceListener = new NucleusSequenceListener(this.sequenceController);
            }

            register();
        }

        public void register() {
            Sponge.getEventManager().registerListeners(this.plugin, this.sequenceListener);

            if (Sponge.getPluginManager().getPlugin("nucleus").isPresent() && this.nucleusSequenceListener != null) {
                Sponge.getEventManager().registerListeners(this.plugin, this.nucleusSequenceListener);
            }
        }

        public void start() {
            this.cleanTask = this.taskBuilder.execute(this.sequenceController::cleanup).intervalTicks(1)
                    .name("Guardian - Sequence Controller Task - Clean Up").submit(this.plugin);

            this.updateTask = this.taskBuilder.execute(this.sequenceController::update).intervalTicks(1)
                    .name("Guardian - Sequence Controller Task - Update").submit(this.plugin);
        }

        public void stop() {
            if (this.cleanTask != null) this.cleanTask.cancel();
            if (this.updateTask != null) this.updateTask.cancel();

            Sponge.getEventManager().unregisterListeners(this.sequenceListener);

            if (nucleusSequenceListener != null) Sponge.getEventManager().unregisterListeners(this.nucleusSequenceListener);
        }

    }

}
