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

package io.spine.tools.mc.js.code.index;

import com.google.common.collect.Maps;
import io.spine.tools.js.code.TypeName;
import io.spine.code.proto.FileSet;
import io.spine.code.proto.TypeSet;
import io.spine.tools.mc.js.code.Snippet;
import io.spine.tools.mc.js.code.output.CodeLines;
import io.spine.tools.mc.js.code.output.snippet.MapExportSnippet;
import io.spine.type.Type;
import io.spine.type.TypeUrl;

import java.util.List;
import java.util.Map;
import java.util.Set;

import static java.util.stream.Collectors.toList;

/**
 * The code of the known types {@code Map}.
 *
 * <p>This class generates the map with all the known types written in the form of
 * "{@linkplain io.spine.type.TypeUrl type-url}-to-JS-type".
 */
final class KnownTypesMap implements Snippet {

    /**
     * The exported map name.
     */
    private static final String MAP_NAME = "types";

    private final FileSet fileSet;

    /**
     * Creates a new {@code KnownTypesGenerator}.
     *
     * <p>All the known types will be acquired from the {@code fileSet}.
     *
     * @param fileSet
     *         the {@code FileSet} containing all the known types
     */
    KnownTypesMap(FileSet fileSet) {
        this.fileSet = fileSet;
    }

    @Override
    public CodeLines value() {
        List<Map.Entry<String, TypeName>> entries = mapEntries(fileSet);
        MapExportSnippet mapSnippet = MapExportSnippet
                .newBuilder(MAP_NAME)
                .withEntries(entries)
                .build();
        return mapSnippet.value();
    }

    private static List<Map.Entry<String, TypeName>> mapEntries(FileSet fileSet) {
        Set<Type<?, ?>> types = TypeSet.from(fileSet)
                                       .allTypes();
        List<Map.Entry<String, TypeName>> entries = types.stream()
                                                         .map(KnownTypesMap::mapEntry)
                                                         .collect(toList());
        return entries;
    }

    /**
     * Obtains type URL and JS type name of the {@code message} and creates a {@code Map} entry of
     * the "{@linkplain TypeUrl type-url}-to-JS-type" format.
     */
    private static Map.Entry<String, TypeName> mapEntry(Type type) {
        TypeUrl typeUrl = type.url();
        TypeName typeName = TypeName.from(type.descriptor());
        return Maps.immutableEntry(typeUrl.value(), typeName);
    }
}
