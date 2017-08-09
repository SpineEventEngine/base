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

import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static org.gradle.internal.impldep.com.google.common.collect.Lists.newLinkedList;

/**
 * @author Alexander Aleksandrov
 */
class BackTickFormatting implements FormattingAction {

    private static final Pattern PATTERN = Pattern.compile("`[^`]+`");

    @Override
    public List<String> execute(List<String> lines) {
        final List<String> result = newLinkedList();
        for (String line : lines) {
            final String formattedLine = formatLine(line);
            result.add(formattedLine);
        }
        return result;
    }

    private static String formatLine(String line) {
        final StringBuffer buffer = new StringBuffer();
        final Matcher matcher = PATTERN.matcher(line);
        while (matcher.find()) {
            final String replacement = matcher.group(1);
            matcher.appendReplacement(buffer, replacement);
        }
        matcher.appendTail(buffer);
        return buffer.toString();
    }
}
