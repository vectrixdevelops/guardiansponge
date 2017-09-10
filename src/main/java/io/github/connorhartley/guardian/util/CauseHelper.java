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
package io.github.connorhartley.guardian.util;

import io.github.connorhartley.guardian.GuardianPlugin;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.event.CauseStackManager;
import org.spongepowered.api.event.cause.Cause;
import org.spongepowered.api.event.cause.EventContext;
import org.spongepowered.api.event.cause.EventContextKey;

/*
 * Inspired by the Nucleus CauseStackHelper class.
 */
public final class CauseHelper {

    private static GuardianPlugin plugin;

    public CauseHelper(GuardianPlugin plugin) {
        CauseHelper.plugin = plugin;
    }

    public static CauseStackManager.StackFrame populateCauseFrame(Object... stack) {
        return populateCauseFrame(EventContext.empty(), stack);
    }

    public static CauseStackManager.StackFrame populateCauseFrame(EventContext eventContext, Object... stack) {
        CauseStackManager.StackFrame stackFrame = Sponge.getCauseStackManager().pushCauseFrame();
        eventContext.asMap().forEach((eventContextKey, o) -> Sponge.getCauseStackManager().addContext((EventContextKey) eventContextKey, o));

        if (stack.length > 0) {
            for (Object cause : stack) {
                Sponge.getCauseStackManager().pushCause(cause);
            }
        }

        return stackFrame;
    }

    public static Cause populateCause(Object... stack) {
        return populateCause(EventContext.empty(), stack);
    }

    public static Cause populateCause(EventContext eventContext, Object... stack) {
        if (Sponge.getServer().isMainThread()) {
            try (CauseStackManager.StackFrame stackFrame = populateCauseFrame(eventContext, stack)) {
                return Sponge.getCauseStackManager().getCurrentCause();
            }
        }

        Cause.Builder causeBuilder = Cause.builder();
        if (stack.length > 0) {
            for (Object cause : stack) {
                causeBuilder.append(cause);
            }
        } else {
            causeBuilder.append(CauseHelper.plugin);
        }

        return causeBuilder.build(eventContext);
    }

}
