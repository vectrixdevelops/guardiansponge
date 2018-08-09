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
package com.ichorpowered.guardian.sponge.sequence.process;

import com.ichorpowered.guardian.api.sequence.SequenceContext;
import com.ichorpowered.guardian.api.sequence.capture.CaptureRegistry;
import com.ichorpowered.guardian.api.sequence.process.Process;
import com.ichorpowered.guardian.api.sequence.process.ProcessResult;
import org.checkerframework.checker.nullness.qual.NonNull;

public class ProcessImpl implements Process {

    private final CaptureRegistry captureRegistry;
    private final SequenceContext sequenceContext;

    private State state;

    public ProcessImpl(final CaptureRegistry captureRegistry, final SequenceContext sequenceContext,
                       final State state) {
        this.captureRegistry = captureRegistry;
        this.sequenceContext = sequenceContext;
        this.state = state;
    }

    @Override
    public @NonNull ProcessResult next() {
        final ProcessResult processResult = new ProcessResultImpl();
        processResult.setNext(true);
        return processResult;
    }

    @Override
    public @NonNull ProcessResult skip() {
        final ProcessResult processResult = new ProcessResultImpl();
        processResult.setSkip(true);
        return processResult;
    }

    @Override
    public @NonNull ProcessResult end() {
        return new ProcessResultImpl();
    }

    @Override
    public @NonNull CaptureRegistry getCaptures() {
        return this.captureRegistry;
    }

    @Override
    public @NonNull Process setState(final State state) {
        this.state = state;
        return this;
    }

    @Override
    public State getState() {
        return this.state;
    }

    @Override
    public @NonNull SequenceContext getContext() {
        return this.sequenceContext;
    }

}
