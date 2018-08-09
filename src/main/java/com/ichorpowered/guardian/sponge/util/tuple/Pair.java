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
package com.ichorpowered.guardian.sponge.util.tuple;

import static com.google.common.base.Preconditions.checkNotNull;

import com.google.common.base.MoreObjects;
import com.google.common.base.Objects;

/**
 * A pair of elements.
 *
 * @param <A> the first type
 * @param <B> the second type
 */
public class Pair<A, B> {

    /**
     * Creates a new {@link Pair} with the desired {@code first} and
     * {@code second} elements.
     *
     * @param first the first type
     * @param second the second type
     * @param <Z> the type of first type
     * @param <Y> the type of second type
     * @return the new pair
     */
    public static <Z, Y> Pair<Z, Y> of(final Z first, final Y second) {
        return new Pair<>(first, second);
    }

    private final A first;
    private final B second;

    /**
     * Creates a new {@link Pair}.
     *
     * @param first the first type
     * @param second the second type
     */
    public Pair(final A first, final B second) {
        this.first = checkNotNull(first);
        this.second = checkNotNull(second);
    }

    /**
     * Returns the first type.
     *
     * @return the first type
     */
    public final A getFirst() {
        return this.first;
    }

    /**
     * Returns the second type.
     *
     * @return the second type
     */
    public final B getSecond() {
        return this.second;
    }

    @Override
    public final String toString() {
        return MoreObjects.toStringHelper(this)
                .add("first", this.first)
                .add("second", this.second)
                .toString();
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(this.first, this.second);
    }

    @SuppressWarnings("rawtypes")
    @Override
    public boolean equals(final Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null || getClass() != obj.getClass()) {
            return false;
        }
        final Pair other = (Pair) obj;
        return Objects.equal(this.first, other.first)
                && Objects.equal(this.second, other.second);
    }
}
