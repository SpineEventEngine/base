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

package io.spine.net;

import com.google.common.collect.ImmutableMap;
import io.spine.net.Uri.Schema;

import java.util.Locale;
import java.util.Map;
import java.util.Objects;

import static com.google.common.base.Preconditions.checkNotNull;

/**
 * Utility class for working with {@link Schema}.
 */
final class Schemas {

    /** Maps a lowercase name of a schema to its instance. */
    private static final Map<SchemaKey, Schema> map = buildSchemasMap();

    /** Prevents instantiation of this utility class. */
    private Schemas() {
    }

    /**
     * Tries to find a schema by the passed name.
     *
     * @param name the name of the schema
     * @return {@link Schema} instance, or {@link Schema#UNDEFINED} if there is no schema with such
     * a name
     */
    static Schema parse(String name) {
        checkNotNull(name);
        SchemaKey key = new SchemaKey(name);
        Schema result = map.getOrDefault(key, Schema.UNDEFINED);
        return result;
    }

    private static Map<SchemaKey, Schema> buildSchemasMap() {
        ImmutableMap.Builder<SchemaKey, Schema> schemas = ImmutableMap.builder();
        for (Schema schema : Schema.values()) {
            if (schema == Schema.UNDEFINED) {
                continue;
            }
            schemas.put(new SchemaKey(schema), schema);
        }
        return schemas.build();
    }

    /**
     * A key in the {@link #map} responsible for lowercase conversion of a schema name
     * for lookup purposes.
     *
     * @see #parse(String)
     */
    private static final class SchemaKey {

        private final String value;

        private SchemaKey(String value) {
            this.value = toValue(value);
        }

        private SchemaKey(Schema schema) {
            this(schema.name());
        }

        private static String toValue(String input) {
            return input.toLowerCase(Locale.ENGLISH);
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) {
                return true;
            }
            if (o == null || getClass() != o.getClass()) {
                return false;
            }
            SchemaKey schemaKey = (SchemaKey) o;
            return Objects.equals(value, schemaKey.value);
        }

        @Override
        public int hashCode() {
            return value.hashCode();
        }
    }
}
