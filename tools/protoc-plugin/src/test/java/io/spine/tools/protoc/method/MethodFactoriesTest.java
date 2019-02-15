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
import io.spine.code.proto.MessageType;
import io.spine.protoc.MethodBody;
import io.spine.protoc.MethodFactory;
import io.spine.tools.protoc.GeneratedMethod;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

@DisplayName("MessageFactories should")
class MethodFactoriesTest {

    @DisplayName("return NoOpMessageFactory if generator name is")
    @ParameterizedTest(name = "\"{0}\"")
    @ValueSource(strings = {"", "  "})
    void returnNoOpFactoryForBlankGeneratorName(String generatorName) {
        GeneratedMethod spec = GeneratedMethod.newBuilder()
                                              .setGeneratorName(generatorName)
                                              .build();
        MethodFactory result = MethodFactories.newFactoryFor(spec);
        Assertions.assertEquals(MethodFactories.NoOpMethodFactory.INSTANCE, result);
    }

    @DisplayName("return MethodFactory instance by it's fully-qualified name")
    @Test
    void returnMethodFactoryInstanceByFullyQualifiedName() {
        GeneratedMethod spec = GeneratedMethod.newBuilder()
                                              .setGeneratorName(StubMethodFactory.class.getName())
                                              .build();
        MethodFactory result = MethodFactories.newFactoryFor(spec);
        Assertions.assertTrue(result instanceof StubMethodFactory);
    }

    public static class StubMethodFactory implements MethodFactory {

        private final ImmutableList<MethodBody> methods = ImmutableList.of();

        public StubMethodFactory() {
        }

        @Override
        public ImmutableList<MethodBody> newMethodsFor(MessageType messageType) {
            return methods;
        }
    }
}
