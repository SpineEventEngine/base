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

package io.spine.tools.validate.field;

import com.google.protobuf.Descriptors.Descriptor;
import com.google.protobuf.Descriptors.FieldDescriptor;
import com.squareup.javapoet.CodeBlock;
import io.spine.code.proto.FieldDeclaration;
import io.spine.test.tools.validate.AllFields;
import io.spine.test.tools.validate.WithBoolean;
import io.spine.tools.validate.code.Expression;
import io.spine.type.MessageType;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;

import java.util.Optional;
import java.util.stream.Stream;

import static com.google.common.truth.Truth.assertThat;
import static com.google.common.truth.Truth8.assertThat;
import static org.junit.jupiter.params.ParameterizedTest.ARGUMENTS_PLACEHOLDER;

@DisplayName("`FieldValidatorFactories` should")
class FieldValidatorFactoriesTest {

    @ParameterizedTest(name = ARGUMENTS_PLACEHOLDER)
    @MethodSource("fields")
    @DisplayName("create factories for all field types")
    void generateValidation(String fieldName) {
        FieldValidatorFactory factory = create(AllFields.getDescriptor(), fieldName);
        assertThat(factory.isNotSet()).isNotNull();
        Optional<CodeBlock> validationCode = factory.generate(v -> v);
        assertThat(validationCode)
                .isNotNull();
        assertThat(validationCode)
                .isPresent();
        CodeBlock code = validationCode.get();
        assertThat(code.toString())
                .isNotEmpty();
    }

    @Test
    @DisplayName("create a factory for bool fields")
    void createFactoriesForBool() {
        FieldValidatorFactory factory = create(WithBoolean.getDescriptor(), "boolean");
        assertThat(factory).isNotNull();
        assertThat(factory.isNotSet().toString())
                .isEqualTo("false");
        assertThat(factory.generate(v -> v))
                .isEmpty();
    }

    private static Stream<String> fields() {
        return AllFields.getDescriptor()
                        .getFields()
                        .stream()
                        .map(FieldDescriptor::getName);
    }

    private static FieldValidatorFactory create(Descriptor type, String fieldName) {
        MessageType messageType = new MessageType(type);
        FieldDeclaration fieldDeclaration = messageType
                .fields()
                .stream()
                .filter(field -> field.name()
                                      .value()
                                      .equals(fieldName))
                .findAny()
                .orElseGet(Assertions::fail);
        Expression<?> access = Expression.of(FieldValidatorFactoriesTest.class.getSimpleName());
        FieldValidatorFactories factories = new FieldValidatorFactories(access);
        FieldValidatorFactory factory = factories.forField(fieldDeclaration);
        assertThat(factory).isNotNull();
        return factory;
    }
}
