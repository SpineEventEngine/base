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

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static java.lang.String.format;
import static java.util.regex.Matcher.quoteReplacement;

/**
 * A formatting action, which handles a text in back ticks.
 *
 * <p>The action will replace all entries like {@code `text`} by {@code {@code text}}.
 *
 * <p>The multi-lined text is not supported, e.g a text as follows will not be handled:
 * <pre>{@code
 * `some multi
 * lined text`
 * }</pre>
 *
 * @author Alexander Aleksandrov
 */
class BackTickFormatting extends LineFormatting {

    private static final String BACK_TICK = "`";
    private static final String CODE_TAG_FORMAT = "{@code %s}";
    private static final Pattern PATTERN_BACK_TICK = Pattern.compile(BACK_TICK);

    /**
     * A pattern to match a text surrounded with back ticks.
     */
    private static final Pattern PATTERN = Pattern.compile("(`[^`]*?`)");

    @Override
    String formatLine(String line) {
        final StringBuffer buffer = new StringBuffer(line.length() * 2);
        final Matcher matcher = PATTERN.matcher(line);
        while (matcher.find()) {
            final String partToFormat = matcher.group();
            final String partWithoutBackTicks = PATTERN_BACK_TICK.matcher(partToFormat)
                                                                 .replaceAll("");
            final String replacement = wrapWithCodeTag(partWithoutBackTicks);
            matcher.appendReplacement(buffer, quoteReplacement(replacement));
        }
        matcher.appendTail(buffer);
        return buffer.toString();
    }

    @VisibleForTesting
    static String wrapWithCodeTag(String value) {
        return format(CODE_TAG_FORMAT, value);
    }
}
