package io.github.connorhartley.guardian.internal.punishments;

import io.github.connorhartley.guardian.Guardian;
import io.github.connorhartley.guardian.data.DataKeys;
import io.github.connorhartley.guardian.detection.Detection;
import io.github.connorhartley.guardian.punishment.Punishment;
import io.github.connorhartley.guardian.punishment.PunishmentType;
import org.spongepowered.api.entity.living.player.User;
import org.spongepowered.api.text.Text;
import org.spongepowered.api.text.channel.MessageChannel;
import org.spongepowered.api.text.format.TextColors;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class ReportPunishment implements PunishmentType {

    private MessageChannel reportChannel;

    private final Guardian plugin;
    private final Detection detection;

    public ReportPunishment(Guardian plugin, Detection detection) {
        this.plugin = plugin;
        this.detection = detection;

        this.reportChannel = MessageChannel.permission("guardian.punishment.report-channel." + detection.getId());
    }

    @Override
    public String getName() {
        return "report";
    }

    @Override
    public Optional<Detection> getDetection() {
        if (this.detection == null) return Optional.empty();
        return Optional.of(this.detection);
    }

    @Override
    public boolean handle(String[] args, User user, Punishment punishment) {
        List<PunishmentType> punishmentTypes = new ArrayList<>();
        if (user.get(DataKeys.GUARDIAN_PUNISHMENT_TAG).isPresent()) {
            punishmentTypes.addAll(user.get(DataKeys.GUARDIAN_PUNISHMENT_TAG).get());
        }

        punishmentTypes.add(this);

        user.offer(DataKeys.GUARDIAN_PUNISHMENT_TAG, punishmentTypes);

        Double probability = punishment.getProbability() * 100;

        if (user.getPlayer().isPresent()) {
            this.reportChannel.send(this.plugin, Text.of(Guardian.GUARDIAN_PREFIX, TextColors.GRAY, "The player ",
                    TextColors.DARK_AQUA, user.getPlayer().get(), TextColors.GRAY, " has been found illegally ",
                    TextColors.DARK_AQUA, punishment.getDetectionReason(), TextColors.GRAY, " with a certainty of ",
                    TextColors.DARK_AQUA, "%", probability.intValue(), TextColors.GRAY, ". This has been reported to an administrator."));

            return true;
        }
        return false;
    }
}
