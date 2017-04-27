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

import com.google.common.reflect.TypeToken;
import io.github.connorhartley.guardian.Guardian;
import io.github.connorhartley.guardian.sequence.context.Context;
import io.github.connorhartley.guardian.sequence.context.ContextContainer;
import io.github.connorhartley.guardian.detection.Detection;
import io.github.connorhartley.guardian.storage.container.StorageKey;
import org.spongepowered.api.data.property.block.MatterProperty;
import org.spongepowered.api.util.Direction;

import java.util.HashMap;
import java.util.Map;

public class MaterialSpeedContext extends Context {

    private double gasSpeedModifier = 1.045;
    private double solidSpeedModifier = 1.025;
    private double liquidSpeedModifier = 1.015;

    private ContextContainer valuation;
    private boolean stopped = false;

    public MaterialSpeedContext(Guardian plugin, Detection detection) {
        super(plugin, detection);

        if (this.getDetection().getConfiguration().get(new StorageKey<>("material-values"), new TypeToken<HashMap<String, Double>>(){}).isPresent()) {
            Map<String, Double> storageValueMap = this.getDetection().getConfiguration().get(new StorageKey<>("material-values"),
                    new TypeToken<HashMap<String, Double>>(){}).get().getValue();

            this.gasSpeedModifier = storageValueMap.get("gas");
            this.solidSpeedModifier = storageValueMap.get("solid");
            this.liquidSpeedModifier = storageValueMap.get("liquid");
        }
    }

    @Override
    public ContextContainer getValuation() {
        return this.valuation;
    }

    @Override
    public void start(ContextContainer valuation) {
        this.valuation = valuation;
        this.stopped = false;

        this.getValuation().set(MaterialSpeedContext.class, "speed_amplifier", 1.0);
        this.getValuation().set(MaterialSpeedContext.class, "update", 0);
    }

    @Override
    public void update(ContextContainer valuation) {
        this.valuation = valuation;

        if (!this.getPlayer().getLocation().getBlockRelative(Direction.DOWN).getProperty(MatterProperty.class).isPresent())
            this.getValuation().<MaterialSpeedContext, Double>transform(
                    MaterialSpeedContext.class, "speed_amplifier", oldValue -> oldValue * this.gasSpeedModifier);

        MatterProperty matterProperty = this.getPlayer().getLocation().getBlockRelative(Direction.DOWN).getProperty(MatterProperty.class).get();

        if (matterProperty.getValue() != null) {
            if (matterProperty.getValue().equals(MatterProperty.Matter.LIQUID)) {
                this.getValuation().<MaterialSpeedContext, Double>transform(
                        MaterialSpeedContext.class, "speed_amplifier", oldValue -> oldValue * this.liquidSpeedModifier);
            } else if (matterProperty.getValue().equals(MatterProperty.Matter.GAS)) {
                this.getValuation().<MaterialSpeedContext, Double>transform(
                        MaterialSpeedContext.class, "speed_amplifier", oldValue -> oldValue * this.gasSpeedModifier);
            } else {
                this.getValuation().<MaterialSpeedContext, Double>transform(
                        MaterialSpeedContext.class, "speed_amplifier", oldValue -> oldValue * this.solidSpeedModifier);
            }
        }

        this.getValuation().<MaterialSpeedContext, Integer>transform(MaterialSpeedContext.class, "update", oldValue -> oldValue + 1);
    }

    @Override
    public void stop(ContextContainer valuation) {
        this.valuation = valuation;

        this.stopped = true;
    }

    @Override
    public boolean hasStopped() {
        return this.stopped;
    }
}
