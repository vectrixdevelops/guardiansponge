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
package io.ichorpowered.guardian.phase;

import com.ichorpowered.guardian.api.phase.PhaseFilter;

import javax.annotation.Nonnull;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class GuardianPhaseFilter implements PhaseFilter {

    private final List<Class<?>> inclusion = new ArrayList<>();
    private final List<Class<?>> exclusion = new ArrayList<>();

    public GuardianPhaseFilter() {}

    @Override
    public PhaseFilter include(@Nonnull Class<?> include) {
        this.exclusion.remove(include);
        this.inclusion.add(include);
        return this;
    }

    @Override
    public PhaseFilter include(@Nonnull Class<?>... includeAll) {
        this.exclusion.removeAll(Arrays.asList(includeAll));
        this.inclusion.addAll(Arrays.asList(includeAll));
        return this;
    }

    @Override
    public PhaseFilter exclude(@Nonnull Class<?> exclude) {
        this.inclusion.remove(exclude);
        this.exclusion.add(exclude);
        return this;
    }

    @Override
    public PhaseFilter exclude(@Nonnull Class<?>... excludeAll) {
        this.inclusion.removeAll(Arrays.asList(excludeAll));
        this.exclusion.addAll(Arrays.asList(excludeAll));
        return this;
    }

    @Override
    public boolean accept(@Nonnull Class<?> phaseClass) {
        if (this.inclusion.isEmpty()) return !this.exclusion.contains(phaseClass);
        return this.inclusion.contains(phaseClass) && !this.exclusion.contains(phaseClass);
    }

}
