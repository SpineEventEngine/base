/*
 * Copyright 2019, TeamDev. All rights reserved.
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

package io.spine.net;

import io.spine.net.Uri.QueryParameter;

import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Preconditions.checkNotNull;
import static io.spine.util.Exceptions.newIllegalArgumentException;

/**
 * The utility class which helps performing URL query parameters parsing and String conversion.
 */
final class UrlQueryParameters {

    private static final char SEPARATOR = '=';

    /** Prevent instantiation of this utility class. */
    private UrlQueryParameters() {
    }

    /**
     * Performs parsing of {@link QueryParameter} from String.
     *
     * @param queryParameter String representation of {@link QueryParameter}
     * @return parsed instance
     * @throws IllegalArgumentException in case of not well formed argument value
     */
    public static QueryParameter parse(String queryParameter) {
        int separatorIndex = queryParameter.indexOf(SEPARATOR);

        if (separatorIndex == -1) {
            throw newIllegalArgumentException("Query Parameter is invalid: %s", queryParameter);
        }

        String key = queryParameter.substring(0, separatorIndex);
        String value = queryParameter.substring(separatorIndex + 1);

        QueryParameter result = QueryParameter
                .newBuilder()
                .setKey(key)
                .setValue(value)
                .build();
        return result;
    }

    /**
     * Builds {@link QueryParameter} from given key-value pair.
     *
     * <p>Performs simple validation
     *
     * @param key   {@link QueryParameter} key
     * @param value {@link QueryParameter} value
     * @return {@link QueryParameter} instance
     */
    public static QueryParameter from(String key, String value) {
        checkNotNull(key);
        checkNotNull(value);
        checkArgument(!key.isEmpty(), "Query parameter key cannot be empty.");
        checkArgument(!value.isEmpty(), "Query parameter value cannot be empty.");

        QueryParameter result = QueryParameter
                .newBuilder()
                .setKey(key)
                .setValue(value)
                .build();
        return result;
    }

    /**
     * Performs String conversion of {@link QueryParameter}.
     *
     * @param param {@link QueryParameter} instance
     * @return key=value String
     */
    public static String toString(QueryParameter param) {
        String result = param.getKey() + SEPARATOR + param.getValue();
        return result;
    }
}
