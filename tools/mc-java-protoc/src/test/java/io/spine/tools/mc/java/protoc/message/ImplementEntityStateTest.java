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

package io.spine.tools.mc.java.protoc.message;

import com.google.common.collect.ImmutableList;
import com.google.common.testing.NullPointerTester;
import io.spine.base.EntityState;
import io.spine.option.OptionsProto;
import io.spine.tools.mc.java.protoc.CompilerOutput;
import io.spine.tools.protoc.AddInterface;
import io.spine.tools.protoc.ForEntities;
import io.spine.tools.protoc.JavaClassName;
import io.spine.tools.protoc.ProtoOption;
import io.spine.tools.protoc.plugin.message.tests.ProtocProject;
import io.spine.tools.protoc.plugin.message.tests.ProtocProjectId;
import io.spine.type.MessageType;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import static com.google.common.truth.Truth.assertThat;
import static io.spine.testing.Assertions.assertIllegalArgument;
import static io.spine.testing.DisplayNames.NOT_ACCEPT_NULLS;
import static io.spine.tools.mc.java.protoc.InsertionPoint.message_implements;

@DisplayName("`GenerateEntityStateInterfaces` task should")
class ImplementEntityStateTest {

    private ImplementEntityState task;

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

    @Nested
    @DisplayName("throw `IllegalArgumentException` when the specified class name is")
    @SuppressWarnings({"CheckReturnValue", "ResultOfMethodCallIgnored"})
            // Method called to throw exception.
    class ThrowOnClassName {

        @Test
        @DisplayName("blank")
        void blank() {
            assertIllegalArgument(() -> markEntityStatesAs(""));
        }

        @Test
        @DisplayName("effectively blank")
        void effectivelyBlank() {
            assertIllegalArgument(() -> markEntityStatesAs("   "));
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

    @Test
    @DisplayName("return empty output if the message is not marked with `(entity)`")
    void forNonEntity() {
        MessageType nonEntityType = new MessageType(ProtocProjectId.getDescriptor());
        ImmutableList<CompilerOutput> output = task.generateFor(nonEntityType);
        assertThat(output).isEmpty();
    }

    private static ImplementEntityState markEntityStatesAs(String className) {
        JavaClassName name = JavaClassName.newBuilder()
                .setCanonical(className)
                .build();
        return markEntityStatesAs(name);
    }

    @SuppressWarnings("rawtypes")   // due to the nature of {@code Some.class} in Java.
    private static ImplementEntityState
    markEntityStatesAs(Class<? extends EntityState> clazz) {
        return markEntityStatesAs(clazz.getCanonicalName());
    }

    private static ImplementEntityState markEntityStatesAs(JavaClassName className) {
        AddInterface iface = AddInterface.newBuilder()
                .setName(className)
                .build();
        ProtoOption option = ProtoOption.newBuilder()
                .setName(OptionsProto.entity.getDescriptor().getName())
                .build();
        ForEntities config = ForEntities
                .newBuilder()
                .addAddInterface(iface)
                .addOption(option)
                .build();
        return new ImplementEntityState(className, config);
    }
}
