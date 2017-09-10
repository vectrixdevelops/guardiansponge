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
import com.ichorpowered.guardian.api.entry.EntityEntry;
import com.ichorpowered.guardian.api.report.Summary;
import com.ichorpowered.guardian.api.sequence.Sequence;
import com.ichorpowered.guardian.api.sequence.action.Action;
import com.ichorpowered.guardian.api.sequence.condition.Condition;
import io.github.connorhartley.guardian.sequence.SequenceReport;
import io.github.connorhartley.guardian.sequence.condition.GuardianCondition;

import java.util.ArrayList;
import java.util.Collection;

import javax.annotation.Nonnull;

public class GuardianAction<T> implements Action<T> {

    private final Class<T> eventClass;

    private final Collection<Condition<T>> conditions = new ArrayList<>();

    private int delay;
    private int expire;

    @SuppressWarnings("unchecked")
    public static <K> GuardianAction<K> of(Action<K> action) {
        return new GuardianAction<>((Class<K>) action.getClass());
    }

    public GuardianAction(Class<T> aClass) {
        this.eventClass = aClass;
    }


    @Override
    public void addCondition(@Nonnull Condition<T> condition) {
        this.conditions.add(condition);
    }

    @Override
    public void setDelay(int value) {
        this.delay = value;
    }

    @Override
    public void setExpire(int value) {
        this.expire = value;
    }

    @Override
    public <E, F extends DetectionConfiguration> boolean apply(@Nonnull Sequence<E, F> sequence, @Nonnull EntityEntry entry, @Nonnull T event, long lastActionTime) {
        return this.conditions.stream()
                .filter(condition -> condition.getType().equals(GuardianCondition.Type.NORMAL))
                .noneMatch(condition -> {
                    Summary<E, F> summary = condition.<E, F>get().apply(entry, event, sequence.getCaptureRegistry().getContainer(), sequence.getSummary(), lastActionTime);

                    if (summary.view(SequenceReport.class) == null) return true;
                    return !summary.view(SequenceReport.class).passed();
                });
    }

    @Override
    public <E, F extends DetectionConfiguration> boolean succeed(@Nonnull Sequence<E, F> sequence, @Nonnull EntityEntry entry, @Nonnull T event, long lastActionTime) {
        this.conditions.stream()
                .filter(condition -> condition.getType().equals(GuardianCondition.Type.SUCCESS))
                .forEach(condition -> {
                    Summary<E, F> summary = condition.<E, F>get().apply(entry, event, sequence.getCaptureRegistry().getContainer(), sequence.getSummary(), lastActionTime);
                });
        return true;
    }

    @Override
    public <E, F extends DetectionConfiguration> boolean fail(@Nonnull Sequence<E, F> sequence, @Nonnull EntityEntry entry, @Nonnull T event, long lastActionTime) {
        return this.conditions.stream()
                .filter(condition -> condition.getType().equals(GuardianCondition.Type.FAIL))
                .anyMatch(condition -> {
                    Summary<E, F> summary = condition.<E, F>get().apply(entry, event, sequence.getCaptureRegistry().getContainer(), sequence.getSummary(), lastActionTime);

                    if (summary.view(SequenceReport.class) == null) return false;
                    return summary.view(SequenceReport.class).passed();
                });
    }

    @Override
    public int getDelay() {
        return this.delay;
    }

    @Override
    public int getExpire() {
        return this.expire;
    }

    @Nonnull
    @Override
    public Class<? extends T> getEvent() {
        return this.eventClass;
    }

    @Deprecated
    public void onSuccess(Condition<T> condition) {
        this.conditions.add(condition);
    }

    @Deprecated
    public void onFailure(Condition<T> condition) {
        this.conditions.add(condition);
    }

}
