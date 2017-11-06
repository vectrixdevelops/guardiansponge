package io.github.connorhartley.guardian.util;

public class ElementProvider<T> {

    private T object = null;

    public ElementProvider() {}

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
