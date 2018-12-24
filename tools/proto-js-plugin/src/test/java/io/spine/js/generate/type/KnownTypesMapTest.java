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

package io.spine.js.generate.type;

import com.google.common.collect.ImmutableList;
import com.google.protobuf.Any;
import com.google.protobuf.Descriptors.Descriptor;
import com.google.protobuf.Descriptors.FileDescriptor;
import com.google.protobuf.StringValue;
import io.spine.code.js.FileName;
import io.spine.code.js.TypeName;
import io.spine.code.proto.FileSet;
import io.spine.js.generate.output.CodeLines;
import io.spine.type.TypeUrl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static io.spine.js.generate.given.Generators.assertContains;

@SuppressWarnings("DuplicateStringLiteralInspection")
// Duplication necessary to check main class code.
@DisplayName("KnownTypesMap should")
class KnownTypesMapTest {

    /**
     * {@link Any} type is used for tests as we known for sure it will be present among the
     * {@linkplain FileSet#load() types loaded from classpath}.
     */
    private static final Descriptor ANY = Any.getDescriptor();
    private static final Descriptor STRING_VALUE = StringValue.getDescriptor();

    private KnownTypesMap generator;

    @BeforeEach
    void setUp() {
        FileSet fileSet = FileSet.load();
        generator = new KnownTypesMap(fileSet);
    }

    @Test
    @DisplayName("generate imports for known types")
    void generateImports() {
        CodeLines output = new CodeLines();
        generator.generateImports(output);
        FileDescriptor file = Any.getDescriptor()
                                 .getFile();
        FileName fileName = FileName.from(file);
        String taskImport = "require('./" + fileName + "');";
        assertContains(output, taskImport);
    }

    @Test
    @DisplayName("generate known types map for several files")
    void generateKnownTypesMap() {
        List<FileDescriptor> files = ImmutableList.of(ANY.getFile(), STRING_VALUE.getFile());
        CodeLines entries = KnownTypesMap.knownTypeEntries(files);
        String expectedForAny = expectedEntry(ANY) + ',';
        String expectedForString = expectedEntry(STRING_VALUE) + ',';
        assertContains(entries, expectedForAny);
        assertContains(entries, expectedForString);
    }

    private static String expectedEntry(Descriptor message) {
        TypeUrl typeUrl = TypeUrl.from(message);
        TypeName typeName = TypeName.from(message);
        return "['" + typeUrl + "', " + typeName + ']';
    }
}
