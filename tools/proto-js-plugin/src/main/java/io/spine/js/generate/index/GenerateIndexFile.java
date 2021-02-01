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

package io.spine.js.generate.index;

import com.google.common.annotations.VisibleForTesting;
import com.google.protobuf.Descriptors;
import io.spine.code.fs.js.Directory;
import io.spine.code.fs.js.FileName;
import io.spine.code.proto.FileSet;
import io.spine.code.proto.TypeSet;
import io.spine.js.generate.GenerationTask;
import io.spine.js.generate.output.CodeLines;
import io.spine.js.generate.output.FileWriter;
import io.spine.js.generate.output.snippet.Import;

import java.util.Collection;
import java.util.Set;

import static io.spine.code.fs.js.LibraryFile.INDEX;
import static io.spine.js.generate.output.CodeLine.emptyLine;
import static java.util.stream.Collectors.toSet;

/**
 * The task to generate the {@code index.js} for generated Protobuf types.
 *
 * <p>The index file is used by the Spine Web to register known types and their parsers.
 *
 * <p>The index file provides:
 * <ul>
 *     <li>The map of known types.
 *     <li>The map of parsers for known types.
 * </ul>
 */
public final class GenerateIndexFile extends GenerationTask {

    public GenerateIndexFile(Directory generatedRoot) {
        super(generatedRoot);
    }

    @Override
    protected void generateFor(FileSet fileSet) {
        CodeLines code = codeFor(fileSet);
        FileWriter writer = FileWriter.createFor(generatedRoot(), INDEX);
        writer.write(code);
    }

    @VisibleForTesting
    static CodeLines codeFor(FileSet fileSet) {
        CodeLines lines = new CodeLines();
        lines.append(knownTypesImports(fileSet));
        lines.append(emptyLine());
        lines.append(new KnownTypesMap(fileSet));
        lines.append(emptyLine());
        lines.append(new TypeParsersMap(fileSet));
        return lines;
    }

    /**
     * Generates import statements for all files declaring generated messages.
     */
    private static CodeLines knownTypesImports(FileSet fileSet) {
        Collection<Descriptors.FileDescriptor> files = fileSet.files();
        Set<FileName> imports = files.stream()
                                     .filter(file -> !TypeSet.from(file)
                                                             .isEmpty())
                                     .map(FileName::from)
                                     .collect(toSet());
        CodeLines importLines = new CodeLines();
        for (FileName fileName : imports) {
            Import fileImport = Import.fileRelativeToRoot(fileName);
            importLines.append(fileImport);
        }
        return importLines;
    }
}
