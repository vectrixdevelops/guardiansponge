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
import org.spongepowered.api.event.Event;
import org.spongepowered.api.event.cause.Cause;

import java.util.List;

public class Sequence<H> {

    public Sequence(H human, CheckProvider<H> checkProvider, List<Action<H, ?>> actions) {

    }

    <T extends Event> boolean check(H human, T event) {
        return false;
    }

    // Human has passed with no detection of exploitation.
    boolean pass(H human, Event event, Action<H, ?> action, Cause cause) {
        return false;
    }

    // Human has failed with a detection of exploitaton.
    boolean fail(H human, Event event, Cause cause) {
        return false;
    }

    boolean hasExpired() {
        return false;
    }

    boolean isCancelled() {
        return false;
    }

    boolean isFinished() {
        return false;
    }

    public H getHuman() {
        return null;
    }

    public CheckProvider<H> getProvider() {
        return null;
    }

    public List<Event> getCompleteEvents() {
        return null;
    }

}
