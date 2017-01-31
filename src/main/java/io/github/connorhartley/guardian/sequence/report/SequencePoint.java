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
package io.github.connorhartley.guardian.sequence.report;

import java.util.List;

public class SequencePoint {

    private final List<String> lines;
    private final boolean pass;

    public SequencePoint(Builder builder) {
        this.lines = builder.lines;
        this.pass = builder.pass;
    }

    public boolean hasPassed() {
        return this.pass;
    }

    public List<String> getDescription() {
        return this.lines;
    }

    public static class Builder {

        private List<String> lines;
        private boolean pass;

        public Builder() {}

        public Builder of(SequencePoint sequencePoint) {
            this.lines = sequencePoint.lines;
            this.pass = sequencePoint.pass;
            return this;
        }

        public Builder setPass(boolean pass) {
            this.pass = pass;
            return this;
        }

        public Builder addDescriptions(List<String> lines) {
            this.lines.addAll(lines);
            return this;
        }

        public Builder addDescription(String line) {
            this.lines.add(line);
            return this;
        }

        public SequencePoint build() {
            return new SequencePoint(this);
        }

    }

}
