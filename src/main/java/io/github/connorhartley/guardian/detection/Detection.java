package io.github.connorhartley.guardian.detection;

/**
 * Detection
 *
 * Represents a cheat / hack / exploit module that is loaded
 * by the module registry.
 */
public abstract class Detection {

    // MODULE : Loading

    public abstract void onConstruction();

    public abstract void onDeconstruction();

    // MODULE : Status

    public abstract boolean isReady();

}
