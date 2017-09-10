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
package io.github.connorhartley.guardian.util;

import ninja.leaping.configurate.hocon.HoconConfigurationLoader;
import tech.ferus.util.config.HoconConfigFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import javax.annotation.Nonnull;

/**
 * Patch issued by FerusGrim
 * On 20/06/2017
 */
public final class HoconLoaderPatch {

    public static HoconConfigFile load(@Nonnull final Path path,
                                       @Nonnull final String resource,
                                       final boolean overwrite) throws IOException {
        if (overwrite) {
            Files.deleteIfExists(path);
        }

        if (!Files.exists(path)) {
            Files.createDirectories(path.getParent());

            if (!resource.isEmpty()) {
                Files.copy(HoconConfigFile.class.getResourceAsStream(resource), path);
            }
        }

        final HoconConfigurationLoader fileLoader = HoconConfigurationLoader.builder().setPath(path).build();

        return new HoconConfigFile(path, fileLoader, fileLoader.load());
    }

}
