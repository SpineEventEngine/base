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

package io.spine.codegen.proto;

import io.spine.codegen.UnderscoredName;

import java.util.Iterator;

/**
 * Utilities for working with {@code CamelCapitalization}.
 *
 * @author Alexander Yevsyukov
 */
public class CamelCase {

    /** Prevent instantiation of this utility class. */
    private CamelCase() {
    }

    /**
     * Converts an underscored name to {@code CamelCase} string.
     *
     * <p>Does not force lowercase conversion so that {@code "test_HTTP_request"} would become
     * {@code "TestHTTPRequest"}.
     */
    public static String convert(UnderscoredName name) {
        final Iterator<String> iterator = name.words()
                                              .iterator();
        final StringBuilder builder = new StringBuilder(name.value()
                                                            .length());
        while (iterator.hasNext()) {
            final String word = iterator.next();
            if (!word.isEmpty()) {
                builder.append(Character.toUpperCase(word.charAt(0)))
                       .append(word.substring(1));
            }
        }

        return builder.toString();
    }
}
