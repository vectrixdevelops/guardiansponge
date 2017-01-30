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
package io.github.connorhartley.guardian.event.check;

import io.github.connorhartley.guardian.detection.check.Check;
import io.github.connorhartley.guardian.detection.check.CheckResult;
import org.spongepowered.api.entity.living.player.User;
import org.spongepowered.api.event.cause.Cause;
import org.spongepowered.api.event.impl.AbstractEvent;

import java.util.Optional;

public class CheckEndEvent extends AbstractEvent {

    private final Check check;
    private final User user;
    private final Cause cause;
    private final CheckResult checkResult;

    public CheckEndEvent(Check check, Cause cause) {
        this(check, null, cause);
    }

    public CheckEndEvent(Check check, User user, Cause cause) {
        this(check, user, null, cause);
    }

    public CheckEndEvent(Check check, User user, CheckResult checkResult, Cause cause) {
        this.check = check;
        this.user = user;
        this.checkResult = checkResult;
        this.cause = cause;
    }

    public Check getCheck() {
        return this.check;
    }

    public Optional<User> getUser() {
        if (this.user == null) return Optional.empty();
        return Optional.of(this.user);
    }

    public Optional<CheckResult> getCheckResult() {
        if (this.checkResult == null) return Optional.empty();
        return Optional.of(this.checkResult);
    }

    @Override
    public Cause getCause() {
        return null;
    }
}
