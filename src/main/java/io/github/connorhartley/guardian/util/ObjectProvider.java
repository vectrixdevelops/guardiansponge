package io.github.connorhartley.guardian.util;

public class ObjectProvider<T> {

    private T object = null;

    public ObjectProvider() {}

    public final T get() {
        return this.object;
    }

    public boolean provide(final T object) {
        if (this.isPresent()) return false;

        this.object = object;
        return true;
    }

    public boolean isPresent() {
        return this.object != null;
    }

}
