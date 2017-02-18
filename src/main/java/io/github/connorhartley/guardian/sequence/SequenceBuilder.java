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
package io.github.connorhartley.guardian.sequence;

import io.github.connorhartley.guardian.context.ContextProvider;
import io.github.connorhartley.guardian.context.ContextTracker;
import io.github.connorhartley.guardian.detection.check.CheckProvider;
import io.github.connorhartley.guardian.sequence.action.Action;
import io.github.connorhartley.guardian.sequence.action.ActionBlueprint;
import io.github.connorhartley.guardian.sequence.action.ActionBuilder;
import io.github.connorhartley.guardian.sequence.report.SequenceReport;
import org.spongepowered.api.entity.living.player.User;
import org.spongepowered.api.event.Event;

import java.util.ArrayList;
import java.util.List;

/**
 * Sequence Builder
 *
 * A {@link SequenceBlueprint} builder giving the ability to chain
 * actions for a {@link Sequence}.
 */
public class SequenceBuilder {

    private final ContextProvider contextProvider;

    private SequenceReport sequenceReport;
    private ContextTracker contextTracker;
    private List<Action> actions = new ArrayList<>();

    public SequenceBuilder(ContextProvider contextProvider) {
        this(contextProvider,null);
    }

    public SequenceBuilder(ContextProvider contextProvider, ContextTracker contextTracker) {
        this(contextProvider, contextTracker, null);
    }

    public SequenceBuilder(ContextProvider contextProvider, ContextTracker contextTracker, SequenceReport sequenceReport) {
        this.contextProvider = contextProvider;
        this.sequenceReport = (sequenceReport == null) ? SequenceReport.builder().build() : sequenceReport;
        this.contextTracker = (contextTracker == null) ? new ContextTracker.Builder().build() : contextTracker;
    }

    public <T extends Event> ActionBuilder<T> action(Class<T> clazz, ContextTracker contextTracker) {
        return action(new Action<>(this.contextProvider, clazz, this.sequenceReport, contextTracker));
    }

    public <T extends Event> ActionBuilder<T> action(ActionBlueprint<T> builder) {
        return action(builder.create());
    }

    public <T extends Event> ActionBuilder<T> action(Action<T> action) {
        this.actions.add(action);

        return new ActionBuilder<>(this.contextProvider, this, action, action.getContextTracker(), this.sequenceReport);
    }

    public SequenceBlueprint build(CheckProvider provider) {
        return new SequenceBlueprint(provider) {
            @Override
            public Sequence create(User user) {
                return new Sequence(user, provider, actions, sequenceReport);
            }
        };
    }

}
