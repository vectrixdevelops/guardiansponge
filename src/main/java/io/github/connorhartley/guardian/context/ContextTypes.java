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
package io.github.connorhartley.guardian.context;

import io.github.connorhartley.guardian.context.container.ContextKey;
import io.github.connorhartley.guardian.internal.contexts.player.PlayerControlContext;
import io.github.connorhartley.guardian.internal.contexts.player.PlayerLocationContext;
import io.github.connorhartley.guardian.internal.contexts.player.PlayerPositionContext;
import io.github.connorhartley.guardian.internal.contexts.world.MaterialSpeedContext;
import org.spongepowered.api.world.Location;
import org.spongepowered.api.world.World;

/**
 * Context Types
 *
 * Represents keys used for contexts.
 *
 * <p>This is deprecated! DO NOT use this for more than one context!</p>
 */
@Deprecated
public class ContextTypes {

    /* Player Location */

    public static ContextKey<Location<World>> START_LOCATION =
            new ContextKey<>("start_location", PlayerLocationContext.class, null);

    public static ContextKey<Location<World>> PRESENT_LOCATION =
            new ContextKey<>("present_location", PlayerLocationContext.class, null);

    /* Player Controller | Used in: PlayerControlContext.HorizontalSpeed */

    public static ContextKey<Double> VERTICAL_CONTROL_SPEED =
            new ContextKey<>("control_speed", PlayerControlContext.VerticalSpeed.class, 1.0);

    public static ContextKey<Double> HORIZONTAL_CONTROL_SPEED =
            new ContextKey<>("control_speed", PlayerControlContext.HorizontalSpeed.class, 1.0);

    public static ContextKey<PlayerControlContext.HorizontalSpeed.State> CONTROL_SPEED_STATE =
            new ContextKey<>("control_speed_state", PlayerControlContext.HorizontalSpeed.class,
                    PlayerControlContext.HorizontalSpeed.State.WALKING);

    public static ContextKey<Double> CONTROL_MODIFIER =
            new ContextKey<>("control_modifier", PlayerControlContext.HorizontalSpeed.class, 1.0);

    /* Player Position | Used in: PlayerPositionContext.Altitude */

    public static ContextKey<Double> GAINED_ALTITUDE =
            new ContextKey<>("control_speed", PlayerPositionContext.Altitude.class, 0.0);

    /* Speed Amplifier | Used in: MaterialSpeedContext */

    public static ContextKey<Double> SPEED_AMPLIFIER =
            new ContextKey<>("speed_amplifier", MaterialSpeedContext.class, 1.0);

}
