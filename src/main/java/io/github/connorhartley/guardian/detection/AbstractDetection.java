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

import com.ichorpowered.guardian.api.detection.Detection;
import com.ichorpowered.guardian.api.detection.DetectionChain;
import com.ichorpowered.guardian.api.detection.DetectionConfiguration;
import com.ichorpowered.guardian.api.detection.check.Check;
import com.ichorpowered.guardian.api.detection.check.CheckBlueprint;
import com.ichorpowered.guardian.api.detection.module.ModuleExtension;
import io.github.connorhartley.guardian.GuardianPlugin;

import java.util.ArrayList;
import java.util.List;

import javax.annotation.Nonnull;

public abstract class AbstractDetection implements Detection<GuardianPlugin, DetectionConfiguration>, ModuleExtension {

    private final GuardianPlugin plugin;
    private final List<Check<GuardianPlugin, DetectionConfiguration>> checks = new ArrayList<>();

    public AbstractDetection(GuardianPlugin plugin) {
        this.plugin = plugin;
    }

    public void initializeDetection() {
        this.getChain().<CheckBlueprint<GuardianPlugin, DetectionConfiguration>>get(DetectionChain.ProcessType.CHECK)
                .forEach(checkClass -> {
                    CheckBlueprint<GuardianPlugin, DetectionConfiguration> blueprint =
                            this.plugin.getCheckRegistry().expect(checkClass);
                    this.checks.add(blueprint.create(this));
                });
    }

    @Nonnull
    @Override
    public GuardianPlugin getOwner() {
        return this.plugin;
    }

    public List<Check<GuardianPlugin, DetectionConfiguration>> getChecks() {
        return this.checks;
    }

}
