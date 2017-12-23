package com.ichorpowered.guardian.content.transaction;

import com.google.common.collect.ImmutableList;
import com.ichorpowered.guardianapi.content.ContentContainer;
import com.ichorpowered.guardianapi.content.transaction.ContentKey;
import com.ichorpowered.guardianapi.content.transaction.result.BatchValue;

import java.util.List;
import java.util.Optional;

public class GuardianBatchValue implements BatchValue {

    private final List<ContentKey> contentKeys;

    private ContentContainer contentContainer;
    private boolean dirty = false;
    private List<?> elements;

    public GuardianBatchValue(final List<ContentKey> keys, final ContentContainer contentContainer) {
        this.contentKeys = ImmutableList.copyOf(keys);
        this.contentContainer = contentContainer;
    }

    public GuardianBatchValue setDirty(final boolean dirty) {
        this.dirty = dirty;
        return this;
    }

    public GuardianBatchValue setElements(final List<?> elements) {
        this.elements = ImmutableList.copyOf(elements);
        return this;
    }

    @Override
    public boolean isDirty() {
        return this.dirty;
    }

    @Override
    public List<ContentKey> getKeys() {
        return this.contentKeys;
    }

    @Override
    public List<?> getElements() {
        return this.elements;
    }

    @Override
    public Optional<ContentContainer> getOriginalContainer() {
        return Optional.ofNullable(this.contentContainer);
    }
}
