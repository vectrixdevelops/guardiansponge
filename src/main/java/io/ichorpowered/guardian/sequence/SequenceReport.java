package io.ichorpowered.guardian.sequence;

import com.ichorpowered.guardian.api.event.origin.Origin;
import com.ichorpowered.guardian.api.report.Report;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.HashMap;
import java.util.Set;

public class SequenceReport implements Report {

    private final HashMap<String, Object> reportMap = new HashMap<>();
    private final boolean pass;
    private final Origin origin;

    public SequenceReport(boolean pass, @Nonnull Origin origin) {
        this.pass = pass;
        this.origin = origin;
    }

    public boolean isPassed() {
        return this.pass;
    }

    @Override
    public <T> void put(@Nonnull String key, @Nullable T object) {
        this.reportMap.put(key, object);
    }

    @Nullable
    @Override
    @SuppressWarnings("unchecked")
    public <T> T get(@Nonnull String key) throws IllegalArgumentException {
        return (T) this.reportMap.get(key);
    }

    @Nonnull
    @Override
    public Set<String> keySet() {
        return this.reportMap.keySet();
    }

    @Nonnull
    @Override
    public Origin getOrigin() {
        return this.origin;
    }

}
