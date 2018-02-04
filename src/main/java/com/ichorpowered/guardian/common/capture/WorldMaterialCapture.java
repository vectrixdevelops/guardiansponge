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
package com.ichorpowered.guardian.common.capture;

import com.google.common.collect.Maps;
import com.google.common.reflect.TypeToken;
import com.ichorpowered.guardian.sequence.GuardianSequence;
import com.ichorpowered.guardian.sequence.capture.AbstractCapture;
import com.ichorpowered.guardian.sequence.capture.GuardianCaptureKey;
import com.ichorpowered.guardian.util.ContentUtil;
import com.ichorpowered.guardian.util.WorldUtil;
import com.ichorpowered.guardian.util.entity.BoundingBox;
import com.ichorpowered.guardian.util.item.mutable.GuardianMapValue;
import com.ichorpowered.guardian.util.item.mutable.GuardianValue;
import com.ichorpowered.guardianapi.content.ContentKeys;
import com.ichorpowered.guardianapi.detection.Detection;
import com.ichorpowered.guardianapi.detection.capture.CaptureContainer;
import com.ichorpowered.guardianapi.detection.capture.CaptureKey;
import com.ichorpowered.guardianapi.entry.entity.PlayerEntry;
import com.ichorpowered.guardianapi.util.item.value.mutable.MapValue;
import com.ichorpowered.guardianapi.util.item.value.mutable.Value;
import org.spongepowered.api.block.BlockType;
import org.spongepowered.api.data.key.Keys;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.world.Location;
import org.spongepowered.api.world.World;

import java.util.List;
import java.util.Map;

import javax.annotation.Nonnull;

public class WorldMaterialCapture extends AbstractCapture {

    private static final String CLASS_NAME = WorldMaterialCapture.class.getSimpleName().toUpperCase();

    public static final CaptureKey<Value<Double>> SPEED_MODIFIER = GuardianCaptureKey.<Value<Double>>builder()
            .id(CLASS_NAME + ":horizontalSpeedModifier")
            .name("HorizontalSpeedModifier")
            .type(GuardianValue.empty(), TypeToken.of(Double.class))
            .build();

    public static final CaptureKey<MapValue<String, Integer>> ACTIVE_MATERIAL_TICKS = GuardianCaptureKey.<MapValue<String, Integer>>builder()
            .id(CLASS_NAME + ":activeMaterialTicks")
            .name("ActiveMaterialTicks")
            .type(GuardianMapValue.empty(), TypeToken.of(Map.class))
            .build();

    public static String SOLID = "solid";
    public static String GAS = "gas";
    public static String LIQUID = "liquid";

    private Map<String, Double> materialSpeed;
    private Map<String, Double> matterSpeed;

    public WorldMaterialCapture(@Nonnull Object plugin, @Nonnull Detection detection) {
        super(plugin, detection);

        this.materialSpeed = detection.getContentContainer().get(ContentKeys.MOVEMENT_MATERIAL_SPEED)
                .orElse(GuardianMapValue.empty()).getDirect().orElse(Maps.newHashMap());

        this.matterSpeed = detection.getContentContainer().get(ContentKeys.MOVEMENT_MATTER_SPEED)
                .orElse(GuardianMapValue.empty()).getDirect().orElse(Maps.newHashMap());
    }

    @Override
    public void update(@Nonnull PlayerEntry entry, @Nonnull CaptureContainer captureContainer) {
        if (!entry.getEntity(Player.class).isPresent() || !captureContainer.get(GuardianSequence.INITIAL_LOCATION).isPresent()) return;
        final Player player = entry.getEntity(Player.class).get();
        final Location<World> location = player.getLocation();

        final MapValue<String, Integer> activeMaterials = GuardianMapValue.builder(WorldMaterialCapture.ACTIVE_MATERIAL_TICKS)
                .defaultElement(Maps.newHashMap())
                .element(Maps.newHashMap())
                .create();

        activeMaterials.put(GAS, 0);
        activeMaterials.put(LIQUID, 0);
        activeMaterials.put(SOLID, 0);

        captureContainer.offerIfEmpty(activeMaterials);

        captureContainer.offerIfEmpty(GuardianValue.builder(WorldMaterialCapture.SPEED_MODIFIER)
                .defaultElement(1d)
                .element(1d)
                .create());

        final Value<Double> playerBoxWidth = ContentUtil.getFirst(ContentKeys.BOX_PLAYER_WIDTH, entry, this.getDetection().getContentContainer()).orElse(GuardianValue.empty());
        final Value<Double> playerBoxHeight = ContentUtil.getFirst(ContentKeys.BOX_PLAYER_HEIGHT, entry, this.getDetection().getContentContainer()).orElse(GuardianValue.empty());
        final Value<Double> playerBoxSafety = ContentUtil.getFirst(ContentKeys.BOX_PLAYER_SAFETY, entry, this.getDetection().getContentContainer()).orElse(GuardianValue.empty());

        final double playerWidth = playerBoxWidth.getDirect().orElse(1.2) + playerBoxSafety.getDirect().orElse(0.05);
        final double playerHeight = playerBoxHeight.getDirect().orElse(1.8) + playerBoxSafety.getDirect().orElse(0.05);

        final boolean isSneaking = player.get(Keys.IS_SNEAKING).isPresent() && player.get(Keys.IS_SNEAKING).get();
        final BoundingBox playerBox = WorldUtil.getBoundingBox(playerWidth, isSneaking ? (playerHeight - 0.15) : playerHeight);

        // Checks

        if (!WorldUtil.containsBlocksUnder(location, playerBox, 1.25)) {
            final double gasSpeed = this.matterSpeed.get(GAS);

            captureContainer.getValue(WorldMaterialCapture.SPEED_MODIFIER).ifPresent(value -> value.transform(original -> original * gasSpeed));

            captureContainer.getValue(WorldMaterialCapture.ACTIVE_MATERIAL_TICKS).ifPresent(value -> value.put(GAS, value.get().get(GAS) + 1));
        } else if (WorldUtil.anyLiquidAtDepth(location, playerBox, 1d) || WorldUtil.anyLiquidAtDepth(location, playerBox, 0)
                || WorldUtil.anyLiquidAtDepth(location, playerBox, isSneaking ? -(playerHeight - 0.25) : -playerHeight)) {
            final double liquidSpeed = this.matterSpeed.get(LIQUID);

            captureContainer.getValue(WorldMaterialCapture.SPEED_MODIFIER).ifPresent(value -> value.transform(original -> original * liquidSpeed));

            captureContainer.getValue(WorldMaterialCapture.ACTIVE_MATERIAL_TICKS).ifPresent(value -> value.put(LIQUID, value.get().get(LIQUID) + 1));
        } else {
            final List<BlockType> surroundingBlockTypes = WorldUtil.getBlocksUnder(location, playerBox, 1.25);

            for (final BlockType blockType : surroundingBlockTypes) {
                final double speedModifier = this.materialSpeed.getOrDefault(blockType.getName().toLowerCase(), this.matterSpeed.get(SOLID));

                captureContainer.getValue(WorldMaterialCapture.SPEED_MODIFIER).ifPresent(value -> value.transform(original -> original * speedModifier));
            }

            captureContainer.getValue(WorldMaterialCapture.ACTIVE_MATERIAL_TICKS).ifPresent(value -> value.put(SOLID, value.get().get(SOLID) + 1));
        }
    }
}
