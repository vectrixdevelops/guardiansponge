package io.github.connorhartley.guardian.launch.component;

import com.ichorpowered.guardian.api.SimpleGuardian;
import com.ichorpowered.guardian.api.event.GuardianEvent;
import com.ichorpowered.guardian.api.event.GuardianListener;
import com.me4502.modularframework.ModuleController;
import com.me4502.modularframework.ShadedModularFramework;
import io.github.connorhartley.guardian.GuardianPlugin;
import io.github.connorhartley.guardian.PluginInfo;
import io.github.connorhartley.guardian.launch.Bootstrap;
import io.github.connorhartley.guardian.launch.Component;
import io.github.connorhartley.guardian.launch.ComponentState;
import io.github.connorhartley.guardian.launch.exception.ComponentException;
import io.github.connorhartley.guardian.launch.message.ComponentMessage;
import io.github.connorhartley.guardian.launch.message.SimpleComponentMessage;
import io.github.connorhartley.guardian.util.ConsoleUtil;
import net.kyori.event.ASMEventExecutorFactory;
import net.kyori.event.SimpleEventBus;
import org.fusesource.jansi.Ansi;
import org.slf4j.Logger;
import org.spongepowered.api.Platform;
import org.spongepowered.api.Sponge;

public class CorePluginComponent implements Component {

    private final Logger logger;
    private final GuardianPlugin plugin;

    private ComponentState componentState = ComponentState.STOP;

    public CorePluginComponent(final Logger logger, final GuardianPlugin plugin) {
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
                if (input instanceof SimpleComponentMessage) return (O) this.startup((SimpleComponentMessage) input);
                else throw new ComponentException("Input was of an incorrect type.");
            }
            case 2: {
                // This component should not restart.
                return (O) Boolean.valueOf(true);
            }
            case 3: {
                if (input instanceof SimpleComponentMessage) return (O) this.shutdown((SimpleComponentMessage) input);
                else throw new ComponentException("Input was of an incorrect type.");
            }
        }
        return null;
    }

    public Boolean startup(SimpleComponentMessage componentMessage) {
        this.componentState = ComponentState.PREPARE;

        SimpleGuardian.setInstance(this.plugin);

        if (PluginInfo.EXPERIMENTAL) {
            this.logger.warn(ConsoleUtil.of(Ansi.Color.RED, "You are using an experimental build of Guardian."));
            this.logger.warn(ConsoleUtil.of(Ansi.Color.RED, "This may not be ready for a production environment. Use at your own risk!"));
        }

        this.logger.info(ConsoleUtil.of("Guardian v{} for Sponge {} and Minecraft {}",
                PluginInfo.VERSION,
                Sponge.getPlatform().getContainer(Platform.Component.API).getVersion().orElse("?").substring(0, 5),
                Sponge.getPlatform().getContainer(Platform.Component.GAME).getVersion().orElse("?")));

        ModuleController<GuardianPlugin> moduleController = ShadedModularFramework.registerModuleController(this.plugin, Sponge.getGame());
        moduleController.setPluginContainer(this.plugin.getPluginContainer());

        SimpleEventBus<GuardianEvent, GuardianListener> eventBus = new SimpleEventBus<>(new ASMEventExecutorFactory<GuardianEvent, GuardianListener>());

        this.plugin.timeProvider.provide(componentMessage.getTime());
        this.plugin.eventBusProvider.provide(eventBus);
        this.plugin.moduleControllerProvider.provide(moduleController);

        this.logger.info(ConsoleUtil.of("LOADED [ me4502/modularframework v1.8.5, kyoripowered/event v1.0.0 ]"));

        this.componentState = ComponentState.START;

        return true;
    }

    public Boolean shutdown(SimpleComponentMessage componentMessage) {
        this.logger.info(ConsoleUtil.of("Guardian has shutdown."));

        this.componentState = ComponentState.STOP;

        return true;
    }

}
