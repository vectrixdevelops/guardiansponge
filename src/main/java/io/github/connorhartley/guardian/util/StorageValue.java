/*
 * MIT License
 *
 * Copyright (c) 2016 Connor Hartley
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
import io.github.connorhartley.guardian.util.database.DatabaseConnection;
import io.github.connorhartley.guardian.util.database.DatabaseQuery;
import ninja.leaping.configurate.ConfigurationNode;

import java.util.Optional;

public class StorageValue<T, K, V> {

    // TODO: Represents a storable value, referenced by a key.

    private K key;
    private K defaultKey;
    private V value;
    private V defaultValue;

    private String comment;

    private TypeToken<V> typeToken;

    private boolean modified;

    public StorageValue(K key, V value) {
        this(key, null, value);
    }

    public StorageValue(K key, String comment, V value) {
        this(key, comment, value, null);
    }

    public StorageValue(K key, String comment, V value, TypeToken<V> typeToken) {
        this.defaultKey = key;
        this.comment = comment;
        this.defaultValue = value;
        this.typeToken = typeToken;
    }

    public StorageValue<T, K, V> load(T storageDevice) {
        if (storageDevice instanceof  ConfigurationNode) {
            Optional<V> internalValue = getInternalValue((ConfigurationNode) storageDevice);
            internalValue.ifPresent(v -> {
                this.value = v;
                save(storageDevice);
            });
        }
        return this;
    }

    public StorageValue<T, K, V> save(T storageDevice) {
        if (storageDevice instanceof  ConfigurationNode) {
            setInternalValue((ConfigurationNode) storageDevice);
        }
        return this;
    }

    public boolean isModified() {
        return this.modified;
    }

    public void setModified(boolean modified) {
        this.modified = modified;
    }

    public K getKey() {
        return this.key;
    }

    public void setKey(K key) {
        this.key = key;
    }

    public Optional<String> getComment() {
        if (this.comment != null) {
            return Optional.of(this.comment);
        } else {
            return Optional.empty();
        }
    }

    public void setComment(String comment) {
        this.comment = comment;
    }

    public V getValue() {
        if (this.value == null) {
            this.value = this.defaultValue;
        }
        return this.value;
    }

    public void setValue(V value) {
        this.modified = true;
        this.value = value;
    }

    public TypeToken<V> getTypeToken() {
        return this.typeToken == null ? TypeToken.of((Class<V>) this.defaultValue.getClass()) : this.typeToken;
    }

    private <T extends ConfigurationNode> boolean setInternalValue(T storageDevice) {
        // Configuration stuff...
        return false;
    }

    private <T extends ConfigurationNode> Optional<V> getInternalValue(T storageDevice) {
        // Configuration stuff...
        return Optional.empty();
    }

    private <T extends DatabaseConnection> Optional<V> queryInternalValue(T storageDevice) {
        // Database stuff...
        return Optional.empty();
    }

}
