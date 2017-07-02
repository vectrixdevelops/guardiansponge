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
package io.github.connorhartley.guardian.sequence.capture;

import io.github.connorhartley.guardian.storage.StorageProvider;
import org.spongepowered.api.entity.living.player.Player;
import tech.ferus.util.config.HoconConfigFile;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;

public class CaptureHandler<E, F extends StorageProvider<HoconConfigFile, Path>> {

    private final Player player;
    private final List<CaptureContext<E, F>> captureContexts = new ArrayList<>();

    private CaptureContainer captureContainer;
    private boolean stopped = false;

    public CaptureHandler(Player player) {
        this.player = player;
    }

    @SafeVarargs
    public CaptureHandler(Player player, CaptureContext<E, F>... captureContexts) {
        this.player = player;
        this.captureContexts.addAll(Arrays.asList(captureContexts));
    }

    public void setContainer(CaptureContainer captureContainer) {
        this.captureContainer = captureContainer;
    }

    public CaptureContainer getContainer() {
        return this.captureContainer;
    }

    public Player getPlayer() {
        return this.player;
    }

    public CaptureContainer start() {
        Iterator<CaptureContext<E, F>> iterator = this.captureContexts.iterator();

        if (this.stopped) {
            this.captureContainer.clear();
            this.stopped = false;
        }

        while(iterator.hasNext()) {
            CaptureContext captureContext = iterator.next();

            this.captureContainer = captureContext.start(this.player, this.captureContainer);
        }

        return this.captureContainer;
    }

    public CaptureContainer update() {
        Iterator<CaptureContext<E, F>> iterator = this.captureContexts.iterator();

        if (!this.stopped) {
            while (iterator.hasNext()) {
                CaptureContext captureContext = iterator.next();

                this.captureContainer = captureContext.update(this.player, this.captureContainer);
            }
        }

        return this.captureContainer;
    }

    public void stop() {
        Iterator<CaptureContext<E, F>> iterator = this.captureContexts.iterator();

        while(iterator.hasNext()) {
            CaptureContext captureContext = iterator.next();

            this.captureContainer = captureContext.stop(this.player, this.captureContainer);
        }

        this.stopped = true;
    }

}
