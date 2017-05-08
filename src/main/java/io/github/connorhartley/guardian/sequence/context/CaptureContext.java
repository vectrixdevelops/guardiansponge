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

import io.github.connorhartley.guardian.detection.Detection;
import org.spongepowered.api.entity.living.player.Player;

/**
 * Capture Context
 *
 * Represents context through collecting data in
 * the period of a sequence.
 */
public abstract class CaptureContext {

    private final Object plugin;
    private final Detection detection;

    public CaptureContext(Object plugin, Detection detection) {
        this.plugin = plugin;
        this.detection = detection;
    }

    /**
     * Get Plugin
     *
     * <p>Returns the plugin this context was initialized by.</p>
     *
     * @return Plugin that initialized
     */
    public Object getPlugin() {
        return this.plugin;
    }

    /**
     * Get Detection
     *
     * <p>Returns the detection this context was initialized for.</p>
     *
     * @return Detection for initialize
     */
    public Detection getDetection() {
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

    /**
     * Has Stopped
     *
     * <p>Returns true if the data collection phase has
     * been stopped.</p>
     *
     * @return True if data collection phase is stopped
     */
    public abstract boolean hasStopped();

}
