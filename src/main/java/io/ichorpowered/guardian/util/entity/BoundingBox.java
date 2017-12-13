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
