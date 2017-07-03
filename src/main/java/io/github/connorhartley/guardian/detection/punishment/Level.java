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

import org.spongepowered.api.util.Tuple;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class Level {

    private final String name;
    private final Tuple<Double, Double> range;
    private final List<String> actions;

    public Level(Builder builder) {
        this.name = builder.name;
        this.range = builder.range;
        this.actions = builder.actions;
    }

    public static Builder builder() {
        return new Builder();
    }

    public String getName() {
        return this.name;
    }

    public Tuple<Double, Double> getRange() {
        return range;
    }

    public List<String> getActions() {
        return actions;
    }

    public static class Builder {

        private String name;
        private Tuple<Double, Double> range;
        private List<String> actions;

        public Builder() {}

        public Builder name(String name) {
            this.name = name;
            return this;
        }

        public Builder range(Tuple<Double, Double> range) {
            this.range = range;
            return this;
        }

        public Builder action(String[] actions) {
            if (this.actions == null) this.actions = new ArrayList<>();

            this.actions.addAll(Arrays.asList(actions));
            return this;
        }

        public Level build() {
            return new Level(this);
        }

    }

}
