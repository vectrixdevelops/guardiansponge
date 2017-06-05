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
import io.github.connorhartley.guardian.detection.Detection;
import io.github.connorhartley.guardian.sequence.capture.CaptureContainer;
import io.github.connorhartley.guardian.sequence.capture.CaptureContext;
import io.github.connorhartley.guardian.storage.StorageSupplier;
import io.github.connorhartley.guardian.storage.container.StorageKey;
import org.spongepowered.api.data.property.block.MatterProperty;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.util.Direction;

import java.io.File;
import java.util.Map;

public class MaterialSpeedContext<E, F extends StorageSupplier<File>> extends CaptureContext<E, F> {

    private double gasSpeedModifier = 1.035;
    private double solidSpeedModifier = 1.02;
    private double liquidSpeedModifier = 1.01;

    public MaterialSpeedContext(Guardian plugin, Detection<E, F> detection) {
        super(plugin, detection);

        if (this.getDetection().getConfiguration().get().get(new StorageKey<>("material-values"), new TypeToken<Map<String, Double>>(){}).isPresent()) {
            Map<String, Double> storageValueMap = this.getDetection().getConfiguration().get().get(new StorageKey<>("material-values"),
                    new TypeToken<Map<String, Double>>(){}).get().getValue();

            this.gasSpeedModifier = storageValueMap.get("gas");
            this.solidSpeedModifier = storageValueMap.get("solid");
            this.liquidSpeedModifier = storageValueMap.get("liquid");
        }
    }

    @Override
    public CaptureContainer start(Player player, CaptureContainer valuation) {
        valuation.set(MaterialSpeedContext.class, "speed_amplifier", 1.0);
        valuation.set(MaterialSpeedContext.class, "amplifier_material_gas", 0);
        valuation.set(MaterialSpeedContext.class, "amplifier_material_liquid", 0);
        valuation.set(MaterialSpeedContext.class, "amplifier_material_solid", 0);
        valuation.set(MaterialSpeedContext.class, "update", 0);

        return valuation;
    }

    @Override
    public CaptureContainer update(Player player, CaptureContainer valuation) {
        MatterProperty matterBelow = player.getLocation().sub(0, 0.75, 0).getBlock().getProperty(MatterProperty.class)
                .orElseGet(() -> new MatterProperty(MatterProperty.Matter.GAS));
        MatterProperty matterInside = player.getLocation().getBlock().getProperty(MatterProperty.class)
                .orElseGet(() -> new MatterProperty(MatterProperty.Matter.GAS));
        MatterProperty matterAbove = player.getLocation().add(0, 1.35, 0).getBlock().getProperty(MatterProperty.class)
                .orElseGet(() -> new MatterProperty(MatterProperty.Matter.GAS));

        if (matterBelow.getValue() == MatterProperty.Matter.GAS && matterInside.getValue() == MatterProperty.Matter.GAS &&
                matterAbove.getValue() == MatterProperty.Matter.GAS) {
            valuation.<MaterialSpeedContext, Double>transform(
                        MaterialSpeedContext.class, "speed_amplifier", oldValue -> oldValue * this.gasSpeedModifier);
            valuation.<MaterialSpeedContext, Integer>transform(
                    MaterialSpeedContext.class, "amplifier_material_gas", oldValue -> oldValue + 1);
        } else if (matterBelow.getValue() == MatterProperty.Matter.LIQUID || matterInside.getValue() == MatterProperty.Matter.LIQUID ||
                matterAbove.getValue() == MatterProperty.Matter.LIQUID) {
            valuation.<MaterialSpeedContext, Double>transform(
                        MaterialSpeedContext.class, "speed_amplifier", oldValue -> oldValue * this.liquidSpeedModifier);
            valuation.<MaterialSpeedContext, Integer>transform(
                    MaterialSpeedContext.class, "amplifier_material_liquid", oldValue -> oldValue + 1);
        } else {
            valuation.<MaterialSpeedContext, Double>transform(
                        MaterialSpeedContext.class, "speed_amplifier", oldValue -> oldValue * this.solidSpeedModifier);
            valuation.<MaterialSpeedContext, Integer>transform(
                    MaterialSpeedContext.class, "amplifier_material_solid", oldValue -> oldValue + 1);
        }

        valuation.<MaterialSpeedContext, Integer>transform(MaterialSpeedContext.class, "update", oldValue -> oldValue + 1);

        return valuation;
    }

    @Override
    public CaptureContainer stop(Player player, CaptureContainer valuation) {
        return valuation;
    }
}
