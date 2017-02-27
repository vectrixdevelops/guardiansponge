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
package io.github.connorhartley.guardian.data.tag;

import io.github.connorhartley.guardian.data.DataKeys;
import io.github.connorhartley.guardian.detection.Offense;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.data.DataContainer;
import org.spongepowered.api.data.DataHolder;
import org.spongepowered.api.data.DataView;
import org.spongepowered.api.data.MemoryDataContainer;
import org.spongepowered.api.data.manipulator.DataManipulatorBuilder;
import org.spongepowered.api.data.manipulator.immutable.common.AbstractImmutableSingleData;
import org.spongepowered.api.data.manipulator.mutable.common.AbstractSingleData;
import org.spongepowered.api.data.merge.MergeFunction;
import org.spongepowered.api.data.persistence.AbstractDataBuilder;
import org.spongepowered.api.data.persistence.InvalidDataException;
import org.spongepowered.api.data.value.immutable.ImmutableValue;
import org.spongepowered.api.data.value.mutable.Value;

import java.time.LocalDateTime;
import java.util.Optional;

public class OffenseTagData extends AbstractSingleData<Offense, OffenseTagData, OffenseTagData.Immutable> {

    protected OffenseTagData(Offense value) {
        super(value, DataKeys.GUARDIAN_OFFENSE_TAG);
    }

    public Value<Offense> getOffense() {
        return this.getValueGetter();
    }

    @Override
    protected Value<Offense> getValueGetter() {
        return Sponge.getRegistry().getValueFactory().createValue(DataKeys.GUARDIAN_OFFENSE_TAG, getValue());
    }

    @Override
    public Optional<OffenseTagData> fill(DataHolder dataHolder, MergeFunction overlap) {
        Optional<OffenseTagData> offenseOptional = dataHolder.get(OffenseTagData.class);
        if (offenseOptional.isPresent()) {
            OffenseTagData offenseTagData = offenseOptional.get();
            OffenseTagData finalOffenseTagData = overlap.merge(this, offenseTagData);
            setValue(finalOffenseTagData.getValue());
        }
        return Optional.of(this);
    }

    @Override
    public OffenseTagData copy() {
        return new OffenseTagData(this.getValue());
    }

    @Override
    public Optional<OffenseTagData> from(DataContainer container) {
        if (container.contains(DataKeys.GUARDIAN_OFFENSE_TAG.getQuery())) {
            return Optional.of(set(DataKeys.GUARDIAN_OFFENSE_TAG, (Offense) container.get(DataKeys.GUARDIAN_OFFENSE_TAG.getQuery()).orElse(this.getValue())));
        }
        return Optional.empty();
    }

    @Override
    public DataContainer toContainer() {
        return new MemoryDataContainer().set(DataKeys.GUARDIAN_OFFENSE_TAG, this.getValue());
    }

    @Override
    public Immutable asImmutable() {
        return new Immutable(this.getValue());
    }

    @Override
    public int getContentVersion() {
        return 1;
    }

    public static class Immutable extends AbstractImmutableSingleData<Offense, Immutable, OffenseTagData> {

        protected Immutable(Offense value) {
            super(value, DataKeys.GUARDIAN_OFFENSE_TAG);
        }

        public ImmutableValue<Offense> getOffense() {
            return this.getValueGetter();
        }

        @Override
        protected ImmutableValue<Offense> getValueGetter() {
            return Sponge.getRegistry().getValueFactory().createValue(DataKeys.GUARDIAN_OFFENSE_TAG, this.getValue()).asImmutable();
        }

        @Override
        public OffenseTagData asMutable() {
            return new OffenseTagData(this.getValue());
        }

        @Override
        public int getContentVersion() {
            return 1;
        }
    }

    public static class Builder extends AbstractDataBuilder<OffenseTagData> implements DataManipulatorBuilder<OffenseTagData, Immutable> {


        public Builder() {
            super(OffenseTagData.class, 1);
        }

        @Override
        public OffenseTagData create() {
            Offense emptyOffense = null;
            try {
                emptyOffense = new Offense.Builder().dateAndTime(LocalDateTime.now()).severity(0).build();
            } catch (Exception e) {
                e.printStackTrace();
            }
            return new OffenseTagData(emptyOffense);
        }

        public OffenseTagData createFrom(Offense offense) {
            return new OffenseTagData(offense);
        }

        @Override
        public Optional<OffenseTagData> createFrom(DataHolder dataHolder) {
            return this.create().fill(dataHolder);
        }

        @Override
        protected Optional<OffenseTagData> buildContent(DataView container) throws InvalidDataException {
            if (!container.contains(DataKeys.GUARDIAN_OFFENSE_TAG.getQuery())) {
                return Optional.empty();
            }

            Offense offense = container.getObject(DataKeys.GUARDIAN_OFFENSE_TAG.getQuery(), Offense.class).get();

            return Optional.of(new OffenseTagData(offense));
        }
    }

}
