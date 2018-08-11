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
import com.ichorpowered.guardian.api.detection.DetectionController;
import com.ichorpowered.guardian.api.game.model.ModelRegistry;
import com.ichorpowered.guardian.api.game.model.value.key.GameKey;
import com.ichorpowered.guardian.api.game.model.value.key.GameKeyRegistry;
import com.ichorpowered.guardian.api.sequence.SequenceController;
import com.ichorpowered.guardian.api.sequence.SequenceRegistry;
import com.ichorpowered.guardian.sponge.feature.CheckFeature;
import com.ichorpowered.guardian.sponge.feature.GameFeature;
import com.ichorpowered.guardian.sponge.sequence.SequenceControllerImpl;
import com.ichorpowered.guardian.sponge.sequence.SequenceListener;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.service.permission.PermissionDescription;
import org.spongepowered.api.service.permission.PermissionService;
import org.spongepowered.api.text.Text;

@Singleton
public final class GuardianImpl {

    private final GuardianPlugin plugin;

    private GameFeature bukkitKeys;
    private CheckFeature guardianChecks;
    private SequenceListener sequenceListener;
    private SequenceControllerImpl.SequenceTask sequenceTask;

    @Inject
    public GuardianImpl(final GuardianPlugin plugin, final SequenceController sequenceController,
                        final DetectionController detectionController, final GameKey.Factory keyFactory,
                        final GameKeyRegistry keyRegistry, final SequenceRegistry sequenceRegistry,
                        final ModelRegistry modelRegistry) {
        this.plugin = plugin;

        this.bukkitKeys = new GameFeature(keyFactory, keyRegistry);
        this.guardianChecks = new CheckFeature(plugin, detectionController, sequenceRegistry);
        this.sequenceListener = new SequenceListener(modelRegistry, sequenceController, this.bukkitKeys);
        this.sequenceTask = new SequenceControllerImpl.SequenceTask(this.plugin, sequenceController);
    }

    public void loadConfiguration() {
        Guardian.getGlobalConfiguration().load(false, false);
    }

    public void loadKeys() {
        this.bukkitKeys.create();
        this.bukkitKeys.register();
    }

    public void loadDetections() {
        this.guardianChecks.create();
        this.guardianChecks.register();
        this.guardianChecks.categorize();
    }

    public void unloadDetections() {
        this.guardianChecks.unregister();
    }

    public void startSequence() {
        Sponge.getEventManager().registerListeners(this.plugin, this.sequenceListener);
        this.sequenceTask.start();
    }

    public void stopSequence() {
        this.sequenceTask.stop();
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
