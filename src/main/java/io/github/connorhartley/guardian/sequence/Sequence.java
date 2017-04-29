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

import io.github.connorhartley.guardian.sequence.context.ContextContainer;
import io.github.connorhartley.guardian.detection.check.Check;
import io.github.connorhartley.guardian.detection.check.CheckType;
import io.github.connorhartley.guardian.event.sequence.SequenceFailEvent;
import io.github.connorhartley.guardian.event.sequence.SequenceSucceedEvent;
import io.github.connorhartley.guardian.sequence.action.Action;
import io.github.connorhartley.guardian.sequence.context.Context;
import io.github.connorhartley.guardian.sequence.context.ContextHandler;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.entity.living.player.User;
import org.spongepowered.api.event.Event;
import org.spongepowered.api.event.cause.Cause;
import org.spongepowered.api.event.cause.NamedCause;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/**
 * Sequence
 *
 * Represents a chain of actions and contexts
 * that get run in order, supplying conditions with
 * heuristic reporting.
 */
public class Sequence {

    private final Player player;
    private final CheckType checkType;
    private final ContextHandler contextHandler;
    private final List<Action> actions = new ArrayList<>();
    private final List<Event> completeEvents = new ArrayList<>();
    private final List<Event> incompleteEvents = new ArrayList<>();

    private SequenceBlueprint sequenceBlueprint;
    private SequenceReport sequenceReport = SequenceReport.builder().build(false);
    private int queue = 0;
    private long last = System.currentTimeMillis();
    private boolean started = false;
    private boolean cancelled = false;
    private boolean finished = false;

    public Sequence(Player player, SequenceBlueprint sequenceBlueprint, CheckType checkType, List<Action> actions,
                    ContextHandler contextHandler) {
        this.player = player;
        this.checkType = checkType;
        this.contextHandler = contextHandler;
        this.sequenceBlueprint = sequenceBlueprint;

        this.actions.addAll(actions);
        this.contextHandler.setContainer(new ContextContainer());
    }

    /**
     * Check
     *
     * <p>Runs through the list of {@link Action}s and {@link Context}s and
     * carries the {@link SequenceReport} through each {@link Action} allowing it to be
     * updated through the chain. {@link Context}s follow a similar run and each tick
     * get passed a {@link ContextContainer} to update values inside the chain.
     * {@link Action}s that fail will fire the {@link SequenceFailEvent}. {@link Action}s
     * that succeed will fire the {@link SequenceSucceedEvent}.</p>
     *
     * @param player The player in the sequence
     * @param event The event that triggered the sequence
     * @param <T> The event type
     * @return True if the sequence should continue, false if the sequence should skip
     */
    <T extends Event> boolean check(Player player, T event) {
        Iterator<Action> iterator = this.actions.iterator();

        if (iterator.hasNext()) {
            Action action = iterator.next();

            this.queue += 1;
            long now = System.currentTimeMillis();

            action.updateReport(this.sequenceReport);

            if (!action.getEvent().isAssignableFrom(event.getClass())) {
                return fail(player, event, action, Cause.of(NamedCause.of("INVALID", this.checkType.getSequence())));
            }

            if (!this.started) {
                this.contextHandler.start();
                this.started = true;
            }

            if (this.queue > 1 && this.last + ((action.getDelay() / 20) * 1000) > now) {
                return fail(player, event, action, Cause.of(NamedCause.of("DELAY", action.getDelay())));
            }

            Action<T> typeAction = (Action<T>) action;

            action.updateContextValuation(this.contextHandler.getContainer());

            if (this.queue > 1 && this.last + ((action.getExpire() / 20) * 1000) < now) {
                return fail(player, event, action, Cause.of(NamedCause.of("EXPIRE", action.getExpire())));
            }

            if (!typeAction.testConditions(player, event, this.last)) {
                return fail(player, event, action, Cause.of(NamedCause.of("CONDITION", action.getConditions())));
            }

            this.sequenceReport = action.getSequenceReport();

            SequenceSucceedEvent attempt = new SequenceSucceedEvent(this, player, event, Cause.of(NamedCause.of("ACTION", this.sequenceReport)));
            Sponge.getEventManager().post(attempt);

            this.completeEvents.add(event);
            iterator.remove();
            typeAction.updateReport(this.sequenceReport);
            typeAction.updateContextValuation(this.contextHandler.getContainer());
            typeAction.succeed(player, event, this.last);
            this.sequenceReport = action.getSequenceReport();

            this.last = System.currentTimeMillis();

            if (!iterator.hasNext()) {
                if (this.started) {
                    this.contextHandler.stop();
                }
                this.finished = true;
            }
        }

        return true;
    }

    // Called when the player does not meet the requirements.
    boolean fail(User user, Event event, Action action, Cause cause) {
        action.updateReport(this.sequenceReport);
        action.updateContextValuation(this.contextHandler.getContainer());

        this.cancelled = action.fail(user, event, this.last);
        this.sequenceReport = action.getSequenceReport();

        SequenceFailEvent attempt = new SequenceFailEvent(this, user, event, cause);
        Sponge.getEventManager().post(attempt);

        this.incompleteEvents.add(event);
        return false;
    }

    /**
     * Get Context Handler
     *
     * <p>Returns the {@link ContextHandler} for this {@link Sequence}.</p>
     *
     * @return This context handler
     */
    public ContextHandler getContextHandler() {
        return this.contextHandler;
    }

    /**
     * Get Context Container
     *
     * <p>Returns a {@link ContextContainer} of data that have been analysed.</p>
     *
     * @return A list of context values
     */
    public ContextContainer getContextContainer() {
        return this.contextHandler.getContainer();
    }

    /**
     * Get Sequence Result
     *
     * <p>Returns the current {@link SequenceReport} for this {@link Sequence}.</p>
     *
     * @return This sequence report
     */
    public SequenceReport getSequenceReport() {
        return this.sequenceReport;
    }

    /**
     * Get Sequence Blueprint
     *
     * @return
     */
    public SequenceBlueprint getSequenceBlueprint() {
        return this.sequenceBlueprint;
    }

    /**
     * Has Started
     *
     * <p>Returns true if the {@link Sequence} has started.</p>
     *
     * @return True if the sequence has started
     */
    public boolean hasStarted() {
        return this.started;
    }

    /**
     * Has Expired
     *
     * <p>Returns true if the {@link Sequence} had an {@link Action} expire. False if
     * it has not.</p>
     *
     * @return True if the sequence has expired
     */
    boolean hasExpired() {
        if (this.actions.isEmpty()) {
            return false;
        }

        Action action = this.actions.get(0);
        long now = System.currentTimeMillis();

        if (action != null && this.last + ((action.getExpire() / 20) * 1000) < now) {
            if (this.started) {
                this.contextHandler.stop();
            }
            return true;
        }

        return false;
    }

    /**
     * Is Cancelled
     *
     * <p>Returns true if the {@link Sequence} had an {@link Action} event cancel. False
     * if it has not.</p>
     *
     * @return True if the sequence is cancelled
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
     * @return True if the sequence is completed successfully
     */
    boolean isFinished() {
        return this.finished;
    }

    /**
     * Get User
     *
     * <p>Returns the {@link Player} in the sequence.</p>
     *
     * @return The player in the sequence
     */
    public Player getPlayer() {
        return this.player;
    }

    /**
     * Get Type
     *
     * <p>Returns the {@link CheckType} providing the {@link Check} containing
     * this {@link Sequence}.</p>
     *
     * @return This sequences check type
     */
    public CheckType getProvider() {
        return this.checkType;
    }

    /**
     * Get Complete Events
     *
     * <p>Returns a {@link List} of {@link Event}s that have been successfully completed.</p>
     *
     * @return A list of successful events
     */
    public List<Event> getCompleteEvents() {
        return this.completeEvents;
    }

    /**
     * Get Incomplete Events
     *
     * <p>Returns a {@link List} of {@link Event}s that have failed to be completed.</p>
     *
     * @return A list of failed events
     */
    public List<Event> getIncompleteEvents() {
        return this.incompleteEvents;
    }

}
