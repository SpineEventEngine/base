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

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static com.google.common.base.Preconditions.checkNotNull;
import static java.util.regex.Pattern.compile;

/**
 * The {@code Quoter} for the {@code Map}.
 */
final class MapQuoter extends Quoter {

    static final MapQuoter INSTANCE = new MapQuoter();

    private static final String QUOTE_PATTERN = "((?=[^\\\\])[^\\w])";
    private static final Pattern DOUBLE_BACKSLASH_PATTERN = compile(BACKSLASH);

    @Override
    String quote(String stringToQuote) {
        checkNotNull(stringToQuote);
        Matcher matcher = compile(QUOTE_PATTERN).matcher(stringToQuote);
        String unslashed = matcher.find() ?
                           matcher.replaceAll(BACKSLASH + matcher.group()) :
                           stringToQuote;
        String result = QUOTE_CHAR + unslashed + QUOTE_CHAR;
        return result;
    }

    @Override
    String unquote(String value) {
        return unquoteValue(value, DOUBLE_BACKSLASH_PATTERN);
    }
}
