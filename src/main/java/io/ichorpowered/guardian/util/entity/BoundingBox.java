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
package io.ichorpowered.guardian.util.entity;

import com.flowpowered.math.vector.Vector3d;
import io.ichorpowered.guardian.util.tuple.Quartet;

import java.util.Optional;

public class BoundingBox {

    private final Bound b1;
    private final Bound b2;

    public BoundingBox(Bound b1) {
        this(b1, null);
    }

    public BoundingBox(Bound b1, Bound b2) {
        this.b1 = b1;
        this.b2 = b2;
    }

    public Optional<Bound> getLowerBounds() {
        return Optional.ofNullable(this.b1);
    }

    public Optional<Bound> getUpperBounds() {
        return Optional.ofNullable(this.b2);
    }

    public static class Bound extends Quartet<Vector3d, Vector3d, Vector3d, Vector3d> {

        /**
         * Creates a new {@link Bound} made up
         * of two {@link Vector3d} offsets.
         *
         * @param a1 first vector offset
         * @param a2 second vector offset
         */
        public Bound(Vector3d a1, Vector3d a2) {
            super(a1, a1.add(Math.abs(a1.getX()), 0, 0), a2.add(-a2.getX(), 0, 0), a2);
        }
    }
}
