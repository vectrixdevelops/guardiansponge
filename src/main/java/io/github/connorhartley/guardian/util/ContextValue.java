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
package io.github.connorhartley.guardian.util;

import com.google.common.reflect.TypeToken;
import io.github.connorhartley.guardian.util.context.ValueTransform;

public class ContextValue {

    private Object value;

    public ContextValue() {}

    public <E> ContextValue set(E value) {
        this.value = value;
        return this;
    }

    public <E> E get() {
        return ((E) this.value);
    }

    public <E> E modify(ValueTransform<E> valueTransform) {
        this.value = valueTransform.transform((E) this.value);
        return (E) this.value;
    }

    public <E> ContextValue transform(ValueTransform<E> valueTransform) {
        this.value = valueTransform.transform((E) this.value);
        return this;
    }

    public <E> TypeToken<E> getTypeToken() {
        return TypeToken.of((Class<E>) this.value.getClass());
    }

}
