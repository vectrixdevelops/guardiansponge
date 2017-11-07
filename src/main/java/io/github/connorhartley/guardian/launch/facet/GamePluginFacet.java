package io.github.connorhartley.guardian.launch.facet;

import com.ichorpowered.guardian.api.GuardianState;
import com.ichorpowered.guardian.api.event.origin.Origin;
import io.github.connorhartley.guardian.GuardianPlugin;
import io.github.connorhartley.guardian.event.state.GuardianStartedEvent;
import io.github.connorhartley.guardian.event.state.GuardianStartingEvent;
import io.github.connorhartley.guardian.event.state.GuardianStoppingEvent;
import io.github.connorhartley.guardian.launch.Facet;
import io.github.connorhartley.guardian.launch.FacetBootstrap;
import io.github.connorhartley.guardian.launch.FacetState;
import io.github.connorhartley.guardian.launch.exception.FacetException;
import io.github.connorhartley.guardian.launch.message.FacetMessage;
import io.github.connorhartley.guardian.launch.message.SimpleFacetMessage;
import io.github.connorhartley.guardian.util.ConsoleUtil;
import io.github.connorhartley.guardian.util.property.PropertyInjector;
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
