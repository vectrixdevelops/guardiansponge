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
package com.ichorpowered.guardian.sponge.storage;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import com.ichorpowered.guardian.api.Guardian;
import com.ichorpowered.guardian.api.game.model.ModelFactories;
import com.ichorpowered.guardian.api.storage.GlobalConfiguration;
import ninja.leaping.configurate.commented.CommentedConfigurationNode;
import ninja.leaping.configurate.hocon.HoconConfigurationLoader;
import ninja.leaping.configurate.loader.ConfigurationLoader;
import org.checkerframework.checker.nullness.qual.NonNull;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

@Singleton
public class SpongeGlobalConfiguration implements GlobalConfiguration {

    private static final String FILE_NAME = "guardian.conf";

    private final ModelFactories modelFactories;

    private Path target;
    private ConfigurationLoader<CommentedConfigurationNode> source;
    private CommentedConfigurationNode root;

    @Inject
    public SpongeGlobalConfiguration(final ModelFactories modelFactories) {
        this.modelFactories = modelFactories;
    }

    @Override
    public void load(final boolean overwrite, final boolean merge) {
        final String resource = "/" + FILE_NAME;
        this.target = Guardian.getConfigPath().resolve(FILE_NAME);

        try {
            if (overwrite) {
                Files.deleteIfExists(this.target);
            }

            final boolean fileIsFresh;
            if (!Files.exists(this.target)) {
                Files.createDirectories(this.target.getParent());
                Files.copy(SpongeGlobalConfiguration.class.getResourceAsStream(resource), this.target);
                fileIsFresh = true;
            } else {
                fileIsFresh = false;
            }

            final ConfigurationLoader<CommentedConfigurationNode> loader = HoconConfigurationLoader.builder().setPath(this.target).build();
            final CommentedConfigurationNode root = loader.load();

            if (!fileIsFresh && merge) {
                final ConfigurationLoader<CommentedConfigurationNode> resourceLoader = HoconConfigurationLoader.builder().setURL(SpongeGlobalConfiguration.class.getResource(resource)).build();
                root.mergeValuesFrom(resourceLoader.load());
                loader.save(root);
            }

            this.source = loader;
            this.root = root;
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public @NonNull ConfigurationLoader<CommentedConfigurationNode> getSource() {
        return this.source;
    }

    @Override
    public @NonNull CommentedConfigurationNode getRoot() {
        return this.root;
    }

    @Override
    public @NonNull CommentedConfigurationNode getDetection(@NonNull String detection) {
        return this.root.getNode("global", "detections", detection);
    }

    @Override
    public @NonNull CommentedConfigurationNode getModel(@NonNull String model) {
        return this.root.getNode("global", "definitions", model);
    }

}
