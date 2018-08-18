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
import com.google.inject.Singleton;
import com.ichorpowered.guardian.api.Guardian;
import com.ichorpowered.guardian.api.GuardianPlatform;
import com.ichorpowered.guardian.api.detection.DetectionController;
import com.ichorpowered.guardian.api.game.model.ModelRegistry;
import com.ichorpowered.guardian.api.game.model.value.key.GameKey;
import com.ichorpowered.guardian.api.game.model.value.key.GameKeyRegistry;
import com.ichorpowered.guardian.api.sequence.SequenceController;
import com.ichorpowered.guardian.api.sequence.SequenceRegistry;
import com.ichorpowered.guardian.common.detection.stage.type.CheckStageImpl;
import com.ichorpowered.guardian.common.detection.stage.type.HeuristicStageImpl;
import com.ichorpowered.guardian.common.detection.stage.type.PenaltyStageImpl;
import com.ichorpowered.guardian.common.util.ConsoleUtil;
import com.ichorpowered.guardian.sponge.feature.CheckFeature;
import com.ichorpowered.guardian.sponge.feature.GameFeature;
import com.ichorpowered.guardian.sponge.sequence.SequenceControllerImpl;
import com.ichorpowered.guardian.sponge.sequence.SequenceListener;
import com.ichorpowered.guardian.sponge.service.BypassServiceImpl;
import com.me4502.precogs.service.AntiCheatService;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.fusesource.jansi.Ansi;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.service.permission.PermissionDescription;
import org.spongepowered.api.service.permission.PermissionService;
import org.spongepowered.api.text.Text;

import java.util.stream.StreamSupport;

@Singleton
public final class GuardianImpl {

    private final GuardianPlugin plugin;
    private final GuardianPlatform platform;
    private final DetectionController detectionController;
    private final SequenceController sequenceController;

    private GameFeature gameFeature;
    private CheckFeature checkFeature;
    private SequenceListener sequenceListener;
    private SequenceControllerImpl.SequenceTask sequenceTask;

    @Inject
    public GuardianImpl(final GuardianPlugin plugin, final GuardianPlatform platform, final SequenceController sequenceController,
                        final DetectionController detectionController, final GameKey.Factory keyFactory,
                        final GameKeyRegistry keyRegistry, final SequenceRegistry sequenceRegistry,
                        final ModelRegistry modelRegistry) {
        this.plugin = plugin;
        this.platform = platform;
        this.detectionController = detectionController;
        this.sequenceController = sequenceController;

        this.gameFeature = new GameFeature(keyFactory, keyRegistry);
        this.checkFeature = new CheckFeature(plugin, detectionController, sequenceRegistry);
        this.sequenceListener = new SequenceListener(modelRegistry, sequenceController, this.gameFeature);
        this.sequenceTask = new SequenceControllerImpl.SequenceTask(this.plugin, sequenceController);
    }

    public void initialize() {
        this.plugin.getLogger().info(ConsoleUtil.of(Ansi.Color.CYAN, false, "     _____               _ _             "));
        this.plugin.getLogger().info(ConsoleUtil.of(Ansi.Color.CYAN, false, "    |   __|_ _ ___ ___ _| |_|___ ___     "));
        this.plugin.getLogger().info(ConsoleUtil.of(Ansi.Color.CYAN, false, "    |  |  | | | .'|  _| . | | .'|   |    "));
        this.plugin.getLogger().info(ConsoleUtil.of(Ansi.Color.CYAN, false, "    |_____|___|__,|_| |___|_|__,|_|_|    "));

        this.plugin.getLogger().info(ConsoleUtil.of(" "));

        this.plugin.getLogger().info(ConsoleUtil.of(Ansi.Color.BLACK, true, "  Starting v{} on {} {} and Minecraft {}  ",
                this.platform.getVersion(),
                this.platform.getPlatformName(),
                this.platform.getPlatformVersion(),
                this.platform.getGameVersion()
        ));

        this.plugin.getLogger().info(ConsoleUtil.of(" "));
    }

    public void loadConfiguration() {
        this.plugin.getLogger().info(ConsoleUtil.of("Loading configuration..."));

        Guardian.getGlobalConfiguration().load(false, false);
    }

    public void loadKeys() {
        this.plugin.getLogger().info(ConsoleUtil.of("Loading models..."));

        this.gameFeature.create();
        this.gameFeature.register();
    }

    public void loadDetections() {
        this.plugin.getLogger().info(ConsoleUtil.of("Loading stages..."));

        this.checkFeature.create();
        this.checkFeature.register();

        final int detectionCount = (int) StreamSupport.stream(this.detectionController.spliterator(), false).count();
        final int checkCount = StreamSupport.stream(this.detectionController.spliterator(), false)
                .mapToInt(detection -> detection.getStageCycle().sizeFor(CheckStageImpl.class)).sum();
        final int heuristicCount = StreamSupport.stream(this.detectionController.spliterator(), false)
                .mapToInt(detection -> detection.getStageCycle().sizeFor(HeuristicStageImpl.class)).sum();
        final int penaltyCount = StreamSupport.stream(this.detectionController.spliterator(), false)
                .mapToInt(detection -> detection.getStageCycle().sizeFor(PenaltyStageImpl.class)).sum();

        this.plugin.getLogger().info(ConsoleUtil.of(Ansi.Color.CYAN, false, "Loaded {} punishment(s), {} heuristic(s) and {} check(s) for {} detection(s).",
                String.valueOf(penaltyCount),
                String.valueOf(heuristicCount),
                String.valueOf(checkCount),
                String.valueOf(detectionCount)
        ));
    }

    public void unloadDetections() {
        this.plugin.getLogger().info(ConsoleUtil.of("Unloading stages..."));

        this.checkFeature.unregister();
    }

    public void loadService() {
        this.plugin.getLogger().info(ConsoleUtil.of("Registering service... [Precogs]"));

        Sponge.getServiceManager().setProvider(this.plugin, AntiCheatService.class, new BypassServiceImpl(this.detectionController, this.sequenceController, this.plugin));
        this.checkFeature.categorize();
    }

    public void startSequence() {
        this.plugin.getLogger().info(ConsoleUtil.of("Starting detection strategies..."));

        Sponge.getEventManager().registerListeners(this.plugin, this.sequenceListener);
        this.sequenceTask.start();
    }

    public void stopSequence() {
        this.plugin.getLogger().info(ConsoleUtil.of("Stopping detection strategies..."));

        this.sequenceTask.stop();
        Sponge.getEventManager().unregisterListeners(this.sequenceListener);
    }

    public void registerPermissions() {
        Sponge.getServiceManager().provide(PermissionService.class).ifPresent(permissionService -> {
            permissionService.newDescriptionBuilder(this.plugin)
                    .id("guardian.admin.detection.bypass")
                    .description(Text.of("Allows a player to bypass all detections."))
                    .assign(PermissionDescription.ROLE_ADMIN, true)
                    .register();

            permissionService.newDescriptionBuilder(this.plugin)
                    .id("guardian.admin.detection.<Detection>.bypass")
                    .description(Text.of("Allows a player to bypass the specified detection."))
                    .assign(PermissionDescription.ROLE_ADMIN, true)
                    .register();

            permissionService.newDescriptionBuilder(this.plugin)
                    .id("guardian.chat.notification")
                    .description(Text.of("Allows a player to receive chat notifications on reported players."))
                    .assign(PermissionDescription.ROLE_STAFF, true)
                    .register();
        });
    }

}
