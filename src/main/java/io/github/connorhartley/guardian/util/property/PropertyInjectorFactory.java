package io.github.connorhartley.guardian.util.property;

public final class PropertyInjectorFactory {

    public PropertyInjectorFactory() {}

    public static PropertyInjector create(Object target) {
        return new PropertyInjector(target);
    }

}
