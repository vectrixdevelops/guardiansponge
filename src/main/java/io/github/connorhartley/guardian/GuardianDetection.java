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
import org.apache.commons.lang3.StringUtils;

/**
 * Guardian Detection
 *
 * Represents the detection registry for Guardian.
 */
public final class GuardianDetection {

    private final String detectionPath;
    private final ModuleController moduleController;

    GuardianDetection(ModuleController moduleController) {
        this.moduleController = moduleController;
        this.detectionPath = "io.github.connorhartley.guardian.internal.detections.";
    }

    void register() {
        this.moduleController.registerModule(StringUtils.join(detectionPath, "SpeedDetection"));
        this.moduleController.registerModule(StringUtils.join(detectionPath, "FlyDetection"));
        this.moduleController.registerModule(StringUtils.join(detectionPath, "JesusDetection"));
        this.moduleController.registerModule(StringUtils.join(detectionPath, "InvalidMovementDetection"));
    }

}
