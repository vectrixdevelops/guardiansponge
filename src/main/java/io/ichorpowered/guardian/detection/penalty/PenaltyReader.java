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
package io.ichorpowered.guardian.detection.penalty;

import com.google.common.reflect.TypeToken;
import com.ichorpowered.guardian.api.detection.Detection;
import com.ichorpowered.guardian.api.detection.DetectionConfiguration;
import ninja.leaping.configurate.objectmapping.ObjectMappingException;
import org.spongepowered.api.util.Tuple;

import javax.annotation.Nonnull;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class PenaltyReader {

    private PenaltyReader() {}

    @Nonnull
    @SuppressWarnings("unchecked")
    public <E, F extends DetectionConfiguration> List<Action> parseActions(Detection<E, F> detection) {
        Map<String, List<String>> actionsMap = (Map<String, List<String>>) detection.getConfiguration().getStorage()
                .getNode("punishment", "actions").getValue(new HashMap<String, List<String>>());

        List<Action> actions = new ArrayList<>();
        actionsMap.forEach((id, strings) -> actions.add(new Action(id, strings)));

        return actions;
    }

    @Nonnull
    @SuppressWarnings("unchecked")
    public <E, F extends DetectionConfiguration> List<ActionLevel> parseActionLevels(Detection<E, F> detection) {
        List<ActionLevel> actionLevels = new ArrayList<>();

        detection.getConfiguration().getStorage().getNode("punishment", "levels").getChildrenMap()
                .forEach((string, node) -> {
                    try {
                        actionLevels.add(new ActionLevel((String) string, node.getNode("action").getList(TypeToken.of(String.class)),
                                (Tuple<Double, Double>) node.getNode("range").getValue(Tuple.of(1d, 1d))));
                    } catch (ObjectMappingException e) {
                        e.printStackTrace();
                    }
                });

        return actionLevels;
    }

    public static class Action {

        private final String id;
        private final List<String> elements;

        private Action(@Nonnull String id, @Nonnull List<String> elements) {
            this.id = id;
            this.elements = elements;
        }

        @Nonnull
        public String getId() {
            return this.id;
        }

        @Nonnull
        public List<String> getElements() {
            return this.elements;
        }

        @Override
        public boolean equals(Object object) {
            if (object instanceof String) {
                return object.equals(this.id);
            }

            return object.equals(this);
        }

    }

    public static class ActionLevel {

        private final String id;
        private final List<String> types;
        private final Tuple<Double, Double> range;

        private ActionLevel(@Nonnull String id, @Nonnull List<String> types,
                            @Nonnull Tuple<Double, Double> range) {
            this.id = id;
            this.types = types;
            this.range = range;
        }

        @Nonnull
        public String getId() {
            return this.id;
        }

        @Nonnull
        public List<String> getType() {
            return this.types;
        }

        @Nonnull
        public Tuple<Double, Double> getRange() {
            return this.range;
        }

    }

    public static class Holder {

        @Nonnull
        public final static PenaltyReader INSTANCE = new PenaltyReader();

    }

}
