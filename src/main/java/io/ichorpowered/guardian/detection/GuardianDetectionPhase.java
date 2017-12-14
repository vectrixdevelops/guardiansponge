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
package io.ichorpowered.guardian.detection;

import com.ichorpowered.guardian.api.detection.Detection;
import com.ichorpowered.guardian.api.detection.DetectionConfiguration;
import com.ichorpowered.guardian.api.detection.DetectionPhase;
import com.ichorpowered.guardian.api.event.origin.Origin;
import com.ichorpowered.guardian.api.phase.PhaseFilter;
import com.ichorpowered.guardian.api.phase.PhaseState;
import com.ichorpowered.guardian.api.phase.PhaseViewer;
import com.ichorpowered.guardian.api.util.key.NamedTypeKey;
import io.ichorpowered.guardian.GuardianPlugin;
import io.ichorpowered.guardian.event.phase.PhaseChangeEvent;
import io.ichorpowered.guardian.phase.GuardianPhaseFilter;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.concurrent.atomic.AtomicInteger;

public class GuardianDetectionPhase<E, F extends DetectionConfiguration> implements DetectionPhase<E, F> {

    private final GuardianPlugin plugin;
    private final Detection<E, F> detection;
    private final PhaseFilter phaseFilter;

    public GuardianDetectionPhase(@Nonnull GuardianPlugin plugin, @Nonnull Detection<E, F> detection) {
        this(plugin, detection, new GuardianPhaseFilter());
    }

    public GuardianDetectionPhase(@Nonnull GuardianPlugin plugin, @Nonnull Detection<E, F> detection,
                                  @Nonnull PhaseFilter phaseFilter) {
        this.plugin = plugin;
        this.detection = detection;
        this.phaseFilter = phaseFilter;
    }

    @Nonnull
    @Override
    public Detection<E, F> getDetection() {
        return this.detection;
    }

    @Nullable
    @Override
    public <T> T next(@Nonnull NamedTypeKey<T> phaseKey) {
        if (!this.phaseFilter.accept(this.plugin.getPhaseRegistry().expect(phaseKey).getPhaseClass())) return null;
        PhaseViewer<T> phaseViewer = this.plugin.getPhaseRegistry().expect(phaseKey);

        T phase = phaseViewer.getOrCreatePhase(this.detection);

        this.plugin.getEventBus().post(new PhaseChangeEvent<>(this.detection, phaseViewer, (Class<T>) phase.getClass(),
                Origin.source(this.detection).owner(this.plugin).named("key", phaseKey)
                        .named("filter", this.phaseFilter).build()));

        phaseViewer.next();
        return phase;
    }

    @Override
    public <T> boolean hasNext(@Nonnull NamedTypeKey<T> phaseKey) {
        PhaseViewer<T> phaseViewer = this.plugin.getPhaseRegistry().expect(phaseKey);

        if (phaseViewer.index() + 1 <= phaseViewer.size()) {
            if (!this.phaseFilter.accept(phaseViewer.getPhaseClass())) {

                phaseViewer.next();

                return this.hasNext(phaseKey);
            } else return true;
        }

        return false;
    }

    @Nullable
    @Override
    public <T> PhaseState getViewerState(@Nonnull NamedTypeKey<T> phaseKey) {
        return this.plugin.getPhaseRegistry().expect(phaseKey).getPhaseState();
    }

    @Nonnull
    @Override
    public PhaseFilter getFilter() {
        return this.phaseFilter;
    }

    @Override
    public int size() {
        AtomicInteger size = new AtomicInteger(0);

        this.plugin.getPhaseRegistry().keySet().forEach(namedTypeKey -> {
            if (this.phaseFilter.accept(this.plugin.getPhaseRegistry().expect(namedTypeKey).getPhaseClass())) {
                size.addAndGet(this.plugin.getPhaseRegistry().get(namedTypeKey).size(this.phaseFilter));
            }
        });

        return size.get();
    }

    @Override
    public <T> int size(NamedTypeKey<T> phaseKey) {
        return this.plugin.getPhaseRegistry().expect(phaseKey).size(this.phaseFilter);
    }

}
