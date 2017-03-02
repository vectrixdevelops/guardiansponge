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
package io.github.connorhartley.guardian.internal.contexts.world;

import io.github.connorhartley.guardian.Guardian;
import io.github.connorhartley.guardian.context.Context;
import io.github.connorhartley.guardian.context.ContextTypes;
import io.github.connorhartley.guardian.context.container.ContextContainer;
import org.spongepowered.api.data.property.block.MatterProperty;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.util.Direction;

public class BlockSpeedContext extends Context {

    private Player player;
    private ContextContainer contextContainer;

    private boolean suspended = false;

    public BlockSpeedContext(Guardian plugin, Player player) {
        super(plugin);
        this.player = player;
        this.contextContainer = new ContextContainer(this);
        this.contextContainer.set(ContextTypes.SPEED_AMPLIFIER);
    }

    @Override
    public void update() {
        System.out.println("Update Block Speed.");
        if (!this.player.getLocation().getBlockRelative(Direction.DOWN).getProperty(MatterProperty.class).isPresent())
            this.contextContainer.transform(ContextTypes.SPEED_AMPLIFIER, oldValue -> oldValue * 1.085);
        MatterProperty matterProperty = this.player.getLocation().getBlockRelative(Direction.DOWN).getProperty(MatterProperty.class).get();

        if (matterProperty.getValue() != null) {
            if (matterProperty.getValue().equals(MatterProperty.Matter.LIQUID)) {
                this.contextContainer.transform(ContextTypes.SPEED_AMPLIFIER, oldValue -> oldValue * 1.025);
            } else if (matterProperty.getValue().equals(MatterProperty.Matter.GAS)) {
                this.contextContainer.transform(ContextTypes.SPEED_AMPLIFIER, oldValue -> oldValue * 1.085);
            } else {
                this.contextContainer.transform(ContextTypes.SPEED_AMPLIFIER, oldValue -> oldValue * 1.065);
            }
        }
    }

    @Override
    public void suspend() {
        this.suspended = true;
    }

    @Override
    public boolean isSuspended() {
        return this.suspended;
    }

    @Override
    public ContextContainer getContainer() {
        return this.contextContainer;
    }

}
