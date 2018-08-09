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

import com.google.common.reflect.TypeToken;
import com.ichorpowered.guardian.api.game.GameReference;
import com.ichorpowered.guardian.api.sequence.Sequence;
import com.ichorpowered.guardian.api.sequence.SequenceBlueprint;
import com.ichorpowered.guardian.api.sequence.action.Action;
import com.ichorpowered.guardian.api.sequence.action.Expirable;
import com.ichorpowered.guardian.api.sequence.action.after.AfterAction;
import com.ichorpowered.guardian.api.sequence.action.observe.ObserverAction;
import com.ichorpowered.guardian.api.sequence.action.schedule.ScheduleAction;
import com.ichorpowered.guardian.api.sequence.process.Process;
import com.ichorpowered.guardian.api.sequence.process.ProcessResult;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.event.Event;
import org.spongepowered.api.world.Location;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.function.BiPredicate;

public class SequenceImpl<T extends Event> implements Sequence<T> {

    private final T event;
    private final Process process;
    private final SequenceBlueprint<T> sequenceBlueprint;
    private final BiPredicate<Class<? extends T>, T> eventPredicate;
    private final List<Action> actions;
    private final int sequenceLength;

    private int sequenceIndex = 0;
    private long sequenceRuns = 0;
    private long sequenceStepTime = System.currentTimeMillis();

    public SequenceImpl(final T event,
                        final Process process,
                        final SequenceBlueprint<T> sequenceBlueprint,
                        final BiPredicate<Class<? extends T>, T> eventPredicate,
                        final List<Action> actions) {
        this.event = event;
        this.process = process;
        this.sequenceBlueprint = sequenceBlueprint;
        this.eventPredicate = eventPredicate;
        this.actions = new ArrayList<>(actions);

        this.sequenceLength = actions.size();
    }

    @Override
    public @NonNull SequenceBlueprint<T> getBlueprint() {
        return this.sequenceBlueprint;
    }

    @Override
    public @NonNull Process getProcess() {
        return this.process;
    }

    @Override
    public @NonNull BiPredicate<Class<? extends T>, T> getEventComparator() {
        return this.eventPredicate;
    }

    @Override
    public @NonNull Class<T> getEventType() {
        return this.sequenceBlueprint.getEventType();
    }

    @Override
    public @NonNull ProcessResult tryObserve(final @NonNull T event) {
        final GameReference<Player> gameReference = this.process.getContext().get("root:player", new TypeToken<GameReference<Player>>() {});
        if (gameReference == null) return this.process.end();

        final Player player = gameReference.get();

        // Add context.

        this.process.getContext().set("root:step_time", TypeToken.of(Long.class), this.sequenceStepTime);

        if (this.process.getState().equals(Process.State.INACTIVE)) {
            this.process.getContext().add("root:player_location", TypeToken.of(Location.class), player.getLocation());
        }

        // Run actions.

        final Iterator<Action> iterator = this.actions.iterator();

        if (iterator.hasNext()) {
            final Action raw = iterator.next();

            final ObserverAction<T> action;
            if (raw instanceof ObserverAction) {
                action = (ObserverAction<T>) raw;
            } else return this.process.end();

            if (this.process.getState().equals(Process.State.INACTIVE)) this.process.setState(Process.State.ACTIVE);

            final long current = System.currentTimeMillis();

            // 1. Check that the event is the correct one for this action.

            if (!this.eventPredicate.test(action.getEventType(), event)) return this.process.end();

            // 2. Fail the action if it is being executed before the delay.

            if (this.sequenceIndex > 0 && this.sequenceStepTime + ((action.getDelay() / 20) * 1000) > current) {
                return this.process.end();
            }

            // 3. Fail the action if it being executed after the expire.

            if (this.sequenceIndex > 0 && this.sequenceStepTime + ((action.getExpire() / 20) * 1000) < current) {
                return this.process.end();
            }

            // 4. Run the action conditions and fail if they do not pass.

            if (!action.apply(this.process)) {
                return this.process.end();
            }

            // 5. Succeed the action, remove it, increment the index and set finish if there are no more actions left.
            this.sequenceIndex++;

            iterator.remove();

            this.sequenceStepTime = System.currentTimeMillis();

            if (!iterator.hasNext()) {
                if (this.sequenceIndex == this.sequenceLength) this.process.setState(Process.State.FINISHED);
            }
        }

        return this.process.next();
    }

    @Override
    public @NonNull ProcessResult tryScheduleAfter() {
        final GameReference<Player> gameReference = this.process.getContext().get("root:player", new TypeToken<GameReference<Player>>() {});
        if (gameReference == null) return this.process.end();

        final Player player = gameReference.get();

        // Add context.

        this.process.getContext().set("root:step_time", TypeToken.of(Long.class), this.sequenceStepTime);

        if (this.process.getState().equals(Process.State.INACTIVE)) {
            this.process.getContext().add("root:player_location", TypeToken.of(Location.class), player.getLocation());
        }

        // Run actions.

        final Iterator<Action> iterator = this.actions.iterator();

        if (iterator.hasNext()) {
            final Action raw = iterator.next();

            final AfterAction action;
            if (raw instanceof AfterAction) {
                action = (AfterAction) raw;
            } else return this.process.skip();

            if (this.process.getState().equals(Process.State.INACTIVE)) this.process.setState(Process.State.ACTIVE);

            final long current = System.currentTimeMillis();

            // 1. Fail the action if it is being executed before the delay.

            if (this.sequenceStepTime + ((action.getDelay() / 20) * 1000) > current) {
                return this.process.skip();
            }

            // 2. Run the action conditions and fail if they do not pass.

            if (!action.apply(this.process)) {
                return this.process.end();
            }

            // 3. Succeed the action, remove it, increment the index and set finish if there are no more actions left.
            this.sequenceIndex++;

            iterator.remove();

            this.sequenceStepTime = System.currentTimeMillis();

            if (!iterator.hasNext()) {
                if (this.sequenceIndex == this.sequenceLength) this.process.setState(Process.State.FINISHED);
            }
        }

        return this.process.next();
    }

    @Override
    public @NonNull ProcessResult tryScheduleAt() {
        this.updateState();

        final GameReference<Player> gameReference = this.process.getContext().get("root:player", new TypeToken<GameReference<Player>>() {});
        if (gameReference == null) return this.process.end();

        final Player player = gameReference.get();

        // Add context.

        this.process.getContext().set("root:step_time", TypeToken.of(Long.class), this.sequenceStepTime);

        if (this.process.getState().equals(Process.State.INACTIVE)) {
            this.process.getContext().add("root:player_location", TypeToken.of(Location.class), player.getLocation());
        }

        // Run actions.

        final Iterator<Action> iterator = this.actions.iterator();

        this.sequenceRuns++;

        if (iterator.hasNext()) {
            final Action raw = iterator.next();

            final ScheduleAction action;
            if (raw instanceof ScheduleAction) {
                action = (ScheduleAction) raw;
            } else return this.process.end();

            if (this.process.getState().equals(Process.State.INACTIVE)) this.process.setState(Process.State.ACTIVE);

            final long current = System.currentTimeMillis();

            // 1. Check that the tick is being executed in the period wanted.

            if (action.getPeriod() != 0 && this.sequenceRuns % action.getPeriod() != 0) return this.process.end();

            // 2. Fail the action if it is being executed before the delay.

            if (this.sequenceStepTime + ((action.getDelay() / 20) * 1000) > current) {
                return this.process.end();
            }

            // 3. Fail the action if it being executed after the expire.

            if (this.sequenceStepTime + ((action.getExpire() / 20) * 1000) < current) {
                return this.process.end();
            }

            // 4. Run the action conditions and fail if they do not pass.

            if (!action.apply(this.process)) {
                return this.process.end();
            }

            // 5. If this is a repeating task that will expire next run remove it.
            if (current + ((action.getPeriod() / 20) * 1000) > this.sequenceStepTime + ((action.getExpire() / 20) * 1000)) {
                this.sequenceIndex++;

                iterator.remove();

                this.sequenceStepTime = System.currentTimeMillis();

                if (!iterator.hasNext()) {
                    if (this.sequenceIndex == this.sequenceLength) this.process.setState(Process.State.FINISHED);
                }
            }
        }

        return this.process.next();
    }

    public void updateState() {
        // Set no more actions as expired.
        if (this.actions.isEmpty()) {
            this.process.setState(Process.State.FINISHED);
            return;
        }

        if (!(this.actions.get(0) instanceof Expirable)) return;

        final Expirable action = (Expirable) this.actions.get(0);
        if (this.sequenceStepTime + ((action.getExpire() / 20) * 1000) < System.currentTimeMillis()) {
            this.process.setState(Process.State.EXPIRED);
        }
    }

}
