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

package io.spine.tools.protoc;

import com.google.common.collect.ImmutableList;
import io.spine.type.MessageType;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import static com.google.common.truth.Truth.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

@DisplayName("CodeGenerationTasks should")
final class CodeGenerationTasksTest {

    @DisplayName("throw NullPointerException if")
    @Nested
    class ThrowNpe {

        @DisplayName("is create with `null` tasks list")
        @Test
        void isCreatedWithNullList() {
            assertThrows(NullPointerException.class, () -> new CodeGenerationTasks(null));
        }

        @DisplayName("`null` MessageType is supplied")
        @Test
        void nullMessageTypeIsSupplied() {
            assertThrows(NullPointerException.class, () ->
                    new CodeGenerationTasks(ImmutableList.of()).generateFor(null)
            );
        }
    }

    @DisplayName("perform code generation using each supplied task")
    @Test
    void performCodeGenerationUsingEachSuppliedTask() {
        ImmutableList<CodeGenerationTask> tasks =
                ImmutableList.of(new SingleResultTask(), new SingleResultTask());
        MessageType type = new MessageType(EnhancedWithCodeGeneration.getDescriptor());
        CodeGenerationTasks codeGenerationTasks = new CodeGenerationTasks(tasks);
        ImmutableList<CompilerOutput> actual = codeGenerationTasks.generateFor(type);
        assertThat(actual).hasSize(2);
    }

    private static class SingleResultTask implements CodeGenerationTask {

        @Override
        public ImmutableList<CompilerOutput> generateFor(MessageType type) {
            return ImmutableList.of(() -> null);
        }
    }
}
