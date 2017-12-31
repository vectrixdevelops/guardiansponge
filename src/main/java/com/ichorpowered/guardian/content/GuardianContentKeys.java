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
package com.ichorpowered.guardian.content;

import com.google.common.reflect.TypeToken;
import com.ichorpowered.guardian.content.assignment.ConfigurationAssignment;
import com.ichorpowered.guardianapi.content.ContentKeys;

import java.util.Map;

public class GuardianContentKeys {

    public GuardianContentKeys() {}

    public void createKeys() {
        ContentKeys.UNDEFINED = GuardianContentKey.builder()
                .id("guardian:undefined")
                .name("undefined")
                .build();

        ContentKeys.ANALYSIS_TIME = GuardianContentKey.<Double>builder()
                .id("guardian:analysis_time")
                .name("time")
                .element(TypeToken.of(Double.class))
                .assignment(ConfigurationAssignment.of("analysis", "time"))
                .build();

        ContentKeys.ANALYSIS_INTERCEPT = GuardianContentKey.<Double>builder()
                .id("guardian:analysis_intercept")
                .name("intercept")
                .element(TypeToken.of(Double.class))
                .assignment(ConfigurationAssignment.of("analysis", "intercept"))
                .build();

        ContentKeys.ANALYSIS_MINIMUM_TICK = GuardianContentKey.<Double>builder()
                .id("guardian:analysis_minimum_tick")
                .name("minimum_tick")
                .element(TypeToken.of(Double.class))
                .assignment(ConfigurationAssignment.of("analysis", "minimum-tick"))
                .build();

        ContentKeys.ANALYSIS_MAXIMUM_TICK = GuardianContentKey.<Double>builder()
                .id("guardian:analysis_maximum_tick")
                .name("maximum_tick")
                .element(TypeToken.of(Double.class))
                .assignment(ConfigurationAssignment.of("analysis", "maximum-tick"))
                .build();

        ContentKeys.MOVEMENT_LIFT_SPEED = GuardianContentKey.<Double>builder()
                .id("guardian:movement_lift_speed")
                .name("lift_speed")
                .element(TypeToken.of(Double.class))
                .assignment(ConfigurationAssignment.of("speed", "control", "lift"))
                .build();

        ContentKeys.MOVEMENT_SNEAK_SPEED = GuardianContentKey.<Double>builder()
                .id("guardian:movement_sneak_speed")
                .name("sneak_speed")
                .element(TypeToken.of(Double.class))
                .assignment(ConfigurationAssignment.of("speed", "control", "sneak"))
                .build();

        ContentKeys.MOVEMENT_WALK_SPEED = GuardianContentKey.<Double>builder()
                .id("guardian:movement_walk_speed")
                .name("walk_speed")
                .element(TypeToken.of(Double.class))
                .assignment(ConfigurationAssignment.of("speed", "control", "walk"))
                .build();

        ContentKeys.MOVEMENT_SPRINT_SPEED = GuardianContentKey.<Double>builder()
                .id("guardian:movement_sprint_speed")
                .name("sprint_speed")
                .element(TypeToken.of(Double.class))
                .assignment(ConfigurationAssignment.of("speed", "control", "sprint"))
                .build();

        ContentKeys.MOVEMENT_FLY_SPEED = GuardianContentKey.<Double>builder()
                .id("guardian:movement_fly_speed")
                .name("fly_speed")
                .element(TypeToken.of(Double.class))
                .assignment(ConfigurationAssignment.of("speed", "control", "fly"))
                .build();

        ContentKeys.MOVEMENT_MATERIAL_SPEED = GuardianContentKey.<Map>builder()
                .id("guardian:movement_material_speed")
                .name("movement_material_speed")
                .element(TypeToken.of(Map.class))
                .assignment(ConfigurationAssignment.of("speed", "material"))
                .build();

        ContentKeys.MOVEMENT_MATTER_SPEED = GuardianContentKey.<Map>builder()
                .id("guardian:movement_matter_speed")
                .name("movement_matter_speed")
                .element(TypeToken.of(Map.class))
                .assignment(ConfigurationAssignment.of("speed", "matter"))
                .build();

        ContentKeys.MOVEMENT_EFFECT_SPEED = GuardianContentKey.<Map>builder()
                .id("guardian:movement_effect_speed")
                .name("effect_speed")
                .element(TypeToken.of(Map.class))
                .assignment(ConfigurationAssignment.of("speed", "effect"))
                .build();

        ContentKeys.MOVEMENT_EFFECT_LIFT = GuardianContentKey.<Map>builder()
                .id("guardian:movement_effect_lift")
                .name("effect_lift")
                .element(TypeToken.of(Map.class))
                .assignment(ConfigurationAssignment.of("lift", "effect"))
                .build();

        ContentKeys.BOX_PLAYER_WIDTH = GuardianContentKey.<Double>builder()
                .id("guardian:box_player_width")
                .name("player_width")
                .element(TypeToken.of(Double.class))
                .assignment(ConfigurationAssignment.of("model", "player", "width"))
                .build();

        ContentKeys.BOX_PLAYER_HEIGHT = GuardianContentKey.<Double>builder()
                .id("guardian:box_player_height")
                .name("player_height")
                .element(TypeToken.of(Double.class))
                .assignment(ConfigurationAssignment.of("model", "player", "height"))
                .build();

        ContentKeys.BOX_PLAYER_SAFETY = GuardianContentKey.<Double>builder()
                .id("guardian:box_player_safety")
                .name("player_safety")
                .element(TypeToken.of(Double.class))
                .assignment(ConfigurationAssignment.of("model", "player", "safety"))
                .build();
    }


}
