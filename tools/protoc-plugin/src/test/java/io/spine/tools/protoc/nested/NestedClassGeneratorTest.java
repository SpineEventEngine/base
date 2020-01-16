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

package io.spine.tools.protoc.nested;

import com.google.common.testing.NullPointerTester;
import io.spine.tools.protoc.AddNestedClasses;
import io.spine.tools.protoc.Classpath;
import io.spine.tools.protoc.CompilerOutput;
import io.spine.tools.protoc.ConfigByPattern;
import io.spine.tools.protoc.FilePatterns;
import io.spine.tools.protoc.QueryableConfig;
import io.spine.tools.protoc.SpineProtocConfig;
import io.spine.tools.protoc.given.TestNestedClassFactory;
import io.spine.type.EnumType;
import io.spine.type.MessageType;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.Collection;

import static com.google.common.truth.Truth.assertThat;
import static io.spine.testing.DisplayNames.NOT_ACCEPT_NULLS;

@DisplayName("`NestedClassGenerator` should")
class NestedClassGeneratorTest {

    @Test
    @DisplayName(NOT_ACCEPT_NULLS)
    void passNullToleranceCheck() {
        new NullPointerTester()
                .testAllPublicStaticMethods(NestedClassGenerator.class);
    }

    @Test
    @DisplayName("generate code for message types where appropriate")
    void generateCodeForMessages() {
        QueryableConfig factory = QueryableConfig
                .newBuilder()
                .setValue(TestNestedClassFactory.class.getName())
                .build();
        AddNestedClasses addNestedClasses = AddNestedClasses
                .newBuilder()
                .setQueryableFactory(factory)
                .setFactoryClasspath(Classpath.getDefaultInstance())
                .build();
        SpineProtocConfig config = SpineProtocConfig
                .newBuilder()
                .setAddNestedClasses(addNestedClasses)
                .build();

        NestedClassGenerator generator = NestedClassGenerator.instance(config);
        MessageType type = new MessageType(TaskView.getDescriptor());
        Collection<CompilerOutput> output = generator.generate(type);

        assertThat(output).isNotEmpty();
    }

    @Test
    @DisplayName("ignore non-message types")
    void ignoreNonMessageTypes() {
        ConfigByPattern pattern = ConfigByPattern
                .newBuilder()
                .setValue(TestNestedClassFactory.class.getName())
                .setPattern(FilePatterns.fileSuffix("test_fields.proto"))
                .build();
        AddNestedClasses addNestedClasses = AddNestedClasses
                .newBuilder()
                .addFactoryByPattern(pattern)
                .setFactoryClasspath(Classpath.getDefaultInstance())
                .build();
        SpineProtocConfig config = SpineProtocConfig
                .newBuilder()
                .setAddNestedClasses(addNestedClasses)
                .build();

        NestedClassGenerator generator = NestedClassGenerator.instance(config);
        EnumType enumType = EnumType.create(Task.Priority.getDescriptor());
        Collection<CompilerOutput> output = generator.generate(enumType);

        assertThat(output).isEmpty();
    }
}
