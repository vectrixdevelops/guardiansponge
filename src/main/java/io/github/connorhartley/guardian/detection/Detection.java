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

import com.me4502.precogs.detection.CommonDetectionTypes;
import com.me4502.precogs.detection.DetectionType;
import io.github.connorhartley.guardian.Guardian;
import io.github.connorhartley.guardian.detection.check.CheckSupplier;
import io.github.connorhartley.guardian.detection.check.Check;
import io.github.connorhartley.guardian.punishment.PunishmentType;
import io.github.connorhartley.guardian.storage.StorageSupplier;
import ninja.leaping.configurate.ConfigurationOptions;
import ninja.leaping.configurate.commented.CommentedConfigurationNode;
import ninja.leaping.configurate.hocon.HoconConfigurationLoader;
import ninja.leaping.configurate.loader.ConfigurationLoader;
import org.apache.commons.lang3.StringUtils;
import org.spongepowered.api.plugin.PluginContainer;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.io.File;
import java.util.List;
import java.util.Optional;
import java.util.function.Supplier;

/**
 * Detection
 *
 * Represents a cheat / hack / exploit internal that is loaded
 * by the global detection manager.
 */
public abstract class Detection<E, F extends StorageSupplier<File>> extends DetectionType {

    private E plugin;
    private File configFile;
    private List<Check> checks;
    private CheckSupplier checkSupplier;
    private F configuration;
    private PluginContainer pluginContainer;
    private ConfigurationLoader<CommentedConfigurationNode> configLoader;
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
     * Guardians internal detections.
     *
     * Sets these optionally:</p>
     * <ul>
     *     <li>Config File</li>
     *     <li>Config Loader</li>
     *     <li>Detection Configuration Supplier</li>
     *     <li>Punishment Bindings</li>
     * </ul>
     *
     * <p>Sets these always:</p>
     * <ul>
     *     <li>Plugin Instance</li>
     *     <li>Check Supplier</li>
     *     <li>Checks</li>
     * </ul>
     *
     * @param detection the detection that these properties are being constructed for
     * @param checkSupplier the supplier of check type creation
     * @param configurationSupplier the supplier of configuration creation
     * @param punishmentTypes the punishment classes that will be bound
     * @param <T> the detection type
     */
    @SafeVarargs
    @SuppressWarnings("unchecked")
    public final <T extends Detection> void construct(@Nonnull T detection,
                                                      @Nonnull CheckSupplier checkSupplier,
                                                      @Nullable Supplier<F> configurationSupplier,
                                                      @Nullable Class<? extends PunishmentType>... punishmentTypes) {
        if (this.pluginContainer.getInstance().isPresent()) {
            this.plugin = (E) this.pluginContainer.getInstance().get();
            this.checkSupplier = checkSupplier;

            if (this.plugin instanceof Guardian) {

                if (this.configFile == null) {
                    this.configFile = new File(((Guardian) this.plugin).getGlobalConfiguration().getLocation().get().toFile(),
                            StringUtils.join("detection", File.separator, this.getId(), ".conf"));
                }

                if (this.configLoader == null) {
                    this.configLoader = HoconConfigurationLoader.builder().setFile(this.configFile)
                            .setDefaultOptions(((Guardian) this.plugin).getConfigurationOptions()).build();
                }

                if (configurationSupplier != null) {
                    this.configuration = configurationSupplier.get();
                    this.configuration.create();
                }

                if (punishmentTypes != null) {
                    for (Class<? extends PunishmentType> punishmentType : punishmentTypes) {
                        ((Guardian) this.plugin).getPunishmentController().bind(punishmentType, detection);
                    }
                }
            } else {

                if (configurationSupplier != null) {
                    this.configuration = configurationSupplier.get();
                    this.configuration.create();
                }
            }

            this.checks = this.checkSupplier.create();

            this.configuration.update();
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
        return StringUtils.join("guardian.detections.", permissionTarget, ".", this.getId());
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
    public Optional<File> getConfigFile() {
        return Optional.ofNullable(this.configFile);
    }

    /**
     * Set Config File
     *
     * <p>Sets a custom path for a configuration file
     * to be used with a {@link StorageSupplier}.
     *
     * This should be used if you do not provide
     * a {@link Supplier<? extends StorageSupplier<File>>} on
     * the construct method.</p>
     *
     * @param configFile the custom configuration file path
     */
    public void setConfigFile(@Nullable File configFile) {
        this.configFile = configFile;
    }

    /**
     * Get Config Loader
     *
     * <p>Returns an optional {@link ConfigurationLoader<CommentedConfigurationNode>}
     * for this detection.
     *
     * If this is for an internal detection, this
     * will have the internal {@link ConfigurationOptions} applied.</p>
     *
     * @return an optional configuration loader
     */
    public Optional<ConfigurationLoader<CommentedConfigurationNode>> getConfigLoader() {
        return Optional.ofNullable(this.configLoader);
    }

    /**
     * Set Config Loader
     *
     * <p>Sets a custom {@link ConfigurationLoader<CommentedConfigurationNode>}
     * to be used with a {@link StorageSupplier}.
     *
     * This should be used if you do not provide
     * a {@link Supplier<? extends StorageSupplier<File>>} on
     * the construct method.</p>
     *
     * @param configLoader the custom config loader
     */
    public void setConfigLoader(@Nullable ConfigurationLoader<CommentedConfigurationNode> configLoader) {
        this.configLoader = configLoader;
    }

    /**
     * Get Configuration
     *
     * <p>Returns an optional {@link StorageSupplier<File>}
     * for this detection.</p>
     *
     * @return an optional configuration
     */
    public Optional<F> getConfiguration() {
        return Optional.ofNullable(this.configuration);
    }

    /**
     * Set Configuration
     *
     * <p>Sets a custom {@link StorageSupplier<File>} to be used with this
     * detection.
     *
     * This should be used if you do not provide
     * a {@link Supplier<? extends StorageSupplier<File>>} on
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
    public void setReady(@Nonnull boolean ready) {
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
    public void setPunish(@Nonnull boolean punish) {
        this.punish = punish;
    }

    /**
     * Get Check Supplier
     *
     * <p>Returns the {@link CheckSupplier} for creating this detections
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

    public static class PermissionTarget {

        public static String BYPASS = "bypass";

        public static String TOGGLE = "toggle";

    }

}
