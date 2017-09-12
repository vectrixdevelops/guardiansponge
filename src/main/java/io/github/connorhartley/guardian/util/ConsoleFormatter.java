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
package io.github.connorhartley.guardian.util;

import org.fusesource.jansi.Ansi;

import java.util.Arrays;

public final class ConsoleFormatter {

    public static ConsoleFormatter of(String message) {
        return new Builder().of(message).build();
    }

    public static ConsoleFormatter of(Ansi.Color color, String message) {
        return new Builder().fg(color, message).build();
    }

    public static Builder builder() {
        return new Builder();
    }

    private final String message;

    private ConsoleFormatter(Builder builder) {
        this.message = builder.message;
    }

    public String get() {
        return this.message;
    }

    public static class Builder {

        private String message = "";

        private Builder() {}

        public Builder of(String... messages) {
            this.message += Arrays.toString(messages);
            return this;
        }

        public Builder of(String message) {
            this.message += message;
            return this;
        }

        public Builder fg(Ansi.Color color) {
            this.message += Ansi.ansi().fg(color).boldOff().toString();
            return this;
        }

        public Builder fg(Ansi.Color color, String message) {
            this.message += Ansi.ansi().fg(color).boldOff().a(message).reset().toString();
            return this;
        }

        public Builder bg(Ansi.Color color) {
            this.message += Ansi.ansi().bg(color).boldOff().toString();
            return this;
        }

        public Builder bg(Ansi.Color color, String message) {
            this.message += Ansi.ansi().bg(color).boldOff().a(message).reset().toString();
            return this;
        }

        public ConsoleFormatter build() {
            return new ConsoleFormatter(this);
        }

        public String buildAndGet() {
            return new ConsoleFormatter(this).get();
        }

    }

}

