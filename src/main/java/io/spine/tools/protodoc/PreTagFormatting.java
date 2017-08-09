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

/**
 * Javadoc comments checker that validates the links wrong execute usage.
 * In case if any violation is found it will be logged as warning in build's
 * stacktrace info or an error will be thrown. That depends on threshold and report type parameters
 * stated in build file.
 *
 * @author Alexander Aleksandrov
 */
public class PreTagFormatting implements FormattingAction {

    @Override
    public List<String> execute(List<String> lines) {
        Pattern p = Pattern.compile("<pre>|<\\/pre>");
        for (int i = 0; i < lines.size(); i++) {
            Matcher matcher = p.matcher(lines.get(i));
            lines.set(i, matcher.replaceAll(""));
        }
        return lines;
    }
}
