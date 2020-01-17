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

package io.spine.tools.protoc;

import com.google.common.testing.NullPointerTester;
import io.spine.code.java.ClassName;
import io.spine.testing.UtilityClassTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import static com.google.common.truth.Truth.assertThat;

@DisplayName("`ProtocTaskConfigs` should")
final class ProtocTaskConfigsTest extends UtilityClassTest<ProtocTaskConfigs> {

    ProtocTaskConfigsTest() {
        super(ProtocTaskConfigs.class);
    }

    @Override
    protected void configure(NullPointerTester nullTester) {
        nullTester.setDefault(FilePattern.class, FilePattern.getDefaultInstance());
        nullTester.setDefault(ClassName.class, ClassName.of("not-empty"));
    }

    @Nested
    @DisplayName("create valid")
    class CreateValid {

        @Test
        @DisplayName("`UuidConfig`")
        void uuidConfig() {
            ClassName className = ClassName.of("test-value");
            UuidConfig actual = ProtocTaskConfigs.uuidConfig(className);
            assertThat(actual.getValue()).isEqualTo(className.value());
        }

        @Test
        @DisplayName("`EntityStateConfig`")
        void entityStateConfig() {
            ClassName className = ClassName.of("TestEntityState");
            EntityStateConfig actual = ProtocTaskConfigs.entityStateConfig(className);
            assertThat(actual.getValue()).isEqualTo(className.value());
        }

        @Test
        @DisplayName("`QueryableConfig`")
        void queryableConfig() {
            ClassName className = ClassName.of("TestProjection");
            QueryableConfig actual = ProtocTaskConfigs.queryableConfig(className);
            assertThat(actual.getValue()).isEqualTo(className.value());
        }

        @Test
        @DisplayName("`SubscribableConfig`")
        void subscribableConfig() {
            ClassName className = ClassName.of("TestEvent");
            SubscribableConfig actual = ProtocTaskConfigs.subscribableConfig(className);
            assertThat(actual.getValue()).isEqualTo(className.value());
        }

        @Test
        @DisplayName("`ConfigByPattern`")
        void configByPattern() {
            ClassName className = ClassName.of("test-value");
            FilePattern pattern = FilePatterns.filePrefix("test");
            ConfigByPattern actual = ProtocTaskConfigs.byPatternConfig(className, pattern);
            assertThat(actual.getValue()).isEqualTo(className.value());
            assertThat(actual.getPattern()).isEqualTo(pattern);
        }
    }
}
