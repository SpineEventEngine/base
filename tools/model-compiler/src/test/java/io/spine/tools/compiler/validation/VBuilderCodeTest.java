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

package io.spine.tools.compiler.validation;

import com.google.protobuf.Descriptors.Descriptor;
import io.spine.code.generate.Indent;
import io.spine.code.proto.MessageType;
import io.spine.test.tools.validation.builder.VbtProcess;
import io.spine.test.tools.validation.builder.VbtProject;
import io.spine.test.tools.validation.builder.VbtScalarFields;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junitpioneer.jupiter.TempDirectory;

import java.io.File;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.assertTrue;

@ExtendWith(TempDirectory.class)
@DisplayName("VBuilderCode should generate code for")
class VBuilderCodeTest {

    private File targetDir;

    @BeforeEach
    void setUp(@TempDirectory.TempDir Path tempDirPath) {
        targetDir = tempDirPath.toFile();
    }

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

    @Nested
    @DisplayName("a message nested into another message")
    class NestedSecondLevel {

        @Test
        @DisplayName("2nd level")
        void doSomething() {
            assertGeneratesFor(VbtProcess.Point.getDescriptor());
        }
    }

    private void assertGeneratesFor(Descriptor descriptor) {
        MessageType type = MessageType.of(descriptor);
        VBuilderCode code = new VBuilderCode(targetDir, Indent.of4(), type);
        File file = code.write();
        assertTrue(file.exists());
    }
}
