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
package io.github.connorhartley.guardian.detection.punishment;

import io.github.connorhartley.guardian.detection.report.Report;
import io.github.connorhartley.guardian.sequence.SequenceResult;
import io.github.connorhartley.guardian.util.Transformer;
import org.spongepowered.api.event.cause.Cause;

import java.time.LocalDateTime;

/**
 * PunishmentReport
 *
 * Contains information regarding handling a punishment.
 */
public class PunishmentReport implements Report {

    private final String detectionType;
    private final SequenceResult sequenceResult;
    private final LocalDateTime localDateTime;
    private final Transformer<Double> severityTransformer;
    private final Cause cause;

    public PunishmentReport(Builder builder) {
        this.detectionType = builder.detectionType;
        this.sequenceResult = builder.sequenceResult;
        this.localDateTime = builder.localDateTime;
        this.severityTransformer = builder.severityTransformer;
        this.cause = builder.cause;
    }

    public static PunishmentReport of(PunishmentReport punishmentReport) {
        Builder builder = new Builder();
        builder.detectionType = punishmentReport.detectionType;
        builder.sequenceResult = punishmentReport.sequenceResult;
        builder.localDateTime = punishmentReport.localDateTime;
        builder.severityTransformer = punishmentReport.severityTransformer;
        builder.cause = punishmentReport.cause;

        return new PunishmentReport(builder);
    }

    public static Builder builder() {
        return new Builder();
    }

    @Override
    public ReportTypes getReportType() {
        return ReportTypes.PUNISHMENT;
    }

    @Override
    public String getDetectionType() {
        return this.detectionType;
    }

    public SequenceResult getSequenceResult() {
        return this.sequenceResult;
    }

    public LocalDateTime getLocalDateTime() {
        return this.localDateTime;
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
        private SequenceResult sequenceResult;
        private LocalDateTime localDateTime;
        private Transformer<Double> severityTransformer;
        private Cause cause;

        public Builder() {}

        public Builder type(String detectionReason) {
            this.detectionType = detectionReason;
            return this;
        }

        public Builder report(SequenceResult sequenceResult) {
            this.sequenceResult = sequenceResult;
            return this;
        }

        public Builder time(LocalDateTime localDateTime) {
            this.localDateTime = localDateTime;
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

        public PunishmentReport build() {
            return new PunishmentReport(this);
        }

    }

}
