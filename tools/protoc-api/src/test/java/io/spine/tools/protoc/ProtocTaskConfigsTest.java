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

import com.google.common.testing.NullPointerTester;
import io.spine.testing.UtilityClassTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

@DisplayName("ProtocTaskConfigs should")
final class ProtocTaskConfigsTest extends UtilityClassTest<ProtocTaskConfigs> {

    ProtocTaskConfigsTest() {
        super(ProtocTaskConfigs.class);
    }

    @Override
    protected void configure(NullPointerTester nullTester) {
        nullTester.setDefault(FilePattern.class, FilePattern.getDefaultInstance());
        nullTester.setDefault(String.class, "not-empty");
    }

    @DisplayName("create valid")
    @Nested
    class CreateValid {

        @DisplayName("UuidConfig")
        @Test
        void uuidConfig() {
            String value = "test-value";
            UuidConfig actual = ProtocTaskConfigs.uuidConfig(value);
            assertEquals(value, actual.getValue());
        }

        @DisplayName("ConfigByPattern")
        @Test
        void configByPattern() {
            String value = "test-value";
            FilePattern pattern = FilePatterns.filePrefix("test");
            ConfigByPattern actual = ProtocTaskConfigs.byPatternConfig(value, pattern);
            assertEquals(value, actual.getValue());
            assertEquals(pattern, actual.getPattern());
        }
    }
}
