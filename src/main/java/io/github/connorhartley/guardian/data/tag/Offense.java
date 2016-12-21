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
package io.github.connorhartley.guardian.data.tag;

import io.github.connorhartley.guardian.violation.offense.OffenseType;
import org.spongepowered.api.data.DataContainer;
import org.spongepowered.api.data.DataHolder;
import org.spongepowered.api.data.DataView;
import org.spongepowered.api.data.key.Key;
import org.spongepowered.api.data.manipulator.DataManipulatorBuilder;
import org.spongepowered.api.data.manipulator.immutable.common.AbstractImmutableSingleData;
import org.spongepowered.api.data.manipulator.mutable.common.AbstractSingleData;
import org.spongepowered.api.data.merge.MergeFunction;
import org.spongepowered.api.data.persistence.AbstractDataBuilder;
import org.spongepowered.api.data.persistence.InvalidDataException;
import org.spongepowered.api.data.value.BaseValue;
import org.spongepowered.api.data.value.immutable.ImmutableValue;
import org.spongepowered.api.data.value.mutable.Value;

import java.util.Optional;

public class Offense extends AbstractSingleData<OffenseType, Offense, Offense.Immutable> {

    protected Offense(OffenseType value, Key<? extends BaseValue<OffenseType>> usedKey) {
        super(value, usedKey);
    }

    @Override
    protected Value<?> getValueGetter() {
        return null;
    }

    @Override
    public Optional<Offense> fill(DataHolder dataHolder, MergeFunction overlap) {
        return null;
    }

    @Override
    public Optional<Offense> from(DataContainer container) {
        return null;
    }

    @Override
    public Offense copy() {
        return null;
    }

    @Override
    public Immutable asImmutable() {
        return null;
    }

    @Override
    public int getContentVersion() {
        return 0;
    }

    public static class Immutable extends AbstractImmutableSingleData<OffenseType, Immutable, Offense> {

        protected Immutable(OffenseType value, Key<? extends BaseValue<OffenseType>> usedKey) {
            super(value, usedKey);
        }

        @Override
        protected ImmutableValue<?> getValueGetter() {
            return null;
        }

        @Override
        public Offense asMutable() {
            return null;
        }

        @Override
        public int getContentVersion() {
            return 0;
        }
    }

    public static class Builder extends AbstractDataBuilder<Offense> implements DataManipulatorBuilder<Offense, Immutable> {


        protected Builder(Class<Offense> requiredClass, int supportedVersion) {
            super(requiredClass, supportedVersion);
        }

        @Override
        public Offense create() {
            return null;
        }

        @Override
        public Optional<Offense> createFrom(DataHolder dataHolder) {
            return null;
        }

        @Override
        protected Optional<Offense> buildContent(DataView container) throws InvalidDataException {
            return null;
        }
    }

}
