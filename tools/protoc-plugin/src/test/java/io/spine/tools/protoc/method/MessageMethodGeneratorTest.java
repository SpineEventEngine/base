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

package io.spine.tools.protoc.method;

import com.google.common.collect.ImmutableList;
import com.google.errorprone.annotations.Immutable;
import io.spine.tools.protoc.CompilerOutput;
import io.spine.tools.protoc.GenerateMethod;
import io.spine.tools.protoc.MethodsGeneration;
import io.spine.type.MessageType;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.util.List;

import static io.spine.tools.protoc.FilePatterns.filePostfix;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

@DisplayName("MessageMethodGenerator should")
final class MessageMethodGeneratorTest {

    @DisplayName("generate type for any generated methods")
    @Test
    void scanTypeForAnyGeneratedMethods() {
        MethodsGeneration config = configBuilder()
                .addGenerateMethod(generatedMethod(F1.class.getName(), "_patterns.proto"))
                .addGenerateMethod(generatedMethod(F2.class.getName(), "_patterns.proto"))
                .build();
        MessageType type = new MessageType(TestMessage.getDescriptor());
        MessageMethodGenerator generator = new MessageMethodGenerator(config);
        ImmutableList<CompilerOutput> result = generator.generate(type);
        assertEquals(3, result.size());
    }

    @DisplayName("filter out")
    @Nested
    class FilterOut {

        @DisplayName("blank MessageGenerator options")
        @Test
        void blankGenerators() {
            MethodsGeneration config = configBuilder()
                    .addGenerateMethod(generatedMethod("", "*"))
                    .addGenerateMethod(generatedMethod(" ", "*"))
                    .addGenerateMethod(GenerateMethod.getDefaultInstance())
                    .build();
            MessageType type = new MessageType(NonEnhancedMessage.getDescriptor());
            noMethodsGeneratedFor(config, type);
        }

        @DisplayName("types from non-matched patterns")
        @Test
        void typesFromNonMatchedPatterns() {
            MethodsGeneration config = configBuilder()
                    .addGenerateMethod(generatedMethod(F1.class.getName(), "NOT_EXIST"))
                    .build();
            MessageType type = new MessageType(NonEnhancedMessage.getDescriptor());
            noMethodsGeneratedFor(config, type);
        }

        private void noMethodsGeneratedFor(MethodsGeneration config, MessageType type) {
            MessageMethodGenerator generator = new MessageMethodGenerator(config);
            ImmutableList<CompilerOutput> result = generator.generate(type);
            assertTrue(result.isEmpty());
        }
    }

    private static MethodsGeneration.Builder configBuilder() {
        return MethodsGeneration.newBuilder();
    }

    private static GenerateMethod generatedMethod(String factoryName, String postfix) {
        return GenerateMethod.newBuilder()
                             .setFactoryName(factoryName)
                             .setPattern(filePostfix(postfix))
                             .build();
    }

    @Immutable
    public static class F1 implements MethodFactory {

        private static final GeneratedMethod TEST_METHOD = new GeneratedMethod(
                "public void first(){}");

        @Override
        public List<GeneratedMethod> newMethodsFor(MessageType ignored) {
            return ImmutableList.of(TEST_METHOD);
        }
    }

    @Immutable
    public static class F2 implements MethodFactory {

        private static final GeneratedMethod TEST_METHOD_1 =
                new GeneratedMethod("public void second1(){}");
        private static final GeneratedMethod TEST_METHOD_2 =
                new GeneratedMethod("public void second2(){}");

        @Override
        public List<GeneratedMethod> newMethodsFor(MessageType ignored) {
            return ImmutableList.of(TEST_METHOD_1, TEST_METHOD_2);
        }
    }
}
