/*
 * MIT License
 *
 * Copyright (c) 2017 Connor Hartley
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */
package io.github.connorhartley.guardian.sequence;

import com.google.common.reflect.TypeToken;
import com.ichorpowered.guardian.api.detection.DetectionConfiguration;
import com.ichorpowered.guardian.api.detection.check.Check;
import com.ichorpowered.guardian.api.entry.EntityEntry;
import com.ichorpowered.guardian.api.event.origin.Origin;
import com.ichorpowered.guardian.api.report.Summary;
import com.ichorpowered.guardian.api.sequence.Sequence;
import com.ichorpowered.guardian.api.sequence.SequenceBlueprint;
import com.ichorpowered.guardian.api.sequence.capture.Capture;
import com.ichorpowered.guardian.api.sequence.capture.CaptureRegistry;
import com.ichorpowered.guardian.api.util.key.NamedKey;
import io.github.connorhartley.guardian.report.GuardianSummary;
import io.github.connorhartley.guardian.sequence.action.GuardianAction;
import org.spongepowered.api.entity.living.player.Player;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.Objects;

import javax.annotation.Nonnull;

public class GuardianSequence<E, F extends DetectionConfiguration> implements Sequence<E, F> {

    public static NamedKey INITIAL_LOCATION =
            NamedKey.of(GuardianSequence.class.getCanonicalName() + "_INITIAL_LOCATION");

    private final EntityEntry entry;
    private final GuardianSummary<E, F> summary;
    private final CaptureRegistry captureRegistry;
    private final SequenceBlueprint<E, F> sequenceBlueprint;
    private final List<GuardianAction> actions = new ArrayList<>();
    private final Origin.Builder originSource = Origin.source(this);

    private int queue = 0;
    private boolean capturing = false;
    private boolean finished = false;
    private boolean cancelled = false;
    private long last = System.currentTimeMillis();

    public GuardianSequence(@Nonnull EntityEntry entry, @Nonnull SequenceBlueprint<E, F> sequenceBlueprint,
                            @Nonnull Check<E, F> check, @Nonnull Collection<GuardianAction> action,
                            @Nonnull CaptureRegistry captureRegistry) {
        this.entry = entry;
        this.captureRegistry = captureRegistry;
        this.sequenceBlueprint = sequenceBlueprint;

        this.actions.addAll(action);
        this.summary = new GuardianSummary<>(check.getDetection().getOwner(), check.getDetection(), entry, this.originSource.build());
    }

    @Override
    @SuppressWarnings("unchecked")
    public <T> boolean apply(@Nonnull EntityEntry entry, @Nonnull T event) {
        Iterator<GuardianAction> iterator = this.actions.iterator();

        if (iterator.hasNext()) {
            GuardianAction action = iterator.next();

            this.queue += 1;
            long current = System.currentTimeMillis();

            // 1. Check that the event is the correct one for this action.

            if (!action.getEvent().isAssignableFrom(event.getClass())) {
                return fail(entry, event, action, this.originSource.build());
            }

            // 2. Run capture if it is not already running.

            if (!this.capturing) {
                for (Capture<E, F> capture : this.captureRegistry) {
                    capture.start(entry, this.captureRegistry.getContainer());
                }

                this.capturing = true;

                if (entry.getEntity(TypeToken.of(Player.class)).isPresent()) this.captureRegistry.getContainer()
                        .put(GuardianSequence.INITIAL_LOCATION, entry.getEntity(TypeToken.of(Player.class)).get().getLocation());
            }

            GuardianAction<T> typeAction = (GuardianAction<T>) action;

            // 3. Fail the action if it is being executed before the delay.

            if (this.queue > 1 && this.last + ((action.getDelay() / 20) * 1000) > current) {
                return this.fail(entry, event, action, this.originSource
                        .named("overload", this.last + ((typeAction.getDelay() / 20) * 1000) - current).build());
            }

            // 4. Fail the action if it is being executed after the delay.

            if (this.queue > 1 && this.last + ((action.getExpire() / 20) * 1000) < current) {
                return this.fail(entry, event, action, this.originSource
                        .named("overload", current - this.last + ((typeAction.getExpire() / 20) * 1000)).build());
            }

            // 5. Run the action conditions and fail if they do not pass.

            if (!typeAction.apply(this, entry, event, this.last)) {
                return this.fail(entry, event, action, this.originSource.build());
            }

            // 6. Succeed the action, remove it and set finished if there is no more actions in the sequence.

            iterator.remove();

            typeAction.succeed(this, entry, event, this.last);

            this.last = System.currentTimeMillis();

            if (!iterator.hasNext()) {
                if (this.capturing) {
                    for (Capture<E, F> capture : this.captureRegistry) {
                        capture.stop(entry, this.captureRegistry.getContainer());
                    }

                    this.finished = true;
                }
            }
        }

        return true;
    }

    private <T> boolean fail(@Nonnull EntityEntry entry, @Nonnull T event,
                             @Nonnull GuardianAction<T> action, @Nonnull Origin origin) {
        this.cancelled = action.fail(this, entry, event, this.last);
        return false;
    }

    @Nonnull
    @Override
    public Summary<E, F> getSummary() {
        return this.summary;
    }

    @Nonnull
    @Override
    public CaptureRegistry getCaptureRegistry() {
        return this.captureRegistry;
    }

    @Nonnull
    @Override
    public SequenceBlueprint<E, F> getSequenceBlueprint() {
        return this.sequenceBlueprint;
    }

    @Override
    public boolean isRunning() {
        return this.capturing;
    }

    @Override
    public boolean isCancelled() {
        return this.cancelled;
    }

    @Override
    public boolean isExpired() {
        if (this.actions.isEmpty()) {
            return false;
        }

        GuardianAction action = this.actions.get(0);
        long current = System.currentTimeMillis();

        if (action != null && this.last + ((action.getExpire() / 20) * 1000) < current) {
            if (this.capturing) {
                // noinspection unchecked
                for (Capture<E, F> capture : this.captureRegistry) {
                    capture.stop(this.entry, this.captureRegistry.getContainer());
                }
            }

            return true;
        }

        return false;
    }

    @Override
    public boolean isFinished() {
        return this.finished;
    }

    @Override
    public int hashCode() {
        return Objects.hash(this.entry, this.captureRegistry, this.sequenceBlueprint);
    }

    @Override
    public boolean equals(final Object object) {
        if (this == object) return true;
        if (object == null || !(object instanceof GuardianSequence<?, ?>)) return false;
        final GuardianSequence<?, ?> that = (GuardianSequence<?, ?>) object;
        return Objects.equals(this.entry, that.entry)
                && Objects.equals(this.captureRegistry, that.captureRegistry)
                && Objects.equals(this.sequenceBlueprint, that.sequenceBlueprint);
    }

}

