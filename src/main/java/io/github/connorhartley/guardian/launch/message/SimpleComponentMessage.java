package io.github.connorhartley.guardian.launch.message;

public class SimpleComponentMessage implements ComponentMessage {

    private final long time;
    private final String reason;
    private final Object source;

    public SimpleComponentMessage(long time, String reason, Object source) {
        this.time = time;
        this.reason = reason;
        this.source = source;
    }

    @Override
    public long getTime() {
        return this.time;
    }

    @Override
    public String getReason() {
        return this.reason;
    }

    @Override
    public Object getSource() {
        return this.source;
    }
}
