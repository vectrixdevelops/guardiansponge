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
package io.ichorpowered.guardian.sequence;

import com.abilityapi.sequenceapi.Sequence;
import com.abilityapi.sequenceapi.SequenceBlueprint;
import com.abilityapi.sequenceapi.SequenceContext;
import com.abilityapi.sequenceapi.action.Action;
import com.abilityapi.sequenceapi.action.ActionBuilder;
import com.abilityapi.sequenceapi.action.type.observe.ObserverAction;
import com.abilityapi.sequenceapi.action.type.observe.ObserverActionBlueprint;
import com.abilityapi.sequenceapi.action.type.observe.ObserverActionBuilder;
import com.abilityapi.sequenceapi.action.type.schedule.ScheduleAction;
import com.abilityapi.sequenceapi.action.type.schedule.ScheduleActionBuilder;
import com.google.common.base.MoreObjects;
import com.ichorpowered.guardian.api.detection.DetectionConfiguration;
import com.ichorpowered.guardian.api.sequence.capture.Capture;
import io.ichorpowered.guardian.sequence.capture.GuardianCaptureRegistry;
import io.ichorpowered.guardian.sequence.context.CommonContextKeys;
import org.spongepowered.api.event.Event;

import java.util.ArrayList;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Objects;

public class GuardianSequenceBuilder<E, F extends DetectionConfiguration> implements ActionBuilder<Event> {

    private final List<Capture<E, F>> captures = new ArrayList<>();

    private final List<Action> actions = new ArrayList<>();

    public GuardianSequenceBuilder<E, F> capture(final Capture<E, F> capture) {
        this.captures.add(capture);
        return this;
    }

    @Override
    public ObserverActionBuilder<Event> observe(final Class<? extends Event> event) {
        return this.observe(new ObserverAction<>(event));
    }

    @Override
    public ObserverActionBuilder<Event> observe(final ObserverActionBlueprint<Event> actionBlueprint) {
        return this.observe(actionBlueprint.create());
    }

    @Override
    public ObserverActionBuilder<Event> observe(final ObserverAction<Event> action) {
        this.actions.add(action);

        return new ObserverActionBuilder<>(this, action);
    }

    @Override
    public ScheduleActionBuilder<Event> schedule() {
        return this.schedule(new ScheduleAction());
    }

    @Override
    public ScheduleActionBuilder<Event> schedule(final ScheduleAction action) {
        this.actions.add(action);

        return new ScheduleActionBuilder<>(this, action);
    }

    @Override
    public final SequenceBlueprint<Event> build(final SequenceContext buildContext) {
        return new SequenceBlueprint<Event>() {
            @Override
            public final Sequence<Event> create(final SequenceContext createContext) {
                final SequenceContext modifiedContext = SequenceContext.from(createContext)
                        .custom(CommonContextKeys.TRIGGER, getTrigger())
                        .merge(buildContext).build();

                final GuardianCaptureRegistry captureRegistry = new GuardianCaptureRegistry(
                        modifiedContext.get(CommonContextKeys.ENTITY_ENTRY)
                );

                GuardianSequenceBuilder.this.captures.forEach(capture ->
                        captureRegistry.put(modifiedContext.getId(), capture.getClass(), capture));

                return new GuardianSequence<E, F>(modifiedContext, this, captureRegistry, GuardianSequenceBuilder.this.actions);
            }

            @Override
            public final Class<? extends Event> getTrigger() {
                if (GuardianSequenceBuilder.this.actions.isEmpty()) throw new NoSuchElementException("Sequence could not be established without an initial observer action.");
                if (GuardianSequenceBuilder.this.actions.get(0) instanceof ObserverAction) {
                    return ((ObserverAction<Event>) GuardianSequenceBuilder.this.actions.get(0)).getEventClass();
                } else throw new ClassCastException("Sequence could not be established without an initial observer action.");
            }

            @Override
            public SequenceContext getContext() {
                return buildContext;
            }

            @Override
            public int hashCode() {
                return Objects.hash(buildContext.getOwner(), buildContext.getRoot(), getTrigger());
            }

            @Override
            public boolean equals(final Object other) {
                if(this == other) return true;
                if(other == null || !(other instanceof SequenceBlueprint<?>)) return false;
                final SequenceBlueprint<?> that = (SequenceBlueprint<?>) other;
                return Objects.equals(buildContext.getOwner(), that.getContext().getOwner())
                        && Objects.equals(buildContext.getRoot(), that.getContext().getRoot())
                        && Objects.equals(getTrigger(), that.getTrigger());
            }

            @Override
            public String toString() {
                return MoreObjects.toStringHelper(this)
                        .add("owner", buildContext.getOwner())
                        .add("root", buildContext.getRoot())
                        .add("trigger", getTrigger())
                        .toString();
            }
        };
    }

}

