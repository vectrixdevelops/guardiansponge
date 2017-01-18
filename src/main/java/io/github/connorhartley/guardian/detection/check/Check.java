package io.github.connorhartley.guardian.detection.check;

import org.spongepowered.api.entity.living.player.User;
import org.spongepowered.api.network.PlayerConnection;

import java.util.Optional;

public abstract class Check<T> {

    private final T human;

    public Check(T human) {
        if (human instanceof User || human instanceof PlayerConnection) {
            this.human = human;
        } else {
            this.human = null;
        }
    }

    abstract void pass();

    abstract void fail();

    public Optional<T> getHuman() {
        if (this.human != null) {
            return Optional.of(this.human);
        }
        return Optional.empty();
    }

}
