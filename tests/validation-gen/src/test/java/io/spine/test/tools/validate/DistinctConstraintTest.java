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

import io.spine.base.FieldPath;
import io.spine.validate.ConstraintViolation;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static com.google.common.truth.extensions.proto.ProtoTruth.assertThat;
import static io.spine.protobuf.TypeConverter.toAny;

@DisplayName("`(distinct)` option should be compiled, so that")
class DistinctConstraintTest {

    @Test
    @DisplayName("duplicates result in a violation")
    void notUnique() {
        ProtoSet msg = ProtoSet
                .newBuilder()
                .addElement(toAny("123"))
                .addElement(toAny("321"))
                .addElement(toAny("123"))
                .buildPartial();
        assertThat(msg.validate())
                .comparingExpectedFieldsOnly()
                .containsExactly(ConstraintViolation
                                         .newBuilder()
                                         .setFieldPath(FieldPath.newBuilder()
                                                                .addFieldName("element"))
                                         .build());
    }

    @Test
    @DisplayName("unique elements do not result in a violation")
    void unique() {
        ProtoSet msg = ProtoSet
                .newBuilder()
                .addElement(toAny("42"))
                .addElement(toAny(42))
                .build();
        assertThat(msg.validate()).isEmpty();
    }

    @Test
    @DisplayName("empty list does not result in a violation")
    void empty() {
        ProtoSet msg = ProtoSet
                .newBuilder()
                .build();
        assertThat(msg.validate()).isEmpty();
    }
}
