package io.github.connorhartley.guardian.util.property;

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

            final String fieldName = field.getName() + "$" + field.getType().getName();
            final String fieldAlias = point.alias();

            if (fieldAlias.equals("") || this.aliases.containsKey(point.alias())) {
                this.fields.put(fieldName, new PropertyContainer(field, fieldName, point.effectFinal()));
            } else {
                this.aliases.put(fieldAlias, fieldName);
                this.fields.put(fieldName, new PropertyContainer(field, fieldAlias, point.effectFinal()));
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

            if (propertyContainer.isEffectivelyFinal() && propertyContainer.getField().get(this.target) == null) {
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
        final boolean effectivelyFinal;

        PropertyContainer(final Field field, final String alias, final boolean effectivelyFinal) {
            this.type = field.getType();
            this.field = field;
            this.alias = alias;
            this.effectivelyFinal = effectivelyFinal;
        }

        public Field getField() {
            return this.field;
        }

        public String getAlias() {
            return this.alias;
        }

        public boolean isEffectivelyFinal() {
            return this.effectivelyFinal;
        }
    }

}
