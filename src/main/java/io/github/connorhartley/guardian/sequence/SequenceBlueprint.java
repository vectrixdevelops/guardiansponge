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
package io.github.connorhartley.guardian.sequence;

import io.github.connorhartley.guardian.detection.check.Check;
import org.spongepowered.api.entity.living.player.Player;

/**
 * Sequence Blueprint
 *
 * Represents an abstract way to create a {@link Sequence}
 * from a {@link SequenceBuilder}.
 */
public abstract class SequenceBlueprint {

    private final Check check;

    public SequenceBlueprint(Check check) {
        this.check = check;
    }

    /**
     * Create
     *
     * <p>An abstract method to create a {@link Sequence}.</p>
     *
     * @param player The player to create the sequence for
     * @return The sequence for the player
     */
    public abstract Sequence create(Player player);

    /**
     * Get Check
     *
     * <p>Returns the {@link Check} for this {@link SequenceBlueprint}.</p>
     *
     * @return The check type
     */
    public Check getCheck() {
        return this.check;
    }

    @Override
    public boolean equals(Object object) {
        if (object instanceof Sequence) {
            return ((Sequence) object).getCheck().equals(this.check);
        } else if (object instanceof SequenceBlueprint) {
            return ((SequenceBlueprint) object).getCheck().equals(this.check);
        }

        return false;
    }

}
