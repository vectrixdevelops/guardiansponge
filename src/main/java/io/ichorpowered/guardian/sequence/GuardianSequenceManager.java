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
package io.ichorpowered.guardian.sequence;

import com.abilityapi.sequenceapi.Sequence;
import com.abilityapi.sequenceapi.SequenceBlueprint;
import com.abilityapi.sequenceapi.SequenceManager;
import com.abilityapi.sequenceapi.SequenceRegistry;
import com.abilityapi.sequenceapi.context.SequenceContext;
import com.ichorpowered.guardian.api.detection.Detection;
import com.ichorpowered.guardian.api.detection.DetectionConfiguration;
import com.ichorpowered.guardian.api.detection.DetectionPhase;
import com.ichorpowered.guardian.api.detection.heuristic.Heuristic;
import com.ichorpowered.guardian.api.detection.penalty.Penalty;
import com.ichorpowered.guardian.api.phase.type.PhaseTypes;
import io.ichorpowered.guardian.report.GuardianSummary;
import org.spongepowered.api.event.Event;

public class GuardianSequenceManager<E, F extends DetectionConfiguration> extends SequenceManager<Event> {

    public GuardianSequenceManager(final SequenceRegistry<Event> sequenceRegistry) {
        super(sequenceRegistry);
    }

    @Override
    public boolean _invokeObserver(final Event event,
                                   final Sequence<Event> sequence,
                                   final SequenceContext sequenceContext) {
        boolean result = super._invokeObserver(event, sequence, sequenceContext);

        if (sequence.getState().equals(Sequence.State.FINISHED)) {

            // ----------------  Phase Transition TEMPORARY PATH ------------------

            DetectionPhase<?, ?> detectionPhase = ((AbstractSequenceBlueprint<E, F>) sequence.getBlueprint()).getDetection().getPhaseManipulator();
            Detection detection = ((AbstractSequenceBlueprint<E, F>) sequence.getBlueprint()).getDetection();
            GuardianSummary summary = ((GuardianSequence) sequence).getSummary();

            while (detectionPhase.hasNext(PhaseTypes.HEURISTIC)) {
                Heuristic heuristic = detectionPhase.next(PhaseTypes.HEURISTIC);
            }

            while (detectionPhase.hasNext(PhaseTypes.PENALTY)) {
                Penalty penalty = detectionPhase.next(PhaseTypes.PENALTY);
            }

            // --------------------------------------------------------------------

        }

        return result;
    }
}
