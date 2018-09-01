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
package com.ichorpowered.guardian.sponge.sequence;

import com.google.common.collect.HashMultimap;
import com.google.common.collect.Lists;
import com.google.common.collect.Multimap;
import com.google.common.reflect.TypeToken;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import com.ichorpowered.guardian.api.Guardian;
import com.ichorpowered.guardian.api.detection.Detection;
import com.ichorpowered.guardian.api.detection.stage.StageCycle;
import com.ichorpowered.guardian.api.detection.stage.process.Heuristic;
import com.ichorpowered.guardian.api.detection.stage.process.Penalty;
import com.ichorpowered.guardian.api.game.GameReference;
import com.ichorpowered.guardian.api.game.resource.PlayerGroupResource;
import com.ichorpowered.guardian.api.game.resource.PlayerResource;
import com.ichorpowered.guardian.api.game.resource.ResourceFactories;
import com.ichorpowered.guardian.api.sequence.Sequence;
import com.ichorpowered.guardian.api.sequence.SequenceBlueprint;
import com.ichorpowered.guardian.api.sequence.SequenceContext;
import com.ichorpowered.guardian.api.sequence.SequenceController;
import com.ichorpowered.guardian.api.sequence.SequenceRegistry;
import com.ichorpowered.guardian.api.sequence.process.Process;
import com.ichorpowered.guardian.api.sequence.process.ProcessResult;
import com.ichorpowered.guardian.common.detection.stage.type.CheckStageImpl;
import com.ichorpowered.guardian.common.detection.stage.type.HeuristicStageImpl;
import com.ichorpowered.guardian.common.detection.stage.type.PenaltyStageImpl;
import com.ichorpowered.guardian.common.util.Ordered;
import com.ichorpowered.guardian.sponge.GuardianPlugin;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.event.Event;
import org.spongepowered.api.scheduler.Task;

import java.util.Collection;
import java.util.List;
import java.util.function.Predicate;

@Singleton
public final class SequenceControllerImpl implements SequenceController<Event> {

    private final SequenceRegistry sequenceRegistry;
    private final PlayerResource playerResource;
    private final PlayerGroupResource playerGroupResource;

    private final Multimap<String, Sequence<Event>> sequences = HashMultimap.create();
    private final Multimap<GameReference<?>, Ordered<Class<? extends Event>>> avoidedObservers = HashMultimap.create();
    private final List<GameReference<?>> observingPlayers = Lists.newArrayList();

    private int observeIndex = 0;
    private long avoidIndex = 0;

    @Inject
    public SequenceControllerImpl(final SequenceRegistry sequenceRegistry,
                                  final ResourceFactories resourceFactories) {
        this.sequenceRegistry = sequenceRegistry;
        this.playerResource = resourceFactories.create();
        this.playerGroupResource = resourceFactories.create(20, 5);
    }

    @Override
    public void invokeObserver(final @NonNull Event event, final @NonNull GameReference<?> gameReference,
                               final @NonNull SequenceContext sequenceContext, final @NonNull Predicate<Sequence<Event>> predicate) {
        final boolean forceObserve = sequenceContext.get("root:force_observe", TypeToken.of(Boolean.class));
        if (!forceObserve && !this.observingPlayers.contains(gameReference)) return;

        this.sequences.get(gameReference.getGameId()).removeIf(sequence -> {
            for (Ordered<Class<? extends Event>> order : this.avoidedObservers.get(gameReference)) {
                if (order.getElement().equals(sequence.getEventType())) return true;
            }

            // Add new or overwrite context to the sequence.
            sequence.getProcess().getContext().from(sequenceContext);

            if (!predicate.test(sequence)) return false;

            return this._invokeObserver(event, sequence);
        });

        this._createBlueprints(event, gameReference, sequenceContext);
    }

    @Override
    public void invokeScheduler(final @NonNull GameReference<?> gameReference, final @NonNull SequenceContext sequenceContext,
                                final @NonNull Predicate<Sequence<Event>> predicate) {
        if (!this.observingPlayers.contains(gameReference)) return;

        this.sequences.get(gameReference.getGameId()).removeIf(sequence -> {
            for (Ordered<Class<? extends Event>> order : this.avoidedObservers.get(gameReference)) {
                if (order.getElement().equals(sequence.getEventType())) return true;
            }

            // Add new or overwrite context to the sequence.
            sequence.getProcess().getContext().from(sequenceContext);

            if (!predicate.test(sequence)) return false;

            return this._invokeScheduleAt(sequence) || this._invokeScheduleAfter(sequence);
        });
    }

    @Override
    public long avoidObserver(final @NonNull GameReference<?> gameReference, final @NonNull Class<? extends Event> eventType) {
        final long index = this.avoidIndex++;

        this.avoidedObservers.put(gameReference, new Ordered<>(index, eventType));
        this.sequences.get(gameReference.getGameId()).removeIf(sequence -> eventType.equals(sequence.getEventType()));

        return index;
    }

    @Override
    public boolean unavoidObserver(final @NonNull GameReference<?> gameReference, final @NonNull Class<? extends Event> eventType,
                                   final long index) {
        return this.avoidedObservers.remove(gameReference, new Ordered<>(index, eventType));
    }

    @Override
    public @NonNull PlayerResource getPlayerResource() {
        return this.playerResource;
    }

    @Override
    public @NonNull PlayerGroupResource getPlayerGroupResource() {
        return this.playerGroupResource;
    }

    @Override
    public void clean(final boolean force) {
        Sponge.getServer().getOnlinePlayers().forEach(player -> this.playerResource.get(player.getUniqueId().toString()).ifPresent(gameReference -> this.clean(gameReference, force)));
    }

    @Override
    public void clean(final @NonNull GameReference<?> gameReference, final boolean force) {
        this.sequences.get(gameReference.getGameId()).removeIf(sequence -> {
           if (sequence.getProcess().getState().equals(Process.State.FINISHED)) {
               return force;
           }

           return force || sequence.getProcess().getState().equals(Process.State.EXPIRED);
        });
    }

    private boolean _invokeObserver(final Event event, final Sequence<Event> sequence) {
        boolean remove = false;

        // 1. Apply the sequence, update the process state.

        sequence.tryObserve(event);

        // 2. Check if the sequence is cancelled, or is expired.

        final SequenceImpl concreteSequence = (SequenceImpl) sequence;

        concreteSequence.updateState();

        if (!sequence.getProcess().getState().isSafe()) {
            remove = true;
        }

        // 3. Check if the sequence has finished and fire the hook and remove.

        if (sequence.getProcess().getState().equals(Process.State.FINISHED)) {
            // Fire sequence finish hook.

            // Transition phase.
            this.transition(sequence);

            remove = true;
        }

        return remove;
    }

    private boolean _invokeScheduleAfter(final Sequence<Event> sequence) {
        boolean remove;

        // 1. Apply the sequence, update the process state, this has one chance to pass, so if it fails it can be removed.

        final ProcessResult result = sequence.tryScheduleAfter();

        remove = !result.toNext() && !result.toSkip();

        // 2. Check if the sequence is cancelled, or is expired.

        final SequenceImpl concreteSequence = (SequenceImpl) sequence;

        concreteSequence.updateState();

        if (!sequence.getProcess().getState().isSafe()) {
            remove = true;
        }

        // 3. Check if the sequence has finished and fire the hook and remove.

        if (sequence.getProcess().getState().equals(Process.State.FINISHED)) {
            // Fire sequence finish hook.

            // Transition phase.
            this.transition(sequence);

            remove = true;
        }

        return remove;
    }

    private boolean _invokeScheduleAt(final Sequence<Event> sequence) {
        boolean remove = false;

        // 1. Apply the sequence, update the process state.

        sequence.tryScheduleAt();

        // 2. Check if the sequence is cancelled, or is expired.

        final SequenceImpl concreteSequence = (SequenceImpl) sequence;

        concreteSequence.updateState();

        if (!sequence.getProcess().getState().isSafe()) {
            remove = true;
        }

        // 3. Check if the sequence has finished and fire the hook and remove.

        if (sequence.getProcess().getState().equals(Process.State.FINISHED)) {
            // Fire sequence finish hook.

            // Transition phase.
            this.transition(sequence);

            remove = true;
        }

        return remove;
    }

    private void _createBlueprints(final Event event, final GameReference<?> gameReference,
                                   final SequenceContext sequenceContext) {
        for (SequenceBlueprint<?> raw : this.sequenceRegistry.values()) {
            final SequenceBlueprint<Event> sequenceBlueprint = (SequenceBlueprint<Event>) raw;

            // 1. Check for matching sequence.

            if (this.sequences.get(gameReference.getGameId()).stream()
                    .anyMatch(playerSequence -> playerSequence.getBlueprint()
                            .equals(sequenceBlueprint))) continue;

            // 2. Apply the sequence for the first time to check the observer or leave it.

            final Sequence<Event> sequence = sequenceBlueprint.create(event, sequenceContext);

            final ProcessResult result = sequence.tryObserve(event);

            if (result.toNext()) {

                if (!sequence.getProcess().getState().isSafe()) {
                    continue;
                }

                if (sequence.getProcess().getState().equals(Process.State.FINISHED)) {
                    // Fire sequence finish hook.

                    continue;
                }

                this.sequences.put(gameReference.getGameId(), sequence);
            }
        }
    }

    private void transition(final Sequence<Event> sequence) {
        final StageCycle stageCycle = sequence.getProcess().getContext().get("root:owner", TypeToken.of(Detection.class)).getStageCycle();

        while (stageCycle.next()) {
            if (stageCycle.getStage().isPresent() && CheckStageImpl.class.isAssignableFrom(stageCycle.getStage().get().getClass())) {
                continue;
            }

            if (stageCycle.getStage().isPresent() && HeuristicStageImpl.class.isAssignableFrom(stageCycle.getStage().get().getClass())) {
                if (!stageCycle.<Heuristic>getStageProcess().isPresent()) continue;
                final Heuristic heuristic = stageCycle.<Heuristic>getStageProcess().get();
                heuristic.test(sequence.getProcess());
            }

            if (stageCycle.getStage().isPresent() && PenaltyStageImpl.class.isAssignableFrom(stageCycle.getStage().get().getClass())) {
                if (!stageCycle.<Penalty>getStageProcess().isPresent()) continue;
                final Penalty penalty = stageCycle.<Penalty>getStageProcess().get();
                penalty.test(sequence.getProcess());
            }
        }
    }

    private void run() {
        this.observingPlayers.forEach(playerReference -> this.invokeScheduler(playerReference,
                new SequenceContextImpl()
                        .add("root:player", new TypeToken<GameReference<Player>>() {}, (GameReference<Player>) playerReference), sequence -> {
            if (sequence.getProcess().getState().equals(Process.State.ACTIVE)) {
                sequence.getProcess().getCaptures().values().forEach(value -> value.get().apply(sequence.getProcess()));
                return true;
            }

            return false;
        }));
    }

    private void nextObservers() {
        final Collection<GameReference<?>> gameReferences = this.playerGroupResource.getGroup(this.observeIndex++);
        if (this.playerGroupResource.getGroup(this.observeIndex).isEmpty() || this.observeIndex >= 100) {
            this.observeIndex = 0;
        }

        this.observingPlayers.clear();
        this.observingPlayers.addAll(gameReferences);
    }

    public static class SequenceTask {

        private final GuardianPlugin plugin;
        private final SequenceControllerImpl sequenceController;

        private Task task;

        public SequenceTask(final GuardianPlugin plugin,
                            final SequenceController sequenceController) {
            this.plugin = plugin;
            this.sequenceController = (SequenceControllerImpl) sequenceController;
        }

        public void start() {
            final long updatePeriod = Guardian.getGlobalConfiguration().getRoot().getNode("global", "sequence", "update-period").getLong();

            this.task = Sponge.getScheduler().createTaskBuilder()
                    .name("Guardian - SequenceController")
                    .execute(executor -> {
                        this.sequenceController.nextObservers();
                        this.sequenceController.run();
                        this.sequenceController.clean(false);
                    })
                    .intervalTicks(updatePeriod)
                    .submit(this.plugin);
        }

        public void stop() {
            if (this.task != null) this.task.cancel();
        }

    }

}
