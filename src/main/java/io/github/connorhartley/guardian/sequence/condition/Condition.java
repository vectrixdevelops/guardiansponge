package io.github.connorhartley.guardian.sequence.condition;

import org.spongepowered.api.event.Event;

public interface Condition<H, T extends Event> {

    boolean test(H human, T event);

}
