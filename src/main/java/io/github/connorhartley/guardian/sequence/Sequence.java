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

import io.github.connorhartley.guardian.detection.check.CheckProvider;
import io.github.connorhartley.guardian.detection.check.CheckResult;
import io.github.connorhartley.guardian.event.sequence.SequenceFailEvent;
import io.github.connorhartley.guardian.event.sequence.SequenceSucceedEvent;
import io.github.connorhartley.guardian.sequence.action.Action;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.entity.living.player.User;
import org.spongepowered.api.event.Event;
import org.spongepowered.api.event.cause.Cause;
import org.spongepowered.api.event.cause.NamedCause;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class Sequence {

    private final User user;
    private final CheckProvider checkProvider;

    private CheckResult checkResult;

    private final List<Action> actions = new ArrayList<>();
    private final List<Event> completeEvents = new ArrayList<>();
    private final List<Event> incompleteEvents = new ArrayList<>();

    private long last = System.currentTimeMillis();

    private boolean cancelled = false;
    private boolean finished = false;

    private Iterator<Action> iterator;

    public Sequence(User user, CheckProvider checkProvider, List<Action> actions, CheckResult checkResult) {
        this.user = user;
        this.checkProvider = checkProvider;
        this.actions.addAll(actions);
        this.checkResult = checkResult;
    }

    <T extends Event> boolean check(User user, T event) {
        this.iterator = this.actions.iterator();

        if (iterator.hasNext()) {
            Action action = iterator.next();

            long now = System.currentTimeMillis();

            if (!action.getEvent().equals(event.getClass())) {
                action.updateResult(this.checkResult);
                return fail(user, event, action, Cause.of(NamedCause.of("INVALID", checkProvider.getSequence())));
            }

            this.checkResult = action.getCheckResult();

            if (this.last + ((action.getDelay() / 20) * 1000) > now) {
                action.updateResult(this.checkResult);
                return fail(user, event, action, Cause.of(NamedCause.of("DELAY", action.getDelay())));
            }

            this.checkResult = action.getCheckResult();

            if (this.last + ((action.getExpire() / 20) * 1000) < now) {
                action.updateResult(this.checkResult);
                return fail(user, event, action, Cause.of(NamedCause.of("EXPIRE", action.getExpire())));
            }

            this.checkResult = action.getCheckResult();

            Action<T> typeAction = (Action<T>) action;

            if (!typeAction.testConditions(user, event)) {
                action.updateResult(this.checkResult);
                return fail(user, event, action, Cause.of(NamedCause.of("CONDITION", action.getConditions())));
            }

            this.iterator.remove();

            typeAction.updateResult(this.checkResult);
            pass(user, event, Cause.of(NamedCause.of("ACTION_SUCCEED", action.getCheckResult())));
            typeAction.succeed(user, event);

            this.checkResult = action.getCheckResult();

            if (!iterator.hasNext()) {
                this.finished = true;
            }

            return true;
        }
        return true;
    }

    // Called when the player meets the action requirements.
    boolean pass(User user, Event event, Cause cause) {
        this.last = System.currentTimeMillis();

        SequenceSucceedEvent attempt = new SequenceSucceedEvent(this, user, event, cause);
        Sponge.getEventManager().post(attempt);

        this.completeEvents.add(event);
        return true;
    }

    // Called when the player does not meet the requirements.
    boolean fail(User user, Event event, Action action, Cause cause) {
        this.cancelled = action.fail(user, event);

        SequenceFailEvent attempt = new SequenceFailEvent(this, user, event, cause);
        Sponge.getEventManager().post(attempt);

        this.incompleteEvents.add(event);
        return false;
    }

    CheckResult getCheckResult() {
        return this.checkResult;
    }

    boolean hasExpired() {
        if (this.actions.isEmpty()) {
            return false;
        }

        Action action = this.actions.get(0);
        long now = System.currentTimeMillis();

        return action != null && this.last + ((action.getExpire() / 20) * 1000) < now;
    }

    boolean isCancelled() {
        return this.cancelled;
    }

    boolean isFinished() {
        return this.finished;
    }

    public User getUser() {
        return this.user;
    }

    public CheckProvider getProvider() {
        return this.checkProvider;
    }

    public List<Event> getCompleteEvents() {
        return this.completeEvents;
    }

}
