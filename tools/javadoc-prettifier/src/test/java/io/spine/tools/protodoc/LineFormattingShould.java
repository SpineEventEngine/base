/*
 * Copyright 2018, TeamDev. All rights reserved.
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

import com.google.common.base.Joiner;
import org.junit.Test;

import java.util.Collections;

import static java.lang.System.lineSeparator;
import static org.junit.Assert.assertEquals;

/**
 * @author Dmytro Grankin
 */
public class LineFormattingShould {

    private final FormattingAction formatting = new ALineFormatting();

    @Test
    public void merge_lines_correctly() {
        final String lineText = "a text in a single line";
        final int lineCount = 5;
        final Iterable<String> lines = Collections.nCopies(lineCount, lineText);
        final String linesAsString = Joiner.on(lineSeparator())
                                           .join(lines);
        assertEquals(linesAsString, formatting.execute(linesAsString));
    }

    private static class ALineFormatting extends LineFormatting {

        @Override
        String formatLine(String line) {
            return line;
        }
    }
}
