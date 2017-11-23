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
package io.ichorpowered.guardian.util;

import org.fusesource.jansi.Ansi;
import org.fusesource.jansi.Ansi.Color;

import static org.slf4j.helpers.MessageFormatter.arrayFormat;

public final class ConsoleUtil {

    private final String message;

    private ConsoleUtil(final String message) {
        this.message = message;
    }

    public static String of(final String message, final String... replace) {
        return builder().add(message, replace).buildAndGet();
    }

    public static ConsoleUtil append(final String message, final String... replace) {
        return builder().add(message, replace).build();
    }

    public static String of(final Color color, final String message, final String... replace) {
        return builder(color).add(message, replace).buildAndGet();
    }

    public static ConsoleUtil append(final Color color, final String message, final String... replace) {
        return builder(color).add(message, replace).build();
    }

    public static String warn(final String message, final String... replace) {
        return builder(Color.YELLOW).withHeader(Color.YELLOW, "WARNING").add(Color.YELLOW, message, replace).build().message;
    }

    public static String error(final String message, final String... replace) {
        return builder(Color.RED).withHeader(Color.RED, "ERROR").add(Color.RED, message, replace).build().message;
    }

    public static Builder builder() {
        return builder(Color.WHITE);
    }

    public static Builder builder(final Color color) {
        return new Builder(color);
    }

    public static class Builder {

        private final Color def;
        private String header = "";
        private String message = "";

        private Builder(final Color def) {
            this.def = def;
        }

        public Builder withHeader(final Color color, final String header) {
            this.header = Ansi.ansi().fg(color).bold().a("** " + header + " ** ").reset().toString();
            return this;
        }

        public Builder add(final String message, final String... replace) {
            this.message += Ansi.ansi().fg(this.def).boldOff().a(arrayFormat(message, replace).getMessage()).reset().toString();
            return this;
        }

        public Builder add(final Color color, final String message, final String... replace) {
            this.message += Ansi.ansi().fg(color).boldOff().a(arrayFormat(message, replace).getMessage()).reset().toString();
            return this;
        }

        public Builder addBold(final String message, final String... replace) {
            this.message += Ansi.ansi().fg(this.def).bold().a(arrayFormat(message, replace).getMessage()).reset().toString();
            return this;
        }

        public Builder addBold(final Color color, final String message, final String... replace) {
            this.message += Ansi.ansi().fg(color).bold().a(arrayFormat(message, replace).getMessage()).reset().toString();
            return this;
        }

        public ConsoleUtil build() {
            return new ConsoleUtil(this.message);
        }

        public String buildAndGet() {
            return new ConsoleUtil(this.message).message;
        }

    }

}
