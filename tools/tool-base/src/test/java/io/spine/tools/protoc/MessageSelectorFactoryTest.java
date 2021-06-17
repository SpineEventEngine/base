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

package io.spine.tools.protoc;

import com.google.common.collect.ImmutableMap;
import com.google.common.testing.NullPointerTester;
import com.google.errorprone.annotations.CanIgnoreReturnValue;
import io.spine.code.proto.FileName;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import static com.google.common.truth.Truth.assertThat;
import static io.spine.testing.Assertions.assertIllegalArgument;
import static io.spine.testing.DisplayNames.NOT_ACCEPT_NULLS;

@DisplayName("`MessageSelectorFactory` should")
final class MessageSelectorFactoryTest {

    private final MessageSelectorFactory factory = MessageSelectorFactory.INSTANCE;

    @CanIgnoreReturnValue
    private ByPattern inFiles(ImmutableMap<String, String> conf) {
        return factory.inFiles(conf);
    }

    @Test
    @DisplayName(NOT_ACCEPT_NULLS)
    void passNullToleranceCheck() {
        NullPointerTester tester = new NullPointerTester();
        tester.testAllPublicStaticMethods(MessageSelectorFactory.class);
        tester.testAllPublicInstanceMethods(factory);
    }

    @Test
    @DisplayName("create `IsUuidMessage` selector")
    void createUuidSelector() {
        assertThat(factory.uuid())
                .isInstanceOf(IsUuidMessage.class);
    }

    @Test
    @DisplayName("create `IsEntityState` selector")
    void createEntityStateSelector() {
        assertThat(factory.entityState())
                .isInstanceOf(IsEntityState.class);
    }

    @Test
    @DisplayName("create all messages selector")
    void createAllSelector() {
        ByPattern allSelector = factory.all();
        assertThat(allSelector)
                .isInstanceOf(WithSuffix.class);
        assertThat(allSelector.getPattern())
                .isEqualTo(FileName.EXTENSION);
    }

    @Nested
    @DisplayName("create `ByPattern` out of")
    final class CreateByPatternSelector {

        @Test
        @DisplayName(MessageSelectorFactory.SUFFIX)
        void suffix() {
            String suffix = "_documents.proto";
            ByPattern withSuffix = inFiles(MessageSelectorFactory.suffix(suffix));
            assertThat(withSuffix)
                    .isInstanceOf(WithSuffix.class);
            FilePattern filePattern = withSuffix.toProto();
            assertThat(filePattern.getSuffix())
                    .isEqualTo(suffix);
        }

        @Test
        @DisplayName(MessageSelectorFactory.PREFIX)
        void prefix() {
            String prefix = "io/spine/test/orders_";
            ByPattern withPrefix = inFiles(MessageSelectorFactory.prefix(prefix));
            assertThat(withPrefix)
                    .isInstanceOf(WithPrefix.class);
            FilePattern filePattern = withPrefix.toProto();
            assertThat(filePattern.getPrefix())
                    .isEqualTo(prefix);
        }

        @Test
        @DisplayName(MessageSelectorFactory.REGEX)
        void regex() {
            String regex = ".*test.*";
            ByPattern byRegex = inFiles(MessageSelectorFactory.regex(regex));
            assertThat(byRegex)
                    .isInstanceOf(ByRegex.class);
            FilePattern filePattern = byRegex.toProto();
            assertThat(filePattern.getRegex())
                    .isEqualTo(regex);
        }
    }

    @Nested
    @DisplayName("throw `IllegalArgumentException` if `inFiles` configuration has")
    final class Prohibit {

        @Test
        @DisplayName("more than one element")
        void moreThanOneElement() {
            assertIllegalArgument(() -> inFiles(ImmutableMap.of("first", "v1", "second", "v2")));
        }

        @Test
        @DisplayName("non supported parameter")
        void nonSupportParameter() {
            assertIllegalArgument(() -> inFiles(ImmutableMap.of("NON_SUPPORTED", "v1")));
        }
    }
}
