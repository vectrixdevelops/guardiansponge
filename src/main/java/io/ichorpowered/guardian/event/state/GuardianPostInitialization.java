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
package io.ichorpowered.guardian.event.state;

import com.ichorpowered.guardian.api.Guardian;
import com.ichorpowered.guardian.api.event.origin.Origin;
import com.ichorpowered.guardian.api.event.state.PostInitializationEvent;

import javax.annotation.Nonnull;

public class GuardianPostInitialization implements PostInitializationEvent {

    private final Guardian guardian;
    private final Origin origin;

    private boolean cancelled = false;

    public GuardianPostInitialization(@Nonnull Guardian guardian, @Nonnull Origin origin) {
        this.guardian = guardian;
        this.origin = origin;
    }

    @Nonnull
    @Override
    public Guardian getGuardian() {
        return this.guardian;
    }

    @Nonnull
    @Override
    public Origin getOrigin() {
        return this.origin;
    }

    @Override
    public boolean cancelled() {
        return this.cancelled;
    }

    @Override
    public void cancelled(boolean cancelled) {
        this.cancelled = cancelled;
    }

}
