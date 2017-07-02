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

import org.spongepowered.api.Sponge;
import org.spongepowered.api.service.permission.PermissionDescription;
import org.spongepowered.api.service.permission.PermissionService;
import org.spongepowered.api.text.Text;

import java.util.Optional;

/**
 * Guardian Permission
 *
 * Represents the permission0 registry for Guardian.
 */
public final class GuardianPermission {

    private final Guardian plugin;

    GuardianPermission(Guardian plugin) {
        this.plugin = plugin;
    }

    void register() {
        if (Sponge.getServiceManager().provide(PermissionService.class).isPresent()) {
            PermissionService permissionService = Sponge.getServiceManager().provide(PermissionService.class).get();

            Optional<PermissionDescription.Builder> builderOptional = permissionService.newDescriptionBuilder(this.plugin);
            if (builderOptional.isPresent()) {
                PermissionDescription.Builder builder = builderOptional.get();

                builder.id("guardian.general.info")
                        .description(Text.of("Shows basic running information about Guardian."))
                        .assign(PermissionDescription.ROLE_USER, true)
                        .register();

                builder.id("guardian.detection.bypass.<Detection>")
                        .description(Text.of("Exempts the user from being checked for a <Detection> cheat."))
                        .assign(PermissionDescription.ROLE_ADMIN, true)
                        .register();

                builder.id("guardian.detection.toggle.<Detection>")
                        .description(Text.of("Allows the user to toggle the ability to be checked for a <Detection> cheat."))
                        .assign(PermissionDescription.ROLE_STAFF, true)
                        .register();

                builder.id("guardian.punishment.report-channel.<Detection>")
                        .description(Text.of("Those with this permission will receive reports of cheating players from <Detection>."))
                        .assign(PermissionDescription.ROLE_STAFF, true)
                        .register();
            }
        }
    }

}
