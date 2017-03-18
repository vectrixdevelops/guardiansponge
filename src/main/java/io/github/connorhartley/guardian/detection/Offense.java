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
package io.github.connorhartley.guardian.detection;

import java.time.LocalDateTime;

import io.github.connorhartley.guardian.sequence.report.SequenceReport;
import org.spongepowered.api.entity.living.player.Player;

/**
 * Offense
 *
 * Represents information about the time and severity of
 * a {@link Player}'s valid {@link Detection} report.
 */
public class Offense {

    private final Detection offenseDetection;
    private final SequenceReport offenseReport;
    private final LocalDateTime offenseDateTime;
    private final double offenseSeverity;

    private Offense(Builder builder) {
        this.offenseDetection = builder.offenseDetection;
        this.offenseReport = builder.offenseReport;
        this.offenseDateTime = builder.offenseDateTime;
        this.offenseSeverity = builder.offenseSeverity;
    }

    /**
     * Get Detection
     *
     * <p>Returns the detection this offense was created by.</p>
     *
     * @return The {@link Detection}
     */
    public Detection getDetection() { return this.offenseDetection; }

    /**
     * Get Report
     *
     * <p>Returns the sequence report for this offense.</p>
     *
     * @return The {@link SequenceReport}
     */
    public SequenceReport getReport() {
        return this.offenseReport;
    }

    /**
     * Get Date and Time
     *
     * <p>Returns the date and time of when this offense was created.</p>
     *
     * @return The {@link LocalDateTime}
     */
    public LocalDateTime getDateAndTime() {
        return this.offenseDateTime;
    }

    /**
     * Get Severity
     *
     * <p>Returns the severity of the offense.</p>
     *
     * @return An {@link Double} representing the level of severity
     */
    public double getSeverity() {
        return this.offenseSeverity;
    }

    /**
     * Offense Builder
     *
     * Allows you to build a new {@link Offense}.
     */
    public static class Builder {

        private Detection offenseDetection;
        private SequenceReport offenseReport;
        private LocalDateTime offenseDateTime;
        private double offenseSeverity;

        public Builder() {}

        /**
         * Of
         *
         * <p>Sets offense properties from another offense to the builder.</p>
         *
         * @param offense Other {@link Offense}
         * @return This {@link Builder}
         */
        public Builder of(Offense offense) {
            this.offenseDetection = offense.getDetection();
            this.offenseDateTime = offense.getDateAndTime();
            this.offenseSeverity = offense.getSeverity();
            return this;
        }

        /**
         * Report
         *
         * <p>Sets the sequence report of the offense to the builder.</p>
         *
         * @param sequenceReport The {@link SequenceReport}
         * @return This {@link Builder}
         */
        public Builder report(SequenceReport sequenceReport) {
            this.offenseReport = sequenceReport;
            return this;
        }

        /**
         * Date and Time
         *
         * <p>Sets the date and time of the offense to the builder.</p>
         *
         * @param dateAndTime The {@link LocalDateTime}
         * @return This {@link Builder}
         */
        public Builder dateAndTime(LocalDateTime dateAndTime) {
            this.offenseDateTime = dateAndTime;
            return this;
        }

        /**
         * Detection
         *
         * <p>Sets the detection of the offense to the builder.</p>
         *
         * @param detection The {@link Detection}
         * @return This {@link Builder}
         */
        public Builder detection(Detection detection) {
            this.offenseDetection = detection;
            return this;
        }

        /**
         * Severity
         *
         * <p>Sets the severity of the offense to the builder.</p>
         *
         * @param severity The {@link Double} representing the level of severity
         * @return This {@link Builder}
         */
        public Builder severity(double severity) {
            this.offenseSeverity = severity;
            return this;
        }

        /**
         * Build
         *
         * <p>Builds the offense from this {@link Builder}.</p>
         *
         * @return A new {@link Offense}
         */
        public Offense build() {
            return new Offense(this);
        }

    }

}
