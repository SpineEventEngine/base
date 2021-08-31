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

import com.google.common.annotations.VisibleForTesting;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static java.lang.String.format;
import static java.util.regex.Matcher.quoteReplacement;

/**
 * A formatting action, which handles a text in backticks.
 *
 * <p>The action will replace text enclosed into backticks (e.g. {@code `text`}) with
 * the same text wrapped into the {@literal @}{@code code} Javadoc tag.
 *
 * <p>A multi-line text is not supported, e.g a text as follows will not be handled:
 * <pre>{@code
 * `some multi
 * lined text`
 * }</pre>
 */
final class BacktickedToCode extends ByLineFormatting {

    private static final Pattern BACKTICK = Pattern.compile("`");

    /**
     * A pattern to match a text surrounded with backticks.
     */
    private static final Pattern TEXT_IN_BACKTICKS = Pattern.compile("(`[^`]*?`)");

    @Override
    String formatLine(String line) {
        // Double the line size to avoid possible memory reallocation.
        StringBuffer buffer = new StringBuffer(line.length() * 2);

        Matcher matcher = TEXT_IN_BACKTICKS.matcher(line);
        while (matcher.find()) {
            String backtickedText = matcher.group();
            String text = untick(backtickedText);
            String replacement = wrapWithCodeTag(text);
            matcher.appendReplacement(buffer, quoteReplacement(replacement));
        }
        matcher.appendTail(buffer);
        @SuppressWarnings("JdkObsolete") // `StringBuffer` use dictated by regex API.
        String result = buffer.toString();
        return result;
    }

    private static String untick(String backtickedText) {
        return BACKTICK.matcher(backtickedText)
                       .replaceAll("");
    }

    @VisibleForTesting
    static String wrapWithCodeTag(String value) {
        return format("{@code %s}", value);
    }
}
