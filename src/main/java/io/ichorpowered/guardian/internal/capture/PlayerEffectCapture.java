package io.ichorpowered.guardian.internal.capture;

import com.google.common.collect.Lists;
import com.ichorpowered.guardian.api.detection.Detection;
import com.ichorpowered.guardian.api.detection.DetectionConfiguration;
import com.ichorpowered.guardian.api.entry.EntityEntry;
import com.ichorpowered.guardian.api.sequence.capture.CaptureContainer;
import com.ichorpowered.guardian.api.util.key.NamedTypeKey;
import io.ichorpowered.guardian.sequence.GuardianSequence;
import io.ichorpowered.guardian.sequence.capture.AbstractCapture;
import org.spongepowered.api.data.key.Keys;
import org.spongepowered.api.effect.potion.PotionEffect;
import org.spongepowered.api.effect.potion.PotionEffectTypes;
import org.spongepowered.api.entity.living.player.Player;

import javax.annotation.Nonnull;
import java.util.List;

public class PlayerEffectCapture<E, F extends DetectionConfiguration> extends AbstractCapture<E, F> {

    private static final String CLASS_NAME = PlayerEffectCapture.class.getSimpleName().toUpperCase();

    public static NamedTypeKey<Double> SPEED_AMPLIFIER =
            NamedTypeKey.of(CLASS_NAME + "_SPEED_AMPLIFIER", Double.class);

    public static NamedTypeKey<Double> LIFT_AMPLIFIER =
            NamedTypeKey.of(CLASS_NAME + "_LIFT_AMPLIFIER", Double.class);

    private Double speedMovement = 2.386;
    private Double jumpMovement = 1.005;

    private Double jumpLift = 2.786;

    public PlayerEffectCapture(@Nonnull E owner, @Nonnull Detection<E, F> detection) {
        super(owner, detection);

        // Movement

        this.speedMovement = this.getDetection().getConfiguration().getStorage()
                .getNode("analysis", "effect-modifier", "movement", "speed").getDouble(this.speedMovement);

        this.jumpMovement = this.getDetection().getConfiguration().getStorage()
                .getNode("analysis", "effect-modifier", "movement", "jump").getDouble(this.jumpMovement);

        // Lift

        this.jumpLift = this.getDetection().getConfiguration().getStorage()
                .getNode("analysis", "effect-modifier", "lift", "jump").getDouble(this.jumpLift);
    }

    @Override
    public void update(@Nonnull EntityEntry entry, @Nonnull CaptureContainer captureContainer) {
        if (!entry.getEntity(Player.class).isPresent() || !captureContainer.get(GuardianSequence.INITIAL_LOCATION).isPresent()) return;
        Player player = entry.getEntity(Player.class).get();

        List<PotionEffect> potionEffects = player.get(Keys.POTION_EFFECTS).orElse(Lists.newArrayList());

        if (!potionEffects.isEmpty()) {
            for (PotionEffect potionEffect : potionEffects) {
                if (potionEffect.getType().equals(PotionEffectTypes.SPEED)) {
                    captureContainer.transform(PlayerEffectCapture.SPEED_AMPLIFIER,
                            original -> original + ((potionEffect.getAmplifier() + 1) * this.speedMovement),
                            ((potionEffect.getAmplifier() + 1) * this.speedMovement));
                }
                if (potionEffect.getType().equals(PotionEffectTypes.JUMP_BOOST)) {
                    captureContainer.transform(PlayerEffectCapture.SPEED_AMPLIFIER,
                            original -> original + ((potionEffect.getAmplifier() + 1) * this.jumpMovement),
                            ((potionEffect.getAmplifier() + 1) * this.jumpMovement));

                    captureContainer.transform(PlayerEffectCapture.LIFT_AMPLIFIER,
                            original -> original + ((potionEffect.getAmplifier() + 1) * this.jumpLift),
                            ((potionEffect.getAmplifier() + 1) * this.jumpLift));
                }
            }
        }
    }

}
