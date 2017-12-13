package io.ichorpowered.guardian.internal.capture;

import com.flowpowered.math.vector.Vector3d;
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
import java.util.Optional;

public class PlayerPositionCapture {

    public static class RelativeAltitude<E, F extends DetectionConfiguration> extends AbstractCapture<E, F> {

        private static final String CLASS_NAME = RelativeAltitude.class.getSimpleName().toUpperCase();

        public static NamedTypeKey<Location> DEPTH_THRESHOLD =
                NamedTypeKey.of(CLASS_NAME + "_DEPTH_THRESHOLD", Location.class);

        public static NamedTypeKey<Double> RELATIVE_ALTITUDE_OFFSET =
                NamedTypeKey.of(CLASS_NAME + "_RELATIVE_ALTITUDE_OFFSET", Double.class);

        private final double amount;
        private final boolean liftOnly;

        public RelativeAltitude(@Nonnull E owner, @Nonnull Detection<E, F> detection) {
            this(owner, detection, 0.25);
        }

        public RelativeAltitude(@Nonnull E owner, @Nonnull Detection<E, F> detection,
                                double amount) {
            this(owner, detection, amount, false);
        }

        public RelativeAltitude(@Nonnull E owner, @Nonnull Detection<E, F> detection,
                                double amount, boolean liftOnly) {
            super(owner, detection);

            this.amount = amount;
            this.liftOnly = liftOnly;
        }

        @Override
        public void update(EntityEntry entry, CaptureContainer captureContainer) {
            if (!entry.getEntity(Player.class).isPresent() || !captureContainer.get(GuardianSequence.INITIAL_LOCATION).isPresent()) return;
            Player player = entry.getEntity(Player.class).get();

            boolean isSneaking = player.get(Keys.IS_SNEAKING).isPresent() && player.get(Keys.IS_SNEAKING).get();
            BoundingBox playerBox = WorldUtil.getBoundingBox(0.92, isSneaking ? 1.66 : 1.81);

            final Location location = player.getLocation();

            Location relativeAltitude = null;
            double blockDepthOffset = 0;

            for (int n = 0; n < location.getY(); n++) {
                double i = this.amount * n;
                Optional<Location> maximumDepth = captureContainer.get(RelativeAltitude.DEPTH_THRESHOLD);

                if (!WorldUtil.isEmptyAtDepth(location, playerBox, i)) {
                    Location currentDepth = location.sub(0, i, 0);

                    if (maximumDepth.isPresent() && maximumDepth.get().getY() == currentDepth.getY()) {
                        relativeAltitude = currentDepth.add(0, this.amount, 0);
                        blockDepthOffset = 1;
                        break;
                    } else if (maximumDepth.isPresent() && maximumDepth.get().getY() < currentDepth.getY()) {
                        relativeAltitude = currentDepth.add(0, this.amount, 0);
                        blockDepthOffset = (currentDepth.getY() - maximumDepth.get().getY()) > -1 ?
                                -1 : currentDepth.getY() - maximumDepth.get().getY();
                        break;
                    } else if (maximumDepth.isPresent() && maximumDepth.get().getY() > currentDepth.getY()) {
                        relativeAltitude = currentDepth.add(0, this.amount, 0);
                        blockDepthOffset = (maximumDepth.get().getY() - currentDepth.getY()) < 1 ?
                                1 : maximumDepth.get().getY() - currentDepth.getY();
                        break;
                    } else if (!maximumDepth.isPresent()) {
                        captureContainer.put(RelativeAltitude.DEPTH_THRESHOLD, currentDepth);
                        relativeAltitude = currentDepth.add(0, this.amount, 0);
                        break;
                    }
                }
            }

            if (!captureContainer.get(RelativeAltitude.DEPTH_THRESHOLD).isPresent() || relativeAltitude == null) {
                relativeAltitude = player.getLocation().setPosition(new Vector3d(player.getLocation().getX(), 0, player.getLocation().getZ()));
            }

            double relativeAltitudeOffset = (player.getLocation().getY() - relativeAltitude.getY()) - Math.abs(blockDepthOffset);

            if (this.liftOnly && relativeAltitudeOffset < 0) return;

            captureContainer.transform(RelativeAltitude.RELATIVE_ALTITUDE_OFFSET, original -> original + relativeAltitudeOffset, 1.0);
        }

    }

}
