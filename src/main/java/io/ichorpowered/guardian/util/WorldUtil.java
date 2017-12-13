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
package io.ichorpowered.guardian.util;

import com.flowpowered.math.vector.Vector3d;
import io.ichorpowered.guardian.util.entity.BoundingBox;
import org.spongepowered.api.block.BlockTypes;
import org.spongepowered.api.data.property.block.MatterProperty;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.world.Location;
import org.spongepowered.api.world.World;

public class WorldUtil {

    public static BoundingBox getBoundingBox(double width, double height) {
        double width2 = width / 2;

        Vector3d a1 = new Vector3d(-width2, 0, width2);
        Vector3d b2 = new Vector3d(width2, 0, -width2);

        return new BoundingBox(new BoundingBox.Bound(a1, b2), new BoundingBox.Bound(a1.add(0, height, 0), b2.add(0, height, 0)));
    }

    public static boolean isEmptyUnder(final Player player, final BoundingBox boundingBox, final double maxDepth) {
        double depthPortion = 0.25;

        for (int n = 0; n < player.getLocation().getY(); n++) {
            double i = depthPortion * n;

            if (i >= maxDepth) break;
            if (isEmptyAtDepth(player.getLocation(), boundingBox, i)) return true;
        }

        return false;
    }

    public static boolean isEmptyAtDepth(final Location<World> location, final BoundingBox boundingBox, final double depth) {
        if (boundingBox.getLowerBounds().isPresent()) {
            BoundingBox.Bound bound = boundingBox.getLowerBounds().get();

            if (location.sub(bound.getFirst().add(0, depth, 0)).getBlock().getType().equals(BlockTypes.AIR)
                    && location.sub(bound.getSecond().add(0, depth, 0)).getBlock().getType().equals(BlockTypes.AIR)
                    && location.sub(bound.getThird().add(0, depth, 0)).getBlock().getType().equals(BlockTypes.AIR)
                    && location.sub(bound.getFourth().add(0, depth, 0)).getBlock().getType().equals(BlockTypes.AIR)
                    && location.sub(0, depth, 0).getBlock().getType().equals(BlockTypes.AIR)) return true;
        }

        return false;
    }

    public static boolean isLiquidAtDepth(final Location location, final BoundingBox boundingBox, final double depth) {
        if (boundingBox.getLowerBounds().isPresent()) {
            BoundingBox.Bound bound = boundingBox.getLowerBounds().get();

            if (location.sub(bound.getFirst().add(0, depth, 0)).getBlock().getType().getProperty(MatterProperty.class).get().getValue().equals(MatterProperty.Matter.LIQUID)
                    && location.sub(bound.getSecond().add(0, depth, 0)).getBlock().getType().getProperty(MatterProperty.class).get().getValue().equals(MatterProperty.Matter.LIQUID)
                    && location.sub(bound.getThird().add(0, depth, 0)).getBlock().getType().getProperty(MatterProperty.class).get().getValue().equals(MatterProperty.Matter.LIQUID)
                    && location.sub(bound.getFourth().add(0, depth, 0)).getBlock().getType().getProperty(MatterProperty.class).get().getValue().equals(MatterProperty.Matter.LIQUID)
                    && location.sub(0, depth, 0).getBlock().getType().getProperty(MatterProperty.class).get().getValue().equals(MatterProperty.Matter.LIQUID)) return true;
        }

        return false;
    }

    public static boolean anyLiquidAtDepth(final Location location, final BoundingBox boundingBox, final double depth) {
        if (boundingBox.getLowerBounds().isPresent()) {
            BoundingBox.Bound bound = boundingBox.getLowerBounds().get();

            if (location.sub(bound.getFirst().add(0, depth, 0)).getBlock().getType().getProperty(MatterProperty.class).get().getValue().equals(MatterProperty.Matter.LIQUID)
                    || location.sub(bound.getSecond().add(0, depth, 0)).getBlock().getType().getProperty(MatterProperty.class).get().getValue().equals(MatterProperty.Matter.LIQUID)
                    || location.sub(bound.getThird().add(0, depth, 0)).getBlock().getType().getProperty(MatterProperty.class).get().getValue().equals(MatterProperty.Matter.LIQUID)
                    || location.sub(bound.getFourth().add(0, depth, 0)).getBlock().getType().getProperty(MatterProperty.class).get().getValue().equals(MatterProperty.Matter.LIQUID)
                    || location.sub(0, depth, 0).getBlock().getType().getProperty(MatterProperty.class).get().getValue().equals(MatterProperty.Matter.LIQUID)) return true;
        }

        return false;
    }

}
