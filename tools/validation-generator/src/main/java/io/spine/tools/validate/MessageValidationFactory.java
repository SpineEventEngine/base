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

import com.squareup.javapoet.AnnotationSpec;
import com.squareup.javapoet.ClassName;
import com.squareup.javapoet.CodeBlock;
import com.squareup.javapoet.JavaFile;
import com.squareup.javapoet.MethodSpec;
import com.squareup.javapoet.TypeSpec;
import io.spine.code.gen.java.GeneratedBySpine;
import io.spine.code.java.PackageName;
import io.spine.code.java.SimpleClassName;
import io.spine.type.MessageType;
import io.spine.validate.ValidationException;

import javax.annotation.Generated;

import static com.google.common.base.Preconditions.checkNotNull;
import static javax.lang.model.element.Modifier.FINAL;
import static javax.lang.model.element.Modifier.PUBLIC;
import static javax.lang.model.element.Modifier.STATIC;

public final class MessageValidationFactory {

    private final SimpleClassName validatorClassName;
    private final ClassName validatorTypeName;
    private final PackageName packageName;
    private final ClassName messageClassName;

    public MessageValidationFactory(MessageType messageType) {
        checkNotNull(messageType);
        this.packageName = messageType.javaPackage();
        io.spine.code.java.ClassName messageClassName = messageType.javaClassName();
        this.validatorClassName = messageClassName
                                             .toSimple()
                                             .with("_Validator");
        this.validatorTypeName = ClassName.bestGuess(validatorClassName.value());
        this.messageClassName = ClassName.bestGuess(messageClassName
                                                               .canonicalName());
    }

    public JavaFile generateClass() {
        GeneratedBySpine bySpine = GeneratedBySpine.instance();
        MethodSpec validateMethod = MethodSpec
                .methodBuilder("validate")
                .returns(void.class)
                .addModifiers(STATIC)
                .addException(ValidationException.class)
                .addParameter(messageClassName, "msg")
                .build();
        AnnotationSpec generatedAnnotation = AnnotationSpec
                .builder(Generated.class)
                .addMember(bySpine.fieldName(), bySpine.codeBlock())
                .build();
        TypeSpec classSpec = TypeSpec
                .classBuilder(validatorClassName.value())
                .addModifiers(FINAL)
                .addAnnotation(generatedAnnotation)
                .addMethod(validateMethod)
                .build();
        return JavaFile.builder(packageName.value(), classSpec)
                       .build();
    }

    public MethodSpec generateValidationMethod() {
        String msg = "msg";
        CodeBlock body = CodeBlock
                .builder()
                .addStatement("$T $N = build()", messageClassName, msg)
                .addStatement("$T.validate($N)", validatorTypeName, msg)
                .addStatement("return $N", msg)
                .build();
        return MethodSpec.methodBuilder("vBuild")
                         .addModifiers(PUBLIC)
                         .returns(messageClassName)
                         .addAnnotation(Override.class)
                         .addCode(body)
                         .build();
    }
}
