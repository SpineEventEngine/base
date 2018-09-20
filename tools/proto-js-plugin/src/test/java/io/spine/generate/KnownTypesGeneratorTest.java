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

package io.spine.generate;

import com.google.protobuf.Any;
import com.google.protobuf.Descriptors.Descriptor;
import com.google.protobuf.Descriptors.FileDescriptor;
import io.spine.code.js.FileName;
import io.spine.code.js.TypeName;
import io.spine.code.proto.FileSet;
import io.spine.type.TypeUrl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static io.spine.generate.given.Generators.assertContains;

/**
 * @author Dmytro Kuzmin
 */
@SuppressWarnings("DuplicateStringLiteralInspection")
// Duplication necessary to check main class code.
@DisplayName("KnownTypesGenerator should")
class KnownTypesGeneratorTest {

    /**
     * {@link Any} type is used for tests as we known for sure it will be present among the
     * {@linkplain FileSet#load() types loaded from classpath}.
     */
    private static final Descriptor ANY = Any.getDescriptor();

    private JsOutput jsOutput;
    private KnownTypesGenerator generator;

    @BeforeEach
    void setUp() {
        FileSet fileSet = FileSet.load();
        jsOutput = new JsOutput();
        generator = new KnownTypesGenerator(fileSet, jsOutput);
    }

    @Test
    @DisplayName("generate imports for known types")
    void generateImports() {
        generator.generateImports();
        FileDescriptor file = Any.getDescriptor()
                                 .getFile();
        FileName fileName = FileName.from(file);
        String taskImport = "require('" + fileName + "');";
        assertContains(jsOutput, taskImport);
    }

    @Test
    @DisplayName("generate known types map")
    void generateKnownTypesMap() {
        generator.generateKnownTypesMap();
        TypeUrl typeUrl = TypeUrl.from(Any.getDescriptor());
        TypeName typeName = TypeName.from(ANY);
        String mapEntry = "['" + typeUrl + "', " + typeName + ']';
        assertContains(jsOutput, mapEntry);
    }
}
