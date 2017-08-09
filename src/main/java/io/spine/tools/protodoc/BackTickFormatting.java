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

import com.google.common.annotations.VisibleForTesting;

import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static java.lang.String.format;
import static org.gradle.internal.impldep.com.google.common.collect.Lists.newLinkedList;

/**
 * A formatting action, which handles a text in back ticks.
 *
 * <p>The action will replace all entries like {@code `text`} by {@code {@code text}}.
 *
 * <p>The multi lined text is not supported, e.g a text as follows will not be handled:
 * <pre>{@code
 * `some multi
 * lined text`
 * }</pre>
 *
 * @author Alexander Aleksandrov
 */
public class BackTickFormatting implements FormattingAction {

    private static final String BACK_TICK = "`";
    private static final String CODE_TAG_FORMAT = "{@code %s}";
    private static final Pattern PATTERN_BACK_TICK = Pattern.compile(BACK_TICK);

    /**
     * A pattern to match a text surrounded with back ticks.
     */
    private static final Pattern PATTERN = Pattern.compile("(`[^`]*?`)");

    @Override
    public List<String> execute(List<String> lines) {
        final List<String> result = newLinkedList();
        for (String line : lines) {
            final String formattedLine = formatLine(line);
            result.add(formattedLine);
        }
        return result;
    }

    private static String formatLine(CharSequence lineText) {
        final StringBuffer buffer = new StringBuffer(lineText.length() * 2);
        final Matcher matcher = PATTERN.matcher(lineText);
        while (matcher.find()) {
            final String partToFormat = matcher.group();
            final String partWithoutBackTicks = PATTERN_BACK_TICK.matcher(partToFormat)
                                                                 .replaceAll("");
            final String replacement = putInCodeTag(partWithoutBackTicks);
            matcher.appendReplacement(buffer, replacement);
        }
        matcher.appendTail(buffer);
        return buffer.toString();
    }

    @VisibleForTesting
    static String putInCodeTag(String value) {
        return format(CODE_TAG_FORMAT, value);
    }
}
