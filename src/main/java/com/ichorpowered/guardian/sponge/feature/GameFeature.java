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
package com.ichorpowered.guardian.sponge.feature;

import com.google.common.reflect.TypeToken;
import com.ichorpowered.guardian.api.game.model.Component;
import com.ichorpowered.guardian.api.game.model.Model;
import com.ichorpowered.guardian.api.game.model.value.key.GameKey;
import com.ichorpowered.guardian.api.game.model.value.key.GameKeyRegistry;
import com.ichorpowered.guardian.api.game.model.value.key.GameKeys;
import com.ichorpowered.guardian.api.game.model.value.store.GameStores;
import com.ichorpowered.guardian.api.sequence.capture.CaptureKeys;
import com.ichorpowered.guardian.api.sequence.capture.CaptureValue;
import com.ichorpowered.guardian.sponge.common.capture.AltitudeCapture;
import com.ichorpowered.guardian.sponge.common.capture.ControlCapture;
import com.ichorpowered.guardian.sponge.common.capture.EffectCapture;
import com.ichorpowered.guardian.sponge.common.capture.MaterialCapture;
import com.ichorpowered.guardian.sponge.common.capture.PingCapture;
import com.ichorpowered.guardian.sponge.common.capture.TickCapture;

import java.util.Map;

public class GameFeature {

    private final GameKey.Factory keyFactory;
    private final GameKeyRegistry keyRegistry;

    public GameFeature(final GameKey.Factory keyFactory, final GameKeyRegistry keyRegistry) {
        this.keyFactory = keyFactory;
        this.keyRegistry = keyRegistry;
    }

    public void create() {
        // Normal

        GameKeys.PLAYER_WIDTH = (GameKey<Double>) this.keyFactory.create("width", TypeToken.of(Double.class), TypeToken.of(Double.class), GameStores.PHYSICAL);
        GameKeys.PLAYER_HEIGHT = (GameKey<Double>) this.keyFactory.create("height", TypeToken.of(Double.class), TypeToken.of(Double.class), GameStores.PHYSICAL);
        GameKeys.RAY_TRACE_STEP = (GameKey<Double>) this.keyFactory.create("trace-step", TypeToken.of(Double.class), TypeToken.of(Double.class), GameStores.PHYSICAL);

        GameKeys.JOIN_PING = (GameKey<Integer>) this.keyFactory.create("join-ping", TypeToken.of(Integer.class), TypeToken.of(Integer.class), GameStores.VIRTUAL);
        GameKeys.AVERAGE_PING = (GameKey<Double>) this.keyFactory.create("join-ping", TypeToken.of(Double.class), TypeToken.of(Double.class), GameStores.VIRTUAL);

        GameKeys.WALK_SPEED = (GameKey<Double>) this.keyFactory.create("walk", TypeToken.of(Double.class), TypeToken.of(Double.class), GameStores.PHYSICAL);
        GameKeys.SNEAK_SPEED = (GameKey<Double>) this.keyFactory.create("sneak", TypeToken.of(Double.class), TypeToken.of(Double.class), GameStores.PHYSICAL);
        GameKeys.SPRINT_SPEED = (GameKey<Double>) this.keyFactory.create("sprint", TypeToken.of(Double.class), TypeToken.of(Double.class), GameStores.PHYSICAL);
        GameKeys.FLIGHT_SPEED = (GameKey<Double>) this.keyFactory.create("flight", TypeToken.of(Double.class), TypeToken.of(Double.class), GameStores.PHYSICAL);

        GameKeys.LIFT_SPEED = (GameKey<Double>) this.keyFactory.create("lift", TypeToken.of(Double.class), TypeToken.of(Double.class), GameStores.PHYSICAL);

        GameKeys.MATTER_HORIZONTAL_DISTANCE = (GameKey<Map<String, Double>>) this.keyFactory.create("matter", new TypeToken<Map<String, Double>>() {}, TypeToken.of(Map.class), GameStores.PHYSICAL);
        GameKeys.MATERIAL_HORIZONTAL_DISTANCE = (GameKey<Map<String, Double>>) this.keyFactory.create("material", new TypeToken<Map<String, Double>>() {}, TypeToken.of(Map.class), GameStores.PHYSICAL);

        GameKeys.EFFECT_HORIZONTAL_DISTANCE = (GameKey<Map<String, Double>>) this.keyFactory.create("horizontal", new TypeToken<Map<String, Double>>() {}, TypeToken.of(Map.class), GameStores.PHYSICAL);
        GameKeys.EFFECT_VERTICAL_DISTANCE = (GameKey<Map<String, Double>>) this.keyFactory.create("vertical", new TypeToken<Map<String, Double>>() {}, TypeToken.of(Map.class), GameStores.PHYSICAL);

        // Capture

        CaptureKeys.MOVEMENT_CONTROL_CAPTURE = (GameKey<CaptureValue>) this.keyFactory.create("movement_control_capture", TypeToken.of(CaptureValue.class), TypeToken.of(ControlCapture.class), GameStores.VIRTUAL);
        CaptureKeys.MOVEMENT_ALTITUDE_CAPTURE = (GameKey<CaptureValue>) this.keyFactory.create("movement_altitude_capture", TypeToken.of(CaptureValue.class), TypeToken.of(AltitudeCapture.class), GameStores.VIRTUAL);
        CaptureKeys.MOVEMENT_MATERIAL_CAPTURE = (GameKey<CaptureValue>) this.keyFactory.create("movement_material_capture", TypeToken.of(CaptureValue.class), TypeToken.of(MaterialCapture.class), GameStores.VIRTUAL);
        CaptureKeys.MOVEMENT_EFFECT_CAPTURE = (GameKey<CaptureValue>) this.keyFactory.create("movement_effect_capture", TypeToken.of(CaptureValue.class), TypeToken.of(EffectCapture.class), GameStores.VIRTUAL);
        CaptureKeys.CLIENT_PING_CAPTURE = (GameKey<CaptureValue>) this.keyFactory.create("client_ping_capture", TypeToken.of(CaptureValue.class), TypeToken.of(PingCapture.class), GameStores.VIRTUAL);
        CaptureKeys.CLIENT_TICK_CAPTURE = (GameKey<CaptureValue>) this.keyFactory.create("client_tick_capture", TypeToken.of(CaptureValue.class), TypeToken.of(TickCapture.class), GameStores.VIRTUAL);
    }

    public void register() {
        this.keyRegistry
                .register(GameKeys.PLAYER_WIDTH)
                .register(GameKeys.PLAYER_HEIGHT)
                .register(GameKeys.RAY_TRACE_STEP)
                .register(GameKeys.WALK_SPEED)
                .register(GameKeys.SNEAK_SPEED)
                .register(GameKeys.SPRINT_SPEED)
                .register(GameKeys.FLIGHT_SPEED)
                .register(GameKeys.LIFT_SPEED)
                .register(GameKeys.MATTER_HORIZONTAL_DISTANCE)
                .register(GameKeys.MATERIAL_HORIZONTAL_DISTANCE)
                .register(GameKeys.EFFECT_HORIZONTAL_DISTANCE)
                .register(GameKeys.EFFECT_VERTICAL_DISTANCE);
    }

    public void populate(final Model model) {
        final Component component = model.getComponent("capture").orElse(model.createComponent("capture"));

        component.set(CaptureKeys.MOVEMENT_CONTROL_CAPTURE, new ControlCapture());
        component.set(CaptureKeys.MOVEMENT_ALTITUDE_CAPTURE, new AltitudeCapture());
        component.set(CaptureKeys.MOVEMENT_MATERIAL_CAPTURE, new MaterialCapture());
        component.set(CaptureKeys.MOVEMENT_EFFECT_CAPTURE, new EffectCapture());
        component.set(CaptureKeys.CLIENT_PING_CAPTURE, new PingCapture());
        component.set(CaptureKeys.CLIENT_TICK_CAPTURE, new TickCapture());
    }

}
