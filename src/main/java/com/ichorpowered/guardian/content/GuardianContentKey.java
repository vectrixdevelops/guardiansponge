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
package com.ichorpowered.guardian.content;

import com.google.common.collect.Sets;
import com.google.common.reflect.TypeToken;
import com.ichorpowered.guardianapi.content.transaction.ContentAssignment;
import com.ichorpowered.guardianapi.content.transaction.ContentKey;
import com.ichorpowered.guardianapi.content.transaction.ContentKeyBuilder;

import java.util.Set;

public class GuardianContentKey<T> implements ContentKey<T> {

    private final String id;
    private final String name;
    private final TypeToken<T> elementType;
    private final Set<? extends ContentAssignment<?>> assignments;

    public GuardianContentKey(final Builder builder) {
        this.id = builder.id;
        this.name = builder.name;
        this.elementType = builder.elementType;
        this.assignments = builder.assignments;
    }

    public static <A> Builder<A> builder() {
        return new GuardianContentKey.Builder<>();
    }

    @Override
    public String getId() {
        return this.id;
    }

    @Override
    public String getName() {
        return this.name;
    }

    @Override
    public TypeToken<T> getElementType() {
        return this.elementType;
    }

    @Override
    public Set<? extends ContentAssignment<?>> getAssignments() {
        return this.assignments;
    }

    public static class Builder<B> implements ContentKeyBuilder<B> {

        private String id;
        private String name;
        private TypeToken<?> elementType;
        private Set<ContentAssignment<?>> assignments = Sets.newHashSet();

        @Override
        public ContentKeyBuilder<B> id(String id) {
            this.id = id;
            return this;
        }

        @Override
        public ContentKeyBuilder<B> name(String name) {
            this.name = name;
            return this;
        }

        @Override
        public ContentKeyBuilder<B> element(TypeToken<B> typeToken) {
            this.elementType = typeToken;
            return this;
        }

        @Override
        public ContentKeyBuilder<B> assignment(ContentAssignment<?> assignment) {
            this.assignments.add(assignment);
            return this;
        }

        @Override
        public ContentKey<B> build() {
            return new GuardianContentKey<>(this);
        }
    }
}
