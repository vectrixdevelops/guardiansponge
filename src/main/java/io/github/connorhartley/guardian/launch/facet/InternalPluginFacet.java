package io.github.connorhartley.guardian.launch.facet;

import io.github.connorhartley.guardian.GuardianPlugin;
import io.github.connorhartley.guardian.launch.Facet;
import io.github.connorhartley.guardian.launch.FacetBootstrap;
import io.github.connorhartley.guardian.launch.FacetState;
import io.github.connorhartley.guardian.launch.exception.FacetException;
import io.github.connorhartley.guardian.launch.message.FacetMessage;
import io.github.connorhartley.guardian.launch.message.SimpleFacetMessage;
import io.github.connorhartley.guardian.util.ConsoleUtil;
import org.fusesource.jansi.Ansi;
import org.slf4j.Logger;

public class InternalPluginFacet implements Facet {

    private final Logger logger;
    private final GuardianPlugin plugin;

    private final String facetPrefix = ConsoleUtil.of(Ansi.Color.CYAN, " INTERNAL ");

    private FacetState facetState = FacetState.STOP;

    public InternalPluginFacet(final Logger logger, final GuardianPlugin plugin) {
        this.logger = logger;
        this.plugin = plugin;
    }

    @Override
    public <I extends FacetMessage, O> O handle(FacetBootstrap.FacetRequest<I, O> facetRequest, I input) throws FacetException {
        switch (facetRequest.getId()) {
            case 0: {
                if (input instanceof SimpleFacetMessage) return (O) this.facetState;
            }
            case 1: {
                if (input instanceof SimpleFacetMessage) return (O) this.startup((SimpleFacetMessage) input);
                else throw new FacetException("Input was of an incorrect type.");
            }
            case 2: {
                // This facet should not restart.
                return (O) Boolean.valueOf(true);
            }
            case 3: {
                if (input instanceof SimpleFacetMessage) return (O) this.shutdown((SimpleFacetMessage) input);
                else throw new FacetException("Input was of an incorrect type.");
            }
        }
        return null;
    }

    public Boolean startup(SimpleFacetMessage message) {
        this.facetState = FacetState.PREPARE;

        this.facetState = FacetState.START;
        return true;
    }

    public Boolean shutdown(SimpleFacetMessage message) {
        this.logger.info(ConsoleUtil.of("Shutdown internals."));

        this.facetState = FacetState.STOP;
        return true;
    }
}
