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
