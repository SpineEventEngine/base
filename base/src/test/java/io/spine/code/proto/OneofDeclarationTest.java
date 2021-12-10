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

package io.spine.code.proto;

import com.google.common.testing.NullPointerTester;
import com.google.protobuf.Descriptors;
import com.google.protobuf.Empty;
import io.spine.test.type.Transmission;
import io.spine.type.MessageType;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static com.google.common.truth.Truth.assertThat;

@DisplayName("`OneofDeclaration` should")
class OneofDeclarationTest {

    @Test
    @DisplayName("not accept `null`s")
    void nonNull() {
        new NullPointerTester()
                .setDefault(MessageType.class, new MessageType(Empty.getDescriptor()))
                .setDefault(Descriptors.OneofDescriptor.class, Transmission.getDescriptor()
                                                                           .getOneofs()
                                                                           .get(0))
                .testAllPublicConstructors(OneofDeclaration.class);
    }

    @Test
    @DisplayName("obtain name")
    void obtainName() {
        var declaringType = Transmission.getDescriptor();
        var protocolOneof = declaringType.getOneofs().get(0);
        var declaringMessageType = new MessageType(declaringType);
        var declaration = new OneofDeclaration(protocolOneof, declaringMessageType);
        var name = declaration.name();
        assertThat(name.javaCase()).isEqualTo("type");
    }
}
