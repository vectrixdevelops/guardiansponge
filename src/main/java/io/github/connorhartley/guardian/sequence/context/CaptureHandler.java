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
package io.github.connorhartley.guardian.sequence.context;

import org.spongepowered.api.entity.living.player.Player;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;

public class CaptureHandler {

    private final Player player;
    private final List<CaptureContext> captureContexts = new ArrayList<>();

    private CaptureContainer captureContainer;

    public CaptureHandler(Player player) {
        this.player = player;
    }

    public CaptureHandler(Player player, CaptureContext... captureContexts) {
        this.player = player;
        this.captureContexts.addAll(Arrays.asList(captureContexts));
    }

    public void addContext(CaptureContext captureContext) {
        this.captureContexts.add(captureContext);
    }

    public List<CaptureContext> getCaptureContexts() {
        return this.captureContexts;
    }

    public void setContainer(CaptureContainer captureContainer) {
        this.captureContainer = captureContainer;
    }

    public CaptureContainer getContainer() {
        return this.captureContainer;
    }

    public CaptureContainer start() {
        Iterator<CaptureContext> iterator = this.captureContexts.iterator();

        while(iterator.hasNext()) {
            CaptureContext captureContext = iterator.next();

            captureContext.setPlayer(this.player);
            captureContext.start(this.captureContainer);
            this.captureContainer = captureContext.getContainer();
        }

        return this.captureContainer;
    }

    public CaptureContainer update() {
        Iterator<CaptureContext> iterator = this.captureContexts.iterator();

        while(iterator.hasNext()) {
            CaptureContext captureContext = iterator.next();

            if (!captureContext.hasStopped()) {
                captureContext.update(this.captureContainer);
                this.captureContainer = captureContext.getContainer();
            }
        }

        return this.captureContainer;
    }

    public void stop() {
        for (CaptureContext captureContext : this.captureContexts) {
            if (!captureContext.hasStopped()) {
                captureContext.stop(this.captureContainer);
                this.captureContainer = captureContext.getContainer();
            }
        }
    }

}
