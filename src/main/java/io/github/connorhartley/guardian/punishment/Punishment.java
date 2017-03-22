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
package io.github.connorhartley.guardian.punishment;

import io.github.connorhartley.guardian.sequence.report.SequenceReport;

import java.time.LocalDateTime;

public class Punishment {

    private final SequenceReport sequenceReport;
    private final LocalDateTime localDateTime;
    private final double probability;

    public Punishment(Builder builder) {
        this.sequenceReport = builder.sequenceReport;
        this.localDateTime = builder.localDateTime;
        this.probability = builder.probability;
    }

    public static Builder builder() {
        return new Builder();
    }

    public SequenceReport getSequenceReport() {
        return this.sequenceReport;
    }

    public LocalDateTime getLocalDateTime() {
        return this.localDateTime;
    }

    public double getProbability() {
        return this.probability;
    }

    public static class Builder {

        private SequenceReport sequenceReport;
        private LocalDateTime localDateTime;
        private double probability;

        public Builder() {}

        public Builder report(SequenceReport sequenceReport) {
            this.sequenceReport = sequenceReport;
            return this;
        }

        public Builder time(LocalDateTime localDateTime) {
            this.localDateTime = localDateTime;
            return this;
        }

        public Builder probability(double probability) {
            this.probability = probability;
            return this;
        }

        public Punishment build() {
            return new Punishment(this);
        }

    }

}
