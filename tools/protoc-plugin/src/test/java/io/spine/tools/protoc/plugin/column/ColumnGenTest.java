/*
 * Copyright 2020, TeamDev. All rights reserved.
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

package io.spine.tools.protoc.plugin.column;

import com.google.common.testing.NullPointerTester;
import io.spine.tools.protoc.AddEntityQueries;
import io.spine.tools.protoc.Classpath;
import io.spine.tools.protoc.plugin.CompilerOutput;
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

@DisplayName("`ColumnGenerator` should")
class ColumnGenTest {

    @Test
    @DisplayName(NOT_ACCEPT_NULLS)
    void passNullToleranceCheck() {
        new NullPointerTester()
                .testAllPublicStaticMethods(ColumnGen.class);
    }

    @Test
    @DisplayName("generate code for message types where appropriate")
    void generateCodeForMessages() {
        SpineProtocConfig config = newConfig();

        ColumnGen generator = ColumnGen.instance(config);
        MessageType type = new MessageType(TaskView.getDescriptor());
        Collection<CompilerOutput> output = generator.generate(type);

        assertThat(output).isNotEmpty();
    }

    @Test
    @DisplayName("ignore non-`Message` types")
    void ignoreNonMessageTypes() {
        SpineProtocConfig config = newConfig();

        ColumnGen generator = ColumnGen.instance(config);
        EnumType enumType = EnumType.create(Task.Priority.getDescriptor());
        Collection<CompilerOutput> output = generator.generate(enumType);

        assertThat(output).isEmpty();
    }

    private static SpineProtocConfig newConfig() {
        AddEntityQueries addQueries = AddEntityQueries
                .newBuilder()
                .setGenerate(true)
                .build();
        SpineProtocConfig result = SpineProtocConfig
                .newBuilder()
                .setAddEntityQueries(addQueries)
                .setClasspath(Classpath.getDefaultInstance())
                .build();
        return result;
    }
}