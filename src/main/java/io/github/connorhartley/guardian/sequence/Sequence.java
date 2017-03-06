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
import io.github.connorhartley.guardian.context.ContextBuilder;
import io.github.connorhartley.guardian.context.ContextProvider;
import io.github.connorhartley.guardian.context.container.ContextContainer;
import io.github.connorhartley.guardian.detection.check.Check;
import io.github.connorhartley.guardian.detection.check.CheckProvider;
import io.github.connorhartley.guardian.event.sequence.SequenceFailEvent;
import io.github.connorhartley.guardian.event.sequence.SequenceSucceedEvent;
import io.github.connorhartley.guardian.sequence.action.Action;
import io.github.connorhartley.guardian.sequence.condition.Condition;
import io.github.connorhartley.guardian.sequence.report.SequenceReport;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.entity.living.player.User;
import org.spongepowered.api.event.Event;
import org.spongepowered.api.event.cause.Cause;
import org.spongepowered.api.event.cause.NamedCause;

import java.util.*;

/**
 * Sequence
 *
 * Represents a chain of actions and conditions
 * that get run in order, supplying conditions with
 * heuristic reporting.
 */
public class Sequence {

    private final Player player;
    private final CheckProvider checkProvider;
    private final ContextProvider contextProvider;
    private final ContextBuilder contextBuilder;

    private final List<ContextContainer> contextContainer = new ArrayList<>();
    private final List<Action> actions = new ArrayList<>();
    private final List<Event> completeEvents = new ArrayList<>();
    private final List<Event> incompleteEvents = new ArrayList<>();

    private SequenceBlueprint sequenceBlueprint;
    private SequenceReport sequenceReport;
    private int queue = 0;
    private long last = System.currentTimeMillis();
    private boolean cancelled = false;
    private boolean finished = false;

    public Sequence(Player player, SequenceBlueprint sequenceBlueprint, CheckProvider checkProvider, List<Action> actions,
                    SequenceReport sequenceReport, ContextProvider contextProvider, ContextBuilder contextBuilder) {
        this.player = player;
        this.sequenceBlueprint = sequenceBlueprint;
        this.checkProvider = checkProvider;
        this.sequenceReport = sequenceReport;
        this.contextProvider = contextProvider;
        this.contextBuilder = contextBuilder;
        this.actions.addAll(actions);
    }

    /**
     * Check
     *
     * <p>Runs through the list of {@link Action}s and their {@link Condition}s and
     * carries the {@link SequenceReport} through each allowing it to be
     * updated through the chain. {@link Action}s that fail will fire the {@link SequenceFailEvent}.
     * {@link Action}s that succeed will fire the {@link SequenceSucceedEvent}.</p>
     *
     * @param player The {@link Player} in the sequence
     * @param event The {@link Event} that triggered the sequence
     * @param <T> The {@link Event} type
     * @return True if the sequence should continue, false if the sequence should stop
     */
    <T extends Event> boolean check(Player player, T event) {
        Iterator<Action> iterator = this.actions.iterator();

        if (iterator.hasNext()) {
            Action action = iterator.next();

            this.queue += 1;
            long now = System.currentTimeMillis();

            action.updateReport(this.sequenceReport);

            if (!action.getEvent().isAssignableFrom(event.getClass())) {
                return fail(player, event, action, Cause.of(NamedCause.of("INVALID", checkProvider.getSequence())));
            }

            if (this.queue > 1 && this.last + ((action.getDelay() / 20) * 1000) > now) {
                return fail(player, event, action, Cause.of(NamedCause.of("DELAY", action.getDelay())));
            }

            Action<T> typeAction = (Action<T>) action;

            this.testContext(player);
            typeAction.addContextContainers(this.contextContainer);


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
            typeAction.succeed(player, event, this.last);
            this.sequenceReport = action.getSequenceReport();

            this.last = System.currentTimeMillis();

            if (!iterator.hasNext()) {
                for (ContextContainer contextContainer : this.contextContainer) {
                    this.contextProvider.getContextController().suspend(player, contextContainer.getContext());
                }
                this.finished = true;
            }
        }

        return true;
    }

    // Called when the player does not meet the requirements.
    boolean fail(User user, Event event, Action action, Cause cause) {
        action.updateReport(this.sequenceReport);

        this.cancelled = action.fail(user, event, this.last);
        this.sequenceReport = action.getSequenceReport();

        SequenceFailEvent attempt = new SequenceFailEvent(this, user, event, cause);
        Sponge.getEventManager().post(attempt);

        this.incompleteEvents.add(event);
        return false;
    }

    void testContext(Player player) {
        if (this.contextContainer.isEmpty()) {
            this.contextBuilder.getContexts().forEach(actionContextClass ->
                    this.contextProvider.getContextController().construct(player,
                            this.getProvider().getDetection().getConfiguration(), actionContextClass)
                            .ifPresent(context -> this.contextContainer.add(context.getContainer())));
        }
    }

    /**
     * Get Context
     *
     * <p>Returns a list of {@link Context}s that have been analysed.</p>
     *
     * @return A list of contextContainer
     */
    List<ContextContainer> getContext() {
        return this.contextContainer;
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

    /**
     * Get Sequence Blueprint
     *
     * @return
     */
    SequenceBlueprint getSequenceBlueprint() {
        return this.sequenceBlueprint;
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

        if (action != null && this.last + ((action.getExpire() / 20) * 1000) < now) {
            for (ContextContainer contextContainer : this.contextContainer) {
                this.contextProvider.getContextController().suspend(player, contextContainer.getContext());
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
     * <p>Returns the {@link Player} in the sequence.</p>
     *
     * @return The {@link Player} in the sequence
     */
    public Player getPlayer() {
        return this.player;
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
