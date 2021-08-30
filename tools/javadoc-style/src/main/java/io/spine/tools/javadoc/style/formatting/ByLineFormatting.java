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

import com.google.common.base.Splitter;

import static java.lang.System.lineSeparator;
import static java.util.stream.Collectors.joining;

/**
 * Formats lines independently of each other.
 */
abstract class ByLineFormatting implements Formatting {

    private static final Splitter splitter = Splitter.on(lineSeparator());

    /**
     * Obtains the formatted representation of the specified text.
     *
     * <p>The text will be split by lines using {@link System#lineSeparator()}, and
     * lines will be {@linkplain #formatLine(String) formatted} independently of each other.
     *
     * <p>Formatted lines are gathered back into one piece of text using the same line separator.
     *
     * @param text
     *         the text to format
     * @return the formatted text
     * @see #formatLine(String)
     */
    @Override
    public String apply(String text) {
        String result =
                splitter.splitToStream(text)
                        .map(this::formatLine)
                        .collect(joining(lineSeparator()));
        return result;
    }

    /**
     * Obtains the formatted representation of the specified line.
     *
     * @param line
     *         the single line without line separators
     * @return the formatted representation
     */
    abstract String formatLine(String line);
}
