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
package com.ichorpowered.guardian.sequence.capture;

import com.google.common.reflect.TypeToken;
import com.ichorpowered.guardianapi.detection.capture.CaptureKey;
import com.ichorpowered.guardianapi.detection.capture.CaptureKeyBuilder;
import com.ichorpowered.guardianapi.util.item.value.BaseValue;

public class GuardianCaptureKey<V extends BaseValue<?>> implements CaptureKey<V> {

    private final String id;
    private final String name;
    private final V defaultValue;
    private final TypeToken<?> elementType;

    public GuardianCaptureKey(final Builder<V> builder) {
        this.id = builder.id;
        this.name = builder.name;
        this.defaultValue = builder.defaultValue;
        this.elementType = builder.elementType;
    }

    public static <A extends BaseValue<?>> Builder<A> builder() {
        return new GuardianCaptureKey.Builder<>();
    }

    @Override
    public V getDefaultValue() {
        return this.defaultValue;
    }

    @Override
    public TypeToken<?> getElementToken() {
        return this.elementType;
    }

    @Override
    public String getId() {
        return this.id;
    }

    @Override
    public String getName() {
        return this.name;
    }

    public static class Builder<V extends BaseValue<?>> implements CaptureKeyBuilder<V> {

        private String id;
        private String name;
        private V defaultValue;
        private TypeToken<?> elementType;

        @Override
        public Builder<V> id(String id) {
            this.id = id;
            return this;
        }

        @Override
        public Builder<V> name(String name) {
            this.name = name;
            return this;
        }

        @Override
        public Builder<V> type(V defaultValue, TypeToken<?> elementToken) {
            this.defaultValue = defaultValue;
            this.elementType = elementToken;
            return this;
        }

        @Override
        public GuardianCaptureKey<V> build() {
            return new GuardianCaptureKey<>(this);
        }
    }
}
