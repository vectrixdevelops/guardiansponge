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
package com.ichorpowered.guardian.content.transaction;

import com.ichorpowered.guardian.content.EmptyContentContainer;
import com.ichorpowered.guardianapi.content.ContentContainer;
import com.ichorpowered.guardianapi.content.ContentKeys;
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

    public static <B> GuardianSingleValue<B> empty() {
        return new GuardianSingleValue<>(ContentKeys.UNDEFINED, new EmptyContentContainer());
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
