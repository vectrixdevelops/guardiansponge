package io.github.connorhartley.guardian.launch.component;

import com.ichorpowered.guardian.api.GuardianState;
import io.github.connorhartley.guardian.GuardianPlugin;
import io.github.connorhartley.guardian.detection.GuardianDetectionRegistry;
import io.github.connorhartley.guardian.detection.check.GuardianCheckRegistry;
import io.github.connorhartley.guardian.detection.heuristics.GuardianHeuristicRegistry;
import io.github.connorhartley.guardian.detection.penalty.GuardianPenaltyRegistry;
import io.github.connorhartley.guardian.launch.Bootstrap;
import io.github.connorhartley.guardian.launch.Component;
import io.github.connorhartley.guardian.launch.ComponentState;
import io.github.connorhartley.guardian.launch.exception.ComponentException;
import io.github.connorhartley.guardian.launch.message.ComponentMessage;
import io.github.connorhartley.guardian.launch.message.SimpleComponentMessage;
import io.github.connorhartley.guardian.phase.GuardianPhaseRegistry;
import io.github.connorhartley.guardian.sequence.GuardianSequenceRegistry;
import io.github.connorhartley.guardian.util.ConsoleUtil;
import org.fusesource.jansi.Ansi;
import org.slf4j.Logger;

public class RegistryPluginComponent implements Component {

    private final Logger logger;
    private final GuardianPlugin plugin;

    private final String initPrefix = ConsoleUtil.of(Ansi.Color.CYAN, " INIT ");

    private ComponentState componentState = ComponentState.STOP;

    public RegistryPluginComponent(final Logger logger, final GuardianPlugin plugin) {
        this.logger = logger;
        this.plugin = plugin;
    }

    @Override
    public <I extends ComponentMessage, O> O handle(Bootstrap.ComponentRequest<I, O> componentRequest, I input) throws ComponentException {
        switch (componentRequest.getId()) {
            case 0: {
                if (input instanceof SimpleComponentMessage) return (O) this.componentState;
            }
            case 1: {
                return (O) this.startup();
            }
            case 2: {
                return (O) this.restart();
            }
            case 3: {
                return (O) this.shutdown();
            }
        }
        return null;
    }

    public Boolean startup() {
        this.componentState = ComponentState.PREPARE;

        this.plugin.stateProvider.provide(GuardianState.PRE_INITIALIZATION);

        this.logger.info(this.initPrefix + "Initializing registries.");

        this.plugin.detectionRegistryProvider.provide(new GuardianDetectionRegistry(this.plugin));
        this.plugin.checkRegistryProvider.provide(new GuardianCheckRegistry(this.plugin));
        this.plugin.sequenceRegistryProvider.provide(new GuardianSequenceRegistry(this.plugin));
        this.plugin.heuristicRegistryProvider.provide(new GuardianHeuristicRegistry(this.plugin));
        this.plugin.penaltyRegistryProvider.provide(new GuardianPenaltyRegistry(this.plugin));
        this.plugin.phaseRegistryProvider.provide(new GuardianPhaseRegistry(this.plugin));

        this.componentState = ComponentState.START;

        return true;
    }

    public Boolean restart() {
        this.componentState = ComponentState.PREPARE;



        this.componentState = ComponentState.START;

        return true;
    }

    public Boolean shutdown() {

        this.componentState = ComponentState.STOP;

        return true;
    }

}
