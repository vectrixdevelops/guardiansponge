/*
 * MIT License
 *
 * Copyright (c) 2016 Connor Hartley
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
package io.github.connorhartley.guardian.violation;

public class Offense {

    private final String offenseName;
    private final String offenseDescription;
    private final int offenseSeverity;

    private Offense(Builder builder) {
        this.offenseName = builder.offenseName;
        this.offenseDescription = builder.offenseDescription;
        this.offenseSeverity = builder.offenseSeverity;
    }

    public String getName() {
        return this.offenseName;
    }

    public String getDescription() {
        return this.offenseDescription;
    }

    public int getSeverity() {
        return this.offenseSeverity;
    }

    public static class Builder {

        private String offenseName;
        private String offenseDescription;
        private int offenseSeverity;

        public Builder() {}

        public Builder of(Offense offense) {
            this.offenseName = offense.getName();
            this.offenseDescription = offense.getDescription();
            this.offenseSeverity = offense.getSeverity();
            return this;
        }

        public Builder setName(String name) {
            this.offenseName = name;
            return this;
        }

        public Builder setDescription(String description) {
            this.offenseDescription = description;
            return this;
        }

        public Builder setSeverity(int severity) {
            this.offenseSeverity = severity;
            return this;
        }

        public Offense build() {
            return new Offense(this);
        }

    }
}
