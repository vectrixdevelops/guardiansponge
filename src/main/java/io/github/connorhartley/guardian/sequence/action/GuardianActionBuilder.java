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

import com.ichorpowered.guardian.api.detection.DetectionConfiguration;
import com.ichorpowered.guardian.api.detection.check.Check;
import com.ichorpowered.guardian.api.sequence.SequenceBlueprint;
import com.ichorpowered.guardian.api.sequence.SequenceBuilder;
import com.ichorpowered.guardian.api.sequence.action.Action;
import com.ichorpowered.guardian.api.sequence.action.ActionBlueprint;
import com.ichorpowered.guardian.api.sequence.action.ActionBuilder;
import com.ichorpowered.guardian.api.sequence.condition.Condition;
import com.ichorpowered.guardian.api.sequence.condition.ConditionSupplier;
import io.github.connorhartley.guardian.sequence.condition.GuardianCondition;

import javax.annotation.Nonnull;

public class GuardianActionBuilder<E, F extends DetectionConfiguration, T> implements ActionBuilder<E, F, T> {

    private final Action<T> action;
    private final SequenceBuilder<E, F> sequenceBuilder;

    public GuardianActionBuilder(@Nonnull SequenceBuilder<E, F> sequenceBuilder, @Nonnull Action<T> action) {
        this.sequenceBuilder = sequenceBuilder;
        this.action = action;
    }

    @Nonnull
    @Override
    public ActionBuilder<E, F, T> condition(@Nonnull ConditionSupplier<E, F, T> conditionSupplier) {
        this.action.addCondition(GuardianCondition.of(conditionSupplier, Condition.Type.NORMAL));
        return this;
    }

    @Nonnull
    @Override
    public ActionBuilder<E, F, T> delay(int value) {
        this.action.setDelay(value);
        return this;
    }

    @Nonnull
    @Override
    public ActionBuilder<E, F, T> expire(int value) {
        this.action.setExpire(value);
        return this;
    }

    @Nonnull
    @Override
    public ActionBuilder<E, F, T> success(@Nonnull ConditionSupplier<E, F, T> conditionSupplier) {
        this.action.addCondition(GuardianCondition.of(conditionSupplier, Condition.Type.SUCCESS));
        return this;
    }

    @Nonnull
    @Override
    public ActionBuilder<E, F, T> failure(@Nonnull ConditionSupplier<E, F, T> conditionSupplier) {
        this.action.addCondition(GuardianCondition.of(conditionSupplier, Condition.Type.FAIL));
        return this;
    }

    @Nonnull
    @Override
    public <K> ActionBuilder<E, F, K> action(@Nonnull Class<K> aClass) {
        return this.action(new GuardianAction<>(aClass));
    }

    @Nonnull
    @Override
    public <K> ActionBuilder<E, F, K> action(@Nonnull ActionBlueprint<K> actionBlueprint) {
        return this.action(actionBlueprint.create());
    }

    @Nonnull
    @Override
    public <K> ActionBuilder<E, F, K> action(@Nonnull Action<K> action) {
        return this.sequenceBuilder.action(action);
    }

    @Nonnull
    @Override
    public <C> SequenceBlueprint<E, F> build(@Nonnull C pluginContainer, @Nonnull Check<E, F> check) {
        return this.sequenceBuilder.build(pluginContainer, check);
    }

}

