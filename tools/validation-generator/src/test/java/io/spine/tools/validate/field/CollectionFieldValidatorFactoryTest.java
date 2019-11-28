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

import com.squareup.javapoet.CodeBlock;
import io.spine.code.proto.FieldDeclaration;
import io.spine.test.tools.validate.avocado.Greenhouse;
import io.spine.tools.validate.code.Expression;
import io.spine.tools.validate.field.given.ViolationMemoizer;
import io.spine.type.MessageType;
import io.spine.validate.ConstraintViolation;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Optional;

import static com.google.common.truth.Truth.assertThat;
import static com.google.common.truth.Truth8.assertThat;

@DisplayName("`CollectionFieldValidatorFactory` should")
class CollectionFieldValidatorFactoryTest {

    @Test
    @DisplayName("generate a check for `(distinct)`")
    void distinct() {
        FieldDeclaration sorts = new MessageType(Greenhouse.getDescriptor())
                .fields()
                .stream()
                .filter(field -> field.name()
                                      .value()
                                      .equals("sort"))
                .findAny()
                .orElseGet(Assertions::fail);
        FieldValidatorFactory factory =
                new FieldValidatorFactories(Expression.of("messageAccess"))
                        .forField(sorts);
        assertThat(factory).isInstanceOf(CollectionFieldValidatorFactory.class);
        ViolationMemoizer memoizer = new ViolationMemoizer();
        Optional<CodeBlock> code = factory.generate(memoizer);
        assertThat(code)
                .isPresent();
        assertThat(code.get()
                       .toString())
                .isNotEmpty();
        List<Expression<ConstraintViolation>> violations = memoizer.violations();
        assertThat(violations).hasSize(1);
        String violationCode = violations.get(0)
                                         .toCode()
                                         .toString();
        assertThat(violationCode).contains("should not contain duplicates");
    }
}
