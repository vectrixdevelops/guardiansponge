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

import com.google.common.base.MoreObjects;
import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import com.google.common.reflect.TypeToken;
import com.ichorpowered.guardian.api.detection.Detection;
import com.ichorpowered.guardian.api.game.GameReference;
import com.ichorpowered.guardian.api.game.model.value.key.GameKey;
import com.ichorpowered.guardian.api.sequence.Sequence;
import com.ichorpowered.guardian.api.sequence.SequenceBlueprint;
import com.ichorpowered.guardian.api.sequence.SequenceBuilder;
import com.ichorpowered.guardian.api.sequence.SequenceContext;
import com.ichorpowered.guardian.api.sequence.action.Action;
import com.ichorpowered.guardian.api.sequence.action.after.AfterAction;
import com.ichorpowered.guardian.api.sequence.action.after.AfterActionBuilder;
import com.ichorpowered.guardian.api.sequence.action.observe.ObserverAction;
import com.ichorpowered.guardian.api.sequence.action.observe.ObserverActionBlueprint;
import com.ichorpowered.guardian.api.sequence.action.observe.ObserverActionBuilder;
import com.ichorpowered.guardian.api.sequence.action.schedule.ScheduleAction;
import com.ichorpowered.guardian.api.sequence.action.schedule.ScheduleActionBuilder;
import com.ichorpowered.guardian.api.sequence.capture.CaptureRegistry;
import com.ichorpowered.guardian.api.sequence.capture.CaptureValue;
import com.ichorpowered.guardian.api.sequence.process.Process;
import com.ichorpowered.guardian.sponge.sequence.action.after.AfterActionBuilderImpl;
import com.ichorpowered.guardian.sponge.sequence.action.after.AfterActionImpl;
import com.ichorpowered.guardian.sponge.sequence.action.observe.ObserverActionBuilderImpl;
import com.ichorpowered.guardian.sponge.sequence.action.observe.ObserverActionImpl;
import com.ichorpowered.guardian.sponge.sequence.action.schedule.ScheduleActionBuilderImpl;
import com.ichorpowered.guardian.sponge.sequence.action.schedule.ScheduleActionImpl;
import com.ichorpowered.guardian.sponge.sequence.capture.CaptureRegistryImpl;
import com.ichorpowered.guardian.sponge.sequence.process.ProcessImpl;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.event.Event;

import java.util.List;
import java.util.NoSuchElementException;
import java.util.Objects;

public class SequenceBuilderImpl<T extends Event> implements SequenceBuilder<T> {

    private final List<GameKey<CaptureValue>> captures = Lists.newArrayList();
    private final List<Action> actions = Lists.newArrayList();

    @SuppressWarnings("unchecked")
    @Override
    public @NonNull SequenceBuilder<T> captures(final @NonNull GameKey<?>... captureValues) {
        this.captures.addAll(Sets.newHashSet((GameKey<CaptureValue>[]) captureValues));
        return this;
    }

    @Override
    public ObserverActionBuilder<T> observe(final @NonNull Class<T> event) {
        return this.observe(new ObserverActionImpl<>(event));
    }

    @Override
    public ObserverActionBuilder<T> observe(final @NonNull ObserverActionBlueprint<T> actionBlueprint) {
        return this.observe(actionBlueprint.create());
    }

    @Override
    public ObserverActionBuilder<T> observe(final @NonNull ObserverAction<T> action) {
        this.actions.add(action);

        return new ObserverActionBuilderImpl<>(this, action);
    }

    @Override
    public AfterActionBuilder<T> after() {
        return this.after(new AfterActionImpl());
    }

    @Override
    public AfterActionBuilder<T> after(final @NonNull AfterAction afterAction) {
        this.actions.add(afterAction);

        return new AfterActionBuilderImpl<>(this, afterAction);
    }

    @Override
    public ScheduleActionBuilder<T> schedule() {
        return this.schedule(new ScheduleActionImpl());
    }

    @Override
    public ScheduleActionBuilder<T> schedule(final @NonNull ScheduleAction scheduleAction) {
        this.actions.add(scheduleAction);

        return new ScheduleActionBuilderImpl<>(this, scheduleAction);
    }

    @SuppressWarnings("unchecked")
    @Override
    public SequenceBlueprint<T> build(final @NonNull SequenceContext buildContext) {
        return new SequenceBlueprint<T>() {
            @Override
            public @NonNull Sequence<T> create(final @NonNull T event, final @NonNull SequenceContext sequenceContext) {
                SequenceContext merged = new SequenceContextImpl().from(sequenceContext).from(buildContext);
                merged.add("root:event", new TypeToken<Event>() {}, event);
                merged.add("root:captures", new TypeToken<List<GameKey<CaptureValue>>>() {}, SequenceBuilderImpl.this.captures);

                final CaptureRegistry captureRegistry = new CaptureRegistryImpl(merged.get("root:player", new TypeToken<GameReference<Player>>() {}), SequenceBuilderImpl.this.captures);
                final Process process = new ProcessImpl(captureRegistry, merged, Process.State.INACTIVE);

                return new SequenceImpl<>(event, process, this, (base, that) -> base.equals(that.getClass()), SequenceBuilderImpl.this.actions);
            }

            @Override
            public @NonNull Class<T> getEventType() {
                if (SequenceBuilderImpl.this.actions.isEmpty()) throw new NoSuchElementException("Sequence could not be established without an initial observer action.");
                if (SequenceBuilderImpl.this.actions.get(0) instanceof ObserverAction) {
                    return ((ObserverAction<T>) SequenceBuilderImpl.this.actions.get(0)).getEventType();
                } else throw new ClassCastException("Sequence could not be established without an initial observer action.");
            }

            @Override
            public @NonNull SequenceContext getContext() {
                return buildContext;
            }

            @Override
            public int hashCode() {
                return Objects.hash(buildContext.get("root:owner", TypeToken.of(Detection.class)), buildContext.get("root:event_type", new TypeToken<Class<? extends Event>>() {}));
            }

            @Override
            public boolean equals(final Object other) {
                if (this == other) return true;
                if (!(other instanceof SequenceBlueprint<?>)) return false;
                final SequenceBlueprint<?> that = (SequenceBlueprint<?>) other;
                return Objects.equals(buildContext.get("root:owner", TypeToken.of(Detection.class)), that.getContext().get("root:owner", TypeToken.of(Detection.class)))
                        && Objects.equals(buildContext.get("root:event_type", new TypeToken<Class<? extends Event>>() {}), that.getContext().get("root:event_type", new TypeToken<Class<? extends Event>>() {}));
            }

            @Override
            public String toString() {
                return MoreObjects.toStringHelper(this)
                        .add("owner", buildContext.get("root:owner", TypeToken.of(Detection.class)))
                        .add("event_type", buildContext.get("root:event_type", new TypeToken<Class<? extends Event>>() {}))
                        .toString();
            }

        };
    }

}
