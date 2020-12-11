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
    private PatternSelector inFiles(ImmutableMap<String, String> conf) {
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
    @DisplayName("create `UuidMessage` selector")
    void createUuidSelector() {
        assertThat(factory.uuid()).isInstanceOf(UuidMessage.class);
    }

    @Test
    @DisplayName("create `EntityState` selector")
    void createEntityStateSelector() {
        assertThat(factory.entityState()).isInstanceOf(EntityState.class);
    }

    @Test
    @DisplayName("create all messages selector")
    void createAllSelector() {
        PatternSelector allSelector = factory.all();
        assertThat(allSelector).isInstanceOf(SuffixSelector.class);
        assertThat(allSelector.getPattern()).isEqualTo(FileName.EXTENSION);
    }

    @Nested
    @DisplayName("create `PatternSelector` out of")
    final class CreatePatternSelector {

        @Test
        @DisplayName(MessageSelectorFactory.SUFFIX)
        void suffix() {
            String suffix = "_documents.proto";
            assertThat(inFiles(MessageSelectorFactory.suffix(suffix)))
                    .isInstanceOf(SuffixSelector.class);
        }

        @Test
        @DisplayName(MessageSelectorFactory.PREFIX)
        void prefix() {
            String prefix = "io/spine/test/orders_";
            assertThat(inFiles(MessageSelectorFactory.prefix(prefix)))
                    .isInstanceOf(PrefixSelector.class);
        }

        @Test
        @DisplayName(MessageSelectorFactory.REGEX)
        void regex() {
            String regex = ".*test.*";
            assertThat(inFiles(MessageSelectorFactory.regex(regex)))
                    .isInstanceOf(RegexSelector.class);
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
