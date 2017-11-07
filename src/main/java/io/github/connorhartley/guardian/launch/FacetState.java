package io.github.connorhartley.guardian.launch;

public enum FacetState {

    /**
     * Facet is preparing to start.
     */
    PREPARE,

    /**
     * Facet has started.
     */
    START,

    /**
     * Facet has stopped.
     */
    STOP,

    /**
     * Facet has errored and is awaiting further instructions.
     */
    ERROR

}
