package io.github.connorhartley.guardian.internal.providers;

import io.github.connorhartley.guardian.detection.check.Check;
import io.github.connorhartley.guardian.detection.check.CheckController;
import io.github.connorhartley.guardian.detection.check.CheckProvider;
import io.github.connorhartley.guardian.sequence.Sequence;
import io.github.connorhartley.guardian.sequence.SequenceBlueprint;
import io.github.connorhartley.guardian.sequence.SequenceBuilder;
import io.github.connorhartley.guardian.sequence.report.SequencePoint;
import org.spongepowered.api.entity.living.player.User;
import org.spongepowered.api.event.entity.MoveEntityEvent;

public class MovementSpeedProvider implements CheckProvider {

    @Override
    public SequenceBlueprint getSequence() {
        return new SequenceBuilder()
                .action(MoveEntityEvent.class)
                    .delay(20 * 2)
                    .condition((user, event, sequenceResult) -> {
                        if (user.hasPermission("guardian.detection.movementspeed")) {
                            if (user.hasPermission("guardian.detection.movementspeed.exempt")) {
                                return new SequencePoint.Builder().setPass(false).build();
                            }
                            return new SequencePoint.Builder().setPass(true).build();
                        }
                        return new SequencePoint.Builder().setPass(false).build();
                    })
                .action(MoveEntityEvent.class)
                    .condition((user, event, sequenceResult) -> {

                        // TODO: Juice'y movement speed check here.

                        return new SequencePoint.Builder().build();
                    })
                .build(this);
    }

    @Override
    public Check createInstance(CheckController checkController, Sequence sequence, User user) {
        return null;
    }

}
