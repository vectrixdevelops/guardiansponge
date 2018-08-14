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
package com.ichorpowered.guardian.sponge;

import com.google.inject.Inject;
import com.google.inject.Injector;
import com.ichorpowered.guardian.api.Guardian;
import com.ichorpowered.guardian.common.CommonGuardianPlatform;
import com.ichorpowered.guardian.common.detection.stage.type.CheckStageImpl;
import com.ichorpowered.guardian.common.detection.stage.type.HeuristicStageImpl;
import com.ichorpowered.guardian.common.detection.stage.type.PenaltyStageImpl;
import com.ichorpowered.guardian.common.util.ConsoleUtil;
import com.ichorpowered.guardian.sponge.detection.DetectionWrapper;
import com.ichorpowered.guardian.sponge.inject.SpongeGuardianModule;
import com.ichorpowered.guardian.sponge.service.BypassServiceImpl;
import com.me4502.precogs.detection.DetectionType;
import com.me4502.precogs.service.AntiCheatService;
import org.fusesource.jansi.Ansi;
import org.slf4j.Logger;
import org.spongepowered.api.Platform;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.config.DefaultConfig;
import org.spongepowered.api.event.Listener;
import org.spongepowered.api.event.game.GameRegistryEvent;
import org.spongepowered.api.event.game.GameReloadEvent;
import org.spongepowered.api.event.game.state.GameInitializationEvent;
import org.spongepowered.api.event.game.state.GameStoppingEvent;
import org.spongepowered.api.plugin.Plugin;

import java.nio.file.Path;
import java.util.stream.StreamSupport;

@Plugin(
        id = PluginInfo.ID,
        name = PluginInfo.NAME,
        version = PluginInfo.VERSION,
//        dependencies = { @Dependency(id = "precogs", version = PluginInfo.PRECOGS_VERSION) },
        description = PluginInfo.DESCRIPTION,
        url = PluginInfo.URL,
        authors = { "Vectrix (Connor Hartley)" }
)
public class GuardianPlugin {

    private final Injector rootInjector;
    private final Logger logger;
    private final Path configPath;

    private Injector injector;
    private GuardianImpl guardian;

    @Inject
    public GuardianPlugin(final Injector injector, final Logger logger,
                          final @DefaultConfig(sharedRoot = false) Path configPath) {
        this.rootInjector = injector;
        this.logger = logger;
        this.configPath = configPath;
    }

    @Listener
    public void onGameInitialization(final GameInitializationEvent event) {
        final long startTime = System.currentTimeMillis();

        final CommonGuardianPlatform platform = new CommonGuardianPlatform(
                Sponge.getPlatform().getContainer(Platform.Component.IMPLEMENTATION).getName(),
                Sponge.getPlatform().getContainer(Platform.Component.IMPLEMENTATION).getVersion().orElse("UNKNOWN"),
                Sponge.getPlatform().getContainer(Platform.Component.GAME).getVersion().orElse("UNKNOWN"),
                PluginInfo.VERSION,
                "bleeding"
        );

        this.injector = this.rootInjector.createChildInjector(new SpongeGuardianModule(this.configPath, platform));

        this.getLogger().info(ConsoleUtil.of("Environment [ Platform: {} {}, Minecraft: {} ]",
                platform.getPlatformName(),
                platform.getPlatformVersion(),
                platform.getGameVersion()
        ));

        this.guardian = this.injector.getInstance(GuardianImpl.class);
        this.guardian.loadConfiguration();
        this.guardian.loadKeys();
        this.guardian.loadDetections();
        this.guardian.loadService();

        final int detectionCount = (int) StreamSupport.stream(Guardian.getDetectionController().spliterator(), false).count();
        final int checkCount = StreamSupport.stream(Guardian.getDetectionController().spliterator(), false)
                .mapToInt(detection -> detection.getStageCycle().sizeFor(CheckStageImpl.class)).sum();
        final int heuristicCount = StreamSupport.stream(Guardian.getDetectionController().spliterator(), false)
                .mapToInt(detection -> detection.getStageCycle().sizeFor(HeuristicStageImpl.class)).sum();
        final int penaltyCount = StreamSupport.stream(Guardian.getDetectionController().spliterator(), false)
                .mapToInt(detection -> detection.getStageCycle().sizeFor(PenaltyStageImpl.class)).sum();

        this.getLogger().info(ConsoleUtil.of(Ansi.Color.YELLOW, "Loaded {} punishment(s), {} heuristic(s) and {} check(s) for {} detection(s).",
                String.valueOf(penaltyCount),
                String.valueOf(heuristicCount),
                String.valueOf(checkCount),
                String.valueOf(detectionCount)
        ));

        this.guardian.startSequence();

        this.getLogger().info(ConsoleUtil.of("Startup complete. ({} sec)",
                String.valueOf((double) (System.currentTimeMillis() - startTime) / 1000)));
    }

    @Listener
    public void onGameRegistry(final GameRegistryEvent.Register<DetectionType> event) {
        if (Guardian.getDetectionController() == null) return;

        Guardian.getDetectionController().forEach(detection -> event.register(new DetectionWrapper(detection, detection.getId(), detection.getName())));
    }

    @Listener
    public void onGameReload(final GameReloadEvent event) {
        if (this.guardian == null) return;
        this.guardian.stopSequence();
        this.guardian.unloadDetections();

        this.guardian.loadDetections();
        this.guardian.startSequence();
    }

    @Listener
    public void onGameStopping(final GameStoppingEvent event) {
        if (this.guardian == null) return;
        this.guardian.stopSequence();
        this.guardian.unloadDetections();
    }

    public Logger getLogger() {
        return this.logger;
    }

}
