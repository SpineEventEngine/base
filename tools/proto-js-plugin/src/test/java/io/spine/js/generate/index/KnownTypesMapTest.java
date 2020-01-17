/*
 * Copyright 2020, TeamDev. All rights reserved.
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

import com.google.protobuf.Any;
import com.google.protobuf.Descriptors.Descriptor;
import com.google.protobuf.NullValue;
import com.google.protobuf.StringValue;
import io.spine.code.gen.js.TypeName;
import io.spine.code.proto.FileSet;
import io.spine.js.generate.output.CodeLines;
import io.spine.type.TypeUrl;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static io.spine.js.generate.given.Generators.assertContains;

@DisplayName("KnownTypesMap should")
class KnownTypesMapTest {

    /**
     * {@link Any} type is used for tests as we known for sure it will be present among the
     * {@linkplain FileSet#load() types loaded from classpath}.
     */
    private static final Descriptor ANY = Any.getDescriptor();
    private static final Descriptor STRING_VALUE = StringValue.getDescriptor();

    private final FileSet fileSet = FileSet.load();
    private final KnownTypesMap generator = new KnownTypesMap(fileSet);

    @Test
    @DisplayName("generate known types map for several files")
    void generateKnownTypesMap() {
        CodeLines generatedLines = generator.value();
        String expectedForAny = expectedEntry(ANY) + ',';
        String expectedForString = expectedEntry(STRING_VALUE) + ',';
        assertContains(generatedLines, expectedForAny);
        assertContains(generatedLines, expectedForString);
    }

    @Test
    @DisplayName("include enum types")
    void includeEnums() {
        CodeLines generatedLines = generator.value();
        TypeUrl enumTypeUrl = TypeUrl.from(NullValue.getDescriptor());
        assertContains(generatedLines, enumTypeUrl.toString());
    }

    private static String expectedEntry(Descriptor message) {
        TypeUrl typeUrl = TypeUrl.from(message);
        TypeName typeName = TypeName.from(message);
        return "['" + typeUrl + "', " + typeName + ']';
    }
}
