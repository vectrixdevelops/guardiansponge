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
package com.ichorpowered.guardian.sponge.sequence.action.schedule;

import com.google.common.collect.Lists;
import com.ichorpowered.guardian.api.sequence.action.schedule.ScheduleAction;
import com.ichorpowered.guardian.api.sequence.process.Process;
import com.ichorpowered.guardian.api.sequence.process.ProcessResult;
import org.checkerframework.checker.nullness.qual.NonNull;

import java.util.List;
import java.util.function.Function;

public class ScheduleActionImpl implements ScheduleAction {

    private final List<Function<Process, ProcessResult>> conditions = Lists.newArrayList();
    private int delay = 0;
    private int expire = 0;
    private int period = 0;
    private int repeat = 0;

    public ScheduleActionImpl() {}

    @Override
    public void addCondition(@NonNull Function<Process, ProcessResult> function) {
        this.conditions.add(function);
    }

    @Override
    public void setDelay(final int period) {
        this.delay = period;
    }

    @Override
    public int getDelay() {
        return this.delay;
    }

    @Override
    public void setExpire(final int period) {
        this.expire = period;
    }

    @Override
    public int getExpire() {
        return this.expire;
    }

    @Override
    public void setPeriod(final int period) {
        this.period = period;
    }

    @Override
    public int getPeriod() {
        return this.period;
    }

    @Override
    public int getRepeats() {
        return this.repeat;
    }

    @Override
    public boolean apply(final @NonNull Process process) {
        boolean applyResult = this.conditions.stream()
                .map(condition -> condition.apply(process))
                .allMatch(ProcessResult::toNext);

        if (applyResult && this.period != 0) this.repeat++;
        return applyResult;
    }

}
