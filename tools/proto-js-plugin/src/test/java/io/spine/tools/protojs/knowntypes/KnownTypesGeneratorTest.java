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

package io.spine.tools.protojs.knowntypes;

import com.google.protobuf.Descriptors.FileDescriptor;
import io.spine.code.proto.FileSet;
import io.spine.tools.protojs.generate.JsOutput;
import io.spine.tools.protojs.files.JsFiles;
import io.spine.tools.protojs.generate.KnownTypesGenerator;
import io.spine.tools.protojs.types.Types;
import io.spine.type.TypeUrl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static io.spine.tools.protojs.given.Generators.assertContains;
import static io.spine.tools.protojs.given.Given.file;
import static io.spine.tools.protojs.given.Given.message;

/**
 * @author Dmytro Kuzmin
 */
@DisplayName("KnownTypesGenerator should")
class KnownTypesGeneratorTest {

    private FileDescriptor file;
    private JsOutput jsOutput;
    private KnownTypesGenerator generator;

    @BeforeEach
    void setUp() {
        FileSet fileSet = FileSet.newInstance();
        file = file();
        fileSet.add(file);
        jsOutput = new JsOutput();
        generator = new KnownTypesGenerator(fileSet, jsOutput);
    }

    @Test
    @DisplayName("generate imports for known types")
    void generateImports() {
        generator.generateImports();
        String jsFileName = JsFiles.jsFileName(file);
        String taskImport = "require('./" + jsFileName + "');";
        assertContains(jsOutput, taskImport);
    }

    @Test
    @DisplayName("generate known types map")
    void generateKnownTypesMap() {
        generator.generateKnownTypesMap();
        TypeUrl typeUrl = TypeUrl.from(message());
        String type = Types.typeWithProtoPrefix(message());
        String mapEntry = "['" + typeUrl + "', " + type + ']';
        assertContains(jsOutput, mapEntry);
    }
}
