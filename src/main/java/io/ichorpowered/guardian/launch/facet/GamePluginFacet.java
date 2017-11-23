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
package io.ichorpowered.guardian.launch.facet;

import com.ichorpowered.guardian.api.GuardianState;
import com.ichorpowered.guardian.api.event.origin.Origin;
import io.ichorpowered.guardian.GuardianPlugin;
import io.ichorpowered.guardian.event.state.GuardianStartedEvent;
import io.ichorpowered.guardian.event.state.GuardianStartingEvent;
import io.ichorpowered.guardian.event.state.GuardianStoppingEvent;
import io.ichorpowered.guardian.launch.Facet;
import io.ichorpowered.guardian.launch.FacetBootstrap;
import io.ichorpowered.guardian.launch.FacetState;
import io.ichorpowered.guardian.launch.exception.FacetException;
import io.ichorpowered.guardian.launch.message.FacetMessage;
import io.ichorpowered.guardian.util.property.PropertyInjector;
import org.slf4j.Logger;

public class GamePluginFacet implements Facet {

    private final Logger logger;
    private final GuardianPlugin plugin;

    private FacetState facetState = FacetState.STOP;

    public GamePluginFacet(final Logger logger, final GuardianPlugin plugin) {
        this.logger = logger;
        this.plugin = plugin;
    }

    @Override
    public <I extends FacetMessage, O> O handle(FacetBootstrap.FacetRequest<I, O> facetRequest, I input) throws FacetException {
        switch (facetRequest.getId()) {
            case 0: {
                return (O) this.facetState;
            }
            case 1: {
                return (O) this.startup();
            }
            case 2: {
                // This facet should not restart.
                return (O) Boolean.valueOf(true);
            }
            case 3: {
                return (O) this.shutdown();
            }
        }
        return null;
    }

    public Boolean startup() {
        this.facetState = FacetState.PREPARE;

        PropertyInjector propertyInjector = this.plugin.getPropertyInjector();
        propertyInjector.inject("state", GuardianState.STARTING);

        // STATE: STARTING

        this.plugin.sequenceTask.start();

        this.plugin.eventBus.post(new GuardianStartingEvent(this.plugin, Origin.source(this.plugin.getPluginContainer()).build()));

        propertyInjector.inject("state", GuardianState.STARTED);

        // STATE: STARTED

        this.plugin.eventBus.post(new GuardianStartedEvent(this.plugin, Origin.source(this.plugin.getPluginContainer()).build()));

        this.facetState = FacetState.START;
        return true;
    }

    public Boolean shutdown() {
        PropertyInjector propertyInjector = this.plugin.getPropertyInjector();
        propertyInjector.inject("state", GuardianState.STOPPING);

        this.plugin.eventBus.post(new GuardianStoppingEvent(this.plugin, Origin.source(this.plugin.getPluginContainer()).build()));

        this.plugin.sequenceTask.stop();

        this.facetState = FacetState.STOP;
        return true;
    }
}
