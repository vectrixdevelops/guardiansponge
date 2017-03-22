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
import io.github.connorhartley.guardian.context.ContextBuilder;
import io.github.connorhartley.guardian.detection.check.CheckType;
import io.github.connorhartley.guardian.sequence.action.Action;
import io.github.connorhartley.guardian.sequence.action.ActionBlueprint;
import io.github.connorhartley.guardian.sequence.action.ActionBuilder;
import org.spongepowered.api.entity.living.player.Player;
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

    private ContextProvider contextProvider;
    private ContextBuilder contextBuilder = ContextBuilder.builder().build();

    private List<Action> actions = new ArrayList<>();

    public SequenceBuilder context(ContextProvider contextProvider, ContextBuilder contextBuilder) {
        this.contextProvider = contextProvider;

        // TODO: Builder should be refactored.
        this.contextBuilder = ContextBuilder.builder().of(this.contextBuilder).of(contextBuilder).build();
        return this;
    }

    public <T extends Event> ActionBuilder<T> action(Class<T> clazz) {
        return action(new Action<>(clazz));
    }

    public <T extends Event> ActionBuilder<T> action(ActionBlueprint<T> builder) {
        return action(builder.create());
    }

    public <T extends Event> ActionBuilder<T> action(Action<T> action) {
        this.actions.add(action);

        return new ActionBuilder<>(this, action);
    }

    public SequenceBlueprint build(CheckType checkType) {
        return new SequenceBlueprint(checkType) {
            @Override
            public Sequence create(Player player) {
                return new Sequence(player, this, checkType, actions, contextProvider, contextBuilder);
            }
        };
    }

}
