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

import com.google.common.testing.NullPointerTester;
import io.spine.tools.mc.java.codegen.FilePatterns;
import io.spine.tools.mc.java.protoc.CompilerOutput;
import io.spine.tools.mc.java.protoc.given.TestNestedClassFactory;
import io.spine.tools.protoc.GenerateNestedClasses;
import io.spine.tools.protoc.JavaClassName;
import io.spine.tools.protoc.Messages;
import io.spine.tools.protoc.NestedClassFactoryName;
import io.spine.tools.protoc.Pattern;
import io.spine.tools.protoc.SpineProtocConfig;
import io.spine.tools.protoc.plugin.nested.Task;
import io.spine.tools.protoc.plugin.nested.TaskView;
import io.spine.type.EnumType;
import io.spine.type.MessageType;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.Collection;

import static com.google.common.truth.Truth.assertThat;
import static io.spine.testing.DisplayNames.NOT_ACCEPT_NULLS;
import static io.spine.tools.mc.java.codegen.Names.className;

@DisplayName("`NestedClassGenerator` should")
class NestedClassGenTest {

    @Test
    @DisplayName(NOT_ACCEPT_NULLS)
    void passNullToleranceCheck() {
        new NullPointerTester()
                .testAllPublicStaticMethods(NestedClassGen.class);
    }

    @Test
    @DisplayName("generate code for message types where appropriate")
    void generateCodeForMessages() {
        SpineProtocConfig config = newConfig();

        NestedClassGen generator = NestedClassGen.instance(config);
        MessageType type = new MessageType(TaskView.getDescriptor());
        Collection<CompilerOutput> output = generator.generate(type);

        assertThat(output).isNotEmpty();
    }

    @Test
    @DisplayName("ignore non-`Message` types")
    void ignoreNonMessageTypes() {
        SpineProtocConfig config = newConfig();

        NestedClassGen generator = NestedClassGen.instance(config);
        EnumType enumType = EnumType.create(Task.Priority.getDescriptor());
        Collection<CompilerOutput> output = generator.generate(enumType);

        assertThat(output).isEmpty();
    }

    private static SpineProtocConfig newConfig() {
        JavaClassName name = className(TestNestedClassFactory.class);
        NestedClassFactoryName factoryName = NestedClassFactoryName.newBuilder()
                .setClassName(name)
                .build();
        GenerateNestedClasses generate = GenerateNestedClasses.newBuilder()
                .setFactory(factoryName)
                .build();
        Pattern pattern = Pattern.newBuilder()
                .setFile(FilePatterns.fileSuffix("test_fields.proto"))
                .build();
        Messages messages = Messages.newBuilder()
                .setPattern(pattern)
                .addGenerateNestedClasses(generate)
                .build();
        SpineProtocConfig result = SpineProtocConfig
                .newBuilder()
                .addMessages(messages)
                .build();
        return result;
    }
}
