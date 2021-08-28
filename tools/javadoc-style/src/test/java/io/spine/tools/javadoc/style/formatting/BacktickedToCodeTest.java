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

package io.spine.tools.javadoc.style.formatting;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static com.google.common.truth.Truth.assertThat;
import static io.spine.string.Diags.backtick;
import static io.spine.testing.TestValues.randomString;
import static io.spine.tools.javadoc.style.formatting.BacktickedToCode.wrapWithCodeTag;
import static java.lang.System.lineSeparator;
import static org.junit.jupiter.api.Assertions.assertEquals;

@DisplayName("`BacktickedToCode` should")
class BacktickedToCodeTest {

    private static final String TEXT = randomString("Random-text-");
    private static final String TEXT_IN_CODE_TAG = wrapWithCodeTag(TEXT);
    private static final String TEXT_IN_BACKTICKS = backtick(TEXT);

    private final Formatting formatting = new BacktickedToCode();

    @Test
    @DisplayName("surround text in backticks with {@code }")
    void backticks() {
        String result = formatting.apply(TEXT_IN_BACKTICKS);
        assertThat(result)
                .isEqualTo(TEXT_IN_CODE_TAG);
    }

    @Test
    @DisplayName("handle multiple entries surrounded with backticks")
    void multipleEntries() {
        String separatingPart = " some other text ";
        String source = TEXT_IN_BACKTICKS + separatingPart + TEXT_IN_BACKTICKS;
        String expected = TEXT_IN_CODE_TAG + separatingPart + TEXT_IN_CODE_TAG;

        String formatted = formatting.apply(source);
        assertThat(formatted)
                .isEqualTo(expected);
    }

    @Test
    @DisplayName("not handle multiline text surrounded with backticks")
    void multilineText() {
        String multilineText =
                '`' + randomString()
                        + lineSeparator()
                        + randomString() + '`';

        String formatted = formatting.apply(multilineText);
        assertThat(formatted)
                .isEqualTo(multilineText);
    }

    /**
     * Tests that the formatter handles lines containing the dollar sign correctly.
     *
     * @see java.util.regex.Matcher#quoteReplacement(String)
     */
    @Test
    @DisplayName("escape replacement for matcher")
    void escapeReplacement() {
        String dollarInBackticks = "`$`";
        String result = formatting.apply(dollarInBackticks);
        assertEquals("{@code $}", result);
    }
}
