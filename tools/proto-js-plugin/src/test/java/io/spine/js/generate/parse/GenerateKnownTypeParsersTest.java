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

package io.spine.js.generate.parse;

import com.google.common.testing.NullPointerTester;
import com.google.protobuf.Any;
import com.google.protobuf.Descriptors.Descriptor;
import com.google.protobuf.Descriptors.FileDescriptor;
import io.spine.code.GooglePackage;
import io.spine.code.fs.js.Directory;
import io.spine.code.fs.js.FileName;
import io.spine.code.gen.js.TypeName;
import io.spine.code.proto.FileSet;
import io.spine.code.proto.TypeSet;
import io.spine.js.generate.TaskId;
import io.spine.js.generate.given.GivenProject;
import io.spine.js.generate.output.CodeLines;
import io.spine.js.generate.output.snippet.Comment;
import io.spine.js.generate.output.snippet.Import;
import io.spine.option.OptionsProto;
import io.spine.type.MessageType;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.file.Path;
import java.util.Collection;
import java.util.List;

import static com.google.common.truth.Truth.assertThat;
import static io.spine.js.generate.given.FileWriters.assertFileContains;
import static io.spine.js.generate.given.Generators.assertContains;
import static io.spine.js.generate.parse.GenerateKnownTypeParsers.ABSTRACT_PARSER_IMPORT_NAME;
import static io.spine.js.generate.parse.GenerateKnownTypeParsers.OBJECT_PARSER_FILE;
import static io.spine.js.generate.parse.GenerateKnownTypeParsers.TYPE_PARSERS_FILE;
import static io.spine.js.generate.parse.GenerateKnownTypeParsers.TYPE_PARSERS_IMPORT_NAME;
import static io.spine.js.generate.parse.GenerateKnownTypeParsers.createFor;
import static io.spine.testing.DisplayNames.NOT_ACCEPT_NULLS;

@DisplayName("GenerateKnownTypeParsers should")
class GenerateKnownTypeParsersTest {

    private final FileDescriptor file = TaskId.getDescriptor()
                                              .getFile();
    private final FileSet fileSet = GivenProject.mainFileSet();
    private final Directory generatedProtoDir = GivenProject.mainProtoSources();
    private final GenerateKnownTypeParsers writer = createFor(generatedProtoDir);

    @Test
    @DisplayName(NOT_ACCEPT_NULLS)
    void passNullToleranceCheck() {
        new NullPointerTester().setDefault(Directory.class, generatedProtoDir)
                               .setDefault(FileSet.class, fileSet)
                               .testAllPublicStaticMethods(GenerateKnownTypeParsers.class);
    }

    @Test
    @DisplayName("generate explaining comment")
    void generateComment() {
        CodeLines code = GenerateKnownTypeParsers.codeFor(file);
        Comment expectedComment = Comment.generatedBySpine();
        assertContains(code, expectedComment.content());
    }

    @Test
    @DisplayName("generate imports")
    void generateImports() {
        CodeLines code = GenerateKnownTypeParsers.codeFor(file);
        String importPrefix = FileName.from(file)
                                      .pathToRoot();
        String abstractParserImport = Import.library(importPrefix + OBJECT_PARSER_FILE)
                                            .toDefault()
                                            .namedAs(ABSTRACT_PARSER_IMPORT_NAME);
        String typeParsersImport = Import.library(importPrefix + TYPE_PARSERS_FILE)
                                         .toDefault()
                                         .namedAs(TYPE_PARSERS_IMPORT_NAME);
        assertContains(code, abstractParserImport);
        assertContains(code, typeParsersImport);
    }

    @Test
    @DisplayName("write code for parsing")
    void writeParsingCode() throws IOException {
        writer.generateFor(fileSet);
        checkProcessedFiles(fileSet);
    }

    @Test
    @DisplayName("write code for parsing of Spine options")
    void writeOptionsParseCode() {
        FileDescriptor optionsFile = OptionsProto.getDescriptor()
                                                 .getFile();
        Collection<MessageType> targets = GenerateKnownTypeParsers.targetTypes(optionsFile);
        assertThat(targets).isNotEmpty();
    }

    @Test
    @DisplayName("not write parsing code for standard Protobuf types")
    void skipStandard() {
        Collection<MessageType> targets = GenerateKnownTypeParsers.targetTypes(Any.getDescriptor()
                                                                                  .getFile());
        assertThat(targets).isEmpty();
    }

    private void checkProcessedFiles(FileSet fileSet) throws IOException {
        Collection<FileDescriptor> fileDescriptors = fileSet.files();
        for (FileDescriptor file : fileDescriptors) {
            List<Descriptor> messageTypes = file.getMessageTypes();
            if (GooglePackage.isNotGoogle(file) && !messageTypes.isEmpty()) {
                checkParseCodeAdded(file);
            }
        }
    }

    private void checkParseCodeAdded(FileDescriptor file) throws IOException {
        Path jsFilePath = generatedProtoDir.resolve(FileName.from(file));
        for (MessageType messageType : TypeSet.onlyMessages(file)) {
            TypeName parserTypeName = TypeName.ofParser(messageType.descriptor());
            assertFileContains(jsFilePath, parserTypeName.value());
        }
    }
}
