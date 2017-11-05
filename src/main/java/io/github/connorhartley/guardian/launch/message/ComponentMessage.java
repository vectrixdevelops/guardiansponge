package io.github.connorhartley.guardian.launch.message;

public interface ComponentMessage {

    long getTime();

    String getReason();

    Object getSource();

}
