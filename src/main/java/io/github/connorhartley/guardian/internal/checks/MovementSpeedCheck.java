package io.github.connorhartley.guardian.internal.checks;

import io.github.connorhartley.guardian.detection.check.Check;
import io.github.connorhartley.guardian.detection.check.CheckProvider;
import org.spongepowered.api.entity.living.player.User;

public class MovementSpeedCheck extends Check {

    public MovementSpeedCheck(CheckProvider checkProvider, User user) {
        super(checkProvider, user);
    }

    @Override
    public void update() {}

    @Override
    public void finish() {}


}
