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
package com.ichorpowered.guardian.common.check.combat;

import com.abilityapi.sequenceapi.SequenceBlueprint;
import com.abilityapi.sequenceapi.SequenceContext;
import com.abilityapi.sequenceapi.action.condition.ConditionType;
import com.google.common.collect.Sets;
import com.ichorpowered.guardian.entry.GuardianPlayerEntry;
import com.ichorpowered.guardian.sequence.GuardianSequenceBuilder;
import com.ichorpowered.guardian.sequence.SequenceReport;
import com.ichorpowered.guardian.sequence.context.CommonContextKeys;
import com.ichorpowered.guardianapi.detection.Detection;
import com.ichorpowered.guardianapi.detection.check.Check;
import com.ichorpowered.guardianapi.detection.report.Summary;
import com.ichorpowered.guardianapi.event.origin.Origin;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.event.Event;
import org.spongepowered.api.event.entity.AttackEntityEvent;

import java.util.Set;

public class AngleCheck implements Check<Event> {

    @Override
    public String getId() {
        return "guardian:anglecheck";
    }

    @Override
    public String getName() {
        return "Angle Check";
    }

    @Override
    public Set<String> getTags() {
        return Sets.newHashSet(
                "guardian",
                "internal",
                "combat",
                "aimcombat",
                "angle"
        );
    }

    @Override
    public SequenceBlueprint<Event> getSequence(final Detection detection) {
        return new GuardianSequenceBuilder()

                // Trigger : Attack Entity Event

                .observe(AttackEntityEvent.class)

                // After : 1 tick.

                .after()
                    .delay(1)

                    // TODO: Permission check.

                    .condition(sequenceContext -> {
                        final GuardianPlayerEntry<Player> entityEntry = sequenceContext.get(CommonContextKeys.ENTITY_ENTRY);
                        final Summary summary = sequenceContext.get(CommonContextKeys.SUMMARY);
                        final AttackEntityEvent event = sequenceContext.get(CommonContextKeys.TRIGGER_INSTANCE);

                        summary.set(SequenceReport.class, new SequenceReport(false, Origin.source(sequenceContext.getRoot())
                                .owner(entityEntry).build()));

                        return true;
                    }, ConditionType.NORMAL)

                .build(SequenceContext.builder()
                        .owner(detection)
                        .root(this)
                        .build());
    }

    @Override
    public Class<? extends Event> getSequenceTrigger() {
        return AttackEntityEvent.class;
    }
}
