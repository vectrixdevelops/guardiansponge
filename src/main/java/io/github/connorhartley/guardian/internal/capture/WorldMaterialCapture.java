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
package io.github.connorhartley.guardian.internal.capture;

import com.google.common.reflect.TypeToken;
import com.ichorpowered.guardian.api.detection.Detection;
import com.ichorpowered.guardian.api.detection.DetectionConfiguration;
import com.ichorpowered.guardian.api.entry.EntityEntry;
import com.ichorpowered.guardian.api.sequence.capture.CaptureContainer;
import com.ichorpowered.guardian.api.util.key.NamedKey;
import io.github.connorhartley.guardian.sequence.GuardianSequence;
import io.github.connorhartley.guardian.sequence.capture.AbstractCapture;
import org.spongepowered.api.data.property.block.MatterProperty;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.world.Location;
import org.spongepowered.api.world.World;

public class WorldMaterialCapture<E, F extends DetectionConfiguration> extends AbstractCapture<E, F> {

    public static NamedKey SPEED_AMPLIFIER =
            NamedKey.of(WorldMaterialCapture.class.getCanonicalName().toUpperCase() + "_SPEED_AMPLIFIER");

    public static NamedKey UPDATE =
            NamedKey.of(WorldMaterialCapture.class.getCanonicalName().toUpperCase() + "_UPDATE");

    private double gasSpeedModifier = 1.035;
    private double solidSpeedModifier = 1.02;
    private double liquidSpeedModifier = 1.01;

    public WorldMaterialCapture(E owner, Detection<E, F> detection) {
        super(owner, detection);

        this.gasSpeedModifier = detection.getConfiguration().getStorage()
                .getNode("analysis", "material-values", "gas")
                .getDouble(this.gasSpeedModifier);

        this.solidSpeedModifier = detection.getConfiguration().getStorage()
                .getNode("analysis", "material-values", "solid")
                .getDouble(this.solidSpeedModifier);

        this.liquidSpeedModifier = detection.getConfiguration().getStorage()
                .getNode("analysis", "material-values", "liquid")
                .getDouble(this.liquidSpeedModifier);
    }

    @Override
    public void start(EntityEntry entry, CaptureContainer captureContainer) {
        captureContainer.put(WorldMaterialCapture.SPEED_AMPLIFIER, 1.0);
        captureContainer.put(WorldMaterialCapture.UPDATE, 0);
    }

    @Override
    public void update(EntityEntry entry, CaptureContainer captureContainer) {
        if (!entry.getEntity(TypeToken.of(Player.class)).isPresent()
                || captureContainer.<Location<World>>get(GuardianSequence.INITIAL_LOCATION) == null) return;
        Player player = entry.getEntity(TypeToken.of(Player.class)).get();

        MatterProperty matterBelow = player.getLocation().sub(0, 0.35, 0).getBlock().getProperty(MatterProperty.class)
                .orElseGet(() -> new MatterProperty(MatterProperty.Matter.GAS));
        MatterProperty matterInside = player.getLocation().getBlock().getProperty(MatterProperty.class)
                .orElseGet(() -> new MatterProperty(MatterProperty.Matter.GAS));
        MatterProperty matterAbove = player.getLocation().add(0, 0.75, 0).getBlock().getProperty(MatterProperty.class)
                .orElseGet(() -> new MatterProperty(MatterProperty.Matter.GAS));

        if (matterBelow.getValue().equals(MatterProperty.Matter.GAS) &&
                matterInside.getValue().equals(MatterProperty.Matter.GAS)) {
            captureContainer.<Double>transform(WorldMaterialCapture.SPEED_AMPLIFIER, original -> original * this.gasSpeedModifier);
        } else if (matterBelow.getValue().equals(MatterProperty.Matter.LIQUID) ||
                matterInside.getValue().equals(MatterProperty.Matter.LIQUID) ||
                matterAbove.getValue().equals(MatterProperty.Matter.LIQUID)) {
            captureContainer.<Double>transform(WorldMaterialCapture.SPEED_AMPLIFIER, original -> original * this.liquidSpeedModifier);
        } else {
            captureContainer.<Double>transform(WorldMaterialCapture.SPEED_AMPLIFIER, original -> original * this.solidSpeedModifier);
        }

        captureContainer.<Integer>transform(WorldMaterialCapture.UPDATE, original -> original + 1);
    }

    @Override
    public void stop(EntityEntry entry, CaptureContainer captureContainer) {}
}
