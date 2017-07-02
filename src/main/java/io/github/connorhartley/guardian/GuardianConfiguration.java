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
package io.github.connorhartley.guardian;

import io.github.connorhartley.guardian.storage.StorageProvider;
import io.github.connorhartley.guardian.util.HoconLoaderPatch;
import tech.ferus.util.config.ConfigKey;
import tech.ferus.util.config.HoconConfigFile;

import javax.annotation.Nonnull;
import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.util.List;

public final class GuardianConfiguration implements StorageProvider<HoconConfigFile, Path> {

    private static final String ROOT = "guardian.conf";

    private final Guardian plugin;
    private final Path configDir;

    private HoconConfigFile configFile;

    public static final ConfigKey<List<String>> ENABLED = ConfigKey.of("general", "enabled");
    public static final ConfigKey<Integer> LOGGING_LEVEL = ConfigKey.of("general", "logging-level");
    public static final ConfigKey<String> DATABASE_TYPE = ConfigKey.of("general", "database-type");
    public static final ConfigKey<String> DATABASE_VERSION = ConfigKey.of("general", "database-version");
    public static final ConfigKey<Double> GLOBAL_TICK_MIN = ConfigKey.of("global", "tick-minimum");
    public static final ConfigKey<Double> GLOBAL_TICK_MAX = ConfigKey.of("global", "tick-maximum");

    GuardianConfiguration(@Nonnull Guardian plugin,
                          @Nonnull Path configDir) {
        this.plugin = plugin;
        this.configDir = configDir;
    }

    @Override
    public void load() {
        final Path path = this.configDir.resolve(ROOT);

        try {
            this.configFile = HoconLoaderPatch.load(path, File.separator + ROOT, !this.exists());
        } catch (final IOException e) {
            this.plugin.getLogger().error("A problem occurred attempting to load Guardians global configuration!", e);
        }
    }

    @Override
    public HoconConfigFile getStorage() {
        return this.configFile;
    }

    @Override
    public Path getLocation() {
        return this.configDir;
    }

    @Override
    public boolean exists() {
        return this.configDir.resolve(ROOT).toFile().exists();
    }

}
