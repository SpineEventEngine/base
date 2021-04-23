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

package io.spine.tools.js.code.index;

import com.google.common.collect.Maps;
import com.google.protobuf.Descriptors.FileDescriptor;
import io.spine.code.proto.FileSet;
import io.spine.tools.js.code.TypeName;
import io.spine.tools.js.code.Snippet;
import io.spine.tools.js.code.output.CodeLines;
import io.spine.tools.js.code.output.snippet.MapExportSnippet;
import io.spine.tools.js.code.parse.GenerateKnownTypeParsers;
import io.spine.type.MessageType;
import io.spine.type.TypeUrl;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;

import static com.google.common.base.Preconditions.checkNotNull;
import static java.util.stream.Collectors.toList;

/**
 * The code of the known type parsers {@code Map}.
 *
 * <p>This class generates the map with all the parsers written in the form of
 * "{@linkplain io.spine.type.TypeUrl type-url}-to-Parser-type".
 */
final class TypeParsersMap implements Snippet {

    private static final String MAP_NAME = "parsers";

    private final FileSet fileSet;

    TypeParsersMap(FileSet fileSet) {
        checkNotNull(fileSet);
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
        Collection<MessageType> typesWithParsers = new ArrayList<>();
        for (FileDescriptor file : fileSet.files()) {
            Collection<MessageType> typesInFile = GenerateKnownTypeParsers.targetTypes(file);
            typesWithParsers.addAll(typesInFile);
        }
        List<Map.Entry<String, TypeName>> entries = typesWithParsers
                .stream()
                .map(TypeParsersMap::mapEntry)
                .collect(toList());
        return entries;
    }

    private static Map.Entry<String, TypeName> mapEntry(MessageType type) {
        TypeUrl typeUrl = type.url();
        TypeName typeName = TypeName.ofParser(type.descriptor());
        return Maps.immutableEntry(typeUrl.value(), typeName);
    }
}
