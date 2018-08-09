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
package com.ichorpowered.guardian.sponge.common.capture;

import static com.ichorpowered.guardian.api.sequence.capture.CaptureValue.asKey;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.reflect.TypeToken;
import com.ichorpowered.guardian.api.Guardian;
import com.ichorpowered.guardian.api.game.GameReference;
import com.ichorpowered.guardian.api.game.model.Model;
import com.ichorpowered.guardian.api.game.model.value.key.GameKeys;
import com.ichorpowered.guardian.api.sequence.capture.CaptureValue;
import com.ichorpowered.guardian.api.sequence.process.Process;
import com.ichorpowered.guardian.sponge.util.WorldUtil;
import com.ichorpowered.guardian.sponge.util.entity.BoundingBox;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.spongepowered.api.block.BlockType;
import org.spongepowered.api.data.key.Keys;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.world.Location;
import org.spongepowered.api.world.World;

import java.util.List;
import java.util.Map;

public class MaterialCapture implements CaptureValue {

    public static String MATTER_TIME = asKey(MaterialCapture.class, "matter_time");
    public static String ACTIVE_MATERIALS = asKey(MaterialCapture.class, "active_materials");
    public static String HORIZONTAL_DISTANCE = asKey(MaterialCapture.class, "horizontal_distance");

    @Override
    public void apply(final @NonNull Process process) {
        final GameReference<Player> gameReference = process.getContext().get("root:player", new TypeToken<GameReference<Player>>() {});
        final Model playerModel = Guardian.getModelRegistry().get(gameReference).orElse(null);
        if (gameReference == null || playerModel == null) return;

        final Player player = gameReference.get();
        final Location<World> location = player.getLocation();

        // Model Values
        final double playerWidth = playerModel.requestFirst(GameKeys.PLAYER_WIDTH).map(value -> value.get()).orElse(0.6);
        final double playerHeight = playerModel.requestFirst(GameKeys.PLAYER_HEIGHT).map(value -> value.get()).orElse(1.8);
        final Map<String, Double> matterHorizontalDistance = playerModel.requestFirst(GameKeys.MATTER_HORIZONTAL_DISTANCE).map(value -> value.get()).orElse(Maps.newHashMap());
        final Map<String, Double> materialHorizontalDistance = playerModel.requestFirst(GameKeys.MATERIAL_HORIZONTAL_DISTANCE).map(value -> value.get()).orElse(Maps.newHashMap());

        // Capture Content

        final BoundingBox playerBox = WorldUtil.getBoundingBox(playerWidth, player.get(Keys.IS_SNEAKING).orElse(false) ? (playerHeight - 0.15) : playerHeight);

        final Map<String, Integer> matterTime = process.getContext().setOnce(MaterialCapture.MATTER_TIME, new TypeToken<Map<String, Integer>>() {}, Maps.newHashMap());
        final List<BlockType> activeMaterials = process.getContext().setOnce(MaterialCapture.ACTIVE_MATERIALS, new TypeToken<List<BlockType>>() {}, Lists.newArrayList());
        process.getContext().setOnce(MaterialCapture.HORIZONTAL_DISTANCE, TypeToken.of(Double.class), 1d);

        matterTime.put("gas", 0);
        matterTime.put("liquid", 0);
        matterTime.put("solid", 0);

        if (!WorldUtil.containsBlocksUnder(location, playerBox, 0.75)) {
            final double gasSpeed = matterHorizontalDistance.get("gas");

            process.getContext().transform(MaterialCapture.HORIZONTAL_DISTANCE, TypeToken.of(Double.class), value -> value * gasSpeed);

            final Integer gasValue = matterTime.get("gas");
            matterTime.put("gas", (gasValue != null ? gasValue : 0) + 1);
        } else if (WorldUtil.anyLiquidAtDepth(location, playerBox, 0.75) || WorldUtil.anyLiquidAtDepth(location, playerBox, 0)
                || WorldUtil.anyLiquidAtDepth(location, playerBox, player.get(Keys.IS_SNEAKING).orElse(false) ? -(playerHeight - 0.15) : -playerHeight)) {
            final double liquidSpeed = matterHorizontalDistance.get("liquid");

            process.getContext().transform(MaterialCapture.HORIZONTAL_DISTANCE, TypeToken.of(Double.class), value -> value * liquidSpeed);

            final Integer liquidValue = matterTime.get("liquid");
            matterTime.put("liquid", (liquidValue != null ? liquidValue : 0) + 1);
        } else {
            final List<BlockType> surroundingBlockTypes = WorldUtil.getBlocksUnder(location, playerBox, 0.75);

            for (BlockType blockType : surroundingBlockTypes) {
                final Double matterHorizontalValue = matterHorizontalDistance.get("solid");
                double speedModifier = materialHorizontalDistance.getOrDefault(blockType.getName().toLowerCase(), (matterHorizontalValue != null ? matterHorizontalValue : 0));

                if (!activeMaterials.contains(blockType)) {
                    activeMaterials.add(blockType);
                }

                process.getContext().transform(MaterialCapture.HORIZONTAL_DISTANCE, TypeToken.of(Double.class), value -> value * speedModifier);

                final Integer solidValue = matterTime.get("solid");
                matterTime.put("solid", (solidValue != null ? solidValue : 0) + 1);
            }
        }
    }

}
