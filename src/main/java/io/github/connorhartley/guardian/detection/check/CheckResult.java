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
package io.github.connorhartley.guardian.detection.check;

import io.github.connorhartley.guardian.sequence.Sequence;
import io.github.connorhartley.guardian.sequence.action.Action;
import org.spongepowered.api.entity.living.player.User;

import java.util.ArrayList;
import java.util.List;

// TODO: Make this into a builder.
public class CheckResult {

    // This is probably going to change a lot.

    // TODO: CheckResult should contain information of Sequence completion or incompletion results used internally and externally.

    private final Sequence sequence;
    private final User user;

    private List<String> passed = new ArrayList<>();
    private List<String> failed = new ArrayList<>();

    public CheckResult(Sequence sequence, User user) {
        this.sequence = sequence;
        this.user = user;
    }

    public void createPoint(String task, boolean pass) {
        // Creates a point in which a report is formed from.
        if (pass) {
            this.passed.add(task);
        } else {
            this.failed.add(task);
        }
    }

    public int getReportSize() {
        return this.passed.size() + this.failed.size();
    }

    public double getPassPercentage() {
        return this.passed.size() / (this.passed.size() + this.failed.size());
    }

}
