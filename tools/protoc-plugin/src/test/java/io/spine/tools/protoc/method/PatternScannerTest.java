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
import io.spine.code.proto.MessageType;
import io.spine.protoc.MethodBody;
import io.spine.protoc.MethodFactory;
import io.spine.tools.protoc.CompilerOutput;
import io.spine.tools.protoc.GeneratedMethod;
import io.spine.tools.protoc.GeneratedMethodsConfig;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.util.List;

import static io.spine.tools.protoc.TypeFilters.filePostfix;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

@DisplayName("PatternScanner should")
final class PatternScannerTest {

    @DisplayName("scan type for any generated methods")
    @Test
    void scanTypeForAnyGeneratedMethods() {
        GeneratedMethodsConfig config = configBuilder()
                .addGeneratedMethod(generatedMethod(FirstMethodFactory.FQN, "_patterns.proto"))
                .addGeneratedMethod(generatedMethod(SecondMethodFactory.FQN, "_patterns.proto"))
                .build();
        MessageType type = MessageType.of(TestMessage.getDescriptor());
        PatternScanner scanner = new PatternScanner(config);
        ImmutableList<CompilerOutput> result = scanner.scan(type);
        assertEquals(3, result.size());
    }

    @DisplayName("filter out")
    @Nested
    class FilterOut {

        @DisplayName("blank MessageGenerator options")
        @Test
        void blankGenerators() {
            GeneratedMethodsConfig config = configBuilder()
                    .addGeneratedMethod(generatedMethod("", "*"))
                    .addGeneratedMethod(generatedMethod(" ", "*"))
                    .addGeneratedMethod(GeneratedMethod.getDefaultInstance())
                    .build();
            MessageType type = MessageType.of(WithEnrichmentFor.getDescriptor());
            noMethodsGeneratedFor(config, type);
        }

        @DisplayName("types from non-matched patterns")
        @Test
        void typesFromNonMatchedPatterns() {
            GeneratedMethodsConfig config = configBuilder()
                    .addGeneratedMethod(generatedMethod(FirstMethodFactory.FQN, "NOT_EXIST"))
                    .build();
            MessageType type = MessageType.of(EnrichedMessage.getDescriptor());
            noMethodsGeneratedFor(config, type);
        }

        private void noMethodsGeneratedFor(GeneratedMethodsConfig config, MessageType type) {
            PatternScanner scanner = new PatternScanner(config);
            ImmutableList<CompilerOutput> result = scanner.scan(type);
            assertTrue(result.isEmpty());
        }
    }

    private static GeneratedMethodsConfig.Builder configBuilder() {
        return GeneratedMethodsConfig.newBuilder();
    }

    private static GeneratedMethod generatedMethod(String factoryName, String postfix) {
        return GeneratedMethod.newBuilder()
                              .setFactoryName(factoryName)
                              .setFilter(filePostfix(postfix))
                              .build();
    }

    @Immutable
    public static class FirstMethodFactory implements MethodFactory {

        private static final String FQN = "io.spine.tools.protoc.method.PatternScannerTest$FirstMethodFactory";

        private static final MethodBody TEST_METHOD = MethodBody.of("public void first(){}");

        @Override
        public List<MethodBody> newMethodsFor(MessageType ignored) {
            return ImmutableList.of(TEST_METHOD);
        }
    }

    @Immutable
    public static class SecondMethodFactory implements MethodFactory {

        private static final String FQN = "io.spine.tools.protoc.method.PatternScannerTest$SecondMethodFactory";

        private static final MethodBody TEST_METHOD_1 = MethodBody.of("public void second1(){}");
        private static final MethodBody TEST_METHOD_2 = MethodBody.of("public void second2(){}");

        @Override
        public List<MethodBody> newMethodsFor(MessageType ignored) {
            return ImmutableList.of(TEST_METHOD_1, TEST_METHOD_2);
        }
    }
}
