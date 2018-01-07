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
package com.ichorpowered.guardian.sequence;

import com.abilityapi.sequenceapi.Sequence;
import com.abilityapi.sequenceapi.SequenceContext;
import com.abilityapi.sequenceapi.SequenceManager;
import com.abilityapi.sequenceapi.SequenceRegistry;
import com.ichorpowered.guardian.GuardianPlugin;
import com.ichorpowered.guardian.entry.GuardianPlayerEntry;
import com.ichorpowered.guardian.sequence.context.CommonContextKeys;
import com.ichorpowered.guardianapi.detection.capture.Capture;
import com.ichorpowered.guardianapi.detection.check.CheckModel;
import com.ichorpowered.guardianapi.detection.heuristic.Heuristic;
import com.ichorpowered.guardianapi.detection.heuristic.HeuristicModel;
import com.ichorpowered.guardianapi.detection.penalty.Penalty;
import com.ichorpowered.guardianapi.detection.penalty.PenaltyModel;
import com.ichorpowered.guardianapi.detection.stage.StageCycle;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.event.Event;
import org.spongepowered.api.scheduler.Task;

public class GuardianSequenceManager extends SequenceManager<Event> {

    private final GuardianPlugin plugin;

    public GuardianSequenceManager(final GuardianPlugin plugin,
                                   final SequenceRegistry<Event> sequenceRegistry) {
        super(sequenceRegistry);
        this.plugin = plugin;
    }

    @Override
    public boolean _invokeObserver(final Event event,
                                   final Sequence<Event> sequence,
                                   final SequenceContext sequenceContext) {
        boolean result = super._invokeObserver(event, sequence, sequenceContext);

        if (sequence.getState().equals(Sequence.State.FINISHED)) {
            this.transitionPhase(sequenceContext.get(CommonContextKeys.ENTITY_ENTRY), (GuardianSequence) sequence);
        }

        return result;
    }

    @Override
    public boolean _invokeScheduler(final Sequence<Event> sequence,
                                    final SequenceContext sequenceContext) {
        boolean result = super._invokeScheduler(sequence, sequenceContext);

        if (sequence.getState().equals(Sequence.State.FINISHED)) {
            this.transitionPhase(sequenceContext.get(CommonContextKeys.ENTITY_ENTRY), (GuardianSequence) sequence);
        }

        return result;
    }

    private void transitionPhase(final GuardianPlayerEntry entityEntry, final GuardianSequence sequence) {
        final StageCycle stageCycle = sequence.getOwner().getStageCycle();

        while (stageCycle.next()) {
            if (stageCycle.getModel().isPresent() && CheckModel.class.isAssignableFrom(stageCycle.getModel().get().getClass())) {
                if (!stageCycle.nextModel()) return;
                continue;
            }

            if (stageCycle.getModel().isPresent() && HeuristicModel.class.isAssignableFrom(stageCycle.getModel().get().getClass())) {
                if (!stageCycle.<Heuristic>getStage().isPresent()) continue;
                Heuristic heuristic = stageCycle.<Heuristic>getStage().get();
                heuristic.getPredicate().test(sequence.getOwner(), sequence.getSummary(), entityEntry);
            }

            if (stageCycle.getModel().isPresent() && PenaltyModel.class.isAssignableFrom(stageCycle.getModel().get().getClass())) {
                if (!stageCycle.<Penalty>getStage().isPresent()) continue;
                Penalty penalty = stageCycle.<Penalty>getStage().get();
                penalty.getPredicate().test(sequence.getOwner(), sequence.getSummary(), entityEntry);
            }
        }
    }

    private void tickScheduler() {
        Sponge.getServer().getOnlinePlayers().forEach(player -> {
            final GuardianPlayerEntry entityEntry = GuardianPlayerEntry.of(player, player.getUniqueId());

            this.updateSchedulerIf(
                    SequenceContext.builder()
                            .id(entityEntry.getUniqueId())
                            .custom(CommonContextKeys.ENTITY_ENTRY, entityEntry)
                            .build(),

                    sequence -> {
                        final GuardianSequence guardianSequence = (GuardianSequence) sequence;

                        // Update captures.
                        if (guardianSequence.getState().equals(Sequence.State.ACTIVE)) {
                            for (Capture capture : guardianSequence.getCaptureRegistry()) {
                                capture.update(entityEntry, guardianSequence.getCaptureRegistry().getContainer());
                            }

                            return true;
                        }

                        return false;
                    }
            );
        });
    }

    public static class SequenceTask {

        private final GuardianPlugin plugin;
        private final GuardianSequenceManager sequenceManager;

        private Task task;

        public SequenceTask(final GuardianPlugin plugin,
                            final GuardianSequenceManager sequenceManager) {
            this.plugin = plugin;
            this.sequenceManager = sequenceManager;
        }

        public void start() {
            this.task = Task.builder()
                    .name("Guardian - Sequence Tick")
                    .execute(() -> {
                        this.sequenceManager.clean(false);
                        this.sequenceManager.tickScheduler();
                    })
                    .intervalTicks(1)
                    .submit(this.plugin);
        }

        public void stop() {
            if (this.task != null) this.task.cancel();
        }
    }
}
