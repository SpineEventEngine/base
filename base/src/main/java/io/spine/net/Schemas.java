/*
 * Copyright 2020, TeamDev. All rights reserved.
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

package io.spine.net;

import com.google.common.collect.ImmutableMap;
import io.spine.net.Uri.Schema;

import java.util.Map;

import static com.google.common.base.Preconditions.checkNotNull;

/**
 * Utility class for working with {@link Schema}.
 */
final class Schemas {

    private static final Map<String, Schema> stringSchemas = buildSchemasMap();

    /** Prevents instantiation of this utility class. */
    private Schemas() {
    }

    /**
     * Parses schema from String representation.
     *
     * @param value String schema representation.
     * @return {@link Schema} value
     */
    static Schema parse(String value) {
        checkNotNull(value);
        String lowercaseValue = value.toLowerCase();
        if (!stringSchemas.containsKey(lowercaseValue)) {
            return Schema.UNDEFINED;
        }
        return stringSchemas.get(lowercaseValue);
    }

    private static Map<String, Schema> buildSchemasMap() {
        ImmutableMap.Builder<String, Schema> schemas = ImmutableMap.builder();

        for (Schema schema : Schema.values()) {
            schemas.put(schema.name()
                              .toLowerCase(), schema);
        }

        return schemas.build();
    }
}
