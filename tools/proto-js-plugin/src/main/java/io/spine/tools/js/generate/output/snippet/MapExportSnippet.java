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

package io.spine.tools.js.generate.output.snippet;

import com.google.errorprone.annotations.CanIgnoreReturnValue;
import io.spine.tools.js.generate.Snippet;
import io.spine.tools.js.generate.output.CodeLine;
import io.spine.tools.js.generate.output.CodeLines;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static com.google.common.base.Preconditions.checkNotNull;
import static io.spine.tools.js.generate.output.CodeLines.commaSeparated;
import static java.lang.String.format;

/**
 * A snippet representing an export of an initialized map.
 */
public class MapExportSnippet implements Snippet {

    private final String mapName;
    private final List<CodeLine> entries;

    private MapExportSnippet(Builder builder) {
        this.mapName = builder.mapName;
        this.entries = builder.entries;
    }

    @Override
    public CodeLines value() {
        CodeLines lines = new CodeLines();
        lines.append("module.exports." + mapName + " = new Map([");
        appendEntries(lines);
        lines.append("]);");
        return lines;
    }

    private void appendEntries(CodeLines lines) {
        lines.increaseDepth();
        lines.append(commaSeparated(entries));
        lines.decreaseDepth();
    }

    /**
     * Obtains a builder for a map export.
     */
    public static Builder newBuilder(String mapName) {
        return new Builder(mapName);
    }

    public static class Builder {

        private final String mapName;
        private final List<CodeLine> entries;

        private Builder(String mapName) {
            this.mapName = mapName;
            this.entries = new ArrayList<>();
        }

        /**
         * Adds an entry with the string literal key.
         */
        @CanIgnoreReturnValue
        public Builder withEntry(String key, Object value) {
            CodeLine entry = mapEntry(key, value);
            entries.add(entry);
            return this;
        }

        /**
         * Adds several {@linkplain #withEntry(String, Object) entries}.
         */
        @CanIgnoreReturnValue
        public <T> Builder withEntries(List<Map.Entry<String, T>> entriesToAdd) {
            for (Map.Entry<String, ?> entry : entriesToAdd) {
                withEntry(entry.getKey(), entry.getValue());
            }
            return this;
        }

        /**
         * Builds an export statement from the builder.
         */
        public MapExportSnippet build() {
            return new MapExportSnippet(this);
        }

        /**
         * Obtains a map entry with the string literal key.
         */
        private static CodeLine mapEntry(String key, Object value) {
            checkNotNull(key);
            checkNotNull(value);
            String entry = format("['%s', %s]", key, value);
            return CodeLine.of(entry);
        }
    }
}
