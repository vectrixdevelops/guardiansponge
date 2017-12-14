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
package io.ichorpowered.guardian;

import com.google.common.collect.Lists;
import com.google.common.reflect.TypeToken;
import ninja.leaping.configurate.commented.CommentedConfigurationNode;
import ninja.leaping.configurate.objectmapping.ObjectMappingException;
import tech.ferus.util.config.ConfigFile;

import javax.annotation.Nonnull;
import java.io.IOException;
import java.nio.file.Path;
import java.util.List;

public class Configuration {

    private static final String FILE_NAME = "guardian.conf";

    private final GuardianPlugin plugin;
    private final Path configDir;

    private ConfigFile<CommentedConfigurationNode> configFile;
    private boolean loaded = false;

    public Configuration(@Nonnull GuardianPlugin plugin,
                         @Nonnull Path configDir) {
        this.plugin = plugin;
        this.configDir = configDir;
    }

    @Nonnull
    public List<String> getEnabledModules() throws ObjectMappingException {
        if (!this.loaded) return Lists.newArrayList();

        return this.getStorage().getNode("general", "enabled").getList(TypeToken.of(String.class));
    }

    public void load() {
        try {
            this.configFile = ConfigFile.loadHocon(this.configDir.resolve(FILE_NAME),
                    "/" + FILE_NAME, false, false);

            this.loaded = true;
        } catch (IOException e) {
            this.plugin.getLogger().error("A problem occurred attempting to load the " +
                    "guardian configuration!", e);
        }
    }

    @Nonnull
    public ConfigFile<CommentedConfigurationNode> getStorage() {
        return this.configFile;
    }

    @Nonnull
    public Path getLocation() {
        return this.configDir;
    }

    public boolean exists() {
        return this.configDir.resolve(FILE_NAME).toFile().exists();
    }

}
