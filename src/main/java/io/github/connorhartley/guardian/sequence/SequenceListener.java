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
package io.github.connorhartley.guardian.sequence;

import org.spongepowered.api.Sponge;
import org.spongepowered.api.service.user.UserStorageService;

public class SequenceListener {

    private final SequenceController sequenceController;
    private final UserStorageService userStorageService;

    public SequenceListener(SequenceController sequenceController) {
        this.sequenceController = sequenceController;
        this.userStorageService = Sponge.getServiceManager().provide(UserStorageService.class).orElseThrow(() ->
                new IllegalStateException("SequenceListener was attempted to be loaded before UserStorageService object was provided."));
    }

    /*
     * Listeners will go here. For example:
     *
     * @Listener
     * public void onEntityCollide(CollideEntityEvent event, @First Player player) {
     *     this.userStorageService.get(player.getUniqueId()).ifPresent(user -> this.sequenceController.invoke(user, event));
     * }
     */

}
