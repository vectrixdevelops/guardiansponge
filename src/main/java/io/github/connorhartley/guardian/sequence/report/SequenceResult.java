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

import java.util.ArrayList;
import java.util.List;

public class SequenceResult {

    private final List<SequencePoint> sequencePoints;
    private final int passed;
    private final int failed;

    private SequenceResult(Builder builder) {
        this.sequencePoints = builder.points;
        this.passed = builder.passed;
        this.failed = builder.failed;
    }

    public List<SequencePoint> getSequencePoints() {
        return this.sequencePoints;
    }

    public double getPassPercentage() {
        return this.passed / (this.passed + this.failed);
    }

    public static class Builder {

        private List<SequencePoint> points = new ArrayList<>();
        private int passed = 0;
        private int failed = 0;

        public Builder() {}

        public Builder of(SequenceResult sequenceResult) {
            this.points = sequenceResult.sequencePoints;
            this.passed = sequenceResult.passed;
            this.failed = sequenceResult.failed;
            return this;
        }

        public Builder addPoints(List<SequencePoint> sequencePoints) {
            this.points.addAll(sequencePoints);
            this.points.forEach(point -> {
                if (point.hasPassed()) this.passed = this.passed++;
                if (!point.hasPassed()) this.failed = this.failed++;
            });
            return this;
        }

        public Builder addPoint(SequencePoint sequencePoint) {
            this.points.add(sequencePoint);
            if (sequencePoint.hasPassed()) this.passed = this.passed++;
            if (sequencePoint.hasPassed()) this.failed = this.failed++;
            return this;
        }

        public SequenceResult build() {
            return new SequenceResult(this);
        }

    }

}
