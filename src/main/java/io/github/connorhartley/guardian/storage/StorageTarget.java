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
package io.github.connorhartley.guardian.storage;

import io.github.connorhartley.guardian.storage.container.ConfigurationValue;

/**
 * Storage Target
 *
 * An annotation attached to a parameter or field to inject a {@link ConfigurationValue}
 * for by an appropriate {@link StorageProvider}.
 */
public @interface StorageTarget {

    /**
     * Key
     *
     * <p>The storage key that holds the {@link ConfigurationValue} you want to inject.</p>
     *
     * @return The storage key
     */
    String key();

    /**
     * Deep
     *
     * <p>If enabled, allows a recursive search through mapped objects to return
     * the {@link ConfigurationValue}. Disabled by default as it may have performance
     * implications.</p>
     *
     * @return The deep setting
     */
    boolean deep() default false;

    /**
     * Ordinal
     *
     * <p>The maximum depth to search through a map if deep is enabled.</p>
     *
     * @return The ordinal setting
     */
    int ordinal() default 1;

}
