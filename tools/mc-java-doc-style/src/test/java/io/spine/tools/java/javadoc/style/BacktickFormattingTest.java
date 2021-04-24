/*
 * Copyright 2021, TeamDev. All rights reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
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

package io.spine.tools.java.javadoc.style;

import io.spine.tools.java.javadoc.style.BacktickFormatting;
import io.spine.tools.java.javadoc.style.FormattingAction;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static io.spine.tools.java.javadoc.style.BacktickFormatting.wrapWithCodeTag;
import static java.lang.System.lineSeparator;
import static org.junit.jupiter.api.Assertions.assertEquals;

@DisplayName("BacktickFormatting should")
class BacktickFormattingTest {

    private static final char BACKTICK = '`';
    private static final String TEXT = "true";
    private static final String TEXT_IN_CODE_TAG = wrapWithCodeTag(TEXT);
    private static final String TEXT_IN_BACKTICKS = BACKTICK + TEXT + BACKTICK;

    private final FormattingAction formatting = new BacktickFormatting();

    @Test
    @DisplayName("surround text in backticks with {@code }")
    void surround_text_in_backticks_with_code_tag() {
        String result = formatting.execute(TEXT_IN_BACKTICKS);
        assertEquals(TEXT_IN_CODE_TAG, result);
    }

    @Test
    @DisplayName("handle multiple entries surrounded with backticks")
    void handle_multiple_entries_surrounded_with_backticks() {
        String separatingPart = " some other text ";
        String source = TEXT_IN_BACKTICKS + separatingPart + TEXT_IN_BACKTICKS;
        String expected = TEXT_IN_CODE_TAG + separatingPart + TEXT_IN_CODE_TAG;
        assertEquals(expected, formatting.execute(source));
    }

    @Test
    @DisplayName("not handle multilined text surrounded with backticks")
    void not_handle_multi_lined_text_surrounded_with_backticks() {
        String lineWithOpeningBacktick = BACKTICK + TEXT;
        String lineWithClosingBacktick = TEXT + BACKTICK;
        String multiLinedText = lineWithOpeningBacktick + lineSeparator()
                + lineWithClosingBacktick;
        assertEquals(multiLinedText, formatting.execute(multiLinedText));
    }

    /**
     * @see java.util.regex.Matcher#quoteReplacement(String)
     */
    @Test
    @DisplayName("escape replacement for matcher")
    void escape_replacement_for_matcher() {
        String dollarInBackticks = BACKTICK + "$" + BACKTICK;
        String result = formatting.execute(dollarInBackticks);
        assertEquals("{@code $}", result);
    }
}
