package io.github.connorhartley.guardian.sequence.action;

import io.github.connorhartley.guardian.sequence.condition.Condition;
import org.spongepowered.api.event.Event;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class Action<H, T extends Event> {

    private final List<Condition<H, T>> conditions = new ArrayList<>();
    private final List<Condition> successfulListeners = new ArrayList<>();
    private final List<Condition> failedListeners = new ArrayList<>();

    private final Class<T> event;

    private int delay;
    private int expire;

    Action(Class<T> event, Condition<H, T>... conditions) {
        this(event);
        this.conditions.addAll(Arrays.asList(conditions));
    }

    public Action(Class<T> event, List<Condition<H, T>> conditions) {
        this(event);
        this.conditions.addAll(conditions);
    }

    public Action(Class<T> event) {
        this.event = event;
    }

    void addCondition(Condition<H, T> condition) {

    }

    void setDelay(int delay) {

    }

    void setExpire(int expire) {

    }

    public void succeed(H human, Event event) {

    }

    public boolean fail(H human, Event event) {
        return false;
    }

    public boolean testConditions(H human, T event) {
        return false;
    }

    public Class<T> getEvent() {
        return event;
    }

    public List<Condition<H, T>> getConditions() {
        return this.conditions;
    }

    public int getDelay() {
        return this.delay;
    }

    public int getExpire() {
        return this.expire;
    }

    void onSuccess(Condition condition) {
        this.successfulListeners.add(condition);
    }

    void onFailure(Condition condition) {
        this.failedListeners.add(condition);
    }

}
