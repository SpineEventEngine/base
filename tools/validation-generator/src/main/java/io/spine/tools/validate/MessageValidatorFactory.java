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

package io.spine.tools.validate;

import com.squareup.javapoet.CodeBlock;
import com.squareup.javapoet.JavaFile;
import com.squareup.javapoet.MethodSpec;
import com.squareup.javapoet.TypeSpec;
import io.spine.code.java.PackageName;
import io.spine.type.MessageType;
import io.spine.validate.ValidationException;

import static com.squareup.javapoet.ClassName.bestGuess;
import static javax.lang.model.element.Modifier.FINAL;
import static javax.lang.model.element.Modifier.PRIVATE;
import static javax.lang.model.element.Modifier.PUBLIC;
import static javax.lang.model.element.Modifier.STATIC;

public final class MessageValidatorFactory {

    private static final String VALIDATE_METHOD = "validate";

    private final MessageType type;
    private final PackageName packageName;
    private final String validatorSimpleName;
    private final String messageSimpleName;

    public MessageValidatorFactory(MessageType type) {
        this.type = type;
        this.packageName = type.javaPackage();
        this.validatorSimpleName = type.nestedSimpleName()
                                       .joinWithUnderscore() + "_Validator";
        this.messageSimpleName = type.nestedSimpleName()
                                     .toString();
    }

    public JavaFile generateClass() {
        MethodSpec validateMethod = MethodSpec
                .methodBuilder(VALIDATE_METHOD)
                .addModifiers(STATIC)
                .returns(void.class)
                .addParameter(bestGuess(messageSimpleName), "msg")
                .build();
        MethodSpec ctor = MethodSpec
                .constructorBuilder()
                .addModifiers(PRIVATE)
                .addJavadoc("Prevents validator class instantiation.")
                .build();
        TypeSpec type = TypeSpec
                .classBuilder(validatorSimpleName)
                .addModifiers(FINAL)
                .addMethod(ctor)
                .addMethod(validateMethod)
                .build();
        return JavaFile.builder(packageName.value(), type)
                       .build();
    }

    public MethodSpec generateValidate() {
        CodeBlock body = CodeBlock.of("$T.validate(this);", bestGuess(validatorSimpleName));
        return MethodSpec
                .methodBuilder(VALIDATE_METHOD)
                .addModifiers(PUBLIC)
                .returns(void.class)
                .addException(ValidationException.class)
                .addCode(body)
                .build();
    }

    public MethodSpec generateVBuild() {
        String msg = "msg";
        CodeBlock body = CodeBlock
                .builder()
                .addStatement("$T $N = build()", bestGuess(messageSimpleName), msg)
                .addStatement("$T." + VALIDATE_METHOD + "($N)", bestGuess(validatorSimpleName), msg)
                .addStatement("return $N", msg)
                .build();
        return MethodSpec
                .methodBuilder("vBuild")
                .addAnnotation(Override.class)
                .addModifiers(PUBLIC)
                .returns(bestGuess(messageSimpleName))
                .addException(ValidationException.class)
                .addCode(body)
                .build();
    }
}
