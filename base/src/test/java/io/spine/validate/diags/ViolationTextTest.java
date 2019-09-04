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

package io.spine.validate.diags;

import com.google.common.collect.ImmutableList;
import com.google.common.testing.NullPointerTester;
import com.google.common.truth.StringSubject;
import com.google.protobuf.Timestamp;
import io.spine.base.Field;
import io.spine.type.TypeName;
import io.spine.validate.ConstraintViolation;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static com.google.common.truth.Truth.assertThat;

@DisplayName("ViolationText should")
class ViolationTextTest {

    @Test
    @DisplayName("not accept null violations")
    void nullTolerance() {
        new NullPointerTester()
                .testAllPublicStaticMethods(ViolationText.class);
    }

    @Test
    @DisplayName("include type info in the violation text")
    void includeType() {
        TypeName type = TypeName.of(Timestamp.class);
        ConstraintViolation violation = ConstraintViolation
                .newBuilder()
                .setTypeName(type.value())
                .build();
        ViolationText text = ViolationText.of(violation);
        assertThat(text.toString()).contains(type.value());
    }

    @Test
    @DisplayName("include field info in the violation text")
    void includeField() {
        Field field = Field.parse("msg.foo.bar");
        ConstraintViolation violation = ConstraintViolation
                .newBuilder()
                .setFieldPath(field.path())
                .build();
        ViolationText text = ViolationText.of(violation);
        assertThat(text.toString())
                .contains(field.toString());
    }

    @Test
    @DisplayName("compile tests for many violations into one")
    void compileManyTexts() {
        ConstraintViolation first = ConstraintViolation
                .newBuilder()
                .setMsgFormat("Errored with a message")
                .build();
        ConstraintViolation second = ConstraintViolation
                .newBuilder()
                .setMsgFormat("Messaged with an error")
                .build();
        String text = ViolationText.ofAll(ImmutableList.of(first, second));
        StringSubject assertText = assertThat(text);
        assertText.contains(ViolationText.of(first).toString());
        assertText.contains(ViolationText.of(second).toString());
    }
}
