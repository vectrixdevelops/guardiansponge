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

import com.google.common.collect.ImmutableList;
import com.ichorpowered.guardianapi.content.ContentContainer;
import com.ichorpowered.guardianapi.content.transaction.ContentKey;
import com.ichorpowered.guardianapi.content.transaction.result.BatchValue;

import java.util.Collection;
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
