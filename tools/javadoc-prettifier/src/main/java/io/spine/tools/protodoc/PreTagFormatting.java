/*
 * Copyright 2020, TeamDev. All rights reserved.
 *
 * Redistribution and use in source and/or binary forms, with or without
 * modification, must retain the above copyright notice and the following
 * disclaimer.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS
 * "AS IS" AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT
 * LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR
 * A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT
 * OWNER OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL,
 * SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT
 * LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE,
 * DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY
 * THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 * (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE
 * OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */
package io.spine.tools.protodoc;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static java.util.regex.Pattern.DOTALL;
import static java.util.regex.Pattern.compile;

/**
 * A formatting action, which handles {@code <pre>} tags.
 *
 * <p>The action removes the tags inserted by the Protobuf compiler,
 * i.e. the first opening tag and the last closing tag.
 */
class PreTagFormatting implements FormattingAction {

    static final String CLOSING_PRE = "</pre>";
    static final String OPENING_PRE = "<pre>";
    private static final Pattern PATTERN_OPENING_PRE = compile(OPENING_PRE);
    private static final Pattern NOT_FORMATTED_DOC_PATTERN =
            compile("^/\\*\\*[\\s*]*<pre>.*</pre>[\\s*]+.*[\\s*]*\\*/$", DOTALL);

    /**
     * Obtains the formatted representation of the specified text.
     *
     * @param javadoc the Javadoc to format
     * @return the text without generated {@code <pre>} tags
     */
    @Override
    public String execute(String javadoc) {
        if (!shouldFormat(javadoc)) {
            return javadoc;
        }

        Matcher matcher = PATTERN_OPENING_PRE.matcher(javadoc);
        String withoutOpeningPre = matcher.replaceFirst("");
        return removeLastClosingPre(withoutOpeningPre);
    }

    private static String removeLastClosingPre(String text) {
        int tagIndex = text.lastIndexOf(CLOSING_PRE);
        String beforeTag = text.substring(0, tagIndex);
        String afterTag = text.substring(tagIndex + CLOSING_PRE.length());
        return beforeTag + afterTag;
    }

    /**
     * Determines whether the specified Javadoc should be formatted.
     *
     * <p>For map getters, the compiler generates Javadocs without {@code pre} tags.
     * So, Javadocs for such methods should not be formatted.
     *
     * @param javadoc the Javadoc to test
     * @return {@code true} if the Javadoc contains opening and closing {@code pre} tag
     */
    private static boolean shouldFormat(String javadoc) {
        return NOT_FORMATTED_DOC_PATTERN.matcher(javadoc)
                                        .matches();
    }
}
