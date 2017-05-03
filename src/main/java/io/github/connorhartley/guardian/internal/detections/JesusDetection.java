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
package io.github.connorhartley.guardian.internal.detections;

import com.google.common.reflect.TypeToken;
import com.google.inject.Inject;
import com.me4502.modularframework.module.Module;
import com.me4502.modularframework.module.guice.ModuleContainer;
import com.me4502.precogs.detection.CommonDetectionTypes;
import io.github.connorhartley.guardian.Guardian;
import io.github.connorhartley.guardian.detection.Detection;
import io.github.connorhartley.guardian.detection.DetectionTypes;
import io.github.connorhartley.guardian.detection.check.CheckType;
import io.github.connorhartley.guardian.internal.punishments.CustomPunishment;
import io.github.connorhartley.guardian.internal.punishments.KickPunishment;
import io.github.connorhartley.guardian.internal.punishments.ReportPunishment;
import io.github.connorhartley.guardian.internal.punishments.WarningPunishment;
import io.github.connorhartley.guardian.storage.StorageConsumer;
import io.github.connorhartley.guardian.storage.container.StorageKey;
import io.github.connorhartley.guardian.storage.container.StorageValue;
import ninja.leaping.configurate.commented.CommentedConfigurationNode;
import ninja.leaping.configurate.hocon.HoconConfigurationLoader;
import ninja.leaping.configurate.loader.ConfigurationLoader;
import org.apache.commons.lang3.StringUtils;
import org.spongepowered.api.event.Listener;
import org.spongepowered.api.event.game.GameReloadEvent;
import org.spongepowered.api.plugin.PluginContainer;

import java.io.File;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

@Module(
        id = "jesus",
        name = "Jesus Detection",
        authors = { "Connor Hartley (vectrix)" },
        version = "0.0.1",
        onEnable = "onConstruction",
        onDisable = "onDeconstruction"
)
public class JesusDetection extends Detection {

    private Guardian plugin;
    private File configFile;
    private List<CheckType> checkTypes;
    private Configuration configuration;
    private PluginContainer moduleContainer;
    private ConfigurationLoader<CommentedConfigurationNode> configManager;
    private boolean ready = false;

    @Inject
    public JesusDetection(@ModuleContainer PluginContainer moduleContainer) {
        super("jesus", "Jesus Detection");
        this.moduleContainer = moduleContainer;
    }

    @Override
    public void onConstruction() {
        if (this.moduleContainer.getInstance().isPresent()) {
            this.plugin = (Guardian) this.moduleContainer.getInstance().get();
            this.configFile = new File(this.plugin.getGlobalConfiguration().getLocation().getParent().toFile(),
                    "detection" + File.separator + this.getId() + ".conf");
            this.configManager = HoconConfigurationLoader.builder().setFile(this.configFile)
                    .setDefaultOptions(this.plugin.getConfigurationOptions()).build();
        }

        this.configuration = new Configuration(this, this.configFile, this.configManager);
        this.configuration.create();

        this.plugin.getPunishmentController().bind(CustomPunishment.class, this);
        this.plugin.getPunishmentController().bind(WarningPunishment.class, this);
        this.plugin.getPunishmentController().bind(KickPunishment.class, this);
        this.plugin.getPunishmentController().bind(ReportPunishment.class, this);

        this.checkTypes = Collections.emptyList();

        this.configuration.update();

        DetectionTypes.JESUS_DETECTION = Optional.of(this);
        this.ready = true;
    }

    @Override
    public void onDeconstruction() {
        this.configuration.update();
        this.ready = false;
    }

    @Listener
    public void onReload(GameReloadEvent event) {
        this.configuration.load();
    }

    @Override
    public String getPermission(String permissionTarget) {
        return StringUtils.join("guardian.detections.", permissionTarget, ".jesus");
    }

    @Override
    public CommonDetectionTypes.Category getCategory() {
        return CommonDetectionTypes.Category.MOVEMENT;
    }

    @Override
    public Object getPlugin() {
        return this.plugin;
    }

    @Override
    public List<CheckType> getChecks() {
        return this.checkTypes;
    }

    @Override
    public StorageConsumer<File> getConfiguration() {
        return this.configuration;
    }

    @Override
    public boolean isReady() {
        return this.ready;
    }

    public static class Configuration implements StorageConsumer<File> {

        private final JesusDetection jesusDetection;
        private final File configFile;
        private final ConfigurationLoader<CommentedConfigurationNode> configManager;

        private Configuration(JesusDetection jesusDetection, File configFile, ConfigurationLoader<CommentedConfigurationNode> configManager) {
            this.jesusDetection = jesusDetection;
            this.configFile = configFile;
            this.configManager = configManager;
        }

        @Override
        public void create() {

        }

        @Override
        public void load() {

        }

        @Override
        public void update() {

        }

        @Override
        public File getLocation() {
            return null;
        }

        @Override
        public <K, E> Optional<StorageValue<K, E>> get(StorageKey<K> key, TypeToken<E> typeToken) {
            return null;
        }

        @Override
        public <K, E> void set(StorageValue<K, E> storageValue) {

        }
    }
}
