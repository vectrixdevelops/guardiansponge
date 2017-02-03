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

import com.me4502.modularframework.ModuleController;
import io.github.connorhartley.guardian.detection.DetectionTypes;

public class GuardianDetections {

    private final Guardian plugin;
    private final ModuleController moduleController;

    private DetectionTypes detectionTypes;

    protected GuardianDetections(Guardian plugin, ModuleController moduleController) {
        this.plugin = plugin;
        this.moduleController = moduleController;
        this.detectionTypes = new DetectionTypes();
    }

    public void registerInternalModules() {
        // Register detections here.
    }

    public void registerModule(String modulePath) {
        this.moduleController.registerModule(modulePath);
    }

    public DetectionTypes getDetectionTypes() {
        return this.detectionTypes;
    }

}
