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

/**
 * Guardian Detection
 *
 * Represents the detection registry for Guardian.
 */
public final class GuardianDetection {

    private final ModuleController moduleController;

    private DetectionTypes detectionTypes;

    GuardianDetection(ModuleController moduleController) {
        this.moduleController = moduleController;
        this.detectionTypes = new DetectionTypes();
    }

    void register() {
        this.moduleController.registerModule("io.github.connorhartley.guardian.internal.detections.SpeedDetection");
        this.moduleController.registerModule("io.github.connorhartley.guardian.internal.detections.FlyDetection");
        this.moduleController.registerModule("io.github.connorhartley.guardian.internal.detections.JesusDetection");
    }

}
