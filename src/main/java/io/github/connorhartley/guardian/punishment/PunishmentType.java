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
package io.github.connorhartley.guardian.punishment;

import io.github.connorhartley.guardian.sequence.report.SequenceReport;
import org.spongepowered.api.entity.living.player.Player;

public enum PunishmentType {

    WARN("warn p:%0 r:%1",
            new String[]{"player", "report"},
            new Class[]{Player.class, String.class}),
    FLAG("flag p:%0 t:%1 r:%2",
            new String[]{"player", "time", "report"},
            new Class[]{Player.class, String.class, SequenceReport.class}),
    REPORT("report p:%0 t:%1 c:%2 r:%3",
            new String[]{"player", "time", "channel", "report"},
            new Class[]{Player.class, String.class, String.class, SequenceReport.class}),
    KICK("kick p:%0 t:%1 c:%2 r:%3",
            new String[]{"player", "time", "channel", "report"},
            new Class[]{Player.class, String.class, String.class, SequenceReport.class}),
    TEMPBAN("tempban p:%0 b:%1 t:%2 c:%3 r:%4",
            new String[]{"player", "releasetime", "time", "channel", "report"},
            new Class[]{Player.class, String.class, String.class, String.class, SequenceReport.class}),
    BAN("tempban p:%0 t:%2 c:%3 r:%4",
            new String[]{"player", "time", "channel", "report"},
            new Class[]{Player.class, String.class, String.class, SequenceReport.class}),
    CUSTOM("custom c:%0",
            new String[]{"command", "player", "releasetime", "time", "channel", "report"},
            new Class[]{String.class, Player.class, String.class, String.class, String.class, SequenceReport.class});

    private String text;
    private String[] placeHolderNames;
    private Class[] placeHolderTypes;

    PunishmentType(String text, String[] placeHolderNames, Class[] placeHolderTypes) {
        this.text = text;
        this.placeHolderNames = placeHolderNames;
        this.placeHolderTypes = placeHolderTypes;
    }

    public String getText() {
        return text;
    }

    public String[] getPlaceHolderNames() {
        return placeHolderNames;
    }

    public Class[] getPlaceHolderTypes() {
        return placeHolderTypes;
    }

    @Override
    public String toString() {
        return text;
    }
}
