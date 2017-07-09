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
import io.github.connorhartley.guardian.detection.punishment.PunishmentMixin;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.data.DataContainer;
import org.spongepowered.api.data.DataHolder;
import org.spongepowered.api.data.DataView;
import org.spongepowered.api.data.MemoryDataContainer;
import org.spongepowered.api.data.manipulator.DataManipulatorBuilder;
import org.spongepowered.api.data.manipulator.immutable.common.AbstractImmutableListData;
import org.spongepowered.api.data.manipulator.mutable.common.AbstractListData;
import org.spongepowered.api.data.merge.MergeFunction;
import org.spongepowered.api.data.persistence.AbstractDataBuilder;
import org.spongepowered.api.data.persistence.InvalidDataException;
import org.spongepowered.api.data.value.mutable.ListValue;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class PunishmentTagData extends AbstractListData<PunishmentMixin, PunishmentTagData, PunishmentTagData.Immutable> {

    protected PunishmentTagData(List<PunishmentMixin> value) {
        super(value, DataKeys.GUARDIAN_PUNISHMENT_TAG);
    }

    @Override
    protected ListValue<PunishmentMixin> getValueGetter() {
        return Sponge.getRegistry().getValueFactory().createListValue(DataKeys.GUARDIAN_PUNISHMENT_TAG, getValue());
    }

    @Override
    public Optional<PunishmentTagData> fill(DataHolder dataHolder, MergeFunction overlap) {
        Optional<PunishmentTagData> punishmentOptional = dataHolder.get(PunishmentTagData.class);
        if (punishmentOptional.isPresent()) {
            PunishmentTagData punishmentTagData = punishmentOptional.get();
            PunishmentTagData finalPunishmentTagData = overlap.merge(this, punishmentTagData);
            setValue(finalPunishmentTagData.getValue());
        }
        return Optional.of(this);
    }

    @Override
    public Optional<PunishmentTagData> from(DataContainer container) {
        if (container.contains(DataKeys.GUARDIAN_PUNISHMENT_TAG.getQuery())) {
            return Optional.of(set(DataKeys.GUARDIAN_PUNISHMENT_TAG,
                    (List<PunishmentMixin>) container.get(DataKeys.GUARDIAN_PUNISHMENT_TAG.getQuery()).orElse(this.getValue())));
        }
        return Optional.empty();
    }

    @Override
    public PunishmentTagData copy() {
        return new PunishmentTagData(this.getValue());
    }

    @Override
    public DataContainer toContainer() {
        return new MemoryDataContainer().set(DataKeys.GUARDIAN_PUNISHMENT_TAG, this.getValue());
    }

    @Override
    public Immutable asImmutable() {
        return new Immutable(this.getValue());
    }

    @Override
    public int getContentVersion() {
        return 1;
    }

    public static class Immutable extends AbstractImmutableListData<PunishmentMixin, Immutable, PunishmentTagData> {

        protected Immutable(List<PunishmentMixin> value) {
            super(value, DataKeys.GUARDIAN_PUNISHMENT_TAG);
        }

        @Override
        public PunishmentTagData asMutable() {
            return new PunishmentTagData(this.getValue());
        }

        @Override
        public int getContentVersion() {
            return 1;
        }
    }

    public static class Builder extends AbstractDataBuilder<PunishmentTagData> implements DataManipulatorBuilder<PunishmentTagData, Immutable> {

        public Builder() {
            super(PunishmentTagData.class, 1);
        }

        @Override
        public PunishmentTagData create() {
            List<PunishmentMixin> punishmentList = new ArrayList<>();

            return new PunishmentTagData(punishmentList);
        }

        @Override
        public Optional<PunishmentTagData> createFrom(DataHolder dataHolder) {
            return this.create().fill(dataHolder);
        }

        @Override
        protected Optional<PunishmentTagData> buildContent(DataView container) throws InvalidDataException {
            if (!container.contains(DataKeys.GUARDIAN_PUNISHMENT_TAG.getQuery())) {
                return Optional.empty();
            }

            List<PunishmentMixin> punishmentMixin = container.getObjectList(DataKeys.GUARDIAN_PUNISHMENT_TAG.getQuery(), PunishmentMixin.class).get();

            return Optional.of(new PunishmentTagData(punishmentMixin));
        }
    }
}
