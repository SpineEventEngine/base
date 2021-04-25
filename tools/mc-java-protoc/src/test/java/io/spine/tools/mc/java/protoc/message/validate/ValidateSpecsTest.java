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

package io.spine.tools.mc.java.protoc.message.validate;

import com.google.protobuf.Descriptors.Descriptor;
import com.squareup.javapoet.MethodSpec;
import com.squareup.javapoet.TypeSpec;
import io.spine.test.tools.validate.NotValidator;
import io.spine.test.tools.validate.Validator;
import io.spine.test.tools.validate.avocado.Greenhouse;
import io.spine.type.MessageType;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static com.google.common.truth.Truth.assertThat;
import static javax.lang.model.SourceVersion.isName;
import static javax.lang.model.element.Modifier.PRIVATE;

@DisplayName("`MessageValidatorFactory` should")
class ValidateSpecsTest {

    @Test
    @DisplayName("generate `Validator` class")
    void generateClass() {
        MessageType type = new MessageType(Greenhouse.getDescriptor());
        ValidateSpecs factory = new ValidateSpecs(type);
        TypeSpec validatorClass = factory.validatorClass();
        assertThat(isName(validatorClass.name))
                .isTrue();
        assertThat(validatorClass.methodSpecs)
                .isNotEmpty();
        assertThat(validatorClass.modifiers)
                .contains(PRIVATE);
    }

    @Test
    @DisplayName("generate `validate()` method")
    void generateValidate() {
        MessageType type = new MessageType(Greenhouse.getDescriptor());
        ValidateSpecs factory = new ValidateSpecs(type);
        MethodSpec validateMethod = factory.validateMethod();
        assertThat(isName(validateMethod.name))
                .isTrue();
        assertThat(validateMethod.returnType.toString())
                .isEqualTo("com.google.common.collect.ImmutableList<io.spine.validate.ConstraintViolation>");
    }

    @Test
    @DisplayName("generate `vBuild()` method")
    void generateVBuild() {
        MessageType type = new MessageType(Greenhouse.getDescriptor());
        ValidateSpecs factory = new ValidateSpecs(type);
        MethodSpec validateMethod = factory.vBuildMethod();
        assertThat(isName(validateMethod.name))
                .isTrue();
        assertThat(validateMethod.returnType.toString())
                .isEqualTo(type.simpleJavaClassName().value());
    }

    @Test
    @DisplayName("generate class name without name collisions")
    void avoidCollisions() {
        checkEscaped(Validator.getDescriptor());
        checkEscaped(NotValidator.getDescriptor());
    }

    private static void checkEscaped(Descriptor type) {
        ValidateSpecs ifOuterClass = new ValidateSpecs(new MessageType(type));
        assertThat(ifOuterClass.validatorClass().name).isEqualTo("Validator$");
    }
}
