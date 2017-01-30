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
package io.github.connorhartley.guardian.sequence.action;

import io.github.connorhartley.guardian.detection.check.CheckResult;
import io.github.connorhartley.guardian.sequence.condition.Condition;
import io.github.connorhartley.guardian.sequence.SequencePoint;
import org.spongepowered.api.entity.living.player.User;
import org.spongepowered.api.event.Event;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class Action<T extends Event> {

    private final List<Condition<T>> conditions = new ArrayList<>();
    private final List<Condition> successfulListeners = new ArrayList<>();
    private final List<Condition> failedListeners = new ArrayList<>();

    private final Class<T> event;

    private int delay;
    private int expire;
    private CheckResult checkResult;

    @SafeVarargs
    Action(Class<T> event, CheckResult checkResult, Condition<T>... conditions) {
        this(event, checkResult);
        this.conditions.addAll(Arrays.asList(conditions));
    }

    public Action(Class<T> event, CheckResult checkResult, List<Condition<T>> conditions) {
        this(event, checkResult);
        this.conditions.addAll(conditions);
    }

    public Action(Class<T> event, CheckResult checkResult) {
        this.event = event;
        this.checkResult = checkResult;
    }

    void addCondition(Condition<T> condition) {
        this.conditions.add(condition);
    }

    void setDelay(int delay) {
        this.delay = delay;
    }

    void setExpire(int expire) {
        this.expire = expire;
    }

    public void updateResult(CheckResult checkResult) {
        this.checkResult = checkResult;
    }

    public void succeed(User user, Event event) {
        this.successfulListeners.forEach(callback -> this.checkResult = callback.test(user, event, this.checkResult).getCheckResult());
    }

    public boolean fail(User user, Event event) {
        return this.failedListeners.stream()
                .anyMatch(callback -> {
                    SequencePoint sequencePoint = callback.test(user, event, this.checkResult);

                    this.checkResult = sequencePoint.getCheckResult();
                    return sequencePoint.hasPassed();
                });
    }

    public boolean testConditions(User user, T event) {
        return !this.conditions.stream()
                .anyMatch(condition -> {
                    SequencePoint sequencePoint = condition.test(user, event, this.checkResult);

                    this.checkResult = sequencePoint.getCheckResult();
                    return !sequencePoint.hasPassed();
                });
    }

    public int getDelay() {
        return this.delay;
    }

    public int getExpire() {
        return this.expire;
    }

    public Class<T> getEvent() {
        return this.event;
    }

    public List<Condition<T>> getConditions() {
        return this.conditions;
    }

    public CheckResult getCheckResult() {
        return this.checkResult;
    }

    void onSuccess(Condition condition) {
        this.successfulListeners.add(condition);
    }

    void onFailure(Condition condition) {
        this.failedListeners.add(condition);
    }

}
