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

package io.spine.query;

import com.google.common.testing.NullPointerTester;
import io.spine.code.proto.FieldDeclaration;
import io.spine.test.code.proto.CoProject;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static com.google.common.truth.Truth.assertThat;
import static io.spine.testing.DisplayNames.NOT_ACCEPT_NULLS;
import static org.junit.jupiter.api.Assertions.assertThrows;

@DisplayName("`ColumnName` should")
class ColumnNameTest {

    @Test
    @DisplayName(NOT_ACCEPT_NULLS)
    void passNullToleranceCheck() {
        new NullPointerTester()
                .testAllPublicStaticMethods(ColumnName.class);
    }

    @Test
    @DisplayName("be constructed from string value")
    void initFromString() {
        var columnName = "the-column-name";
        var name = ColumnName.of(columnName);

        assertThat(name.value()).isEqualTo(columnName);
    }

    @Test
    @DisplayName("not be constructed from empty string")
    @SuppressWarnings({"CheckReturnValue",
            "ResultOfMethodCallIgnored" /* Called to trigger the exception. */ })
    void notInitFromEmpty() {
        assertThrows(IllegalArgumentException.class, () -> ColumnName.of(""));
    }

    @Test
    @DisplayName("be constructed from `FieldDeclaration`")
    void initFromFieldDeclaration() {
        var field = CoProject.getDescriptor()
                             .getFields()
                             .get(0);
        var fieldDeclaration = new FieldDeclaration(field);
        var columnName = ColumnName.of(fieldDeclaration);

        assertThat(columnName.value()).isEqualTo(field.getName());
    }
}
