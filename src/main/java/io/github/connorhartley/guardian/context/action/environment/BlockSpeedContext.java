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
package io.github.connorhartley.guardian.context.action.environment;

import io.github.connorhartley.guardian.context.Context;
import io.github.connorhartley.guardian.context.ContextKeys;
import io.github.connorhartley.guardian.context.TimeContext;
import io.github.connorhartley.guardian.util.ContextValue;
import org.spongepowered.api.block.BlockTypes;
import org.spongepowered.api.data.property.block.MatterProperty;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.entity.living.player.User;
import org.spongepowered.api.event.Event;

import java.util.HashMap;
import java.util.Optional;

public class BlockSpeedContext implements Context<Double>, TimeContext {

    public BlockSpeedContext() {}

    private HashMap<String, ContextValue<Double>> values = new HashMap<>();

    private Player player;

    private boolean ready = false;

    @Override
    public void start(User user, Event event) {
        if (user.getPlayer().isPresent()) {
             this.player = user.getPlayer().get();
        } else return;


        this.values.put(ContextKeys.BLOCK_SPEED_MODIFIER, new ContextValue<Double>().set(1.0));

        this.ready = true;
    }

    @Override
    public void update() {
        this.player.getLocation().add(0, 0, 0).getBlock().getProperty(MatterProperty.class).ifPresent(matterProperty -> {
            if (matterProperty.getValue().equals(MatterProperty.Matter.LIQUID)) {
                // Floating in a liquid.

                this.player.getLocation().add(0, -1, 0).getBlock().getProperty(MatterProperty.class).ifPresent(matterPropertySolid -> {
                    if (matterPropertySolid.getValue().equals(MatterProperty.Matter.SOLID)) {
                        if (this.values.containsKey(ContextKeys.BLOCK_SPEED_MODIFIER)) {
                            this.values.replace(ContextKeys.BLOCK_SPEED_MODIFIER, this.values.get(ContextKeys.BLOCK_SPEED_MODIFIER).transform(oldValue -> oldValue *= 1.2));
                        }
                    }
                });
            } else {
                this.player.getLocation().add(0, -1, 0).getBlock().getProperty(MatterProperty.class).ifPresent(matterPropertySolid -> {
                    if (matterPropertySolid.getValue().equals(MatterProperty.Matter.SOLID)) {
                        // Walking on the floor.
                        if (this.values.containsKey(ContextKeys.BLOCK_SPEED_MODIFIER)) {
                            this.values.replace(ContextKeys.BLOCK_SPEED_MODIFIER, this.values.get(ContextKeys.BLOCK_SPEED_MODIFIER).transform(oldValue -> oldValue *= 1.6));
                        }

                        if (this.player.getLocation().add(0, -1, 0).getBlockType().equals(BlockTypes.ICE)) {
                            if (this.values.containsKey(ContextKeys.BLOCK_SPEED_MODIFIER)) {
                                this.values.replace(ContextKeys.BLOCK_SPEED_MODIFIER, this.values.get(ContextKeys.BLOCK_SPEED_MODIFIER).transform(oldValue -> oldValue *= 1.6));
                            }
                        }

                        if (this.player.getLocation().add(0, 1, 0).getBlockType().equals(BlockTypes.WEB) ||
                                this.player.getLocation().add(0, 0, 0).getBlockType().equals(BlockTypes.WEB)) {

                            if (this.values.containsKey(ContextKeys.BLOCK_SPEED_MODIFIER)) {
                                this.values.replace(ContextKeys.BLOCK_SPEED_MODIFIER, this.values.get(ContextKeys.BLOCK_SPEED_MODIFIER).transform(oldValue -> oldValue *= 0.14));
                            }
                        }
                    }
                });
            }
        });
    }

    @Override
    public void stop() {
        this.ready = false;
    }

    @Override
    public boolean isReady() {
        return this.ready;
    }

    @Override
    public String getName() {
        return "block_speed_modifier";
    }

    @Override
    public HashMap<String, ContextValue<Double>> getValues() {
        return this.values;
    }

    @Override
    public Optional<TimeContext> asTimed() {
        return Optional.of(this);
    }
}
