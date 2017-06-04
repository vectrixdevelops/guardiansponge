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

import io.github.connorhartley.guardian.detection.Detection;
import io.github.connorhartley.guardian.storage.StorageSupplier;
import org.spongepowered.api.entity.living.player.Player;

import java.io.File;

/**
 * Capture Context
 *
 * Represents capture through collecting data in
 * the period of a sequence.
 */
public abstract class CaptureContext<E, F extends StorageSupplier<File>> {

    private final Object plugin;
    private final Detection<E, F> detection;

    public CaptureContext(Object plugin, Detection<E, F> detection) {
        this.plugin = plugin;
        this.detection = detection;
    }

    /**
     * Get Plugin
     *
     * <p>Returns the plugin this capture was initialized by.</p>
     *
     * @return Plugin that initialized
     */
    public Object getPlugin() {
        return this.plugin;
    }

    /**
     * Get Detection
     *
     * <p>Returns the detection this capture was initialized for.</p>
     *
     * @return Detection for initialize
     */
    public Detection<E, F> getDetection() {
        return this.detection;
    }

    /**
     * Start
     *
     * <p>Starts the data collection phase.</p>
     *
     * @param valuation The data container to update
     */
    public abstract CaptureContainer start(Player player, CaptureContainer valuation);

    /**
     * Update
     *
     * <p>Collects a sample of data to be stored.</p>
     *
     * @param valuation The data container to update
     */
    public abstract CaptureContainer update(Player player, CaptureContainer valuation);

    /**
     * Stop
     *
     * <p>Stops the data collection phase.</p>
     *
     * @param valuation The data container to update
     */
    public abstract CaptureContainer stop(Player player, CaptureContainer valuation);

}
