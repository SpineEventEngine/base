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

package io.spine.validate;

import com.google.common.collect.ImmutableList;
import com.google.protobuf.Descriptors.FieldDescriptor;
import io.spine.test.validation.AMessage;
import io.spine.test.validation.AnExternalConstraint;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import static com.google.common.truth.Truth.assertThat;
import static com.google.protobuf.Descriptors.Descriptor;
import static io.spine.testing.Assertions.assertIllegalState;
import static io.spine.testing.Assertions.assertNpe;

@DisplayName("ExternalMessageConstraint should")
final class ExternalConstraintTest {

    private static final String PACKAGE = "spine.test.validation";
    private static final String VALIDATED_FIELD = PACKAGE + ".AMessage.field";
    private static final Descriptor DESCRIPTOR = AnExternalConstraint.getDescriptor();

    private static ExternalMessageConstraint constraint(String path) {
        return new ExternalMessageConstraint(DESCRIPTOR, ImmutableList.of(path));
    }

    @DisplayName("not allow null")
    @Nested
    final class NotAllowNull {

        @DisplayName("descriptor")
        @Test
        void descriptor() {
            assertNpe(() -> new ExternalMessageConstraint(null, ImmutableList.of()));
        }

        @DisplayName("target paths")
        @Test
        void paths() {
            assertNpe(() -> constraint(null));
        }
    }

    @Test
    @DisplayName("build field descriptors")
    void buildFieldDescriptors() {
        ExternalMessageConstraint rule = constraint(VALIDATED_FIELD);
        assertThat(DESCRIPTOR).isEqualTo(rule.getDescriptor());
        ImmutableList<FieldDescriptor> targets =
                rule.getTargets()
                    .asList();
        assertThat(targets).isNotEmpty();
        FieldDescriptor fieldDescriptor = targets.get(0);
        assertThat(fieldDescriptor.getFullName()).isEqualTo(VALIDATED_FIELD);
    }

    @Test
    @DisplayName("build same validation rules")
    void buildSameRules() {
        ExternalMessageConstraint rule = constraint(VALIDATED_FIELD);
        assertThat(rule).isEqualTo(constraint(VALIDATED_FIELD));
    }

    @DisplayName("throw `IllegalStateException` for")
    @Nested
    final class ThrowISE {

        @DisplayName("an invalid path")
        @ParameterizedTest(name = "\"{0}\"")
        @ValueSource(strings = {"", "  ", "package-without-class"})
        void forInvalidPath(String invalidPath) {
            assertIllegalState(() -> constraint(invalidPath));
        }

        @DisplayName("a non-existing field path")
        @Test
        void forNonExistingField() {
            String fieldName = PACKAGE + ".AMessage.non_existing";
            assertIllegalState(() -> constraint(fieldName));
        }

        @DisplayName("a non-message field path")
        @Test
        void forNonMessageField() {
            String fieldName = PACKAGE + ".AMessage.non_message_field";
            assertIllegalState(() ->  constraint(fieldName));
        }

        @DisplayName("for fields that are present in the validation rule" +
                " but do not present in the target")
        @Test
        void forFieldsThatDoNotExistInRule() {
            assertIllegalState(
                    () -> new ExternalMessageConstraint(
                            AMessage.getDescriptor(),
                            ImmutableList.of(VALIDATED_FIELD))
            );
        }
    }
}
