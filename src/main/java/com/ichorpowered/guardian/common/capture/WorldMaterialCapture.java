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
import com.ichorpowered.guardian.content.transaction.GuardianSingleValue;
import com.ichorpowered.guardian.sequence.GuardianSequence;
import com.ichorpowered.guardian.sequence.capture.AbstractCapture;
import com.ichorpowered.guardian.util.ContentUtil;
import com.ichorpowered.guardian.util.WorldUtil;
import com.ichorpowered.guardian.util.entity.BoundingBox;
import com.ichorpowered.guardianapi.content.ContentContainer;
import com.ichorpowered.guardianapi.content.ContentKeys;
import com.ichorpowered.guardianapi.content.transaction.result.SingleValue;
import com.ichorpowered.guardianapi.detection.Detection;
import com.ichorpowered.guardianapi.detection.capture.CaptureContainer;
import com.ichorpowered.guardianapi.entry.entity.PlayerEntry;
import com.ichorpowered.guardianapi.util.key.NamedTypeKey;
import org.spongepowered.api.block.BlockType;
import org.spongepowered.api.data.key.Keys;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.world.Location;
import org.spongepowered.api.world.World;

import javax.annotation.Nonnull;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class WorldMaterialCapture extends AbstractCapture {

    private static final String CLASS_NAME = WorldMaterialCapture.class.getSimpleName().toUpperCase();

    public static NamedTypeKey<Double> SPEED_AMPLIFIER =
            NamedTypeKey.of(CLASS_NAME + "_SPEED_AMPLIFIER", Double.class);

    public static NamedTypeKey<Map> MATERIAL_STATE_TICKS =
            NamedTypeKey.of(CLASS_NAME + "_MATERIAL_STATE_TICKS", Map.class);

    public static String GAS = "gas";
    public static String LIQUID = "liquid";
    public static String SOLID = "solid";

    private Map<String, Double> materialSpeed;
    private Map<String, Double> matterSpeed;

    public WorldMaterialCapture(@Nonnull Object plugin, @Nonnull Detection detection) {
        super(plugin, detection);

        this.materialSpeed = (Map<String, Double>) detection.getContentContainer().<Map>get(ContentKeys.MOVEMENT_MATERIAL_SPEED)
                .orElse(GuardianSingleValue.empty()).getElement().orElse(Maps.newHashMap());

        this.matterSpeed = (Map<String, Double>) detection.getContentContainer().<Map>get(ContentKeys.MOVEMENT_MATTER_SPEED)
                .orElse(GuardianSingleValue.empty()).getElement().orElse(Maps.newHashMap());
    }

    @Override
    public void update(@Nonnull PlayerEntry entry, @Nonnull CaptureContainer captureContainer) {
        if (!entry.getEntity(Player.class).isPresent() || !captureContainer.get(GuardianSequence.INITIAL_LOCATION).isPresent()) return;
        Player player = entry.getEntity(Player.class).get();

        Map<String, Integer> materialState = new HashMap<>();
        materialState.put(GAS, 1);
        materialState.put(LIQUID, 1);
        materialState.put(SOLID, 1);

        captureContainer.putOnce(WorldMaterialCapture.MATERIAL_STATE_TICKS, materialState);

        final SingleValue<Double> playerBoxWidth = ContentUtil.getFirst(ContentKeys.BOX_PLAYER_WIDTH, entry, (ContentContainer) this.getDetection()).orElse(GuardianSingleValue.empty());
        final SingleValue<Double> playerBoxHeight = ContentUtil.getFirst(ContentKeys.BOX_PLAYER_HEIGHT, entry, (ContentContainer) this.getDetection()).orElse(GuardianSingleValue.empty());
        final SingleValue<Double> playerBoxSafety = ContentUtil.getFirst(ContentKeys.BOX_PLAYER_SAFETY, entry, (ContentContainer) this.getDetection()).orElse(GuardianSingleValue.empty());

        final double playerWidth = playerBoxWidth.getElement().orElse(1.0) + playerBoxSafety.getElement().orElse(0.08);
        final double playerHeight = playerBoxHeight.getElement().orElse(1.75) + playerBoxSafety.getElement().orElse(0.08);

        final boolean isSneaking = player.get(Keys.IS_SNEAKING).isPresent() && player.get(Keys.IS_SNEAKING).get();
        final BoundingBox playerBox = WorldUtil.getBoundingBox(playerWidth, isSneaking ? (playerHeight - 0.25) : playerHeight);

        final Location<World> location = player.getLocation();

        // Checks

        if (WorldUtil.isEmptyUnder(player, playerBox, isSneaking ? (playerHeight - 0.25) : playerHeight)) {
            final double gasSpeed = this.matterSpeed.get(GAS);

            captureContainer.transform(WorldMaterialCapture.SPEED_AMPLIFIER, original -> original * gasSpeed, gasSpeed);

            captureContainer.transform(WorldMaterialCapture.MATERIAL_STATE_TICKS, original -> {
                ((Map<String, Integer>) original).put(GAS, ((Map<String, Integer>) original).get(GAS) + 1);
                return (Map<String, Integer>) original;
            }, Maps.newHashMap());
        } else if (WorldUtil.anyLiquidAtDepth(location, playerBox, 1) || WorldUtil.anyLiquidAtDepth(location, playerBox, 0)
                || WorldUtil.anyLiquidAtDepth(location, playerBox, isSneaking ? -(playerHeight - 0.25) : -playerHeight)) {
            final double liquidSpeed = this.matterSpeed.get(LIQUID);

            captureContainer.transform(WorldMaterialCapture.SPEED_AMPLIFIER, original -> original * liquidSpeed, liquidSpeed);

            captureContainer.transform(WorldMaterialCapture.MATERIAL_STATE_TICKS, original -> {
                ((Map<String, Integer>) original).put(LIQUID, ((Map<String, Integer>) original).get(LIQUID) + 1);
                return (Map<String, Integer>) original;
            }, Maps.newHashMap());
        } else {
            final List<BlockType> surroundingBlockTypes = WorldUtil.getBlocksAtDepth(location, playerBox, 1);

            for (BlockType blockType : surroundingBlockTypes) {
                double speedModifier;

                if (this.materialSpeed.containsKey(blockType.getName().toLowerCase())) {
                    speedModifier = this.materialSpeed.get(blockType.getName().toLowerCase());
                } else {
                    speedModifier = this.matterSpeed.get(SOLID);
                }

                captureContainer.transform(WorldMaterialCapture.SPEED_AMPLIFIER, original -> original * speedModifier, speedModifier);

                captureContainer.transform(WorldMaterialCapture.MATERIAL_STATE_TICKS, original -> {
                    ((Map<String, Integer>) original).put(SOLID, ((Map<String, Integer>) original).get(SOLID) + 1);
                    return (Map<String, Integer>) original;
                }, Maps.newHashMap());
            }
        }
    }
}
