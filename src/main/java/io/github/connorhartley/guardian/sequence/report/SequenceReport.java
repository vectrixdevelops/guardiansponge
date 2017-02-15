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

import io.github.connorhartley.guardian.sequence.Sequence;

import java.util.HashMap;

/**
 * Sequence Report
 *
 * Represents a report containing an analysis from a {@link Sequence}.
 */
public class SequenceReport {

    private final HashMap<ReportType, Object> reports;

    public SequenceReport(Builder builder) {
        this.reports = builder.reports;
    }

    public static Builder builder() {
        return new Builder();
    }

    public static Builder of(SequenceReport sequenceReport) {
        return new Builder(sequenceReport);
    }

    public HashMap<ReportType, Object> getReports() {
        return this.reports;
    }

    public static class Builder {

        private HashMap<ReportType, Object> reports = new HashMap<>();

        public Builder() {}

        public Builder(SequenceReport sequenceReport) {
            this.reports = sequenceReport.reports;
        }

        public <T> Builder append(ReportType reportType, T value) {
            if (reportType.isType(value)) {
                this.reports.put(reportType, value);
            }
            return this;
        }

        public SequenceReport build() {
            return new SequenceReport(this);
        }

    }

}
