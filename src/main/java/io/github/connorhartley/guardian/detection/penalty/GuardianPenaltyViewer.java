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
package io.github.connorhartley.guardian.detection.penalty;

import com.ichorpowered.guardian.api.detection.Detection;
import com.ichorpowered.guardian.api.detection.DetectionConfiguration;
import com.ichorpowered.guardian.api.detection.penalty.Penalty;
import com.ichorpowered.guardian.api.phase.PhaseFilter;
import com.ichorpowered.guardian.api.phase.PhaseState;
import com.ichorpowered.guardian.api.phase.PhaseViewer;
import io.github.connorhartley.guardian.GuardianPlugin;

import java.util.Iterator;
import java.util.concurrent.atomic.AtomicInteger;

public class GuardianPenaltyViewer implements PhaseViewer<Penalty> {

    private final GuardianPlugin plugin;

    private PhaseState phaseState = PhaseState.INITIALIZED;

    private Iterator<Penalty> penaltyIterator;
    private Penalty element;
    private int phaseIndex = 0;

    public GuardianPenaltyViewer(GuardianPlugin plugin) {
        this.plugin = plugin;

        this.penaltyIterator = this.plugin.getPenaltyRegistry().iterator();

        if (this.penaltyIterator.hasNext()) {
            this.element = this.penaltyIterator.next();
        }
    }


    @Override
    public <E, F extends DetectionConfiguration> Penalty getOrCreatePhase(Detection<E, F> detection) {
        return this.element;
    }

    @Override
    public Class<? extends Penalty> getPhaseClass() {
        return this.element.getClass();
    }

    @Override
    public void next() {
        if (this.penaltyIterator.hasNext()) {
            this.element = this.penaltyIterator.next();
        }

        this.phaseIndex += 1;
    }

    @Override
    public PhaseState getPhaseState() {
        return this.phaseState;
    }

    @Override
    public int index() {
        return this.phaseIndex;
    }

    @Override
    public int size() {
        return this.plugin.getPenaltyRegistry().keySet().size();
    }

    @Override
    public int size(PhaseFilter phaseFilter) {
        AtomicInteger size = new AtomicInteger(0);

        this.plugin.getPenaltyRegistry().keySet().forEach(key -> {
            if (phaseFilter.accept(key)) {
                size.incrementAndGet();
            }
        });

        return size.get();
    }
}
