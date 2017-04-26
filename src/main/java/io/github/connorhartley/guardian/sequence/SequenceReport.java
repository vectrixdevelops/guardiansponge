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

import java.util.ArrayList;
import java.util.List;

/**
 * Sequence Report
 *
 * Represents a report containing an analysis from a {@link Sequence}.
 */
public class SequenceReport {

    private final List<String> detectionTypes = new ArrayList<>();
    private final List<String> information = new ArrayList<>();
    private final double severity;
    private final boolean accepted;

    public SequenceReport(Builder builder) {
        this.detectionTypes.addAll(builder.detectionTypes);
        this.information.addAll(builder.information);
        this.severity = builder.severity;
        this.accepted = builder.accepted;
    }

    public static Builder builder() {
        return new Builder();
    }

    public List<String> getDetectionTypes() {
        return this.detectionTypes;
    }

    public List<String> getInformation() {
        return this.information;
    }

    public double getSeverity() {
        return this.severity;
    }

    public boolean isAccepted() {
        return this.accepted;
    }

    public static class Builder {

        private List<String> detectionTypes = new ArrayList<>();
        private List<String> information = new ArrayList<>();
        private double severity = 0.0;
        private boolean accepted = false;

        public Builder() {}

        public Builder of(SequenceReport sequenceReport) {
            this.detectionTypes = sequenceReport.detectionTypes;
            this.information = sequenceReport.information;
            this.severity = sequenceReport.severity;
            this.accepted = sequenceReport.accepted;
            return this;
        }

        public Builder type(String detectionType) {
            this.detectionTypes.add(detectionType);
            return this;
        }

        public Builder information(String information) {
            this.information.add(information);
            return this;
        }

        public Builder severity(double severity) {
            this.severity = severity;
            return this;
        }

        public SequenceReport build(boolean accept) {
            this.accepted = accept;
            return new SequenceReport(this);
        }

    }

}
