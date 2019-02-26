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

package io.spine.tools.gradle.compiler.protoc;

import io.spine.tools.protoc.UuidMethod;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static com.google.common.truth.Truth.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;

@DisplayName("MethodUuidMessage should")
final class MethodUuidMessageTest {

    @DisplayName("set MethodFactory for UUID message")
    @Test
    void setMethodFactoryForUuid() {
        String factoryName = "io.spine.tools.protoc.TestMethodFactory";
        MethodUuidMessage selector = new MethodUuidMessage();
        selector.withMethodFactory(factoryName);

        UuidMethod uuidMethod = selector.toProto();

        assertEquals(factoryName, uuidMethod.getFactoryName());
    }

    @DisplayName("ignore UUID MethodFactory")
    @Test
    void ignoreUuidMethodFactory() {
        String factoryName = "io.spine.tools.protoc.TestMethodFactory";
        MethodUuidMessage selector = new MethodUuidMessage();
        selector.withMethodFactory(factoryName);
        selector.ignore();

        UuidMethod uuidMethod = selector.toProto();

        assertThat(uuidMethod.getFactoryName()).isEmpty();
    }
}
