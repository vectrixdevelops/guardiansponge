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

import io.github.connorhartley.guardian.sequence.SequenceResult;

import java.time.LocalDateTime;

/**
 * Punishment
 *
 * Contains information regarding handling a punishment.
 */
public class Punishment {

    private final String detectionReason;
    private final SequenceResult sequenceResult;
    private final LocalDateTime localDateTime;
    private final Double probability;

    public Punishment(Builder builder) {
        this.detectionReason = builder.detectionReason;
        this.sequenceResult = builder.sequenceResult;
        this.localDateTime = builder.localDateTime;
        this.probability = builder.probability;
    }

    public static Builder builder() {
        return new Builder();
    }

    /**
     * Get Detection Reason
     *
     * <p>Returns the detection reason this player is being
     * punished.</p>
     *
     * @return The detection punishment reason
     */
    public String getDetectionReason() {
        return this.detectionReason;
    }

    /**
     * Get Sequence Report
     *
     * <p>Returns the sequence report that accepted this punishment
     * to be created.</p>
     *
     * @return The sequence report
     */
    public SequenceResult getSequenceResult() {
        return this.sequenceResult;
    }

    /**
     * Get Local Date And Time
     *
     * <p>Returns the {@link LocalDateTime} of when this punishment
     * was created.</p>
     *
     * @return The local date and time
     */
    public LocalDateTime getLocalDateTime() {
        return this.localDateTime;
    }

    /**
     * Get Probability
     *
     * <p>Returns the probability of this punishments validity.</p>
     *
     * @return The punishment validity
     */
    public Double getProbability() {
        return this.probability;
    }

    public static class Builder {

        private String detectionReason;
        private SequenceResult sequenceResult;
        private LocalDateTime localDateTime;
        private Double probability;

        public Builder() {}

        public Builder reason(String detectionReason) {
            this.detectionReason = detectionReason;
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

        public Builder probability(Double probability) {
            this.probability = probability;
            return this;
        }

        public Punishment build() {
            return new Punishment(this);
        }

    }

}
