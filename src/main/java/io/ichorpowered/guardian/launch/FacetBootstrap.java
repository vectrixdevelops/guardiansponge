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
package io.ichorpowered.guardian.launch;

import io.ichorpowered.guardian.GuardianPlugin;
import io.ichorpowered.guardian.launch.exception.FacetException;
import io.ichorpowered.guardian.launch.message.FacetMessage;
import io.ichorpowered.guardian.launch.message.SimpleFacetMessage;
import org.slf4j.Logger;

import java.util.*;

public class FacetBootstrap {

    private final Logger logger;
    private final GuardianPlugin plugin;
    private final Map<String, Facet> facets = new LinkedHashMap<>();

    private int attemptedRecoveries = 0;

    public FacetBootstrap(final Logger logger, final GuardianPlugin plugin) {
        this.logger = logger;
        this.plugin = plugin;
    }

    public void addComponent(String id, Facet facet) {
        this.facets.put(id, facet);
    }

    public <I extends FacetMessage, O> Optional<O> send(final FacetRequest<I, O> facetRequest, final I input, final String id) {
        try {
            return Optional.ofNullable(this.facets.get(id).handle(facetRequest, input));
        } catch (Throwable e) {
            this.handleError(e);
        }

        return Optional.empty();
    }

    public <I extends FacetMessage, O> List<O> send(final FacetRequest<I, O> facetRequest, final I input, final Set<String> ids) {
        final List<O> results = new ArrayList<>();

        for (String id : ids) {
            this.send(facetRequest, input, id).ifPresent(results::add);
        }

        return results;
    }

    public final SortedSet<String> getIds() {
        return new TreeSet<>(this.facets.keySet());
    }

    private void handleError(final Throwable e) {
        if (e instanceof FacetException) {
            this.logger.error("Guardian FacetBootstrap has encountered an exception attempting to send a facet request: ", e);
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
        SortedSet<String> backwards = new TreeSet<>(Collections.reverseOrder());
        backwards.addAll(this.facets.keySet());

        this.send(FacetRequest.SHUTDOWN,
                new SimpleFacetMessage(System.currentTimeMillis(), "Critical failure.", this.plugin),
                backwards);
    }

    private void recover() {
        this.attemptedRecoveries++;

        final SortedSet<String> backwards = new TreeSet<>(Collections.reverseOrder());
        backwards.addAll(this.facets.keySet());

        this.send(FacetRequest.SHUTDOWN,
                new SimpleFacetMessage(System.currentTimeMillis(), "Error recovery.", this.plugin),
                backwards);

        this.send(FacetRequest.STARTUP,
                new SimpleFacetMessage(System.currentTimeMillis(), "Error recovery.", this.plugin),
                this.facets.keySet());
    }

    public static class FacetRequest<I extends FacetMessage, O> {

        public static final FacetRequest<SimpleFacetMessage, FacetState> UPDATE =
                new FacetRequest<>(0, SimpleFacetMessage.class, FacetState.class);

        public static final FacetRequest<SimpleFacetMessage, Boolean> STARTUP =
                new FacetRequest<>(1, SimpleFacetMessage.class, Boolean.class);

        public static final FacetRequest<SimpleFacetMessage, Boolean> RESTART =
                new FacetRequest<>(2, SimpleFacetMessage.class, Boolean.class);

        public static final FacetRequest<SimpleFacetMessage, Boolean> SHUTDOWN =
                new FacetRequest<>(3, SimpleFacetMessage.class, Boolean.class);

        private final int id;
        private final Class<I> input;
        private final Class<O> output;

        public FacetRequest(int id, final Class<I> input, final Class<O> output) {
            this.id = id;
            this.input = input;
            this.output = output;
        }

        public int getId() {
            return this.id;
        }

        public final Class<I> getInput() {
            return this.input;
        }

        public final Class<O> getOutput() {
            return this.output;
        }
    }

}
