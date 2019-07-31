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

package io.spine.tools.protoc;

import com.google.common.collect.ImmutableMap;
import io.spine.code.proto.FileName;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import static com.google.common.truth.Truth.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

@DisplayName("MessageSelectorFactory should")
final class MessageSelectorFactoryTest {

    private final MessageSelectorFactory factory = MessageSelectorFactory.INSTANCE;

    @DisplayName("create UuidMessage selector")
    @Test
    void createUuidSelector() {
        assertThat(factory.uuid()).isInstanceOf(UuidMessage.class);
    }

    @DisplayName("create all messages selector")
    @Test
    void createAllSelector() {
        PatternSelector allSelector = factory.all();
        assertThat(allSelector).isInstanceOf(SuffixSelector.class);
        assertThat(allSelector.getPattern()).isEqualTo(FileName.EXTENSION);
    }

    @DisplayName("create PatternSelector out of")
    @Nested
    final class CreatePatternSelector {

        @DisplayName("suffix")
        @Test
        void suffix() {
            String suffix = "_documents.proto";
            assertThat(factory.inFiles(MessageSelectorFactory.suffix(suffix)))
                    .isInstanceOf(SuffixSelector.class);
        }

        @DisplayName("prefix")
        @Test
        void prefix() {
            String prefix = "io/spine/test/orders_";
            assertThat(factory.inFiles(MessageSelectorFactory.prefix(prefix)))
                    .isInstanceOf(PrefixSelector.class);
        }

        @DisplayName("regex")
        @Test
        void regex() {
            String regex = ".*test.*";
            assertThat(factory.inFiles(MessageSelectorFactory.regex(regex)))
                    .isInstanceOf(RegexSelector.class);
        }
    }

    @DisplayName("throw IllegalArgumentException if inFiles configuration has")
    @Nested
    final class ThrowIEA {

        @DisplayName("more than one element")
        @Test
        void moreThanOneElement() {
            assertThrows(IllegalArgumentException.class, () ->
                    factory.inFiles(ImmutableMap.of("first", "v1", "second", "v2")));
        }

        @DisplayName("non supported parameter")
        @Test
        void nonSupportParameter() {
            assertThrows(IllegalArgumentException.class, () ->
                    factory.inFiles(ImmutableMap.of("NON_SUPPORTED", "v1")));
        }
    }

    @DisplayName("throw NullPointerException if a null value is supplied to inFiles method")
    @Test
    void throwNPE() {
        assertThrows(NullPointerException.class, () ->
                factory.inFiles(null));
    }
}
