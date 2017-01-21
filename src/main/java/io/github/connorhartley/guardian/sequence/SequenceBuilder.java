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

import io.github.connorhartley.guardian.detection.check.CheckProvider;
import io.github.connorhartley.guardian.sequence.action.Action;
import io.github.connorhartley.guardian.sequence.action.ActionBlueprint;
import io.github.connorhartley.guardian.sequence.action.ActionBuilder;
import org.spongepowered.api.entity.living.player.User;
import org.spongepowered.api.event.Event;

import java.util.ArrayList;
import java.util.List;

public class SequenceBuilder {

    private List<Action> actions = new ArrayList<>();

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

    public SequenceBlueprint build(CheckProvider provider) {
        return new SequenceBlueprint(provider) {
            @Override
            public Sequence create(User user) {
                return new Sequence(user, provider, actions);
            }
        };
    }

}
