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
package com.ichorpowered.guardian.util.world;

import com.flowpowered.math.vector.Vector3d;
import org.spongepowered.api.block.BlockType;
import org.spongepowered.api.data.property.AbstractProperty;
import org.spongepowered.api.data.property.block.MatterProperty;
import org.spongepowered.api.entity.EntityType;
import org.spongepowered.api.util.blockray.BlockRay;
import org.spongepowered.api.util.blockray.BlockRayHit;
import org.spongepowered.api.world.extent.EntityUniverse;
import org.spongepowered.api.world.extent.Extent;

import java.util.Arrays;
import java.util.Comparator;
import java.util.Optional;
import java.util.Set;
import java.util.function.Predicate;

public class RayUtil {

    public static final Predicate<BlockType> TRANSPARENT_FILTER = createMatterFilter(MatterProperty.Matter.SOLID, MatterProperty.Matter.GAS, MatterProperty.Matter.LIQUID);

    public static Optional<EntityUniverse.EntityHit> getFirstEntity(final Extent extent, final Vector3d from, final Vector3d direction,
                                                                    final Predicate<EntityType> entitySkipFilter, final Predicate<BlockType> blockSkipFilter,
                                                                    final int distanceLimit, final boolean ignoreObstruction) {
        if (ignoreObstruction) return getFirstEntity(extent, from, direction, entitySkipFilter, distanceLimit);

        Optional<EntityUniverse.EntityHit> firstEntity = getFirstEntity(extent, from, direction, entitySkipFilter, distanceLimit);
        Optional<BlockRayHit> firstBlock = getFirstBlock(extent, from, direction, blockSkipFilter, distanceLimit);

        if (firstEntity.isPresent()) {
            if (!firstBlock.isPresent()) return firstEntity;

            if (firstBlock.get().getNormal().distanceSquared(from) > firstEntity.get().getDistance()) return firstEntity;
        }

        return Optional.empty();
    }

    public static Optional<BlockRayHit> getFirstBlock(final Extent extent, final Vector3d from, final Vector3d direction,
                                                      final Predicate<EntityType> entitySkipFilter, final Predicate<BlockType> blockSkipFilter,
                                                      final int distanceLimit, final boolean ignoreObstruction) {
        if (ignoreObstruction) return getFirstBlock(extent, from, direction, blockSkipFilter, distanceLimit);

        Optional<BlockRayHit> firstBlock = getFirstBlock(extent, from, direction, blockSkipFilter, distanceLimit);
        Optional<EntityUniverse.EntityHit> firstEntity = getFirstEntity(extent, from, direction, entitySkipFilter, distanceLimit);

        if (firstBlock.isPresent()) {
            if (!firstEntity.isPresent()) return firstBlock;

            if (firstEntity.get().getDistance() > firstBlock.get().getNormal().distanceSquared(from)) return firstBlock;
        }

        return Optional.empty();
    }

    public static Predicate<BlockType> createBlockFilter(final BlockType... blockTypes) {
        return blockType -> Arrays.asList(blockTypes).contains(blockType);
    }

    public static Predicate<BlockType> createMatterFilter(final MatterProperty.Matter defaultProperty, final MatterProperty.Matter... matter) {
        return blockType -> Arrays.asList(matter).contains(blockType.getProperty(MatterProperty.class).map(AbstractProperty::getValue).orElse(defaultProperty));
    }

    private static Optional<EntityUniverse.EntityHit> getFirstEntity(final Extent extent, final Vector3d from,
                                                                     final Vector3d direction, final Predicate<EntityType> skipFilter,
                                                                     final int distanceLimit) {
        final Set<EntityUniverse.EntityHit> entityRay = extent.getIntersectingEntities(from, direction, distanceLimit);

        return entityRay.stream()
                .filter(entityHit -> !skipFilter.test(entityHit.getEntity().getType()))
                .min(Comparator.comparingDouble(EntityUniverse.EntityHit::getDistance));
    }

    private static Optional<BlockRayHit> getFirstBlock(final Extent extent, final Vector3d from,
                                                       final Vector3d direction, final Predicate<BlockType> skipFilter,
                                                       final int distanceLimit) {
        final BlockRay<Extent> blockRay = BlockRay.from(extent, from).direction(direction).distanceLimit(distanceLimit).build();
        Optional<BlockRayHit> blockRayHit = Optional.empty();

        while (blockRay.hasNext()){
            final BlockRayHit nextBlockRayHit = blockRay.next();
            final BlockType blockType = extent.getBlockType(nextBlockRayHit.getBlockPosition());
            if (!skipFilter.test(blockType)){
                blockRayHit = Optional.of(nextBlockRayHit);
                break;
            }
        }

        return blockRayHit;
    }

}
