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
package com.ichorpowered.guardian.sponge.inject;

import com.google.inject.Provides;
import com.google.inject.Singleton;
import com.google.inject.name.Named;
import com.ichorpowered.guardian.api.GuardianPlatform;
import com.ichorpowered.guardian.api.storage.GlobalConfiguration;
import com.ichorpowered.guardian.common.inject.GuardianModule;
import com.ichorpowered.guardian.sponge.GuardianImpl;
import com.ichorpowered.guardian.sponge.storage.SpongeGlobalConfiguration;

import java.nio.file.Path;

public class SpongeGuardianModule extends GuardianModule {

    private final Path configPath;

    public SpongeGuardianModule(final Path configPath, final GuardianPlatform platform) {
        super(platform);

        this.configPath = configPath;
    }

    protected void configure() {
        this.bind(GlobalConfiguration.class).to(SpongeGlobalConfiguration.class);

        this.install(new SpongeSequenceModule());

        this.bindAndExpose(GuardianImpl.class);

        super.configure();
    }

    @Named("config")
    @Provides
    @Singleton
    Path configPath() {
        return this.configPath;
    }

}
