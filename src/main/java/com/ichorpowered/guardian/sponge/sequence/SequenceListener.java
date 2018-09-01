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
package com.ichorpowered.guardian.sponge.sequence;

import com.google.common.reflect.TypeToken;
import com.ichorpowered.guardian.api.game.GameReference;
import com.ichorpowered.guardian.api.game.model.Model;
import com.ichorpowered.guardian.api.game.model.ModelRegistry;
import com.ichorpowered.guardian.api.game.model.value.key.GameKeys;
import com.ichorpowered.guardian.api.sequence.SequenceController;
import com.ichorpowered.guardian.common.game.GameReferenceImpl;
import com.ichorpowered.guardian.sponge.GuardianPlugin;
import com.ichorpowered.guardian.sponge.feature.GameFeature;
import com.me4502.precogs.Precogs;
import com.me4502.precogs.detection.CommonDetectionTypes;
import com.me4502.precogs.service.AntiCheatService;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.event.Event;
import org.spongepowered.api.event.Listener;
import org.spongepowered.api.event.entity.MoveEntityEvent;
import org.spongepowered.api.event.entity.SpawnEntityEvent;
import org.spongepowered.api.event.filter.cause.First;
import org.spongepowered.api.event.filter.cause.Root;
import org.spongepowered.api.event.network.ClientConnectionEvent;
import org.spongepowered.api.plugin.PluginContainer;

import java.util.UUID;
import java.util.concurrent.TimeUnit;

public class SequenceListener {

    private final GuardianPlugin plugin;
    private final ModelRegistry modelRegistry;
    private final SequenceController<Event> sequenceController;
    private final GameFeature keys;

    public SequenceListener(final GuardianPlugin plugin, final ModelRegistry modelRegistry,
                            final SequenceController sequenceController, final GameFeature gameFeature) {
        this.plugin = plugin;
        this.modelRegistry = modelRegistry;
        this.sequenceController = sequenceController;
        this.keys = gameFeature;
    }

    @Listener
    public void onPlayerJoin(final ClientConnectionEvent.Join event, final @First Player player) {
        final GameReference<Player> gameReference = new GameReferenceImpl<>(player.getUniqueId().toString(), Player.class, id -> Sponge.getServer().getPlayer(UUID.fromString(id)).orElse(null));

        final Model model = this.modelRegistry.create("player", gameReference);
        model.getComponent("model-geometry").ifPresent(component -> component.set(GameKeys.JOIN_PING, player.getConnection().getLatency()));

        this.keys.populate(model);
        this.sequenceController.getPlayerGroupResource().add(gameReference);
        this.sequenceController.getPlayerResource().add(gameReference);
    }

    @Listener
    public void onPlayerLeave(final ClientConnectionEvent.Disconnect event, final @First Player player) {
        this.sequenceController.getPlayerResource().get(player.getUniqueId().toString()).ifPresent(gameReference -> {
            this.sequenceController.clean(gameReference, true);
            this.sequenceController.getPlayerResource().remove(gameReference.getGameId());
            this.sequenceController.getPlayerGroupResource().remove(gameReference);
            this.modelRegistry.remove(gameReference);
        });
    }

    @Listener
    public void onPlayerTeleport(final MoveEntityEvent.Teleport event, final @First Player player) {
        Sponge.getServiceManager().provide(AntiCheatService.class).ifPresent(antiCheatService ->
                antiCheatService.requestTimedBypassTicket(
                        player,
                        CommonDetectionTypes.getDetectionTypesFor(CommonDetectionTypes.Category.MOVEMENT),
                        this.plugin,
                        2500,
                        TimeUnit.MILLISECONDS
                )
        );
    }

    @Listener
    public void onPlayerRespawn(final SpawnEntityEvent event, @Root Player player) {
        Sponge.getServiceManager().provide(AntiCheatService.class).ifPresent(antiCheatService ->
                antiCheatService.requestTimedBypassTicket(
                        player,
                        CommonDetectionTypes.getDetectionTypesFor(CommonDetectionTypes.Category.MOVEMENT),
                        this.plugin,
                        2500,
                        TimeUnit.MILLISECONDS
                )
        );
    }

    // Sequence Handlers

    @Listener
    public void onPlayerMove(final MoveEntityEvent event, final @First Player player) {
        this.sequenceController.getPlayerResource().get(player.getUniqueId().toString()).ifPresent(gameReference -> {
            this.sequenceController.invokeObserver(event, gameReference, new SequenceContextImpl()
                    .add("root:player", new TypeToken<GameReference<Player>>() {}, (GameReference<Player>) gameReference)
                    .add("root:force_observe", TypeToken.of(Boolean.class), false),
                    eventSequence -> !event.getCause().root().getClass().equals(PluginContainer.class));
        });
    }

}
