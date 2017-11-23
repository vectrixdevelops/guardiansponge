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
package io.ichorpowered.guardian.util.property;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.Map;

public class PropertyInjector {

    private final Logger LOGGER = LoggerFactory.getLogger(PropertyInjector.class);
    private final Object target;
    private final Map<String, String> aliases = new HashMap<>();
    private final Map<String, PropertyContainer> fields = new HashMap<>();

    PropertyInjector(Object target) {
        this.target = target;

        for (final Field field : this.target.getClass().getDeclaredFields()) {
            final Property point = field.getAnnotation(Property.class);
            if (point == null) continue;

            final String fieldName = field.getName();
            final String fieldAlias = point.alias();

            if (fieldAlias.equals("") || this.aliases.containsKey(point.alias())) {
                this.fields.put(fieldName, new PropertyContainer(field, fieldName, point.modifier()));
            } else {
                this.aliases.put(fieldAlias, fieldName);
                this.fields.put(fieldName, new PropertyContainer(field, fieldAlias, point.modifier()));
            }
        }
    }

    public <T> PropertyInjector inject(final String name, T object) {
        if (this.aliases.containsKey(name)) {
            final PropertyContainer propertyContainer = this.fields.get(this.aliases.get(name));

            this.inject(name, propertyContainer, object);
        } else if (this.fields.containsKey(name)) {
            final PropertyContainer propertyContainer = this.fields.get(name);

            this.inject(name, propertyContainer, object);
        } else {
            LOGGER.warn("Skipping unknown injection field: {}", name);
        }

        return this;
    }

    <T> void inject(final String name, final PropertyContainer propertyContainer, final T object) {
        try {
            propertyContainer.getField().setAccessible(true);

            if (propertyContainer.getModifier().equals(PropertyModifier.FINAL) && propertyContainer.getField().get(this.target) == null) {
                propertyContainer.getField().set(this.target, object);
            } else {
                propertyContainer.getField().set(this.target, object);
            }
        } catch (IllegalAccessException e) {
            LOGGER.error("Failed to inject field: " + name);
        }
    }

    static class PropertyContainer {

        final Class<?> type;
        final Field field;
        final String alias;
        final PropertyModifier propertyModifier;

        PropertyContainer(final Field field, final String alias, final PropertyModifier propertyModifier) {
            this.type = field.getType();
            this.field = field;
            this.alias = alias;
            this.propertyModifier = propertyModifier;
        }

        public Field getField() {
            return this.field;
        }

        public String getAlias() {
            return this.alias;
        }

        public PropertyModifier getModifier() {
            return this.propertyModifier;
        }
    }

}
