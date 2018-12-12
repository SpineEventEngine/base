/*
 * Copyright 2018, TeamDev. All rights reserved.
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

package io.spine.validate.builders;

import com.google.protobuf.Any;
import com.google.protobuf.BoolValue;
import io.spine.base.Identifier;
import io.spine.validate.AnyVBuilder;
import io.spine.validate.BoolValueVBuilder;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import static com.google.common.truth.extensions.proto.ProtoTruth.assertThat;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

@DisplayName("The framework should provide VBuilder for default Protobuf types")
class DefaultVBuildersTest {

    @Nested
    @DisplayName("Any")
    class OfAny extends BuilderTest<Any, AnyVBuilder> {

        private final Any value = Identifier.pack(getClass().getName());

        @Override
        AnyVBuilder createBuilder() {
            AnyVBuilder result = AnyVBuilder.newBuilder();
            result.setOriginalState(value);
            return result;
        }

        @Test
        @DisplayName("allows setting the value only via `setOriginalState()`")
        void internal() {
            assertThat(builder().internalBuild()).isEqualTo(value);
        }

        @Test
        @DisplayName("produces value")
        void building() {
            assertThat(builder().build()).isEqualTo(value);
        }
    }

    @Nested
    @DisplayName("BoolValue")
    class OfBoolValue extends BuilderTest<BoolValue, BoolValueVBuilder> {

        @Override
        BoolValueVBuilder createBuilder() {
            return BoolValueVBuilder.newBuilder();
        }

        @Test
        @DisplayName("producing true")
        void withTrue() {
            assertTrue(builder().setValue(true)
                                .build()
                                .getValue());
        }

        @Test
        @DisplayName("producing false")
        void withFalse() {
            assertFalse(builder().build()
                                 .getValue());
        }
    }
}
