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
package io.github.connorhartley.guardian.storage.configuration;

import org.apache.commons.lang3.StringUtils;

import java.util.ArrayList;
import java.util.List;

public class CommentDocument {

    public static final String[] LOGO = {
            " _____               _ _         ",
            "|   __|_ _ ___ ___ _| |_|___ ___ ",
            "|  |  | | | .'|  _| . | | .'|   |",
            "|_____|___|__,|_| |___|_|__,|_|_|"
    };

    public static final String[] HEADER_DELCARATION = {
            "{:",
            "> ",
            " <",
            ":}"
    };

    public static final int LOGO_WIDTH = 33;

    private static final String NEW_LINE = "\n";
    private static final int MIN_WIDTH = 40;

    private final int width;
    private final String padding;

    private List<String> document = new ArrayList<>();

    public CommentDocument() throws Exception {
        this(MIN_WIDTH, " ");
    }

    public CommentDocument(int width, String padding) throws Exception {
        if (width < MIN_WIDTH) throw new Exception("Document width is less than the minimum width allowed.");

        this.width = width;
        this.padding = padding;

        this.document.add(NEW_LINE);
    }

    public CommentDocument addLogo(String[] logo) throws Exception {
        int indexWidth = 0;

        for (String index : logo) {
            if (index.length() > indexWidth) {
                indexWidth = index.length();
            }
        }

        return this.addLogo(logo, indexWidth);
    }

    public CommentDocument addLogo(String[] logo, int logoWidth) throws Exception {
        if (logoWidth > this.width) throw new Exception("Logo width is more than the document width.");

        double leftPad;
        double rightPad;

        if ((((this.width - logoWidth) / 2) & 1) == 0) {
            leftPad = (this.width - logoWidth) / 2;
            rightPad = (this.width - logoWidth) / 2;
        } else {
            leftPad = Math.floor(((this.width - logoWidth) / 2));
            rightPad = Math.ceil(((this.width - logoWidth) / 2));
        }

        for (String index : logo) {
            if (index.length() == logoWidth) {
                this.document.add(StringUtils.join(
                        StringUtils.repeat(" ", ((Double) leftPad).intValue()),
                        index,
                        StringUtils.repeat(" ", ((Double) rightPad).intValue()),
                        NEW_LINE
                ));
            } else {
                double additionalRightPad = logoWidth - index.length();

                this.document.add(StringUtils.join(
                        StringUtils.repeat(" ", ((Double) leftPad).intValue()),
                        index,
                        StringUtils.repeat(" ", ((Double) additionalRightPad).intValue()),
                        StringUtils.repeat(" ", ((Double) rightPad).intValue()),
                        NEW_LINE
                ));
            }
        }

        this.document.add(NEW_LINE);

        return this;
    }

    public CommentDocument addHeader(String text) throws Exception {
        int leftHeaderWing = HEADER_DELCARATION[0].length() + HEADER_DELCARATION[1].length();
        int rightHeaderWing = HEADER_DELCARATION[2].length() + HEADER_DELCARATION[3].length();

        if (leftHeaderWing + text.length() + rightHeaderWing >  this.width) throw new Exception("Header width is more than the document width.");

        double leftPad;
        double rightPad;

        if ((((this.width - (leftHeaderWing + text.length() + rightHeaderWing)) / 2) & 1) == 0) {
            leftPad = (this.width - (leftHeaderWing + text.length() + rightHeaderWing)) / 2;
            rightPad = (this.width - (leftHeaderWing + text.length() + rightHeaderWing)) / 2;
        } else {
            leftPad = Math.floor(((this.width - (leftHeaderWing + text.length() + rightHeaderWing)) / 2));
            rightPad = Math.ceil(((this.width - (leftHeaderWing + text.length() + rightHeaderWing)) / 2));
        }

        this.document.add(StringUtils.join(
                HEADER_DELCARATION[0],
                StringUtils.repeat(" ", ((Double) leftPad).intValue()),
                HEADER_DELCARATION[1],
                text,
                HEADER_DELCARATION[2],
                StringUtils.repeat(" ", ((Double) rightPad).intValue()),
                HEADER_DELCARATION[3],
                NEW_LINE
        ));

        this.document.add(NEW_LINE);

        return this;
    }

    public CommentDocument addParagraph(String[] text) {
        for (String index : text) {
            this.document.add(StringUtils.join(
                    index,
                    NEW_LINE
            ));
        }

        return this;
    }

    public String export() {
        return StringUtils.join(this.document, "");
    }

}
