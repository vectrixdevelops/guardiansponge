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
package io.github.connorhartley.guardian.detection;

import com.me4502.precogs.detection.CommonDetectionTypes;
import com.me4502.precogs.detection.DetectionType;
import io.github.connorhartley.guardian.detection.check.CheckType;
import io.github.connorhartley.guardian.storage.StorageConsumer;
import io.github.connorhartley.guardian.storage.StorageProvider;

import java.io.File;
import java.util.List;

/**
 * Detection
 *
 * Represents a cheat / hack / exploit internal that is loaded
 * by the global detection manager.
 */
public abstract class Detection extends DetectionType {

    public Detection(String id, String name) {
        super(id, name);
    }

    /**
     * On Construction
     *
     * <p>Invoked when the internal is enabled.</p>
     */
    public abstract void onConstruction();

    /**
     * On Deconstruction
     *
     * <p>Invoked when the internal is disabled.</p>
     */
    public abstract void onDeconstruction();

    /**
     * Get Permission
     *
     * <p>Returns the permission string for the associated {@link PermissionTarget}.</p>
     *
     * @param permissionTarget The associated permission target
     * @return Permission string
     */
    public abstract String getPermission(String permissionTarget);

    /**
     * Get Plugin
     *
     * <p>Returns the plugin that owns this {@link Detection}.</p>
     *
     * @return The owner of this detection
     */
    public abstract Object getPlugin();

    /**
     * Get Configuration
     *
     * <p>Returns a {@link StorageConsumer<File>} for this {@link Detection}.</p>
     *
     * @return The detections configuration
     */
    public abstract StorageConsumer<File> getConfiguration();

    /**
     * Is Ready
     *
     * <p>True when the detection has finished loading and false when it is not,
     * or being deconstructed.</p>
     *
     * @return True when ready, false when not
     */
    public abstract boolean isReady();

    /**
     * Get Checks
     *
     * <p>Returns the {@link CheckType}s that this {@link Detection} uses.</p>
     *
     * @return Check providers for this detection
     */
    public abstract List<CheckType> getChecks();

    /**
     * Get Category
     *
     * <p>Returns the {@link CommonDetectionTypes.Category} for this {@link Detection}.</p>
     *
     * @return This detection category
     */
    public abstract CommonDetectionTypes.Category getCategory();

    public static class PermissionTarget {

        public static String BYPASS = "bypass";

        public static String TOGGLE = "toggle";

    }

}
