package io.github.connorhartley.guardian.launch.message;

public interface FacetMessage {

    long getTime();

    String getReason();

    Object getSource();

}
