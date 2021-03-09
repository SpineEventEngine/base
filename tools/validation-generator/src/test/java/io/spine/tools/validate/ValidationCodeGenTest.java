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

package io.spine.tools.validate;

import com.google.common.collect.ImmutableList;
import com.google.common.testing.NullPointerTester;
import com.google.errorprone.annotations.Immutable;
import com.google.protobuf.Empty;
import io.spine.code.proto.FieldContext;
import io.spine.type.MessageType;
import io.spine.validate.ConstraintViolation;
import io.spine.validate.CustomConstraint;
import io.spine.validate.MessageValue;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static com.google.common.testing.NullPointerTester.Visibility.PACKAGE;
import static io.spine.testing.DisplayNames.NOT_ACCEPT_NULLS;
import static org.junit.jupiter.api.Assertions.assertThrows;

@DisplayName("`ConstraintCompiler` should")
class ValidationCodeGenTest {

    @Test
    @DisplayName(NOT_ACCEPT_NULLS)
    void notNull() {
        new NullPointerTester()
                .setDefault(MessageType.class, FakeConstraint.INSTANCE.targetType())
                .setDefault(String.class, "nonEmptyString")
                .testConstructors(ValidationCodeGenerator.class, PACKAGE);
    }

    @Test
    @DisplayName("throw `UnsupportedOperationException` upon `CustomConstraint`")
    void notAllowCustom() {
        CustomConstraint constraint = FakeConstraint.INSTANCE;
        ValidationCodeGenerator compiler =
                new ValidationCodeGenerator("test", constraint.targetType());
        assertThrows(UnsupportedOperationException.class, () -> compiler.visitCustom(constraint));
    }

    @Immutable
    private enum FakeConstraint implements CustomConstraint {

        INSTANCE;

        @Override
        public ImmutableList<ConstraintViolation> validate(MessageValue containingMessage) {
            return ImmutableList.of();
        }

        @Override
        public MessageType targetType() {
            return new MessageType(Empty.getDescriptor());
        }

        @Override
        public String errorMessage(FieldContext field) {
            return "";
        }
    }
}
