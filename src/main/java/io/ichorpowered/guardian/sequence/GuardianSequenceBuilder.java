package io.ichorpowered.guardian.sequence;

import com.abilityapi.sequenceapi.Sequence;
import com.abilityapi.sequenceapi.SequenceBlueprint;
import com.abilityapi.sequenceapi.SequenceBuilder;
import com.abilityapi.sequenceapi.action.type.observe.ObserverAction;
import com.abilityapi.sequenceapi.action.type.observe.ObserverActionBlueprint;
import com.abilityapi.sequenceapi.action.type.observe.ObserverActionBuilder;
import com.abilityapi.sequenceapi.action.type.schedule.ScheduleAction;
import com.abilityapi.sequenceapi.action.type.schedule.ScheduleActionBuilder;
import com.abilityapi.sequenceapi.context.SequenceContext;
import com.abilityapi.sequenceapi.context.SequenceContextKey;
import com.google.common.collect.BiMap;
import com.google.common.collect.HashBiMap;
import com.google.common.collect.Lists;
import com.ichorpowered.guardian.api.detection.DetectionConfiguration;
import com.ichorpowered.guardian.api.sequence.capture.Capture;
import io.ichorpowered.guardian.entry.GuardianEntityEntry;
import io.ichorpowered.guardian.sequence.capture.GuardianCaptureRegistry;
import org.spongepowered.api.event.Event;
import org.spongepowered.api.plugin.PluginContainer;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;

public class GuardianSequenceBuilder<E, F extends DetectionConfiguration> extends SequenceBuilder<Event> {

    private final List<Capture<E, F>> captures = new ArrayList<>();

    private int index = 0;
    private final Map<ScheduleAction, Integer> scheduleActions = new HashMap<>();
    private final Map<ObserverAction<Event>, Integer> observerActions = new HashMap<>();

    public GuardianSequenceBuilder<E, F> capture(final Capture<E, F> capture) {
        this.captures.add(capture);
        return this;
    }

    @Override
    public ObserverActionBuilder<Event> observe(final Class<Event> event) {
        return this.observe(new ObserverAction<>(event));
    }

    @Override
    public ObserverActionBuilder<Event> observe(final ObserverActionBlueprint<Event> actionBlueprint) {
        return this.observe(actionBlueprint.create());
    }

    @Override
    public ObserverActionBuilder<Event> observe(final ObserverAction<Event> action) {
        this.observerActions.put(action, this.index++);

        return new ObserverActionBuilder<>(this, action);
    }

    @Override
    public ScheduleActionBuilder<Event> schedule() {
        return this.schedule(new ScheduleAction());
    }

    @Override
    public ScheduleActionBuilder<Event> schedule(final ScheduleAction action) {
        this.scheduleActions.put(action, this.index++);

        return new ScheduleActionBuilder<>(this, action);
    }

    /**
     * Returns a new {@link SequenceBlueprint} containing
     * the {@link Sequence} of {@link ObserverAction}s and
     * {@link ScheduleAction}s.
     *
     * @param sequenceContext the sequence context
     * @return the sequence blueprint
     */
    @Override
    public final SequenceBlueprint<Event> build(final SequenceContext sequenceContext) {
        final SequenceContext context = SequenceContext.from(sequenceContext)
                .custom(SequenceContextKey.of("captures", Lists.newArrayList()), this.captures).build();

        return new SequenceBlueprint<Event>() {
            @Override
            public final Sequence create(final SequenceContext createSequenceContext) {
                final SequenceContext.Builder newOrigin = SequenceContext.from(createSequenceContext);
                if (context != null) newOrigin.merge(context);

                final GuardianCaptureRegistry captureRegistry = new GuardianCaptureRegistry(
                        (GuardianEntityEntry) sequenceContext.get(SequenceContextKey.of("entry", null))
                );

                GuardianSequenceBuilder.this.captures.forEach(capture ->
                        captureRegistry.put(context.getOwner(), capture.getClass(), capture));

                return new GuardianSequence(newOrigin.build(), this, captureRegistry, scheduleActions, this.validateSequence());
            }

            @Override
            public final Class<? extends Event> getTrigger() {
                final BiMap<Integer, ObserverAction<Event>> observers = HashBiMap.create(validateSequence()).inverse();

                return observers.get(0).getEventClass();
            }

            private Map<ObserverAction<Event>, Integer> validateSequence() throws NoSuchElementException {
                if (observerActions.isEmpty() || !observerActions.containsValue(0)) throw
                        new NoSuchElementException("Sequence could not be established without an initial observer.");

                return observerActions;
            }
        };
    }

}
