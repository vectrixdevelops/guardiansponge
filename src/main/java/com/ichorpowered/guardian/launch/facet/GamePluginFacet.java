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
package com.ichorpowered.guardian.launch.facet;

import com.ichorpowered.guardian.GuardianPlugin;
import com.ichorpowered.guardian.event.GuardianStartedEvent;
import com.ichorpowered.guardian.event.GuardianStartingEvent;
import com.ichorpowered.guardian.event.GuardianStoppingEvent;
import com.ichorpowered.guardian.launch.Facet;
import com.ichorpowered.guardian.launch.FacetBootstrap;
import com.ichorpowered.guardian.launch.FacetState;
import com.ichorpowered.guardian.launch.exception.FacetException;
import com.ichorpowered.guardian.launch.message.FacetMessage;
import com.ichorpowered.guardian.util.ConsoleUtil;
import com.ichorpowered.guardian.util.property.PropertyInjector;
import com.ichorpowered.guardianapi.GuardianState;
import com.ichorpowered.guardianapi.event.origin.Origin;
import org.fusesource.jansi.Ansi;
import org.slf4j.Logger;

public class GamePluginFacet implements Facet {

    private final Logger logger;
    private final GuardianPlugin plugin;

    private final String facetPrefix = ConsoleUtil.of(Ansi.Color.CYAN, " GAME ");

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
        this.logger.info(ConsoleUtil.of(this.facetPrefix + "Preparing environment adaptions."));

        // TODO: Environment adaptions are a future feature.

        this.plugin.getSequenceTask().start();

        this.plugin.getEventBus().post(new GuardianStartingEvent(this.plugin, Origin.source(this.plugin.getPluginContainer()).build()));

        propertyInjector.inject("state", GuardianState.STARTED);

        // STATE: STARTED
        this.logger.info(ConsoleUtil.of(this.facetPrefix + "Startup complete. ({} sec)",
                String.valueOf((double) (System.currentTimeMillis() - this.plugin.getCoreTime()) / 1000)));

        this.plugin.getEventBus().post(new GuardianStartedEvent(this.plugin, Origin.source(this.plugin.getPluginContainer()).build()));

        this.facetState = FacetState.START;
        return true;
    }

    public Boolean shutdown() {
        PropertyInjector propertyInjector = this.plugin.getPropertyInjector();
        propertyInjector.inject("state", GuardianState.STOPPING);

        this.plugin.getEventBus().post(new GuardianStoppingEvent(this.plugin, Origin.source(this.plugin.getPluginContainer()).build()));

        this.plugin.getSequenceTask().stop();

        this.facetState = FacetState.STOP;
        return true;
    }
}
