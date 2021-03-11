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

package io.spine.test.tools.validate;

import com.google.common.collect.ImmutableList;
import com.google.protobuf.ByteString;
import io.spine.type.TypeName;
import io.spine.validate.ConstraintViolation;
import io.spine.validate.MessageWithConstraints;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static com.google.common.base.Charsets.UTF_16;
import static com.google.common.truth.extensions.proto.ProtoTruth.assertThat;

@DisplayName("`(required_field)` option should be compiled so that")
class RequiredFieldConstraintTest {

    @Test
    @DisplayName("not set fields produce a violation")
    void notSet() {
        Due invalidMessage = Due.newBuilder()
                                .buildPartial();
        assertInvalid(invalidMessage, "date | never");
    }

    @Test
    @DisplayName("a complete group of fields must be set")
    void notComplete() {
        Combination invalidMessage = Combination
                .newBuilder()
                .setA1("a1")
                .setB2(ByteString.copyFrom("b2", UTF_16))
                .buildPartial();
        assertInvalid(invalidMessage, "a1 & a2 | b1 & b2");

    }

    @Test
    @DisplayName("if at least one alternative is set, no violation")
    void valid() {
        Combination invalidMessage = Combination
                .newBuilder()
                .setA1("a1")
                .addA2("a2")
                .build();
        assertThat(invalidMessage.validate()).isEmpty();
    }

    @Test
    @DisplayName("if all the alternatives are set, no violation")
    void all() {
        Combination invalidMessage = Combination
                .newBuilder()
                .setA1("a1")
                .addA2("a2")
                .putB1(42, 314)
                .setB2(ByteString.copyFromUtf8("b2"))
                .build();
        assertThat(invalidMessage.validate()).isEmpty();
    }

    private static void assertInvalid(MessageWithConstraints message, String violationParam) {
        ImmutableList<ConstraintViolation> violations = message.validate();
        assertThat(violations)
                .comparingExpectedFieldsOnly()
                .containsExactly(ConstraintViolation
                                         .newBuilder()
                                         .setTypeName(TypeName.of(message)
                                                              .value())
                                         .addParam(violationParam)
                                         .build());
    }
}
