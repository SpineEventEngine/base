/*
 * Copyright 2019, TeamDev. All rights reserved.
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

package io.spine.tools.compiler.validation;

import com.google.errorprone.annotations.CanIgnoreReturnValue;
import com.google.protobuf.Descriptors.Descriptor;
import io.spine.code.generate.Indent;
import io.spine.code.structure.java.FileName;
import io.spine.test.tools.validation.builder.TheOuterProto;
import io.spine.test.tools.validation.builder.VbtMap;
import io.spine.test.tools.validation.builder.VbtOrder;
import io.spine.test.tools.validation.builder.VbtProcess;
import io.spine.test.tools.validation.builder.VbtProject;
import io.spine.test.tools.validation.builder.VbtScalarFields;
import io.spine.test.tools.validation.builder.VbtTree;
import io.spine.type.MessageType;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junitpioneer.jupiter.TempDirectory;

import java.io.File;
import java.nio.file.Path;

import static com.google.common.truth.Truth.assertThat;
import static io.spine.type.MessageType.VBUILDER_SUFFIX;
import static org.junit.jupiter.api.Assertions.assertTrue;

@ExtendWith(TempDirectory.class)
@DisplayName("VBuilderCode should")
class VBuilderCodeTest {

    private File targetDir;

    @BeforeEach
    void setUp(@TempDirectory.TempDir Path tempDirPath) {
        targetDir = tempDirPath.toFile();
    }

    /**
     * Attempts to generate a Validating Builder for the passed type, and
     * asserts that the file is created.
     *
     * @param descriptor the type for which to generate Validating Builder
     * @return created file
     */
    @CanIgnoreReturnValue
    private File assertGeneratesFor(Descriptor descriptor) {
        MessageType type = new MessageType(descriptor);
        VBuilderCode code = new VBuilderCode(targetDir, Indent.of4(), type);
        File file = code.write();
        assertTrue(file.exists());
        return file;
    }

    @Nested
    @DisplayName("generate code for")
    class Generate {

        @Nested
        @DisplayName("top-level message")
        class TopLevel {

            @Test
            @DisplayName("with message fields")
            void messageFields() {
                assertGeneratesFor(VbtProject.getDescriptor());
            }

            @Test
            @DisplayName("with scalar fields")
            void scalarFields() {
                assertGeneratesFor(VbtScalarFields.getDescriptor());
            }

        }

        @Test
        @DisplayName("a 2nd level message nested into another message")
        void secondLevel() {
            assertGeneratesFor(VbtProcess.Point.getDescriptor());
            // ... and the outer class is generated too.
            assertGeneratesFor(VbtProcess.getDescriptor());
        }

        @Test
        @DisplayName("Top level message with `repeated` field of a nested type")
        void secondLevelRepeated() {
            assertGeneratesFor(VbtOrder.Item.getDescriptor());
            assertGeneratesFor(VbtOrder.getDescriptor());
        }

        @Test
        @DisplayName("Top level message with map field of a nested type")
        void mapOfNested() {
            assertGeneratesFor(VbtMap.Value.getDescriptor());
            assertGeneratesFor(VbtMap.getDescriptor());
        }
    }

    @Nested
    @DisplayName("produce file with")
    class NameOfFile {

        void assertFileName(String expected, Descriptor descriptor) {
            File file = assertGeneratesFor(descriptor);
            String nameOnly = FileName.nameOnly(file);

            assertThat(nameOnly)
                    .isEqualTo(expected);
        }

        @Test
        @DisplayName("top-level class name")
        void topLevel() {
            assertFileName(VbtProject.class.getSimpleName() + VBUILDER_SUFFIX,
                           VbtProject.getDescriptor());
        }

        @Test
        @DisplayName("nested class message")
        void nested() {
            assertFileName(VbtTree.class.getSimpleName() +
                           VbtTree.Branch.class.getSimpleName() +
                           VbtTree.Branch.Leaf.class.getSimpleName() + VBUILDER_SUFFIX,
                           VbtTree.Branch.Leaf.getDescriptor());
        }

        @Test
        @DisplayName("outer class name")
        void outerClass() {
            assertFileName(TheOuterProto.class.getSimpleName() +
                           TheOuterProto.VbtTopLevel.class.getSimpleName() +
                           TheOuterProto.VbtTopLevel.Nested.class.getSimpleName() + VBUILDER_SUFFIX,
                           TheOuterProto.VbtTopLevel.Nested.getDescriptor());
        }
    }
}
