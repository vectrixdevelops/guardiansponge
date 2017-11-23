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
import com.abilityapi.sequenceapi.action.type.observe.ObserverAction;
import com.abilityapi.sequenceapi.action.type.schedule.ScheduleAction;
import com.abilityapi.sequenceapi.context.SequenceContext;
import com.ichorpowered.guardian.api.detection.DetectionConfiguration;
import com.ichorpowered.guardian.api.util.key.NamedKey;
import io.ichorpowered.guardian.report.GuardianSummary;
import io.ichorpowered.guardian.sequence.capture.GuardianCaptureRegistry;
import org.spongepowered.api.event.Event;

import java.util.Map;

public class GuardianSequence<E, F extends DetectionConfiguration> extends Sequence<Event> {

    public static NamedKey INITIAL_LOCATION =
            NamedKey.of(GuardianSequence.class.getCanonicalName() + "_INITIAL_LOCATION");

    private final GuardianSummary<E, F> summary = null;
    private final GuardianCaptureRegistry captureRegistry;

    public GuardianSequence(final SequenceContext sequenceContext,
                            final SequenceBlueprint<Event> sequenceBlueprint,
                            final GuardianCaptureRegistry captureRegistry,
                            final Map<ScheduleAction, Integer> scheduleActions,
                            final Map<ObserverAction<Event>, Integer> observerActions) {
        super(sequenceContext, sequenceBlueprint, scheduleActions, observerActions);

        // TODO: Initialize summary.
        this.captureRegistry = captureRegistry;
    }

    public GuardianSummary<E, F> getSummary() {
        return this.summary;
    }

    public GuardianCaptureRegistry getCaptureRegistry() {
        return this.captureRegistry;
    }
}
