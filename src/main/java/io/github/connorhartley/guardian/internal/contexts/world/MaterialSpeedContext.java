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
import io.github.connorhartley.guardian.context.valuation.ContextValuation;
import io.github.connorhartley.guardian.detection.Detection;
import org.spongepowered.api.data.property.block.MatterProperty;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.util.Direction;

import java.util.HashMap;
import java.util.Map;

public class MaterialSpeedContext extends Context {

    private double gasSpeedModifier = 1.045;
    private double solidSpeedModifier = 1.025;
    private double liquidSpeedModifier = 1.015;

    private long updateAmount = 0;
    private boolean suspended = false;

    public MaterialSpeedContext(Guardian plugin, Detection detection, ContextValuation contextValuation, Player player) {
        super(plugin, detection, contextValuation, player);

        if (this.getDetection().getConfiguration().get("material-values", new HashMap<String, Double>()).isPresent()) {
            Map<String, Double> storageValueMap = this.getDetection().getConfiguration().get("material-values",
                    new HashMap<String, Double>()).get().getValue();

            this.gasSpeedModifier = storageValueMap.get("gas");
            this.solidSpeedModifier = storageValueMap.get("solid");
            this.liquidSpeedModifier = storageValueMap.get("liquid");
        }

        this.getContextValuation().set(ContextTypes.SPEED_AMPLIFIER);
    }

    @Override
    public void update() {
        if (!this.getPlayer().getLocation().getBlockRelative(Direction.DOWN).getProperty(MatterProperty.class).isPresent())
            this.getContextValuation().transform(ContextTypes.SPEED_AMPLIFIER, oldValue -> oldValue * this.gasSpeedModifier);
        MatterProperty matterProperty = this.getPlayer().getLocation().getBlockRelative(Direction.DOWN).getProperty(MatterProperty.class).get();

        if (matterProperty.getValue() != null) {
            if (matterProperty.getValue().equals(MatterProperty.Matter.LIQUID)) {
                this.getContextValuation().transform(ContextTypes.SPEED_AMPLIFIER, oldValue -> oldValue * this.liquidSpeedModifier);
            } else if (matterProperty.getValue().equals(MatterProperty.Matter.GAS)) {
                this.getContextValuation().transform(ContextTypes.SPEED_AMPLIFIER, oldValue -> oldValue * this.gasSpeedModifier);
            } else {
                this.getContextValuation().transform(ContextTypes.SPEED_AMPLIFIER, oldValue -> oldValue * this.solidSpeedModifier);
            }
        }
        this.updateAmount += 1;
    }

    @Override
    public void suspend() {
        this.suspended = true;
    }

    @Override
    public boolean isSuspended() {
        return this.suspended;
    }

}
