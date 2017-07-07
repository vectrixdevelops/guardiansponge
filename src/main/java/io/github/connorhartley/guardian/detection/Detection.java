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
package io.github.connorhartley.guardian.detection;

import com.google.common.reflect.TypeToken;
import com.me4502.precogs.detection.CommonDetectionTypes;
import com.me4502.precogs.detection.DetectionType;
import io.github.connorhartley.guardian.Guardian;
import io.github.connorhartley.guardian.detection.check.Check;
import io.github.connorhartley.guardian.detection.check.CheckSupplier;
import io.github.connorhartley.guardian.detection.heuristic.HeuristicReport;
import io.github.connorhartley.guardian.detection.punishment.Level;
import io.github.connorhartley.guardian.detection.punishment.Punishment;
import io.github.connorhartley.guardian.detection.punishment.PunishmentProvider;
import io.github.connorhartley.guardian.detection.punishment.PunishmentReport;
import io.github.connorhartley.guardian.event.sequence.SequenceFinishEvent;
import io.github.connorhartley.guardian.storage.StorageProvider;
import ninja.leaping.configurate.commented.CommentedConfigurationNode;
import ninja.leaping.configurate.objectmapping.ObjectMappingException;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.math3.distribution.NormalDistribution;
import org.spongepowered.api.plugin.PluginContainer;
import org.spongepowered.api.util.Tuple;
import tech.ferus.util.config.HoconConfigFile;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.io.File;
import java.nio.file.Path;
import java.time.LocalDateTime;
import java.util.*;
import java.util.function.Supplier;

/**
 * Detection
 *
 * Represents a cheat / hack / exploit internal that is loaded
 * by the global detection manager.
 */
public abstract class Detection<E, F extends StorageProvider<HoconConfigFile, Path>> extends DetectionType implements PunishmentProvider {

    private E plugin;
    private Path configDir;
    private List<Check> checks;
    private CheckSupplier checkSupplier;
    private F configuration;
    private PluginContainer pluginContainer;
    private boolean punish = true;
    private boolean ready = false;

    public Detection(PluginContainer pluginContainer, String id, String name) {
        super(id, name);

        // Sets the plugin container.
        this.pluginContainer = pluginContainer;
    }

    /**
     * On Construction
     *
     * <p>Invoked when the internal is enabled.</p>
     */
    public abstract void onConstruction();

    /**
     * On Deconstruction
     *
     * <p>Invoked when the internal is disabled.</p>
     */
    public abstract void onDeconstruction();

    /**
     * Get Category
     *
     * <p>Returns the {@link CommonDetectionTypes.Category} for this detection.</p>
     *
     * @return this detection category
     */
    public abstract CommonDetectionTypes.Category getCategory();

    /**
     * Construct
     *
     * <p>Creates most of the startup properties if they
     * do not exist already. This is mostly targeted at
     * Guardians internal detection.
     *
     * Sets these optionally:</p>
     * <ul>
     *     <li>Config File</li>
     *     <li>Detection Configuration Supplier</li>
     * </ul>
     *
     * <p>Sets these always:</p>
     * <ul>
     *     <li>Plugin Instance</li>
     *     <li>Check Supplier</li>
     *     <li>Checks</li>
     * </ul>
     *
     * @param checkSupplier the supplier of check type creation
     * @param configurationSupplier the supplier of configuration creation
     */
    @SuppressWarnings("unchecked")
    public void construct(@Nonnull CheckSupplier checkSupplier,
                          @Nullable Supplier<F> configurationSupplier) {
        if (this.pluginContainer.getInstance().isPresent()) {
            this.plugin = (E) this.pluginContainer.getInstance().get();
            this.checkSupplier = checkSupplier;

            if (this.plugin instanceof Guardian) {
                if (this.configDir == null) {
                    this.configDir = new File(((Guardian) this.plugin).getGlobalConfiguration().getLocation().toFile(),
                            "detection").toPath();
                }

                if (configurationSupplier != null) {
                    this.configuration = configurationSupplier.get();
                    this.configuration.load();
                }
            } else {
                if (configurationSupplier != null) {
                    this.configuration = configurationSupplier.get();
                    this.configuration.load();
                }
            }

            this.checks = this.checkSupplier.create();

            for (Check<E, F> check : this.checks) {
                check.load();
            }
        }
    }

    /**
     * Handle Finish
     *
     * <p>Handles the actions and reports once a sequence has finished.</p>
     *
     * @param detection the detection to handle
     * @param event the event to handle
     * @param distributionSupplier the normal distribution properties supplier
     * @param <T> the detection type
     * @throws Throwable the case of an error occurring during heuristic analysis
     */
    public <T extends Detection<E, F>> void handleFinish(@Nonnull T detection,
                                                         @Nonnull SequenceFinishEvent event,
                                                         @Nonnull Supplier<Tuple<NormalDistribution, Double>> distributionSupplier) throws Throwable {
        if (!event.isCancelled() && this.canPunish()) {
            for (Check check : this.getChecks()) {
                if (!check.getSequence().equals(event.getSequence())) {
                    return;
                }
            }

            NormalDistribution normalDistribution = distributionSupplier.get().getFirst();
            double lowerBound = distributionSupplier.get().getSecond();

            if (this.getPlugin() instanceof Guardian) {
                HeuristicReport heuristicReport = ((Guardian) this.getPlugin()).getHeuristicController()
                        .analyze(detection, event.getUser(), event.getResult()).orElseThrow(Error::new);


                // Final report.
                PunishmentReport punishmentReport = PunishmentReport.builder()
                        .type(event.getResult().getDetectionType())
                        .time(LocalDateTime.now())
                        .report(event.getResult())
                        .severity(oldValue -> normalDistribution
                                .probability(lowerBound, heuristicReport.getSeverityTransformer().transform(event.getResult().getSeverity())))
                        .build();

                ((Guardian) this.getPlugin()).getPunishmentController().execute(detection, event.getUser(), punishmentReport);
            }
        }
    }

    /**
     * Get Plugin
     *
     * <p>Returns the plugin that owns this {@link Detection}.</p>
     *
     * @return The owner of this detection
     */
    public E getPlugin() {
        return this.plugin;
    }

    /**
     * Get Permission
     *
     * <p>Returns the permission string for the associated {@link PermissionTarget}.</p>
     *
     * @param permissionTarget the associated permission target
     * @return permission string
     */
    public String getPermission(@Nonnull String permissionTarget) {
        return StringUtils.join("guardian.detection.", permissionTarget, ".", this.getId());
    }

    /**
     * Get Config File
     *
     * <p>Returns an optional configuration file path for
     * this detection.
     *
     * If this is for an internal detection, this
     * will be adjacent to the detection directory
     * path.</p>
     *
     * @return an optional configuration file path
     */
    public Optional<Path> getConfigLocation() {
        return Optional.ofNullable(this.configDir);
    }

    /**
     * Set Config File
     *
     * <p>Sets a custom path for a configuration file path
     * to be used with a {@link StorageProvider<HoconConfigFile, Path>}.
     *
     * This should be used if you do not provide
     * a {@link Supplier<? extends StorageProvider<HoconConfigFile, Path>>} on
     * the construct method.</p>
     *
     * @param configDir the custom configuration file path
     */
    public void setConfigLocation(@Nullable Path configDir) {
        this.configDir = configDir;
    }

    /**
     * Get Configuration
     *
     * <p>Returns the {@link StorageProvider<HoconConfigFile, Path>}
     * for this detection.</p>
     *
     * @return an optional configuration
     */
    public F getConfiguration() {
        return this.configuration;
    }

    /**
     * Set Configuration
     *
     * <p>Sets a custom {@link StorageProvider<HoconConfigFile, Path>} to be used with this
     * detection.
     *
     * This should be used if you do not provide
     * a {@link Supplier<? extends StorageProvider<HoconConfigFile, Path>>} on
     * the construct method.</p>
     *
     * @param configuration the custom configuration
     */
    public void setConfiguration(@Nullable F configuration) {
        this.configuration = configuration;
    }

    /**
     * Is Ready
     *
     * <p>True when the detection has finished loading and false when it is not,
     * or being deconstructed.</p>
     *
     * @return true when ready, false when not
     */
    public boolean isReady() {
        return this.ready;
    }

    /**
     * Set Ready
     *
     * <p>Set to true when the detection has finished to loader and set to false
     * when it is not, or being deconstructed.</p>
     *
     * @param ready true when ready, false when not
     */
    public void setReady(boolean ready) {
        this.ready = ready;
    }

    /**
     * Can Punish
     *
     * <p>True if the detection is allowed to pass on punishments.</p>
     *
     * @return true if allowed
     */
    public boolean canPunish() {
        return this.punish;
    }

    /**
     * Set Punish
     *
     * <p>Sets whether the detection can pass on punishments.</p>
     *
     * @param punish set whether this will execute punishments
     */
    public void setPunish(boolean punish) {
        this.punish = punish;
    }

    /**
     * Get Check Supplier
     *
     * <p>Returns the {@link CheckSupplier} for creating this detection
     * {@link Check}s.</p>
     *
     * @return
     */
    public CheckSupplier getCheckSupplier() {
        return checkSupplier;
    }

    /**
     * Get Checks
     *
     * <p>Returns the {@link Check}s that this detection uses.</p>
     *
     * @return checks for this detection
     */
    public List<Check> getChecks() {
        return this.checks;
    }

    /**
     * Set Checks
     *
     * <p>Sets the {@link Check}s that this detection uses.</p>
     *
     * @param checks check types to set
     */
    public void setChecks(@Nullable List<Check> checks) {
        this.checks = checks;
    }

    @Override
    public boolean globalScope() {
        return false;
    }

    @Override
    public List<Level> getLevels() {
        List<Level> levels = new ArrayList<>();
        Map<Object, CommentedConfigurationNode> levelMap = (Map<Object, CommentedConfigurationNode>)
                this.getConfiguration().getStorage().getNode("punishment", "levels").getChildrenMap();

        for (Map.Entry<Object, CommentedConfigurationNode> configurationNode : levelMap.entrySet()) {
            try {
                levels.add(Level.builder()
                        .name((String) configurationNode.getKey())
                        .range(configurationNode.getValue().getNode("range").getValue(new TypeToken<Tuple<Double, Double>>() {}))
                        .action((String[]) configurationNode.getValue().getNode("action").getList(TypeToken.of(String.class)).toArray())
                        .build());
            } catch (ObjectMappingException e) {
                e.printStackTrace();
            }
        }

        return levels;
    }

    @Override
    public Map<String, String[]> getPunishments() {
        Map<Object, CommentedConfigurationNode> actionMap = (Map<Object, CommentedConfigurationNode>)
                this.getConfiguration().getStorage().getNode("punishment", "actions").getChildrenMap();

        Map<String, String[]> actions = new HashMap<>();

        actionMap.forEach((o, commentedConfigurationNode) -> {
            try {
                actions.put((String) commentedConfigurationNode.getKey(),
                        commentedConfigurationNode.getValue(new TypeToken<String[]>() {}));
            } catch (ObjectMappingException e) {
                e.printStackTrace();
            }
        });

        return actions;
    }

    public static class PermissionTarget {

        public static String BYPASS = "bypass";

        public static String TOGGLE = "toggle";

    }

}
