package io.github.connorhartley.guardian.launch;

public enum ComponentState {

    /**
     * Component is preparing to start.
     */
    PREPARE,

    /**
     * Component has started.
     */
    START,

    /**
     * Component has stopped.
     */
    STOP,

    /**
     * Component has errored and is awaiting further instructions.
     */
    ERROR

}
