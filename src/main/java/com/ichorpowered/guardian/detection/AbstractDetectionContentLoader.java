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
package com.ichorpowered.guardian.detection;

import com.google.common.collect.Maps;
import com.ichorpowered.guardian.content.assignment.ConfigurationAssignment;
import com.ichorpowered.guardian.util.item.mutable.GuardianMapValue;
import com.ichorpowered.guardian.util.item.mutable.GuardianValue;
import com.ichorpowered.guardianapi.content.ContentContainer;
import com.ichorpowered.guardianapi.content.key.ContentKey;
import com.ichorpowered.guardianapi.detection.Detection;
import com.ichorpowered.guardianapi.detection.DetectionContentLoader;
import com.ichorpowered.guardianapi.util.item.key.Key;
import com.ichorpowered.guardianapi.util.item.value.mutable.MapValue;
import com.ichorpowered.guardianapi.util.item.value.mutable.Value;
import ninja.leaping.configurate.ConfigurationNode;
import ninja.leaping.configurate.commented.CommentedConfigurationNode;
import ninja.leaping.configurate.objectmapping.ObjectMappingException;
import tech.ferus.util.config.ConfigFile;

import java.io.IOException;
import java.nio.file.Path;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

public abstract class AbstractDetectionContentLoader<T extends Detection> implements DetectionContentLoader {

    private final T detection;
    private final Path configurationDirectory;

    private ContentContainer contentContainer;
    private ConfigFile<CommentedConfigurationNode> configurationFile;

    public AbstractDetectionContentLoader(final T detection,
                                          final Path configurationDirectory) {
        this.detection = detection;
        this.configurationDirectory = configurationDirectory;
    }

    @Override
    public void set(ContentContainer contentContainer) {
        this.contentContainer = contentContainer;
    }

    @Override
    public void acquireAll() {
        if (this.contentContainer == null || this.contentContainer.getPossibleKeys() == null) return;

        this.contentContainer.getPossibleKeys()
                .forEach(key -> {
                    final Optional<ConfigurationAssignment> assignment = key.getAssignments().stream()
                            .filter(contentAssignment -> contentAssignment.getClass().equals(ConfigurationAssignment.class))
                            .map(contentAssignment -> (ConfigurationAssignment) contentAssignment)
                            .findFirst();

                    if (!assignment.isPresent()) return;
                    final ConfigurationAssignment configurationAssignment = assignment.get();
                    final CommentedConfigurationNode node = this.configurationFile.getNode(configurationAssignment.lookup().toArray());

                    if (MapValue.class.isAssignableFrom(key.getDefaultValue().getClass())) {
                        final Map<Object, Object> collect = Maps.newHashMap();
                        if (node.hasMapChildren()) {
                            for (final Map.Entry<Object, ? extends ConfigurationNode> entry : node.getChildrenMap().entrySet()) {
                                collect.put(entry.getKey(), entry.getValue().getValue());
                                this.contentContainer.attempt(key, GuardianMapValue.builder((Key) key)
                                        .defaultElement(collect)
                                        .element(collect)
                                        .create());
                            }
                            return;
                        }
                    }

                    if (Value.class.isAssignableFrom(key.getDefaultValue().getClass())) {
                        try {
                            Object value = node.getValue(key.getElementToken());
                            this.contentContainer.attempt(key, GuardianValue.builder((Key) key)
                                    .defaultElement(value)
                                    .element(value)
                                    .create());
                        } catch (ObjectMappingException e) {
                            e.printStackTrace();
                        }
                    }
                });
    }

    @Override
    public void acquireAll(Set<ContentKey<?>> contentKeys) {
        if (this.contentContainer == null) return;

        contentKeys
                .forEach(key -> {
                    final Optional<ConfigurationAssignment> assignment = key.getAssignments().stream()
                            .filter(contentAssignment -> contentAssignment.getClass().equals(ConfigurationAssignment.class))
                            .map(contentAssignment -> (ConfigurationAssignment) contentAssignment)
                            .findFirst();

                    if (!assignment.isPresent()) return;
                    final ConfigurationAssignment configurationAssignment = assignment.get();
                    final CommentedConfigurationNode node = this.configurationFile.getNode(configurationAssignment.lookup().toArray());

                    if (MapValue.class.isAssignableFrom(key.getDefaultValue().getClass())) {
                        final Map<Object, Object> collect = Maps.newHashMap();
                        if (node.hasMapChildren()) {
                            for (final Map.Entry<Object, ? extends ConfigurationNode> entry : node.getChildrenMap().entrySet()) {
                                collect.put(entry.getKey(), entry.getValue().getValue());
                                this.contentContainer.attempt(key, GuardianMapValue.builder((Key) key)
                                        .defaultElement(collect)
                                        .element(collect)
                                        .create());
                            }
                            return;
                        }
                    }

                    if (Value.class.isAssignableFrom(key.getDefaultValue().getClass())) {
                        try {
                            Object value = node.getValue(key.getElementToken());
                            this.contentContainer.attempt(key, GuardianValue.builder((Key) key)
                                    .defaultElement(value)
                                    .element(value)
                                    .create());
                        } catch (ObjectMappingException e) {
                            e.printStackTrace();
                        }
                    }
                });
    }

    @Override
    public void acquireSingle(ContentKey<?> key) {
        if (this.contentContainer == null) return;

        final Optional<ConfigurationAssignment> assignment = key.getAssignments().stream()
                .filter(contentAssignment -> contentAssignment.getClass().equals(ConfigurationAssignment.class))
                .map(contentAssignment -> (ConfigurationAssignment) contentAssignment)
                .findFirst();

        if (!assignment.isPresent()) return;
        final ConfigurationAssignment configurationAssignment = assignment.get();
        final CommentedConfigurationNode node = this.configurationFile.getNode(configurationAssignment.lookup().toArray());

        if (MapValue.class.isAssignableFrom(key.getDefaultValue().getClass())) {
            final Map<Object, Object> collect = Maps.newHashMap();
            if (node.hasMapChildren()) {
                for (final Map.Entry<Object, ? extends ConfigurationNode> entry : node.getChildrenMap().entrySet()) {
                    collect.put(entry.getKey(), entry.getValue().getValue());
                    this.contentContainer.attempt(key, GuardianMapValue.builder((Key) key)
                            .defaultElement(collect)
                            .element(collect)
                            .create());
                }
                return;
            }
        }

        if (Value.class.isAssignableFrom(key.getDefaultValue().getClass())) {
            try {
                Object value = node.getValue(key.getElementToken());
                this.contentContainer.attempt(key, GuardianValue.builder((Key) key)
                        .defaultElement(value)
                        .element(value)
                        .create());
            } catch (ObjectMappingException e) {
                e.printStackTrace();
            }
        }
    }

    @Override
    public void load() {
        try {
            this.configurationFile = ConfigFile.loadHocon(this.configurationDirectory.resolve("detection").resolve(this.getRoot()),
                    "/detection/" + this.getRoot(), !this.exists(), false);
        } catch (IOException e) {
            this.detection.getLogger().error("A problem occurred attempting to load the " +
                    "guardian movement speed detection configuration!", e);
        }
    }

    @Override
    public void save() {
        if (this.contentContainer == null) return;

        this.contentContainer.getMap().forEach((key, value) -> {
            final Optional<ConfigurationAssignment> assignment = key.getAssignments().stream()
                    .filter(contentAssignment -> contentAssignment.getClass().equals(ConfigurationAssignment.class))
                    .map(contentAssignment -> (ConfigurationAssignment) contentAssignment)
                    .findFirst();

            if (!assignment.isPresent()) return;
            final ConfigurationAssignment configurationAssignment = assignment.get();

            this.configurationFile.getNode(configurationAssignment.lookup().toArray()).setValue(key.getElementToken());
        });

        try {
            this.configurationFile.save();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public boolean exists() {
        return this.configurationDirectory.resolve("detection").resolve(this.getRoot()).toFile().exists();
    }

}
