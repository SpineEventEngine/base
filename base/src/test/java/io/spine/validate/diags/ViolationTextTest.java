/*
 * Copyright 2022, TeamDev. All rights reserved.
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

package io.spine.validate.diags;

import com.google.common.collect.ImmutableList;
import com.google.common.testing.NullPointerTester;
import com.google.protobuf.Timestamp;
import io.spine.base.Field;
import io.spine.type.TypeName;
import io.spine.validate.ConstraintViolation;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static com.google.common.truth.Truth.assertThat;

@DisplayName("`ViolationText` should")
class ViolationTextTest {

    @Test
    @DisplayName("not accept `null` violations")
    void nullTolerance() {
        new NullPointerTester()
                .testAllPublicStaticMethods(ViolationText.class);
    }

    @Test
    @DisplayName("include type info in the violation text")
    void includeType() {
        var type = TypeName.of(Timestamp.class);
        var violation = ConstraintViolation.newBuilder()
                .setTypeName(type.value())
                .build();
        var text = ViolationText.of(violation);
        assertThat(text.toString()).contains(type.value());
    }

    @Test
    @DisplayName("include field info in the violation text")
    void includeField() {
        var field = Field.parse("msg.foo.bar");
        var violation = ConstraintViolation.newBuilder()
                .setFieldPath(field.path())
                .build();
        var text = ViolationText.of(violation);
        assertThat(text.toString())
                .contains(field.toString());
    }

    @Test
    @DisplayName("compile tests for many violations into one")
    void compileManyTexts() {
        var first = ConstraintViolation.newBuilder()
                .setMsgFormat("Errored with a message")
                .build();
        var second = ConstraintViolation.newBuilder()
                .setMsgFormat("Messaged with an error")
                .build();
        var text = ViolationText.ofAll(ImmutableList.of(first, second));
        var assertText = assertThat(text);
        assertText.contains(ViolationText.of(first).toString());
        assertText.contains(ViolationText.of(second).toString());
    }
}
