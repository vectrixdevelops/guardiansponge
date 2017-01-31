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
package io.github.connorhartley.guardian.sequence;

import io.github.connorhartley.guardian.data.Keys;
import io.github.connorhartley.guardian.data.handler.SequenceHandlerData;
import io.github.connorhartley.guardian.detection.check.CheckManager;
import io.github.connorhartley.guardian.detection.check.CheckProvider;
import io.github.connorhartley.guardian.event.sequence.SequenceBeginEvent;
import io.github.connorhartley.guardian.event.sequence.SequenceFinishEvent;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.entity.living.player.User;
import org.spongepowered.api.event.Event;
import org.spongepowered.api.event.cause.Cause;
import org.spongepowered.api.event.cause.NamedCause;
import org.spongepowered.api.service.user.UserStorageService;

import java.util.ArrayList;
import java.util.List;

public class SequenceManager implements SequenceInvoker {

    private final Object plugin;
    private final CheckManager checkManager;
    private final List<SequenceBlueprint> blueprints = new ArrayList<>();

    public SequenceManager(Object plugin, CheckManager checkManager) {
        this.plugin = plugin;
        this.checkManager = checkManager;
    }

    @Override
    public void invoke(User user, Event event, Cause cause) {
        if (!user.get(Keys.GUARDIAN_SEQUENCE_HANDLE).isPresent()) user.offer((Sponge.getDataManager().getManipulatorBuilder(SequenceHandlerData.class).get()).create());

        user.get(Keys.GUARDIAN_SEQUENCE_HANDLE).ifPresent(sequences -> {
            sequences.forEach(sequence -> sequence.pass(user, event, cause));
            sequences.removeIf(Sequence::isCancelled);
            sequences.removeIf(Sequence::hasExpired);
            sequences.removeIf(sequence -> {
               if (!sequence.isFinished()) {
                   return false;
               }

               SequenceFinishEvent attempt = new SequenceFinishEvent(sequence, user, sequence.getSequenceResult().build(), Cause.of(NamedCause.source(this.plugin)));
               Sponge.getEventManager().post(attempt);

               if (attempt.isCancelled()) {
                   return true;
               }

               CheckProvider checkProvider = sequence.getProvider();
               this.checkManager.post(checkProvider, sequence, user, cause);
               return true;
            });

            this.blueprints.stream()
                    .filter(blueprint -> !sequences.contains(blueprint))
                    .forEach(blueprint -> {
                        Sequence sequence = blueprint.create(user);

                        SequenceBeginEvent attempt = new SequenceBeginEvent(sequence, user, sequence.getSequenceResult().build(), Cause.of(NamedCause.source(this.plugin)));
                        Sponge.getEventManager().post(attempt);

                        if (attempt.isCancelled()) {
                            return;
                        }

                        if (sequence.pass(user, event, cause)) {
                            if (sequence.isCancelled()) {
                                return;
                            }

                            if (sequence.isFinished()) {
                                CheckProvider checkProvider = sequence.getProvider();
                                this.checkManager.post(checkProvider, sequence, user, cause);
                                return;
                            }

                            sequences.add(sequence);
                            user.offer(((SequenceHandlerData.Builder) Sponge.getDataManager().getManipulatorBuilder(SequenceHandlerData.class).get()).createFrom(sequences));
                        }
                    });
        });
    }

    public void cleanup(User user) {
        user.remove(Keys.GUARDIAN_SEQUENCE_HANDLE);
    }

    public void cleanupAll() {
        Sponge.getServer().getOnlinePlayers().forEach(player -> {
            Sponge.getServiceManager().provide(UserStorageService.class).ifPresent(userStorageService -> {
                userStorageService.get(player.getUniqueId()).ifPresent(user -> user.remove(Keys.GUARDIAN_SEQUENCE_HANDLE));
            });
        });
    }

    public void register(CheckProvider checkProvider) {
        this.blueprints.add(checkProvider.getSequence());
    }

    public void unregister(CheckProvider checkProvider) {
        this.blueprints.removeIf(blueprint -> blueprint.getCheckProvider().equals(checkProvider));
    }

}
