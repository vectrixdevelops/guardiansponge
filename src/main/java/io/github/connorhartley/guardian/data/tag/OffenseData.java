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

import io.github.connorhartley.guardian.data.Keys;
import io.github.connorhartley.guardian.violation.Offense;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.data.DataContainer;
import org.spongepowered.api.data.DataHolder;
import org.spongepowered.api.data.DataView;
import org.spongepowered.api.data.manipulator.DataManipulatorBuilder;
import org.spongepowered.api.data.manipulator.immutable.common.AbstractImmutableSingleData;
import org.spongepowered.api.data.manipulator.mutable.common.AbstractSingleData;
import org.spongepowered.api.data.merge.MergeFunction;
import org.spongepowered.api.data.persistence.AbstractDataBuilder;
import org.spongepowered.api.data.persistence.InvalidDataException;
import org.spongepowered.api.data.value.immutable.ImmutableValue;
import org.spongepowered.api.data.value.mutable.Value;

import java.util.Optional;

public class OffenseData extends AbstractSingleData<Offense, OffenseData, OffenseData.Immutable> {

    protected OffenseData(Offense value) {
        super(value, Keys.GUARDIAN_OFFENSE_TAG);
    }

    public Value<Offense> getOffense() {
        return getValueGetter();
    }

    @Override
    protected Value<Offense> getValueGetter() {
        return Sponge.getRegistry().getValueFactory().createValue(Keys.GUARDIAN_OFFENSE_TAG, getValue());
    }

    @Override
    public Optional<OffenseData> fill(DataHolder dataHolder, MergeFunction overlap) {
        Optional<OffenseData> offenseOptional = dataHolder.get(OffenseData.class);
        if (offenseOptional.isPresent()) {
            OffenseData offenseData = offenseOptional.get();
            OffenseData finalOffenseData = overlap.merge(this, offenseData);
        }
        return Optional.of(this);
    }

    @Override
    public Optional<OffenseData> from(DataContainer container) {
        if (container.contains(Keys.GUARDIAN_OFFENSE_TAG.getQuery())) {
            return Optional.of(set(Keys.GUARDIAN_OFFENSE_TAG, (Offense) container.get(Keys.GUARDIAN_OFFENSE_TAG.getQuery()).orElse(getValue())));
        }
        return Optional.empty();
    }

    @Override
    public OffenseData copy() {
        return new OffenseData(this.getValue());
    }

    @Override
    public Immutable asImmutable() {
        return new Immutable(this.getValue());
    }

    @Override
    public int getContentVersion() {
        return 1;
    }

    public static class Immutable extends AbstractImmutableSingleData<Offense, Immutable, OffenseData> {

        protected Immutable(Offense value) {
            super(value, Keys.GUARDIAN_OFFENSE_TAG);
        }

        public ImmutableValue<Offense> getOffense() {
            return getValueGetter();
        }

        @Override
        protected ImmutableValue<Offense> getValueGetter() {
            return Sponge.getRegistry().getValueFactory().createValue(Keys.GUARDIAN_OFFENSE_TAG, getValue()).asImmutable();
        }

        @Override
        public OffenseData asMutable() {
            return new OffenseData(this.getValue());
        }

        @Override
        public int getContentVersion() {
            return 1;
        }
    }

    public static class Builder extends AbstractDataBuilder<OffenseData> implements DataManipulatorBuilder<OffenseData, Immutable> {


        protected Builder(Class<OffenseData> requiredClass, int supportedVersion) {
            super(requiredClass, supportedVersion);
        }

        @Override
        public OffenseData create() {
            Offense emptyOffense = new Offense.Builder().setName("unknown").setDescription("Unknown offense.").setSeverity(0).build();
            return new OffenseData(emptyOffense);
        }

        public Optional<OffenseData> createFrom(Offense offense) {
            return Optional.of(new OffenseData(offense));
        }

        @Override
        public Optional<OffenseData> createFrom(DataHolder dataHolder) {
            return create().fill(dataHolder);
        }

        @Override
        protected Optional<OffenseData> buildContent(DataView container) throws InvalidDataException {
            if (!container.contains(Keys.GUARDIAN_OFFENSE_TAG.getQuery())) {
                return Optional.empty();
            }

            Offense offense = (Offense) container.get(Keys.GUARDIAN_OFFENSE_TAG.getQuery()).get();

            return Optional.of(new OffenseData(offense));
        }
    }

}
