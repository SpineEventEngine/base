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

package io.spine.type;

import com.google.common.testing.NullPointerTester;
import io.spine.net.Uri;
import io.spine.test.type.GreetingServiceProto;
import io.spine.ui.Language;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static com.google.common.truth.Truth.assertThat;
import static io.spine.testing.DisplayNames.NOT_ACCEPT_NULLS;

@DisplayName("`NestedTypeName` should")
class NestedTypeNameTest {

    private static final Type<?, ?> nestedMessage =
            new MessageType(Uri.Authorization.getDescriptor());
    private static final Type<?, ?> nestedEnum = EnumType.create(Uri.Schema.getDescriptor());
    private static final Type<?, ?> topLevelMessage = new MessageType(Uri.getDescriptor());
    private static final Type<?, ?> topLevelEnum = EnumType.create(Language.getDescriptor());
    private static final Type<?, ?> service = ServiceType.of(GreetingServiceProto
                                                                     .getDescriptor()
                                                                     .getServices()
                                                                     .get(0));

    @Test
    @DisplayName(NOT_ACCEPT_NULLS)
    void passNullToleranceCheck() {
        new NullPointerTester()
                .testAllPublicStaticMethods(NestedTypeName.class);
    }

    @Test
    @DisplayName("exist for a nested message type")
    void nestedMessage() {
        check("Uri.Authorization", nestedMessage);
    }

    @Test
    @DisplayName("exist for a nested enum type")
    void nestedEnum() {
        check("Uri.Schema", nestedEnum);
    }

    @Test
    @DisplayName("exist for a top-level message type")
    void message() {
        check("Uri", topLevelMessage);
    }

    @Test
    @DisplayName("exist for a top-level enum type")
    void topLevelEnum() {
        check("Language", topLevelEnum);
    }

    @Test
    @DisplayName("exist for a service type")
    void service() {
        check("GreetingService", service);
    }

    @Test
    @DisplayName("join parts with underscores")
    void printWithUnderscores() {
        Type<?, ?> type = new MessageType(Uri.Protocol.getDescriptor());
        NestedTypeName name = NestedTypeName.of(type);
        assertThat(name.joinWithUnderscore()).isEqualTo("Uri_Protocol");
    }

    private static void check(String expected, Type<?, ?> type) {
        NestedTypeName name = type.nestedSimpleName();
        assertThat(name.value()).isEqualTo(expected);
    }
}
