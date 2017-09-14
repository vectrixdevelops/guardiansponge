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
package io.github.connorhartley.guardian.detection.check;

import com.ichorpowered.guardian.api.detection.Detection;
import com.ichorpowered.guardian.api.detection.DetectionConfiguration;
import com.ichorpowered.guardian.api.detection.check.Check;
import com.ichorpowered.guardian.api.detection.check.CheckBlueprint;
import com.ichorpowered.guardian.api.phase.PhaseFilter;
import com.ichorpowered.guardian.api.phase.PhaseState;
import com.ichorpowered.guardian.api.phase.PhaseViewer;
import io.github.connorhartley.guardian.GuardianPlugin;

import java.util.Iterator;
import java.util.concurrent.atomic.AtomicInteger;

public class GuardianCheckViewer implements PhaseViewer<Check> {

    private final GuardianPlugin plugin;

    private PhaseState phaseState = PhaseState.INITIALIZED;

    private Iterator<CheckBlueprint> checkBlueprintIterator;
    private CheckBlueprint element;
    private int phaseIndex = 0;

    public GuardianCheckViewer(GuardianPlugin plugin) {
        this.plugin = plugin;

        this.checkBlueprintIterator = this.plugin.getCheckRegistry().iterator();

        if (this.checkBlueprintIterator.hasNext()) {
            this.element = this.checkBlueprintIterator.next();
        }
    }

    @Override
    @SuppressWarnings("unchecked")
    public <E, F extends DetectionConfiguration> Check getOrCreatePhase(Detection<E, F> detection) {
        return this.element.create(detection);
    }

    @Override
    @SuppressWarnings("unchecked")
    public Class<? extends Check> getPhaseClass() {
        return this.element.getCheckClass();
    }

    @Override
    public void next() {
        if (this.checkBlueprintIterator.hasNext()) {
            this.element = this.checkBlueprintIterator.next();
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
        return this.plugin.getCheckRegistry().keySet().size();
    }

    public int size(PhaseFilter phaseFilter) {
        AtomicInteger size = new AtomicInteger(0);

        this.plugin.getCheckRegistry().keySet().forEach(key -> {
            if (phaseFilter.accept(this.plugin.getCheckRegistry().get(key).getCheckClass())) {
                size.incrementAndGet();
            }
        });

        return size.get();
    }

}
