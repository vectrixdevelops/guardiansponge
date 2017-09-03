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
import com.ichorpowered.guardian.api.util.IdentifierKey;
import io.github.connorhartley.guardian.report.GuardianSummary;
import io.github.connorhartley.guardian.sequence.action.GuardianAction;
import org.spongepowered.api.entity.living.player.Player;

import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;

public class GuardianSequence<E, F extends DetectionConfiguration> implements Sequence<E, F> {

    public static IdentifierKey<String> INITIAL_LOCATION =
            IdentifierKey.of(GuardianSequence.class.getCanonicalName() + "_INITIAL_LOCATION");

    private final EntityEntry entry;
    private final Check<E, F> check;
    private final GuardianSummary<E, F> summary;
    private final CaptureRegistry captureRegistry;
    private final SequenceBlueprint<E, F> sequenceBlueprint;
    private final Collection<GuardianAction> actions = Collections.emptyList();
    private final Origin.Builder originSource = Origin.source(this);

    private int queue = 0;
    private boolean capturing = false;
    private boolean finished = false;
    private boolean cancelled = false;
    private long last = System.currentTimeMillis();

    public GuardianSequence(EntityEntry entry, SequenceBlueprint<E, F> sequenceBlueprint, Check<E, F> check,
                            Collection<GuardianAction> action, CaptureRegistry captureRegistry) {
        this.entry = entry;
        this.check = check;
        this.captureRegistry = captureRegistry;
        this.sequenceBlueprint = sequenceBlueprint;

        this.actions.addAll(action);
        this.summary = new GuardianSummary<>(check.getDetection().getOwner(), check.getDetection(), entry);
    }

    @Override
    @SuppressWarnings("unchecked")
    public <T> boolean apply(EntityEntry entry, T event) {
        Iterator<GuardianAction> iterator = this.actions.iterator();

        if (iterator.hasNext()) {
            GuardianAction action = iterator.next();

            this.queue += 1;
            long current = System.currentTimeMillis();

            // 1. Check that the event is the correct one for this action.

            if (!action.getEvent().equals(event.getClass())) {
                return fail(entry, event, action, this.originSource.build());
            }

            // 2. Run capture if it is not already running.

            if (!this.capturing) {
                for (Capture<E, F> capture : this.captureRegistry) {
                    capture.start(entry, this.captureRegistry.getContainer());
                }

                this.capturing = true;

                if (!entry.getEntity(TypeToken.of(Player.class)).isPresent())
                    this.captureRegistry.getContainer().put(GuardianSequence.INITIAL_LOCATION, null);

                this.captureRegistry.getContainer().put(GuardianSequence.INITIAL_LOCATION,
                        entry.getEntity(TypeToken.of(Player.class)).get().getLocation());
            }

            GuardianAction<T> typeAction = (GuardianAction<T>) action;

            // 3. Fail the action if it is being executed before the delay.

            if (this.queue > 1 && this.last + ((typeAction.getDelay() / 20) * 1000) > current) {
                return this.fail(entry, event, action, this.originSource
                        .named("overload", this.last + ((typeAction.getDelay() / 20) * 1000) - current).build());
            }

            // 4. Fail the action if it is being executed after the delay.

            if (this.queue > 1 && this.last + ((typeAction.getExpire() / 20) * 1000) < current) {
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

    public <T> boolean fail(EntityEntry entry, T event, GuardianAction<T> action, Origin origin) {
        this.cancelled = action.fail(this, entry, event, this.last);

        // TODO: Sequence fail event.

        return false;
    }

    @Override
    public Summary<E, F> getSummary() {
        return this.summary;
    }

    @Override
    public CaptureRegistry getCaptureRegistry() {
        return this.captureRegistry;
    }

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

        Iterator<GuardianAction> it = this.actions.iterator();
        if (it.hasNext()) {
            GuardianAction action = it.next();
            long current = System.currentTimeMillis();

            if (this.last + ((action.getExpire() / 20) * 1000) < current) {
                if (this.capturing) {
                    // noinspection unchecked
                    for (Capture<E, F> capture : this.captureRegistry) {
                        capture.stop(entry, this.captureRegistry.getContainer());
                    }
                }
            }
        }

        return false;
    }

    @Override
    public boolean isFinished() {
        return this.finished;
    }

}

