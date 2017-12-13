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
package io.ichorpowered.guardian.internal.capture;

import com.google.common.collect.Maps;
import com.ichorpowered.guardian.api.detection.Detection;
import com.ichorpowered.guardian.api.detection.DetectionConfiguration;
import com.ichorpowered.guardian.api.entry.EntityEntry;
import com.ichorpowered.guardian.api.sequence.capture.CaptureContainer;
import com.ichorpowered.guardian.api.util.key.NamedTypeKey;
import io.ichorpowered.guardian.sequence.GuardianSequence;
import io.ichorpowered.guardian.sequence.capture.AbstractCapture;
import io.ichorpowered.guardian.util.WorldUtil;
import io.ichorpowered.guardian.util.entity.BoundingBox;
import org.spongepowered.api.data.key.Keys;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.world.Location;

import javax.annotation.Nonnull;
import java.util.HashMap;
import java.util.Map;

public class WorldMaterialCapture<E, F extends DetectionConfiguration> extends AbstractCapture<E, F> {

    private static final String CLASS_NAME = WorldMaterialCapture.class.getSimpleName().toUpperCase();

    public static NamedTypeKey<Double> SPEED_AMPLIFIER =
            NamedTypeKey.of(CLASS_NAME + "_SPEED_AMPLIFIER", Double.class);

    public static NamedTypeKey<Map> MATERIAL_STATE_TICKS =
            NamedTypeKey.of(CLASS_NAME + "_MATERIAL_STATE_TICKS", Map.class);

    public static String GAS = "gas";
    public static String LIQUID = "liquid";
    public static String SOLID = "solid";

    private double gasSpeedModifier = 1.065;
    private double solidSpeedModifier = 1.045;
    private double liquidSpeedModifier = 1.035;

    public WorldMaterialCapture(@Nonnull E owner, @Nonnull Detection<E, F> detection) {
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
    public void update(@Nonnull EntityEntry entry, @Nonnull CaptureContainer captureContainer) {
        if (!entry.getEntity(Player.class).isPresent() || !captureContainer.get(GuardianSequence.INITIAL_LOCATION).isPresent()) return;
        Player player = entry.getEntity(Player.class).get();

        Map<String, Integer> materialState = new HashMap<>();
        materialState.put(GAS, 1);
        materialState.put(LIQUID, 1);
        materialState.put(SOLID, 1);

        captureContainer.putOnce(WorldMaterialCapture.MATERIAL_STATE_TICKS, materialState);

        boolean isSneaking = player.get(Keys.IS_SNEAKING).isPresent() && player.get(Keys.IS_SNEAKING).get();
        double playerBoxHeight = isSneaking ? 1.66 : 1.81;
        Location location = player.getLocation();

        BoundingBox playerBox = WorldUtil.getBoundingBox(0.92, playerBoxHeight);

        // Checks

        if (WorldUtil.isEmptyUnder(player, playerBox, playerBoxHeight)) {
            captureContainer.transform(WorldMaterialCapture.SPEED_AMPLIFIER, original -> original * this.gasSpeedModifier, this.gasSpeedModifier);

            captureContainer.transform(WorldMaterialCapture.MATERIAL_STATE_TICKS, original -> {
                ((Map<String, Integer>) original).put(GAS, ((Map<String, Integer>) original).get(GAS) + 1);
                return (Map<String, Integer>) original;
            }, Maps.newHashMap());
        } else if (WorldUtil.anyLiquidAtDepth(location, playerBox, 1) || WorldUtil.anyLiquidAtDepth(location, playerBox, 0)
                || WorldUtil.anyLiquidAtDepth(location, playerBox, -playerBoxHeight)) {
            captureContainer.transform(WorldMaterialCapture.SPEED_AMPLIFIER, original -> original * this.liquidSpeedModifier, this.liquidSpeedModifier);

            captureContainer.transform(WorldMaterialCapture.MATERIAL_STATE_TICKS, original -> {
                ((Map<String, Integer>) original).put(LIQUID, ((Map<String, Integer>) original).get(LIQUID) + 1);
                return (Map<String, Integer>) original;
            }, Maps.newHashMap());
        } else {
            captureContainer.transform(WorldMaterialCapture.SPEED_AMPLIFIER, original -> original * this.solidSpeedModifier, this.solidSpeedModifier);

            captureContainer.transform(WorldMaterialCapture.MATERIAL_STATE_TICKS, original -> {
                ((Map<String, Integer>) original).put(SOLID, ((Map<String, Integer>) original).get(SOLID) + 1);
                return (Map<String, Integer>) original;
            }, Maps.newHashMap());
        }
    }
}
