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
import ninja.leaping.configurate.ConfigurationNode;
import ninja.leaping.configurate.commented.CommentedConfigurationNode;
import ninja.leaping.configurate.objectmapping.ObjectMappingException;

public class StorageValue<K, E> {

    private StorageKey<K> key;
    private String comment;
    private E defaultValue;
    private E value;

    private TypeToken<E> valueTypeToken;

    private boolean modified;

    public StorageValue(StorageKey<K> key, String comment, E value) {
        this(key, comment, value, null);
    }

    public StorageValue(StorageKey<K> key, String comment, E value, TypeToken<E> valueTypeToken) {
        this.key = key;
        this.comment = comment;
        this.defaultValue = value;
        this.valueTypeToken = valueTypeToken;
    }

    /* Storage Handler Methods. */

    /**
     * Initializes the storage handler in whatever way is needed.
     *
     * @param storageHandler A handler of storage.
     * @param <T> A handler type.
     * @return This class.
     */
    public <T> StorageValue<K, E> createStorage(T storageHandler) {
        if (storageHandler instanceof ConfigurationNode) {
            this.value = this.getInternalValue(storageHandler);
            this.updateStorage(storageHandler);
        }
        return this;
    }

    /**
     * Load the storage from the storage handler.
     *
     * @param storageHandler A handler of storage.
     * @param <T> A handler type.
     * @return This class.
     */
    public <T> StorageValue<K, E> loadStorage(T storageHandler) {
        if (storageHandler instanceof ConfigurationNode) {
            this.value = this.getInternalValue(storageHandler);
            this.updateStorage(storageHandler);
        }
        return this;
    }

    /**
     * Update the storage from the storage handler.
     *
     * @param storageHandler A handler of storage.
     * @param <T> A handler type.
     * @return This class.
     */
    public <T> StorageValue<K, E> updateStorage(T storageHandler) {
        if (storageHandler instanceof ConfigurationNode) {
            this.setInternalValue(storageHandler);
        }
        return this;
    }

    /* Storage methods. */

    public StorageKey<K> getKey() {
        return this.key;
    }

    public void transformKey(ValueTransform<K> transform) {
        this.key.set(transform.transform(this.key.get()));
    }

    public void setKey(K key) {
        this.key.set(key);
    }

    public String getComment() {
        return this.comment;
    }

    public void setComment(String comment) {
        this.comment = comment;
    }

    public E getValue() {
        if (this.value == null) {
            this.value = this.defaultValue;
        }
        return this.value;
    }

    public void transformValue(ValueTransform<E> transform) {
        this.modified = true;
        this.value = transform.transform(this.getValue());
    }

    public void setValue() {
        this.modified = true;
        this.value = value;
    }

    public TypeToken<E> getValueTypeToken() {
        return this.valueTypeToken == null ? TypeToken.of((Class<E>) this.defaultValue.getClass()) : this.valueTypeToken;
    }

    public boolean isModified() {
        return this.modified;
    }

    public void setModified(boolean modified) {
        this.modified = modified;
    }

    /* Internal Storage Values */

    private <T> void setInternalValue(T storageHandler) {
        if (storageHandler instanceof ConfigurationNode) {
            ConfigurationNode node = ((ConfigurationNode) storageHandler).getNode(this.key.get());
            if (this.comment != null && node instanceof CommentedConfigurationNode) {
                ((CommentedConfigurationNode) node).setComment(this.comment);
            }

            if (this.modified) {
                if (this.valueTypeToken != null) {
                    try {
                        node.setValue(this.valueTypeToken, this.value);
                    } catch (ObjectMappingException e) {
                        e.printStackTrace();
                    }
                } else {
                    node.setValue(this.value);
                }
                this.modified = false;
            }
        }
    }

    private <T> E getInternalValue(T storageHandler) {
        if (storageHandler instanceof ConfigurationNode) {
            ConfigurationNode node = ((ConfigurationNode) storageHandler).getNode(this.key.get());

            if (node.isVirtual()) {
                this.modified = true;
            }

            if (this.comment != null && node instanceof CommentedConfigurationNode) {
                ((CommentedConfigurationNode) node).setComment(this.comment);
            }

            try {
                if (this.valueTypeToken != null) {
                    return node.getValue(this.valueTypeToken, this.defaultValue);
                } else {
                    return node.getValue(new TypeToken<E>(this.defaultValue.getClass()) {}, this.defaultValue);
                }
            } catch (Exception e) {
                return this.defaultValue;
            }
        }
        return null;
    }

    public void serializeDefault(ConfigurationNode configurationNode) {
        try {
            if (valueTypeToken != null) {
                configurationNode.setValue(valueTypeToken, this.defaultValue);
            } else {
                configurationNode.setValue(this.defaultValue);
            }
        } catch (ObjectMappingException e) {
            e.printStackTrace();
        }
    }

}
