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

package io.spine.js.generate.parse;

import com.google.common.testing.NullPointerTester;
import com.google.protobuf.Any;
import com.google.protobuf.Descriptors.FileDescriptor;
import io.spine.code.js.Directory;
import io.spine.code.js.FileName;
import io.spine.code.proto.FileSet;
import io.spine.js.generate.given.GivenProject;
import io.spine.option.OptionsProto;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.file.Path;
import java.util.Collection;

import static io.spine.code.js.LibraryFile.KNOWN_TYPES;
import static io.spine.code.js.LibraryFile.KNOWN_TYPE_PARSERS;
import static io.spine.js.generate.given.FileWriters.assertFileContains;
import static io.spine.js.generate.parse.FromJsonMethod.FROM_JSON;
import static io.spine.js.generate.parse.KnownTypeParsers.createFor;
import static io.spine.js.generate.parse.KnownTypeParsers.shouldSkip;
import static io.spine.testing.DisplayNames.NOT_ACCEPT_NULLS;
import static java.nio.file.Files.exists;
import static org.junit.jupiter.api.Assertions.assertTrue;

@DisplayName("KnownTypeParsers enhancement should")
class KnownTypeParsersTest {

    private final FileSet protoSources = GivenProject.mainFileSet();
    private final Directory generatedProtoDir = GivenProject.mainProtoSources();
    private final KnownTypeParsers writer = createFor(generatedProtoDir, protoSources);

    @Test
    @DisplayName(NOT_ACCEPT_NULLS)
    void passNullToleranceCheck() {
        new NullPointerTester().setDefault(Directory.class, generatedProtoDir)
                               .setDefault(FileSet.class, protoSources)
                               .testAllPublicStaticMethods(KnownTypeParsers.class);
    }

    @Test
    @DisplayName("write known types map to JS file")
    void writeKnownTypes() {
        writer.writeKnownTypes();
        Path knownTypes = generatedProtoDir.resolve(KNOWN_TYPES);
        assertTrue(exists(knownTypes));
    }

    @Test
    @DisplayName("write known type parsers map to JS file")
    void writeKnownTypeParsers() {
        writer.writeKnownTypeParsers();
        Path knownTypeParsers = generatedProtoDir.resolve(KNOWN_TYPE_PARSERS);
        assertTrue(exists(knownTypeParsers));
    }

    @Test
    @DisplayName("write `fromJson` method into generated JS files")
    void writeFromJsonMethod() throws IOException {
        writer.writeFromJsonMethod();
        checkProcessedFiles(protoSources);
    }

    @Test
    @DisplayName("not write `fromJson` method into Spine Options file")
    void skipSpineOptions() {
        FileDescriptor spineOptionsFile = OptionsProto.getDescriptor();
        assertTrue(shouldSkip(spineOptionsFile));
    }

    @Test
    @DisplayName("not write `fromJson` method into files declaring standard Protobuf types")
    void skipStandard() {
        FileDescriptor fileDeclaringAny = Any.getDescriptor()
                                             .getFile();
        assertTrue(shouldSkip(fileDeclaringAny));
    }

    private void checkProcessedFiles(FileSet fileSet) throws IOException {
        Collection<FileDescriptor> fileDescriptors = fileSet.files();
        for (FileDescriptor file : fileDescriptors) {
            if (!shouldSkip(file)) {
                checkFromJsonDeclared(file);
            }
        }
    }

    private void checkFromJsonDeclared(FileDescriptor file) throws IOException {
        Path jsFilePath = generatedProtoDir.resolve(FileName.from(file));
        String fromJsonDeclaration = '.' + FROM_JSON + " = function";
        assertFileContains(jsFilePath, fromJsonDeclaration);
    }
}
