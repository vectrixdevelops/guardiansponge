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
package io.ichorpowered.guardian.sequence;

import com.abilityapi.sequenceapi.Sequence;
import com.abilityapi.sequenceapi.SequenceBlueprint;
import com.abilityapi.sequenceapi.SequenceContext;
import com.abilityapi.sequenceapi.action.Action;
import com.ichorpowered.guardian.api.detection.Detection;
import com.ichorpowered.guardian.api.detection.DetectionConfiguration;
import com.ichorpowered.guardian.api.entry.EntityEntry;
import com.ichorpowered.guardian.api.event.origin.Origin;
import com.ichorpowered.guardian.api.util.key.NamedTypeKey;
import io.ichorpowered.guardian.report.GuardianSummary;
import io.ichorpowered.guardian.sequence.capture.GuardianCaptureContainer;
import io.ichorpowered.guardian.sequence.capture.GuardianCaptureRegistry;
import io.ichorpowered.guardian.sequence.context.CommonContextKeys;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.event.Event;
import org.spongepowered.api.world.Location;

import java.util.List;

public class GuardianSequence<E, F extends DetectionConfiguration> extends Sequence<Event> {

    public static NamedTypeKey<Location> INITIAL_LOCATION =
            NamedTypeKey.of(GuardianSequence.class.getCanonicalName() + "_INITIAL_LOCATION", Location.class);

    private final GuardianSummary<E, F> summary;
    private final GuardianCaptureRegistry captureRegistry;

    public GuardianSequence(final SequenceContext sequenceContext,
                            final SequenceBlueprint<Event> sequenceBlueprint,
                            final GuardianCaptureRegistry captureRegistry,
                            final List<Action> actions) {
        super(sequenceContext, sequenceBlueprint, actions);

        this.captureRegistry = captureRegistry;
        this.captureRegistry.getContainer().merge(GuardianCaptureContainer.create());

        this.summary = new GuardianSummary<>(((Detection<E, F>) sequenceContext.getOwner()).getOwner(),
                sequenceContext.getOwner(),
                sequenceContext.get(CommonContextKeys.ENTITY_ENTRY),
                Origin.merge(sequenceContext).build());
    }

    @Override
    public boolean applyObserve(final Event event, final SequenceContext sequenceContext) {
        final EntityEntry entityEntry = sequenceContext.get(CommonContextKeys.ENTITY_ENTRY);
        final Player player = entityEntry.getEntity(Player.class)
                .orElse(Sponge.getServer().getPlayer(entityEntry.getUniqueId()).orElse(null));

        if (player == null) return false;

        final SequenceContext mergedContext = SequenceContext.from(sequenceContext)
                .custom(CommonContextKeys.LAST_ACTION_TIME, super.getLastActionTime())
                .custom(CommonContextKeys.CAPTURE_REGISTRY, this.captureRegistry)
                .custom(CommonContextKeys.SUMMARY, this.summary)
                .build();

        if (this.getState().equals(State.INACTIVE)) {
            this.captureRegistry.getContainer().putOnce(INITIAL_LOCATION, player.getLocation());
        }

        return super.applyObserve(event, mergedContext);
    }

    @Override
    public final void applySchedule(final SequenceContext sequenceContext) {
        final EntityEntry entityEntry = sequenceContext.get(CommonContextKeys.ENTITY_ENTRY);
        final Player player = entityEntry.getEntity(Player.class)
                .orElse(Sponge.getServer().getPlayer(entityEntry.getUniqueId()).orElse(null));

        if (player == null) return;

        final SequenceContext mergedContext = SequenceContext.from(sequenceContext)
                .custom(CommonContextKeys.LAST_ACTION_TIME, super.getLastActionTime())
                .custom(CommonContextKeys.CAPTURE_REGISTRY, this.captureRegistry)
                .custom(CommonContextKeys.SUMMARY, this.summary)
                .build();

        if (this.getState().equals(State.INACTIVE)) {
            this.captureRegistry.getContainer().putOnce(INITIAL_LOCATION, player.getLocation());
        }

        super.applySchedule(mergedContext);
    }

    public Detection<E, F> getOwner() {
        return super.getSequenceContext().getOwner();
    }

    public GuardianSummary<E, F> getSummary() {
        return this.summary;
    }

    public GuardianCaptureRegistry getCaptureRegistry() {
        return this.captureRegistry;
    }
}
