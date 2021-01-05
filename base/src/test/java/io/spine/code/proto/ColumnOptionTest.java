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

import com.google.common.collect.ImmutableList;
import com.google.common.testing.NullPointerTester;
import com.google.protobuf.Descriptors.FieldDescriptor;
import io.spine.test.code.proto.CoProject;
import io.spine.test.code.proto.CoTask;
import io.spine.test.code.proto.CoTaskDescription;
import io.spine.type.MessageType;
import io.spine.value.StringTypeValue;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static com.google.common.collect.ImmutableList.toImmutableList;
import static com.google.common.truth.Truth.assertThat;
import static io.spine.testing.DisplayNames.NOT_ACCEPT_NULLS;

@DisplayName("`ColumnOption` should")
class ColumnOptionTest {

    private final MessageType type = new MessageType(CoProject.getDescriptor());

    @Test
    @DisplayName(NOT_ACCEPT_NULLS)
    void passNullToleranceCheck() {
        new NullPointerTester()
                .testAllPublicStaticMethods(ColumnOption.class);
    }

    @Test
    @DisplayName("determine if the message type has columns")
    void checkHasColumns() {
        assertThat(ColumnOption.hasColumns(type)).isTrue();
    }

    @Test
    @DisplayName("determine that message type has no columns")
    void checkHasNoColumns() {
        MessageType typeWithoutColumns = new MessageType(CoTask.getDescriptor());
        assertThat(ColumnOption.hasColumns(typeWithoutColumns)).isFalse();
    }

    @Test
    @DisplayName("determine that message type is not eligible for having columns")
    void checkNotEligibleForColumns() {
        MessageType nonEligible = new MessageType(CoTaskDescription.getDescriptor());
        assertThat(ColumnOption.hasColumns(nonEligible)).isFalse();
    }

    @Test
    @DisplayName("obtain columns of the entity")
    void obtainColumns() {
        ImmutableList<FieldDeclaration> columns = ColumnOption.columnsOf(type);
        assertThat(columns).hasSize(2);

        ImmutableList<String> columnNames = columns.stream()
                                                   .map(FieldDeclaration::name)
                                                   .map(StringTypeValue::value)
                                                   .collect(toImmutableList());
        assertThat(columnNames).containsExactly("name", "estimate");
    }

    @Test
    @DisplayName("return empty list of columns if the message is not eligible for having ones")
    void obtainEmptyColumns() {
        MessageType nonEligible = new MessageType(CoTaskDescription.getDescriptor());
        ImmutableList<FieldDeclaration> list = ColumnOption.columnsOf(nonEligible);
        assertThat(list).isEmpty();
    }

    @Test
    @DisplayName("determine that the passed field is a column")
    void checkIsColumn() {
        FieldDeclaration nameField = fieldByName("name");
        boolean isColumn = ColumnOption.isColumn(nameField);
        assertThat(isColumn).isTrue();
    }

    @Test
    @DisplayName("determine that the passed field is not a column")
    void checkIsNotColumn() {
        FieldDeclaration statusField = fieldByName("status");
        boolean isColumn = ColumnOption.isColumn(statusField);
        assertThat(isColumn).isFalse();
    }

    @Test
    @DisplayName("return `false` for fields of type non-eligible for having columns")
    void checkFieldOfNonEligible() {
        FieldDescriptor descriptor = CoTaskDescription.getDescriptor()
                                                 .findFieldByName("value");
        FieldDeclaration field = new FieldDeclaration(descriptor);
        boolean isColumn = ColumnOption.isColumn(field);
        assertThat(isColumn).isFalse();
    }

    private FieldDeclaration fieldByName(String name) {
        FieldDescriptor field = type.descriptor()
                                    .findFieldByName(name);
        FieldDeclaration result = new FieldDeclaration(field);
        return result;
    }
}
