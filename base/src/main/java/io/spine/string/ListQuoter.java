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

package io.spine.string;

import java.util.regex.Pattern;

import static com.google.common.base.Preconditions.checkNotNull;
import static java.util.regex.Pattern.compile;

/**
 * The {@code Quoter} for the {@code List}.
 */
final class ListQuoter extends Quoter {

    static final Quoter INSTANCE = new ListQuoter();

    private static final String BACKSLASH_PATTERN_VALUE = "\\\\\\\\";
    private static final Pattern BACKSLASH_LIST_PATTERN = compile(BACKSLASH_PATTERN_VALUE);
    private static final String ESCAPED_QUOTE = BACKSLASH + QUOTE_CHAR;
    private static final String QUOTE = String.valueOf(QUOTE_CHAR);
    private static final Pattern QUOTE_PATTERN = compile(QUOTE);

    @Override
    String quote(String stringToQuote) {
        checkNotNull(stringToQuote);
        var escaped = QUOTE_PATTERN.matcher(stringToQuote)
                                   .replaceAll(ESCAPED_QUOTE);
        var result = QUOTE_CHAR + escaped + QUOTE_CHAR;
        return result;
    }

    @Override
    String unquote(String value) {
        return unquoteValue(value, BACKSLASH_LIST_PATTERN);
    }
}
