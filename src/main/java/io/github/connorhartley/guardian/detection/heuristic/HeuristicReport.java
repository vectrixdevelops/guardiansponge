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
package io.github.connorhartley.guardian.detection.heuristic;

import io.github.connorhartley.guardian.detection.report.Report;
import io.github.connorhartley.guardian.util.Transformer;
import org.spongepowered.api.event.cause.Cause;

public class HeuristicReport implements Report {

    private final String detectionType;
    private final Transformer<Double> severityTransformer;
    private final Cause cause;

    public HeuristicReport(Builder builder) {
        this.detectionType = builder.detectionType;
        this.severityTransformer = builder.severityTransformer;
        this.cause = builder.cause;
    }

    public static HeuristicReport of(HeuristicReport heuristicReport) {
        Builder builder = new Builder();
        builder.detectionType = heuristicReport.detectionType;
        builder.severityTransformer = heuristicReport.severityTransformer;
        builder.cause = heuristicReport.cause;

        return new HeuristicReport(builder);
    }

    public static Builder builder() {
        return new Builder();
    }

    @Override
    public ReportTypes getReportType() {
        return ReportTypes.HEURISTIC;
    }

    @Override
    public String getDetectionType() {
        return this.detectionType;
    }

    @Override
    public Transformer<Double> getSeverityTransformer() {
        return this.severityTransformer;
    }

    @Override
    public Cause getCause() {
        return this.cause;
    }

    public static class Builder {

        private String detectionType;
        private Transformer<Double> severityTransformer;
        private Cause cause;

        public Builder() {}

        public Builder type(String detectionType) {
            this.detectionType = detectionType;
            return this;
        }

        public Builder severity(Transformer<Double> severityTransformer) {
            this.severityTransformer = severityTransformer;
            return this;
        }

        public Builder cause(Cause cause) {
            this.cause = cause;
            return this;
        }

        public HeuristicReport build() {
            return new HeuristicReport(this);
        }

    }

}
