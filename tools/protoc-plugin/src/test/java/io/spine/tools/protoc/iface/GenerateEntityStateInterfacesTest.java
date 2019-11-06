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

package io.spine.tools.protoc.iface;

import com.google.common.collect.ImmutableList;
import com.google.common.testing.NullPointerTester;
import io.spine.base.EntityState;
import io.spine.code.java.ClassName;
import io.spine.tools.protoc.CompilerOutput;
import io.spine.tools.protoc.EntityStateConfig;
import io.spine.type.MessageType;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import static com.google.common.truth.Truth.assertThat;
import static io.spine.testing.DisplayNames.NOT_ACCEPT_NULLS;
import static io.spine.tools.protoc.InsertionPoint.message_implements;
import static io.spine.tools.protoc.ProtocTaskConfigs.entityStateConfig;
import static org.junit.jupiter.api.Assertions.assertThrows;

@DisplayName("`GenerateEntityStateInterfaces` task should")
class GenerateEntityStateInterfacesTest {

    private GenerateEntityStateInterfaces task;

    @BeforeEach
    void initTask() {
        this.task = markEntityStatesAs(EntityState.class);
    }

    @Test
    @DisplayName(NOT_ACCEPT_NULLS)
    void passNullToleranceCheck() {
        new NullPointerTester()
                .testAllPublicInstanceMethods(task);
    }

    @SuppressWarnings({"CheckReturnValue", "ResultOfMethodCallIgnored"})
    // Method called to throw exception.
    @Nested
    @DisplayName("throw `IllegalArgumentException` when the specified class name is")
    class ThrowOnClassName {

        @Test
        @DisplayName("blank")
        void blank() {
            assertThrows(IllegalArgumentException.class, () -> markEntityStatesAs(""));
        }

        @Test
        @DisplayName("effectively blank")
        void effectivelyBlank() {
            assertThrows(IllegalArgumentException.class, () -> markEntityStatesAs("   "));
        }
    }

    @Test
    @DisplayName("produce code output for message that is entity state")
    void produceOutputIfIsEntityState() {
        MessageType entityStateType = new MessageType(ProtocProject.getDescriptor());
        ImmutableList<CompilerOutput> output = task.generateFor(entityStateType);
        assertThat(output).hasSize(1);

        CompilerOutput compilerOutput = output.get(0);
        String insertionPoint = compilerOutput.asFile()
                                              .getInsertionPoint();
        assertThat(insertionPoint).startsWith(message_implements.name());
    }

    @Nested
    @DisplayName("return empty output")
    class ReturnEmptyOutput {

        @Test
        @DisplayName("if the message is not marked with `(entity)`")
        void forNonEntity() {
            MessageType nonEntityType = new MessageType(ProtocProjectId.getDescriptor());
            ImmutableList<CompilerOutput> output = task.generateFor(nonEntityType);
            assertThat(output).isEmpty();
        }

        @Test
        @DisplayName("if the message does not have the valid `(entity)` kind")
        void forEntityOfInvalidKind() {
            MessageType entityOfInvalidKind = new MessageType(ProtocTask.getDescriptor());
            ImmutableList<CompilerOutput> output = task.generateFor(entityOfInvalidKind);
            assertThat(output).isEmpty();
        }
    }

    private static GenerateEntityStateInterfaces markEntityStatesAs(String className) {
        return markEntityStatesAs(ClassName.of(className));
    }

    private static GenerateEntityStateInterfaces markEntityStatesAs(Class<? extends EntityState> clazz) {
        return markEntityStatesAs(ClassName.of(clazz));
    }

    private static GenerateEntityStateInterfaces markEntityStatesAs(ClassName className) {
        EntityStateConfig config = entityStateConfig(className);
        return new GenerateEntityStateInterfaces(config);
    }
}
