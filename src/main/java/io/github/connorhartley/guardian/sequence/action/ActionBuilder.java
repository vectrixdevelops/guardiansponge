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

import io.github.connorhartley.guardian.detection.check.Check;
import io.github.connorhartley.guardian.sequence.SequenceBlueprint;
import io.github.connorhartley.guardian.sequence.SequenceBuilder;
import io.github.connorhartley.guardian.sequence.condition.Condition;
import io.github.connorhartley.guardian.storage.StorageProvider;
import org.spongepowered.api.event.Event;
import tech.ferus.util.config.HoconConfigFile;

import java.nio.file.Path;

public class ActionBuilder<E, F extends StorageProvider<HoconConfigFile, Path>, T extends Event> {

    private final Action<T> action;
    private final SequenceBuilder<E, F> builder;

    public ActionBuilder(SequenceBuilder<E, F> sequenceBuilder, Action<T> action) {
        this.builder = sequenceBuilder;
        this.action = action;
    }

    public ActionBuilder<E, F, T> condition(Condition condition) {
        this.action.addCondition(condition);
        return this;
    }

    public ActionBuilder<E, F, T> delay(int delay) {
        this.action.setDelay(delay);
        return this;
    }

    public ActionBuilder<E, F, T> expire(int expire) {
        this.action.setExpire(expire);
        return this;
    }

    public ActionBuilder<E, F, T> success(Condition condition) {
        this.action.onSuccess(condition);
        return this;
    }

    public ActionBuilder<E, F, T> failure(Condition condition) {
        this.action.onFailure(condition);
        return this;
    }

    public <K extends Event> ActionBuilder<E, F, K> action(Class<K> clazz) {
        return action(new Action<>(clazz));
    }

    public <K extends Event> ActionBuilder<E, F, K> action(ActionBlueprint<K> blueprint) {
        return action(blueprint.create());
    }

    public <K extends Event> ActionBuilder<E, F, K> action(Action<K> action) {
        return this.builder.action(action);
    }

    public SequenceBlueprint build(Check check) {
        return this.builder.build(check);
    }

}
