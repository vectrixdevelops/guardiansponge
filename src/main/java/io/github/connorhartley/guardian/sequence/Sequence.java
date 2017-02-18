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

import io.github.connorhartley.guardian.context.Context;
import io.github.connorhartley.guardian.detection.check.Check;
import io.github.connorhartley.guardian.detection.check.CheckProvider;
import io.github.connorhartley.guardian.event.sequence.SequenceFailEvent;
import io.github.connorhartley.guardian.event.sequence.SequenceSucceedEvent;
import io.github.connorhartley.guardian.sequence.action.Action;
import io.github.connorhartley.guardian.sequence.condition.Condition;
import io.github.connorhartley.guardian.sequence.report.SequenceReport;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.entity.living.player.User;
import org.spongepowered.api.event.Event;
import org.spongepowered.api.event.cause.Cause;
import org.spongepowered.api.event.cause.NamedCause;
import org.spongepowered.api.util.Tristate;

import java.util.*;

/**
 * Sequence
 *
 * Represents a chain of actions and conditions
 * that get run in order, supplying conditions with
 * heuristic reporting.
 */
public class Sequence {

    private final User user;
    private final CheckProvider checkProvider;

    private ArrayList<Context> contexts = new ArrayList<>();

    private SequenceReport sequenceReport;

    private final List<Action> actions = new ArrayList<>();
    private final List<Event> completeEvents = new ArrayList<>();
    private final List<Event> incompleteEvents = new ArrayList<>();

    private long last = System.currentTimeMillis();

    private boolean wait = false;
    private boolean cancelled = false;
    private boolean finished = false;

    public Sequence(User user, CheckProvider checkProvider, List<Action> actions, SequenceReport sequenceReport) {
        this.user = user;
        this.checkProvider = checkProvider;
        this.actions.addAll(actions);
        this.sequenceReport = sequenceReport;
    }

    /**
     * Check
     *
     * <p>Runs through the list of {@link Action}s and their {@link Condition}s and
     * carries the {@link SequenceReport} through each allowing it to be
     * updated through the chain. {@link Action}s that fail will fire the {@link SequenceFailEvent}.
     * {@link Action}s that succeed will fire the {@link SequenceSucceedEvent}.</p>
     *
     * @param user The {@link User} in the sequence
     * @param event The {@link Event} that triggered the sequence
     * @param <T> The {@link Event} type
     * @return True if the sequence should continue, false if the sequence should stop
     */
    <T extends Event> Tristate check(User user, T event) {
        Iterator<Action> iterator = this.actions.iterator();

        if (iterator.hasNext()) {
            Action action = iterator.next();

            for (ListIterator backIterator = this.contexts.listIterator(this.contexts.size()); backIterator.hasPrevious();) {
                final Object unCastContext = backIterator.previous();
                action.addContext(((Context) unCastContext));
            }

            long now = System.currentTimeMillis();

            if (!action.getEvent().equals(event.getClass())) {
                action.updateReport(this.sequenceReport);
                return fail(user, event, action, Cause.of(NamedCause.of("INVALID", checkProvider.getSequence())));
            }

            Action<T> typeAction = (Action<T>) action;

            typeAction.testContext(user, event);

            if (this.last + ((action.getAfter() / 20) * 1000) > now) {
                this.wait = true;
                return Tristate.UNDEFINED;
            } else {
                this.wait = false;
            }

            if (this.last + ((action.getBefore() / 20) * 1000) < now) {
                this.wait = true;
                return Tristate.UNDEFINED;
            } else {
                this.wait = false;
            }

            this.sequenceReport = action.getSequenceReport();

            if (this.last + ((action.getDelay() / 20) * 1000) > now) {
                action.updateReport(this.sequenceReport);
                return fail(user, event, action, Cause.of(NamedCause.of("DELAY", action.getDelay())));
            }

            this.sequenceReport = action.getSequenceReport();

            if (this.last + ((action.getExpire() / 20) * 1000) < now) {
                action.updateReport(this.sequenceReport);
                return fail(user, event, action, Cause.of(NamedCause.of("EXPIRE", action.getExpire())));
            }

            this.sequenceReport = action.getSequenceReport();

            this.contexts.addAll(typeAction.getContext());

            if (!typeAction.testConditions(user, event)) {
                action.updateReport(this.sequenceReport);
                return fail(user, event, action, Cause.of(NamedCause.of("CONDITION", action.getConditions())));
            }

            this.sequenceReport = action.getSequenceReport();

            iterator.remove();

            typeAction.updateReport(this.sequenceReport);
            pass(user, event, Cause.of(NamedCause.of("ACTION_SUCCEED", action.getSequenceReport())));
            typeAction.succeed(user, event);

            this.sequenceReport = action.getSequenceReport();

            if (!iterator.hasNext()) {
                this.finished = true;
            }

            return Tristate.TRUE;
        }

        this.actions.forEach(action -> action.suspendContext());

        return Tristate.TRUE;
    }

    // Called when the player meets the action requirements.
    Tristate pass(User user, Event event, Cause cause) {
        this.last = System.currentTimeMillis();

        SequenceSucceedEvent attempt = new SequenceSucceedEvent(this, user, event, cause);
        Sponge.getEventManager().post(attempt);

        this.completeEvents.add(event);
        return Tristate.TRUE;
    }

    // Called when the player does not meet the requirements.
    Tristate fail(User user, Event event, Action action, Cause cause) {
        this.cancelled = action.fail(user, event);

        SequenceFailEvent attempt = new SequenceFailEvent(this, user, event, cause);
        Sponge.getEventManager().post(attempt);

        this.incompleteEvents.add(event);
        return Tristate.FALSE;
    }

    /**
     * Get Context
     *
     * <p>Returns a list of {@link Context}s that have been analysed.</p>
     *
     * @return A list of contexts
     */
    List<Context> getContext() {
        return this.contexts;
    }

    /**
     * Get Sequence Result
     *
     * <p>Returns the current {@link SequenceReport} for this {@link Sequence}.</p>
     *
     * @return This {@link SequenceReport}
     */
    SequenceReport getSequenceReport() {
        return this.sequenceReport;
    }

    boolean isWaiting() {
        return this.wait;
    }

    /**
     * Has Expired
     *
     * <p>Returns true if the {@link Sequence} had an {@link Action} expire. False if
     * it has not.</p>
     *
     * @return True if the {@link Sequence} has expired
     */
    boolean hasExpired() {
        if (this.actions.isEmpty()) {
            return false;
        }

        Action action = this.actions.get(0);
        long now = System.currentTimeMillis();

        return action != null && this.last + ((action.getExpire() / 20) * 1000) < now;
    }

    /**
     * Is Cancelled
     *
     * <p>Returns true if the {@link Sequence} had an {@link Action} event cancel. False
     * if it has not.</p>
     *
     * @return True if the {@link Sequence} is cancelled
     */
    boolean isCancelled() {
        return this.cancelled;
    }

    /**
     * Is Finished
     *
     * <p>Returns true of the {@link Sequence} has completed all it's {@link Action}s successfully.
     * False if it has not.</p>
     *
     * @return True if the {@link Sequence} is completed successfully
     */
    boolean isFinished() {
        return this.finished;
    }

    /**
     * Get User
     *
     * <p>Returns the {@link User} in the sequence.</p>
     *
     * @return The {@link User} in the sequence
     */
    public User getUser() {
        return this.user;
    }

    /**
     * Get Provider
     *
     * <p>Returns the {@link CheckProvider} providing the {@link Check} containing
     * this {@link Sequence}.</p>
     *
     * @return This {@link Sequence}s {@link CheckProvider}
     */
    public CheckProvider getProvider() {
        return this.checkProvider;
    }

    /**
     * Get Complete Events
     *
     * <p>Returns a {@link List} of {@link Event}s that have been successfully completed.</p>
     *
     * @return A {@link List} of successful {@link Event}s
     */
    public List<Event> getCompleteEvents() {
        return this.completeEvents;
    }

    /**
     * Get Incomplete Events
     *
     * <p>Returns a {@link List} of {@link Event}s that have failed to be completed.</p>
     *
     * @return A {@link List} of failed {@link Event}s
     */
    public List<Event> getIncompleteEvents() {
        return this.incompleteEvents;
    }

}
