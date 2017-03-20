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
import org.spongepowered.api.entity.living.player.User;

public enum PunishmentType {

    EMPTY("empty",
            "",
            new String[]{},
            new Class[]{}),
    WARN("warn",
            "warn p:%0 r:%1",
            new String[]{"user", "report"},
            new Class[]{User.class, SequenceReport.class}),
    FLAG("flag",
            "flag p:%0 t:%1 r:%2",
            new String[]{"user", "time", "report"},
            new Class[]{User.class, String.class, SequenceReport.class}),
    REPORT("report",
            "report p:%0 t:%1 c:%2 r:%3",
            new String[]{"user", "time", "channel", "report"},
            new Class[]{User.class, String.class, String.class, SequenceReport.class}),
    KICK("kick",
            "kick p:%0 t:%1 c:%2 r:%3",
            new String[]{"user", "time", "channel", "report"},
            new Class[]{User.class, String.class, String.class, SequenceReport.class}),
    TEMPBAN("tempban",
            "tempban p:%0 b:%1 t:%2 c:%3 r:%4",
            new String[]{"user", "releasetime", "time", "channel", "report"},
            new Class[]{User.class, String.class, String.class, String.class, SequenceReport.class}),
    BAN("ban",
            "ban p:%0 t:%2 c:%3 r:%4",
            new String[]{"user", "time", "channel", "report"},
            new Class[]{User.class, String.class, String.class, SequenceReport.class}),
    CUSTOM("custom",
            "custom c:%0",
            new String[]{"command", "user", "releasetime", "time", "channel", "report"},
            new Class[]{String.class, User.class, String.class, String.class, String.class, SequenceReport.class});

    private String name;
    private String text;
    private String[] placeHolderNames;
    private Class[] placeHolderTypes;

    PunishmentType(String name, String text, String[] placeHolderNames, Class[] placeHolderTypes) {
        this.name = name;
        this.text = text;
        this.placeHolderNames = placeHolderNames;
        this.placeHolderTypes = placeHolderTypes;
    }

    public PunishmentType find(String search) {
        for (PunishmentType punishmentType : getDeclaringClass().getEnumConstants()) {
            if (punishmentType.getName().equalsIgnoreCase(search)) {
                return punishmentType;
            }
        }
        return PunishmentType.EMPTY;
    }

    public String getName() {
        return this.name;
    }

    public String getText() {
        return this.text;
    }

    public String[] getPlaceHolderNames() {
        return this.placeHolderNames;
    }

    public Class[] getPlaceHolderTypes() {
        return this.placeHolderTypes;
    }

    @Override
    public String toString() {
        return this.name;
    }
}
