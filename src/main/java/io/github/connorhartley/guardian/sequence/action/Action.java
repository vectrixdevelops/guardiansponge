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

import io.github.connorhartley.guardian.report.SequenceReport;
import io.github.connorhartley.guardian.sequence.capture.CaptureContainer;
import io.github.connorhartley.guardian.sequence.condition.Condition;
import io.github.connorhartley.guardian.sequence.condition.ConditionResult;
import org.spongepowered.api.entity.living.player.User;
import org.spongepowered.api.event.Event;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class Action<T extends Event> {

    private final Class<T> event;

    private final List<Condition> conditions = new ArrayList<>();
    private final List<Condition> successfulListeners = new ArrayList<>();
    private final List<Condition> failedListeners = new ArrayList<>();

    private SequenceReport sequenceReport = SequenceReport.builder().build(false);
    private CaptureContainer captureContainer = new CaptureContainer();
    private int delay;
    private int expire;

    Action(Class<T> event, Condition... conditions) {
        this(event);
        this.conditions.addAll(Arrays.asList(conditions));
    }

    public Action(Class<T> event, List<Condition> conditions) {
        this(event);
        this.conditions.addAll(conditions);
    }

    public Action(Class<T> event) {
        this.event = event;
    }

    public void updateCaptureContainer(CaptureContainer captureContainer) {
        this.captureContainer = captureContainer;
    }

    void addCondition(Condition condition) {
        this.conditions.add(condition);
    }

    void setDelay(int delay) {
        this.delay = delay;
    }

    void setExpire(int expire) {
        this.expire = expire;
    }

    public void updateReport(SequenceReport sequenceReport) {
        this.sequenceReport = sequenceReport;
    }

    public void succeed(User user, T event, long lastAction) {
        this.successfulListeners.forEach(callback -> {
            ConditionResult testResult = callback.test(user, event, this.captureContainer, this.sequenceReport, lastAction);

            this.sequenceReport =
                    SequenceReport.builder().of(testResult.getSequenceReport())
                            .build(testResult.hasPassed());
        });
    }

    public boolean fail(User user, T event, long lastAction) {
        return this.failedListeners.stream()
                .anyMatch(callback -> {
                    ConditionResult testResult = callback.test(user, event, this.captureContainer, this.sequenceReport, lastAction);

                    this.sequenceReport = SequenceReport.builder().of(testResult.getSequenceReport())
                            .build(testResult.hasPassed());

                    return testResult.hasPassed();
                });
    }

    public boolean testConditions(User user, T event, long lastAction) {
        return !this.conditions.stream()
                .anyMatch(condition -> {
                    ConditionResult testResult = condition.test(user, event, this.captureContainer, this.sequenceReport, lastAction);

                    this.sequenceReport = SequenceReport.builder().of(testResult.getSequenceReport())
                            .build(testResult.hasPassed());

                    return !testResult.hasPassed();
                });
    }

    public int getDelay() {
        return this.delay;
    }

    public int getExpire() {
        return this.expire;
    }

    public Class<?> getEvent() {
        return this.event;
    }

    public CaptureContainer getCaptureContainer() {
        return this.captureContainer;
    }

    public List<Condition> getConditions() {
        return this.conditions;
    }

    public SequenceReport getSequenceReport() {
        return this.sequenceReport;
    }

    void onSuccess(Condition condition) {
        this.successfulListeners.add(condition);
    }

    void onFailure(Condition condition) {
        this.failedListeners.add(condition);
    }

}
