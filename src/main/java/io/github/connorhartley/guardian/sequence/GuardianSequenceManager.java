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

import com.google.common.collect.HashMultimap;
import com.google.common.collect.Multimap;
import com.ichorpowered.guardian.api.entry.EntityEntry;
import com.ichorpowered.guardian.api.sequence.Sequence;
import com.ichorpowered.guardian.api.sequence.SequenceBlueprint;
import com.ichorpowered.guardian.api.sequence.SequenceManager;
import com.ichorpowered.guardian.api.sequence.capture.Capture;
import io.github.connorhartley.guardian.GuardianPlugin;
import org.spongepowered.api.event.impl.AbstractEvent;

import javax.annotation.Nonnull;
import java.util.Collections;
import java.util.List;
import java.util.function.Predicate;

public class GuardianSequenceManager implements SequenceManager<AbstractEvent> {

    private final GuardianPlugin plugin;
    private final Multimap<EntityEntry, Sequence<?, ?>> sequences = HashMultimap.create();
    private final List<SequenceBlueprint<?, ?>> blueprints = Collections.emptyList();

    public GuardianSequenceManager(GuardianPlugin plugin) {
        this.plugin = plugin;
    }

    @Override
    public void invoke(@Nonnull EntityEntry entry, @Nonnull AbstractEvent abstractEvent) {
        // Sequence Executor
        this.sequences.get(entry).removeIf(sequence -> this.invokeSequence(sequence, entry, abstractEvent));

        // Sequence Blueprint Executor
        this.invokeBluepront(entry, abstractEvent);
    }

    @Override
    public void invokeFor(@Nonnull EntityEntry entry, @Nonnull AbstractEvent abstractEvent, Predicate<Sequence> predicate) {
        // Sequence Executor
        this.sequences.get(entry).removeIf(sequence -> {
           if (predicate.test(sequence)) {
               if (this.invokeSequence(sequence, entry, abstractEvent)) {
                   return true;
               }
           }
           return false;
        });

        // Sequence Blueprint Executor
        this.invokeBluepront(entry, abstractEvent);
    }

    private boolean invokeSequence(@Nonnull Sequence<?, ?> sequence, @Nonnull EntityEntry entry, @Nonnull AbstractEvent event) {
        boolean remove = false;

        // 1. Check the event is valid.

        sequence.apply(entry, event);

        // 2. Check if the sequence is cancelled, or is expired.

        if (sequence.isCancelled() || sequence.isExpired()) {
            if (sequence.isRunning()) {
                for (Capture<?, ?> capture : sequence.getCaptureRegistry()) {
                    capture.stop(entry, sequence.getCaptureRegistry().getContainer());
                }
            }

            remove = true;
        }

        // 3. Check if the sequence has finished and fire the event and remove.

        if (sequence.isFinished()) {
            if (sequence.isRunning()) {
                for (Capture<?, ?> capture : sequence.getCaptureRegistry()) {
                    capture.stop(entry, sequence.getCaptureRegistry().getContainer());
                }
            }

            // Fire SequenceFinishEvent.

            // Pop report.

            remove = true;
        }

        return remove;

    }

    private void invokeBluepront(@Nonnull EntityEntry entry, @Nonnull AbstractEvent event) {
        this.blueprints.stream()
                .filter(blueprint -> this.sequences.get(entry).stream()
                        .noneMatch(playerSequence -> playerSequence.getSequenceBlueprint()
                                .getCheck().compare(blueprint.getCheck())))
                .forEach(blueprint -> {
                    Sequence<?, ?> sequence = blueprint.create(entry);

                    // Fire SequenceStartEvent

                    if (sequence.apply(entry, event)) {
                        if (sequence.isCancelled()) {
                            return;
                        }

                        if (sequence.isFinished()) {
                            if (sequence.isRunning()) {
                                for (Capture<?, ?> capture : sequence.getCaptureRegistry()) {
                                    capture.stop(entry, sequence.getCaptureRegistry().getContainer());
                                }
                            }

                            // Fire SequenceFinishEvent.

                            return;
                        }

                        this.sequences.put(entry, sequence);
                    }
                });

    }

    @Override
    public void clean(boolean force) {
        this.sequences.keys().forEach(player -> this.clean(player, force));
    }

    @Override
    public void clean(@Nonnull EntityEntry entry, boolean force) {
        if (force) this.sequences.removeAll(entry);
        else this.sequences.entries().forEach((el) -> {
            if (el.getKey().equals(entry) && el.getValue().isExpired())
                this.sequences.remove(el.getKey(), el.getValue());
        });

    }

    public void update() {
        this.sequences.forEach((entry, playerSequence) -> {
            if (playerSequence.isRunning()) {
                for (Capture capture : playerSequence.getCaptureRegistry()) {
                    capture.update(entry, playerSequence.getCaptureRegistry().getContainer());
                }
            }
        });
    }


}
