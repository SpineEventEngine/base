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

package io.spine.tools.protoc.plugin.field;

import com.google.common.collect.ImmutableList;
import com.google.common.testing.NullPointerTester;
import io.spine.base.SubscribableField;
import io.spine.tools.java.gen.FieldFactory;
import io.spine.tools.protoc.plugin.CompilerOutput;
import io.spine.tools.protoc.EntityStateConfig;
import io.spine.tools.protoc.plugin.message.tests.ProtocProject;
import io.spine.tools.protoc.plugin.message.tests.ProtocProjectId;
import io.spine.tools.protoc.plugin.message.tests.ProtocTask;
import io.spine.type.MessageType;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import static com.google.common.truth.Truth.assertThat;
import static io.spine.testing.Assertions.assertIllegalArgument;
import static io.spine.testing.DisplayNames.NOT_ACCEPT_NULLS;
import static io.spine.tools.protoc.plugin.InsertionPoint.class_scope;

@DisplayName("`GenerateEntityStateFields` task should")
final class GenerateEntityStateFieldsTest {

    private final FieldFactory factory = new FieldFactory();
    private GenerateEntityStateFields task;

    @BeforeEach
    void initTask() {
        task = newTask();
    }

    @Test
    @DisplayName(NOT_ACCEPT_NULLS)
    void passNullToleranceCheck() {
        new NullPointerTester()
                .testAllPublicInstanceMethods(task);
    }

    @SuppressWarnings("CheckReturnValue") // The method called to throw an exception.
    @Nested
    @DisplayName("throw `IllegalArgumentException` when the specified field type name is")
    class ThrowOnClassName {

        @Test
        @DisplayName("blank")
        void blank() {
            assertIllegalArgument(() -> newTask(config("")));
        }

        @Test
        @DisplayName("effectively blank")
        void effectivelyBlank() {
            assertIllegalArgument(() -> newTask(config("   ")));
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
        assertThat(insertionPoint).startsWith(class_scope.name());
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

    private GenerateEntityStateFields newTask() {
        return newTask(config());
    }

    private GenerateEntityStateFields newTask(EntityStateConfig config) {
        return new GenerateEntityStateFields(config, factory);
    }

    private static EntityStateConfig config() {
        return config(SubscribableField.class.getCanonicalName());
    }

    private static EntityStateConfig config(String fieldType) {
        EntityStateConfig result = EntityStateConfig
                .newBuilder()
                .setValue(fieldType)
                .build();
        return result;
    }
}
