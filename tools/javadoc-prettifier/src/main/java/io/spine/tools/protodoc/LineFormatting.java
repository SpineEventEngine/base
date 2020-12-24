/*
 * Copyright 2020, TeamDev. All rights reserved.
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

package io.spine.tools.protodoc;

import com.google.common.base.Joiner;
import com.google.common.base.Splitter;

import java.util.List;

import static com.google.common.collect.Lists.newLinkedList;
import static java.lang.System.lineSeparator;

/**
 * A {@link FormattingAction}, that formats lines independently from each other.
 */
abstract class LineFormatting implements FormattingAction {

    /**
     * Obtains the formatted representation of the specified text.
     *
     * <p>The text will be split and lines will be formatted independently from each other.
     *
     * @param text the text to format
     * @return the formatted text
     */
    @Override
    public String execute(String text) {
        List<String> textAsLines = Splitter.on(lineSeparator())
                                           .splitToList(text);
        List<String> formattedLines = newLinkedList();
        for (String line : textAsLines) {
            String formattedLine = formatLine(line);
            formattedLines.add(formattedLine);
        }
        return Joiner.on(lineSeparator())
                     .join(formattedLines);
    }

    /**
     * Obtains the formatted representation of the specified line.
     *
     * @param line the single line without line separators
     * @return the formatted representation
     */
    abstract String formatLine(String line);
}
