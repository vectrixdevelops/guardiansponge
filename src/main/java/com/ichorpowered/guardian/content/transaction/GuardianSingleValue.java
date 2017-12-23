package com.ichorpowered.guardian.content.transaction;

import com.ichorpowered.guardianapi.content.ContentContainer;
import com.ichorpowered.guardianapi.content.transaction.ContentKey;
import com.ichorpowered.guardianapi.content.transaction.result.SingleValue;

import java.util.Optional;

public class GuardianSingleValue<E> implements SingleValue<E> {

    private final ContentKey contentKey;

    private ContentContainer contentContainer;
    private boolean dirty = false;
    private E element;

    public GuardianSingleValue(final ContentKey contentKey, final ContentContainer contentContainer) {
        this.contentKey = contentKey;
        this.contentContainer = contentContainer;
    }

    public GuardianSingleValue<E> setDirty(final boolean dirty) {
        this.dirty = dirty;
        return this;
    }

    public GuardianSingleValue<E> setElement(final E element) {
        this.element = element;
        return this;
    }

    @Override
    public boolean isDirty() {
        return this.dirty;
    }

    @Override
    public ContentKey getKey() {
        return this.contentKey;
    }

    @Override
    public Optional<E> getElement() {
        return Optional.ofNullable(this.element);
    }

    @Override
    public Optional<ContentContainer> getOriginalContainer() {
        return Optional.ofNullable(this.contentContainer);
    }
}
