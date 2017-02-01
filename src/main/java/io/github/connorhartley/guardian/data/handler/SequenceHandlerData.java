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
package io.github.connorhartley.guardian.data.handler;

import io.github.connorhartley.guardian.data.Keys;
import io.github.connorhartley.guardian.sequence.Sequence;
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

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class SequenceHandlerData extends AbstractSingleData<List<Sequence>, SequenceHandlerData, SequenceHandlerData.Immutable> {

    protected SequenceHandlerData(List<Sequence> value) {
        super(value, Keys.GUARDIAN_SEQUENCE_HANDLER);
    }

    public Value<List<Sequence>> getHandledSequences() {
        return this.getValueGetter();
    }

    @Override
    protected Value<List<Sequence>> getValueGetter() {
        return Sponge.getRegistry().getValueFactory().createValue(Keys.GUARDIAN_SEQUENCE_HANDLER, this.getValue());
    }

    @Override
    public Optional<SequenceHandlerData> fill(DataHolder dataHolder, MergeFunction overlap) {
        Optional<SequenceHandlerData> offenseOptional = dataHolder.get(SequenceHandlerData.class);
        if (offenseOptional.isPresent()) {
            SequenceHandlerData sequenceHandlerData = offenseOptional.get();
            SequenceHandlerData finalSequenceHandlerData = overlap.merge(this, sequenceHandlerData);
            setValue(finalSequenceHandlerData.getValue());
        }
        return Optional.of(this);
    }

    @Override
    public SequenceHandlerData copy() {
        return new SequenceHandlerData(this.getValue());
    }

    @Override
    public Optional<SequenceHandlerData> from(DataContainer container) {
        if (container.contains(Keys.GUARDIAN_SEQUENCE_HANDLER.getQuery())) {
            return Optional.of(set(Keys.GUARDIAN_SEQUENCE_HANDLER, container.getObjectList(Keys.GUARDIAN_SEQUENCE_HANDLER.getQuery(), Sequence.class).orElse(this.getValue())));
        }
        return Optional.empty();
    }

    @Override
    public DataContainer toContainer() {
        return new MemoryDataContainer().set(Keys.GUARDIAN_SEQUENCE_HANDLER, this.getValue());
    }

    @Override
    public Immutable asImmutable() {
        return new Immutable(this.getValue());
    }

    @Override
    public int getContentVersion() {
        return 1;
    }

    public static class Immutable extends AbstractImmutableSingleData<List<Sequence>, Immutable, SequenceHandlerData> {

        protected Immutable(List<Sequence> value) {
            super(value, Keys.GUARDIAN_SEQUENCE_HANDLER);
        }

        public ImmutableValue<List<Sequence>> getImmutableHandledSequences() {
            return this.getValueGetter();
        }

        @Override
        protected ImmutableValue<List<Sequence>> getValueGetter() {
            return Sponge.getRegistry().getValueFactory().createValue(Keys.GUARDIAN_SEQUENCE_HANDLER, this.getValue()).asImmutable();
        }

        @Override
        public SequenceHandlerData asMutable() {
            return new SequenceHandlerData(this.getValue());
        }

        @Override
        public int getContentVersion() {
            return 1;
        }
    }

    public static class Builder extends AbstractDataBuilder<SequenceHandlerData> implements DataManipulatorBuilder<SequenceHandlerData, Immutable> {

        public Builder() {
            super(SequenceHandlerData.class, 1);
        }

        @Override
        public SequenceHandlerData create() {
            return new SequenceHandlerData(new ArrayList<>());
        }

        public SequenceHandlerData createFrom(List<Sequence> sequences) {
            return new SequenceHandlerData(sequences);
        }

        @Override
        public Optional<SequenceHandlerData> createFrom(DataHolder dataHolder) {
            return this.create().fill(dataHolder);
        }

        @Override
        protected Optional<SequenceHandlerData> buildContent(DataView container) throws InvalidDataException {
            if (!container.contains(Keys.GUARDIAN_SEQUENCE_HANDLER.getQuery())) {
                return Optional.empty();
            }

            Optional<List<Sequence>> sequences = container.getObjectList(Keys.GUARDIAN_SEQUENCE_HANDLER.getQuery(), Sequence.class);
            return sequences.map(SequenceHandlerData::new);
        }
    }
}
