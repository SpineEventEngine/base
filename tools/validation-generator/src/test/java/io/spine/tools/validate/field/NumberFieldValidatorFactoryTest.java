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

import com.google.common.truth.StringSubject;
import com.squareup.javapoet.CodeBlock;
import io.spine.code.proto.FieldDeclaration;
import io.spine.test.validate.field.Numbers;
import io.spine.tools.validate.code.Expression;
import io.spine.tools.validate.field.given.ViolationMemoizer;
import io.spine.type.MessageType;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.util.Optional;

import static com.google.common.truth.Truth.assertThat;
import static com.google.common.truth.Truth8.assertThat;
import static io.spine.tools.validate.field.FieldCardinality.SINGULAR;
import static org.junit.jupiter.api.Assertions.assertThrows;

@DisplayName("`NumberFieldValidatorFactory` should")
class NumberFieldValidatorFactoryTest {

    private final MessageType type = new MessageType(Numbers.getDescriptor());
    private final Expression access = Expression.of("field");

    @Nested
    @DisplayName("compile a constraint for")
    class CompileConstraints {

        @Test
        @DisplayName("(min)")
        void min() {
            StringSubject assertCode = checkFieldConstraints("positive_int");
            assertCode.contains("<=");
        }

        @Test
        @DisplayName("(max)")
        void max() {
            StringSubject assertCode = checkFieldConstraints("negative_float");
            assertCode.contains(">");
            assertCode.doesNotContain(">=");
        }

        @Test
        @DisplayName("(range)")
        void range() {
            ViolationMemoizer memoizer = new ViolationMemoizer();
            StringSubject assertCode = checkFieldConstraints("switch", memoizer);
            assertCode.contains(">");
            assertCode.contains("<");
            assertThat(memoizer.violations()).hasSize(2);
        }

        private StringSubject checkFieldConstraints(String fieldName) {
            ViolationMemoizer memoizer = new ViolationMemoizer();
            StringSubject subject = checkFieldConstraints(fieldName, memoizer);
            assertThat(memoizer.violations()).isNotEmpty();
            return subject;
        }

        private StringSubject checkFieldConstraints(String fieldName, ViolationMemoizer memoizer) {
            FieldDeclaration field = field(fieldName);
            NumberFieldValidatorFactory factory =
                    new NumberFieldValidatorFactory(field, field.javaType(), access, SINGULAR);
            Optional<CodeBlock> validationCode = factory.generate(memoizer);
            assertThat(validationCode).isPresent();
            CodeBlock code = validationCode.get();
            StringSubject assertCode = assertThat(code.toString());
            assertCode.isNotEmpty();
            return assertCode;
        }
    }

    @Nested
    @DisplayName("not compile a constraint if")
    class NotCompileInvalidConstraints {

        @Test
        @DisplayName("(range) is invalid")
        void invalidRange() {
            checkInvalid("broken_range");
        }

        @Test
        @DisplayName("(min) and (range) are used on the same field")
        void minAndRange() {
            checkInvalid("too_many_options");
        }

        @Test
        @DisplayName("(max) and (range) are used on the same field")
        void maxAndRange() {
            checkInvalid("yet_more_options");
        }

        private void checkInvalid(String fieldName) {
            FieldDeclaration field = field(fieldName);
            NumberFieldValidatorFactory factory =
                    new NumberFieldValidatorFactory(field, field.javaType(), access, SINGULAR);
            assertThrows(IllegalStateException.class,
                         () -> factory.generate(new ViolationMemoizer()));
        }
    }

    private FieldDeclaration field(String name) {
        return type.fields()
                   .stream()
                   .filter(field -> field.name()
                                         .value()
                                         .equals(name))
                   .findAny()
                   .orElseGet(Assertions::fail);
    }
}
