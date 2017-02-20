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

import io.github.connorhartley.guardian.context.ContextProvider;
import io.github.connorhartley.guardian.context.ContextBuilder;
import io.github.connorhartley.guardian.context.ContextTypes;
import io.github.connorhartley.guardian.detection.check.CheckProvider;
import io.github.connorhartley.guardian.sequence.SequenceBlueprint;
import io.github.connorhartley.guardian.sequence.SequenceBuilder;
import io.github.connorhartley.guardian.sequence.condition.Condition;
import io.github.connorhartley.guardian.sequence.report.SequenceReport;
import org.spongepowered.api.event.Event;

public class ActionBuilder<T extends Event> {

    private final ContextProvider contextProvider;
    private final SequenceBuilder builder;
    private final Action<T> action;
    private final ContextBuilder contextBuilder;
    private final SequenceReport sequenceReport;

    public ActionBuilder(ContextProvider contextProvider, SequenceBuilder sequenceBuilder, Action<T> action, ContextBuilder contextBuilder, SequenceReport sequenceReport) {
        this.contextProvider = contextProvider;
        this.builder = sequenceBuilder;
        this.action = action;
        this.contextBuilder = contextBuilder;
        this.sequenceReport = sequenceReport;
    }

    public ActionBuilder<T> suspend(String... id) {
        this.action.suspendContext(id);
        return this;
    }

    public ActionBuilder<T> condition(Condition condition) {
        this.action.addCondition(condition);
        return this;
    }

    public ActionBuilder<T> delay(int delay) {
        this.action.setDelay(delay);
        return this;
    }

    public ActionBuilder<T> expire(int expire) {
        this.action.setExpire(expire);
        return this;
    }

    public ActionBuilder<T> success(Condition condition) {
        this.action.onSuccess(condition);
        return this;
    }

    public ActionBuilder<T> failure(Condition condition) {
        this.action.onFailure(condition);
        return this;
    }

    public ActionBuilder<T> action(Class<T> clazz) {
        return action(new Action<>(this.contextProvider, clazz, this.sequenceReport, this.contextBuilder));
    }

    public ActionBuilder<T> action(ActionBlueprint<T> blueprint) {
        return action(blueprint.create());
    }

    public <K extends Event> ActionBuilder<K> action(Action<K> action) {
        return this.builder.action(action);
    }

    public SequenceBlueprint build(CheckProvider checkProvider, ContextProvider contextProvider) {
        return this.builder.build(checkProvider, contextProvider);
    }

}
