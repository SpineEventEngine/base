/*
 * Copyright 2017, TeamDev Ltd. All rights reserved.
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

import org.junit.Test;

import java.util.List;

import static io.spine.tools.protodoc.BackTickFormatting.putInCodeTag;
import static java.util.Arrays.asList;
import static java.util.Collections.singletonList;
import static org.junit.Assert.assertEquals;

/**
 * @author Dmytro Grankin
 */
public class BackTickFormattingShould {

    private static final String TEXT = "true";
    private static final char BACK_TICK = '`';
    private static final String TEXT_IN_BACK_TICKS = BACK_TICK + TEXT + BACK_TICK;
    private static final String TEXT_IN_CODE_TAG = putInCodeTag(TEXT);

    private final FormattingAction formatting = new BackTickFormatting();

    @Test
    public void surround_text_in_back_ticks_with_code_tag() {
        final String result = format(TEXT_IN_BACK_TICKS);
        assertEquals(TEXT_IN_CODE_TAG, result);
    }

    @Test
    public void handle_multiple_entries_surrounded_with_back_ticks() {
        final String separatingPart = " some other text ";
        final String source = TEXT_IN_BACK_TICKS + separatingPart + TEXT_IN_BACK_TICKS;
        final String expectedResult = TEXT_IN_CODE_TAG + separatingPart + TEXT_IN_CODE_TAG;
        assertEquals(expectedResult, format(source));
    }

    @Test
    public void not_handle_multi_lined_text_surrounded_with_back_ticks() {
        final String lineWithOpeningBackTick = BACK_TICK + TEXT;
        final String lineWithClosingBackTick = TEXT + BACK_TICK;
        final List<String> source = asList(lineWithOpeningBackTick, lineWithClosingBackTick);
        assertEquals(source, formatting.execute(source));
    }

    private String format(String line) {
        final List<String> lines = singletonList(line);
        return formatting.execute(lines)
                         .get(0);
    }
}
