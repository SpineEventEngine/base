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

import com.google.protobuf.Descriptors.FileDescriptor;
import io.spine.tools.js.fs.Directory;
import io.spine.tools.js.fs.FileName;
import io.spine.code.proto.FileSet;
import io.spine.js.generate.given.GivenProject;
import io.spine.tools.mc.js.code.CodeWriter;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.nio.file.Path;

import static io.spine.tools.js.fs.LibraryFile.INDEX;
import static io.spine.js.generate.given.Generators.assertContains;
import static java.nio.file.Files.exists;
import static org.junit.jupiter.api.Assertions.assertTrue;

@DisplayName("GenerateIndexFile should")
class GenerateIndexFileTest {

    private final FileSet fileSet = GivenProject.mainFileSet();
    private final Directory generatedProtoDir = GivenProject.mainProtoSources();
    private final GenerateIndexFile task = new GenerateIndexFile(generatedProtoDir);

    @Test
    @DisplayName("write known types map to JS file")
    void writeKnownTypes() {
        task.performFor(fileSet);
        Path knownTypes = generatedProtoDir.resolve(INDEX);
        assertTrue(exists(knownTypes));
    }

    @Test
    @DisplayName("generate imports for known types")
    void generateImports() {
        CodeWriter generatedCode = GenerateIndexFile.codeFor(fileSet);
        for (FileDescriptor file : fileSet.files()) {
            FileName fileName = FileName.from(file);
            String fileImport = "require('./" + fileName + "');";
            assertContains(generatedCode, fileImport);
        }
    }
}
