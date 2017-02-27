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

import com.google.common.collect.ImmutableList;
import io.github.connorhartley.guardian.data.DataKeys;
import io.github.connorhartley.guardian.sequence.Sequence;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.data.DataContainer;
import org.spongepowered.api.data.DataHolder;
import org.spongepowered.api.data.DataView;
import org.spongepowered.api.data.MemoryDataContainer;
import org.spongepowered.api.data.key.Key;
import org.spongepowered.api.data.manipulator.DataManipulatorBuilder;
import org.spongepowered.api.data.manipulator.immutable.common.AbstractImmutableListData;
import org.spongepowered.api.data.manipulator.immutable.common.AbstractImmutableSingleData;
import org.spongepowered.api.data.manipulator.mutable.ListData;
import org.spongepowered.api.data.manipulator.mutable.common.AbstractListData;
import org.spongepowered.api.data.manipulator.mutable.common.AbstractSingleData;
import org.spongepowered.api.data.merge.MergeFunction;
import org.spongepowered.api.data.persistence.AbstractDataBuilder;
import org.spongepowered.api.data.persistence.InvalidDataException;
import org.spongepowered.api.data.value.BaseValue;
import org.spongepowered.api.data.value.immutable.ImmutableValue;
import org.spongepowered.api.data.value.mutable.ListValue;
import org.spongepowered.api.data.value.mutable.Value;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.Set;

public class SequenceHandlerData extends AbstractListData<Sequence, SequenceHandlerData, SequenceHandlerData.Immutable> {


    public SequenceHandlerData() {
        this(ImmutableList.of());
    }

    public SequenceHandlerData(List<Sequence> value) {
        super(value, DataKeys.GUARDIAN_SEQUENCE_HANDLER);
    }

    @Override
    public Optional<SequenceHandlerData> fill(DataHolder dataHolder, MergeFunction overlap) {
        SequenceHandlerData merged = overlap.merge(this, dataHolder.get(SequenceHandlerData.class).orElse(null));
        this.setValue(merged.getListValue().get());

        return Optional.of(this);
    }

    @Override
    public Optional<SequenceHandlerData> from(DataContainer container) {
        if (!container.contains(DataKeys.GUARDIAN_SEQUENCE_HANDLER)) {
            return Optional.empty();
        }

        List<Sequence> sequences = container.getObjectList(DataKeys.GUARDIAN_SEQUENCE_HANDLER.getQuery(), Sequence.class).get();
        return Optional.of(this.setValue(sequences));
    }

    @Override
    public SequenceHandlerData copy() {
        return new SequenceHandlerData(getValue());
    }

    @Override
    public Immutable asImmutable() {
        return new Immutable(getValue());
    }

    @Override
    public int getContentVersion() {
        return Builder.CONTENT_VERSION;
    }

    public static class Immutable extends AbstractImmutableListData<Sequence, Immutable, SequenceHandlerData> {


        public Immutable(List<Sequence> value) {
            super(value, DataKeys.GUARDIAN_SEQUENCE_HANDLER);
        }

        @Override
        public SequenceHandlerData asMutable() {
            return new SequenceHandlerData(getValue());
        }

        @Override
        public int getContentVersion() {
            return Builder.CONTENT_VERSION;
        }
    }

    public static class Builder extends AbstractDataBuilder<SequenceHandlerData> implements DataManipulatorBuilder<SequenceHandlerData, Immutable> {

        private static final int CONTENT_VERSION = 1;

        public Builder() {
            super(SequenceHandlerData.class, CONTENT_VERSION);
        }

        @Override
        public SequenceHandlerData create() {
            return new SequenceHandlerData();
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
            if (!container.contains(DataKeys.GUARDIAN_SEQUENCE_HANDLER.getQuery())) {
                return Optional.empty();
            }

            List<Sequence> sequences = container.getObjectList(DataKeys.GUARDIAN_SEQUENCE_HANDLER.getQuery(), Sequence.class).get();
            return Optional.of(new SequenceHandlerData(sequences));
        }
    }
}
