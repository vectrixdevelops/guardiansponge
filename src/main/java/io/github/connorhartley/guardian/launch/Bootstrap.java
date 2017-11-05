package io.github.connorhartley.guardian.launch;

import io.github.connorhartley.guardian.GuardianPlugin;
import io.github.connorhartley.guardian.GuardianPluginOld;
import io.github.connorhartley.guardian.launch.exception.ComponentException;
import io.github.connorhartley.guardian.launch.message.ComponentMessage;
import io.github.connorhartley.guardian.launch.message.SimpleComponentMessage;
import org.slf4j.Logger;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.SortedSet;
import java.util.TreeSet;

public class Bootstrap {

    private final Logger logger;
    private final GuardianPlugin plugin;
    private final Map<String, Component> components = new LinkedHashMap<>();

    private int attemptedRecoveries = 0;

    public Bootstrap(final Logger logger, final GuardianPlugin plugin) {
        this.logger = logger;
        this.plugin = plugin;
    }

    public void addComponent(String id, Component component) {
        this.components.put(id, component);
    }

    public <I extends ComponentMessage, O> Optional<O> send(ComponentRequest<I, O> componentRequest, I input, String id) {
        try {
            return Optional.ofNullable(this.components.get(id).handle(componentRequest, input));
        } catch (Throwable e) {
            this.handleError(e);
        }

        return Optional.empty();
    }

    public <I extends ComponentMessage, O> List<O> send(ComponentRequest<I, O> componentRequest, I input, Set<String> ids) {
        final List<O> results = new ArrayList<>();

        for (String id : ids) {
            this.send(componentRequest, input, id).ifPresent(results::add);
        }

        return results;
    }

    public SortedSet<String> getIds() {
        return new TreeSet<>(this.components.keySet());
    }

    private void handleError(Throwable e) {
        if (e instanceof ComponentException) {
            this.logger.error("Guardian Bootstrap has encountered an exception attempting to send a component request: ", e);
        } else {
            this.logger.error("Guardian has encountered an exception: ", e);

            if (this.attemptedRecoveries <= 3) {
                this.logger.info("Attempting to recover...");
                recover();
                return;
            } else {
                this.logger.error("Cannot recover due to exceeded attempts.");
            }
        }

        this.logger.error("This is a critical failure and Guardian will be disabled.");
        this.logger.error("Report this issue here: https://github.com/ichorpowered/guardian/issues");
        forceShutdown();
    }

    private void forceShutdown() {
        SortedSet<String> lastComponents = new TreeSet<>(Collections.reverseOrder());
        lastComponents.addAll(this.components.keySet());

        this.send(ComponentRequest.SHUTDOWN,
                new SimpleComponentMessage(System.currentTimeMillis(), "Critical failure.", this.plugin),
                lastComponents);
    }

    private void recover() {
        this.attemptedRecoveries++;

        SortedSet<String> lastComponents = new TreeSet<>(Collections.reverseOrder());
        lastComponents.addAll(this.components.keySet());

        this.send(ComponentRequest.RESTART,
                new SimpleComponentMessage(System.currentTimeMillis(), "Failure recovery.", this.plugin),
                lastComponents);
    }

    public static class ComponentRequest<I extends ComponentMessage, O> {

        public static final ComponentRequest<SimpleComponentMessage, ComponentState> UPDATE =
                new ComponentRequest<>(0, SimpleComponentMessage.class, ComponentState.class);

        public static final ComponentRequest<SimpleComponentMessage, Boolean> STARTUP =
                new ComponentRequest<>(1, SimpleComponentMessage.class, Boolean.class);

        public static final ComponentRequest<SimpleComponentMessage, Boolean> RESTART =
                new ComponentRequest<>(2, SimpleComponentMessage.class, Boolean.class);

        public static final ComponentRequest<SimpleComponentMessage, Boolean> SHUTDOWN =
                new ComponentRequest<>(3, SimpleComponentMessage.class, Boolean.class);

        private final int id;
        private final Class<I> input;
        private final Class<O> output;

        public ComponentRequest(int id, Class<I> input, Class<O> output) {
            this.id = id;
            this.input = input;
            this.output = output;
        }

        public int getId() {
            return this.id;
        }

        public Class<I> getInput() {
            return this.input;
        }

        public Class<O> getOutput() {
            return this.output;
        }
    }

}
