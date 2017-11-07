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
package io.github.connorhartley.guardian.detection.heuristics;

import com.ichorpowered.guardian.api.detection.Detection;
import com.ichorpowered.guardian.api.detection.DetectionConfiguration;
import com.ichorpowered.guardian.api.detection.heuristic.Heuristic;
import com.ichorpowered.guardian.api.phase.PhaseFilter;
import com.ichorpowered.guardian.api.phase.PhaseState;
import com.ichorpowered.guardian.api.phase.PhaseViewer;
import io.github.connorhartley.guardian.GuardianPlugin;

import java.util.Iterator;
import java.util.concurrent.atomic.AtomicInteger;

import javax.annotation.Nonnull;

public class GuardianHeuristicViewer implements PhaseViewer<Heuristic> {

    private final GuardianPlugin plugin;

    private PhaseState phaseState = PhaseState.INITIALIZED;

    private Iterator<Heuristic> heuristicIterator;
    private Heuristic element;
    private int phaseIndes = 0;

    public GuardianHeuristicViewer(@Nonnull GuardianPlugin plugin) {
        this.plugin = plugin;

        this.heuristicIterator = this.plugin.getHeuristicRegistry().iterator();

        if (this.heuristicIterator.hasNext()) {
            this.element = this.heuristicIterator.next();
        }
    }

    @Nonnull
    @Override
    public <E, F extends DetectionConfiguration> Heuristic getOrCreatePhase(@Nonnull Detection<E, F> detection) {
        return this.element;
    }

    @Nonnull
    @Override
    public Class<? extends Heuristic> getPhaseClass() {
        return this.element.getClass();
    }

    @Override
    public void next() {
        if (this.heuristicIterator.hasNext()) {
            this.element = this.heuristicIterator.next();
        }

        this.phaseIndes += 1;
    }

    @Nonnull
    @Override
    public PhaseState getPhaseState() {
        return this.phaseState;
    }

    @Override
    public int index() {
        return this.phaseIndes;
    }

    @Override
    public int size() {
        return this.plugin.getHeuristicRegistry().keySet().size();
    }

    @Override
    public int size(@Nonnull PhaseFilter phaseFilter) {
        AtomicInteger size = new AtomicInteger(0);

        this.plugin.getHeuristicRegistry().keySet().forEach(key -> {
            if (phaseFilter.accept(key)) {
                size.incrementAndGet();
            }
        });

        return size.get();
    }
}
