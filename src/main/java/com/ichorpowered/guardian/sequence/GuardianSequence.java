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
package com.ichorpowered.guardian.sequence;

import com.abilityapi.sequenceapi.Sequence;
import com.abilityapi.sequenceapi.SequenceBlueprint;
import com.abilityapi.sequenceapi.SequenceContext;
import com.abilityapi.sequenceapi.action.Action;
import com.google.common.reflect.TypeToken;
import com.ichorpowered.guardian.report.GuardianSummary;
import com.ichorpowered.guardian.sequence.capture.GuardianCaptureContainer;
import com.ichorpowered.guardian.sequence.capture.GuardianCaptureKey;
import com.ichorpowered.guardian.sequence.capture.GuardianCaptureRegistry;
import com.ichorpowered.guardian.sequence.context.CommonContextKeys;
import com.ichorpowered.guardian.util.item.mutable.GuardianValue;
import com.ichorpowered.guardianapi.detection.Detection;
import com.ichorpowered.guardianapi.detection.capture.CaptureKey;
import com.ichorpowered.guardianapi.entry.entity.PlayerEntry;
import com.ichorpowered.guardianapi.event.origin.Origin;
import com.ichorpowered.guardianapi.util.item.value.mutable.Value;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.event.Event;
import org.spongepowered.api.world.Location;

import java.util.List;

public class GuardianSequence extends Sequence<Event> {

    public static CaptureKey<Value<Location>> INITIAL_LOCATION = GuardianCaptureKey.<Value<Location>>builder()
            .id(GuardianSequence.class.getSimpleName() + ":initialLocation")
            .name("InitialLocation")
            .type(GuardianValue.empty(), TypeToken.of(Location.class))
            .build();

    private final GuardianSummary summary;
    private final GuardianCaptureRegistry captureRegistry;

    public GuardianSequence(final SequenceContext sequenceContext,
                            final SequenceBlueprint<Event> sequenceBlueprint,
                            final GuardianCaptureRegistry captureRegistry,
                            final List<Action> actions) {
        super(actions, sequenceContext, sequenceBlueprint, Sequence.getComparatorAssignable());

        this.captureRegistry = captureRegistry;
        this.captureRegistry.getContainer().merge(GuardianCaptureContainer.create());

        this.summary = new GuardianSummary(((Detection) sequenceContext.getOwner()).getPlugin(),
                sequenceContext.getOwner(),
                sequenceContext.get(CommonContextKeys.ENTITY_ENTRY),
                Origin.merge(sequenceContext).build());
    }

    @Override
    public boolean applyObserve(final Event event, final SequenceContext sequenceContext) {
        final PlayerEntry entityEntry = sequenceContext.get(CommonContextKeys.ENTITY_ENTRY);
        final Player player = entityEntry.getEntity(Player.class)
                .orElse(Sponge.getServer().getPlayer(entityEntry.getUniqueId()).orElse(null));

        if (player == null) return false;

        final SequenceContext mergedContext = SequenceContext.from(sequenceContext)
                .custom(CommonContextKeys.LAST_ACTION_TIME, super.getLastActionTime())
                .custom(CommonContextKeys.CAPTURE_REGISTRY, this.captureRegistry)
                .custom(CommonContextKeys.SUMMARY, this.summary)
                .build();

        if (this.getState().equals(State.INACTIVE)) {
            this.captureRegistry.getContainer().offerIfEmpty(GuardianValue.builder(GuardianSequence.INITIAL_LOCATION)
                    .defaultElement(player.getLocation())
                    .element(player.getLocation())
                    .create());
        }

        return super.applyObserve(event, mergedContext);
    }

    @Override
    public boolean applyAfter(SequenceContext sequenceContext) {
        final PlayerEntry entityEntry = sequenceContext.get(CommonContextKeys.ENTITY_ENTRY);
        final Player player = entityEntry.getEntity(Player.class)
                .orElse(Sponge.getServer().getPlayer(entityEntry.getUniqueId()).orElse(null));

        if (player == null) return false;

        final SequenceContext mergedContext = SequenceContext.from(sequenceContext)
                .custom(CommonContextKeys.LAST_ACTION_TIME, super.getLastActionTime())
                .custom(CommonContextKeys.CAPTURE_REGISTRY, this.captureRegistry)
                .custom(CommonContextKeys.SUMMARY, this.summary)
                .build();

        if (this.getState().equals(State.INACTIVE)) {
            this.captureRegistry.getContainer().offerIfEmpty(GuardianValue.builder(GuardianSequence.INITIAL_LOCATION)
                    .defaultElement(player.getLocation())
                    .element(player.getLocation())
                    .create());
        }

        return super.applyAfter(mergedContext);
    }

    @Override
    public final void applySchedule(final SequenceContext sequenceContext) {
        final PlayerEntry playerEntry = sequenceContext.get(CommonContextKeys.ENTITY_ENTRY);
        final Player player = playerEntry.getEntity(Player.class)
                .orElse(Sponge.getServer().getPlayer(playerEntry.getUniqueId()).orElse(null));

        if (player == null) return;

        final SequenceContext mergedContext = SequenceContext.from(sequenceContext)
                .custom(CommonContextKeys.LAST_ACTION_TIME, super.getLastActionTime())
                .custom(CommonContextKeys.CAPTURE_REGISTRY, this.captureRegistry)
                .custom(CommonContextKeys.SUMMARY, this.summary)
                .build();

        if (this.getState().equals(State.INACTIVE)) {
            this.captureRegistry.getContainer().offerIfEmpty(GuardianValue.builder(GuardianSequence.INITIAL_LOCATION)
                    .defaultElement(player.getLocation())
                    .element(player.getLocation())
                    .create());
        }

        super.applySchedule(mergedContext);
    }

    public Detection getOwner() {
        return super.getSequenceContext().getOwner();
    }

    public GuardianSummary getSummary() {
        return this.summary;
    }

    public GuardianCaptureRegistry getCaptureRegistry() {
        return this.captureRegistry;
    }
}
