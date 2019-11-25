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

import com.google.common.reflect.TypeToken;
import com.squareup.javapoet.AnnotationSpec;
import com.squareup.javapoet.ClassName;
import com.squareup.javapoet.CodeBlock;
import com.squareup.javapoet.MethodSpec;
import com.squareup.javapoet.TypeSpec;
import io.spine.code.gen.java.GeneratedBySpine;
import io.spine.code.java.NestedClassName;
import io.spine.code.proto.FieldDeclaration;
import io.spine.tools.validate.code.Expression;
import io.spine.tools.validate.field.FieldValidatorFactories;
import io.spine.tools.validate.field.FieldValidatorFactory;
import io.spine.type.MessageType;
import io.spine.validate.ConstraintViolation;
import io.spine.validate.ValidationException;

import javax.annotation.Generated;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.function.Function;

import static com.squareup.javapoet.ClassName.bestGuess;
import static javax.lang.model.element.Modifier.FINAL;
import static javax.lang.model.element.Modifier.PRIVATE;
import static javax.lang.model.element.Modifier.PUBLIC;
import static javax.lang.model.element.Modifier.STATIC;

public final class MessageValidatorFactory {

    private static final String VALIDATE_METHOD = "validate";
    private static final String MESSAGE_PARAMETER = "msg";
    private static final String MESSAGE_VARIABLE = "msg";
    private static final String RETURN_N = "return $N";
    private static final String VIOLATIONS = "violations";

    @SuppressWarnings("UnstableApiUsage")
    private static final Type listOfViolations =
            new TypeToken<List<ConstraintViolation>>() {}.getType();
    @SuppressWarnings("UnstableApiUsage")
    private static final Type arrayListOfViolations =
            new TypeToken<ArrayList<ConstraintViolation>>() {}.getType();

    private final MessageType type;
    private final NestedClassName messageSimpleName;
    private final String validatorSimpleName;

    public MessageValidatorFactory(MessageType type) {
        this.type = type;
        this.messageSimpleName = type.javaClassName()
                                     .asNested();
        this.validatorSimpleName = nameForValidator(type);
    }

    private static String nameForValidator(MessageType type) {
        StringBuilder candidate = new StringBuilder("Validator");
        while (nameClashes(type, candidate.toString())) {
            candidate.append('$');
        }
        return candidate.toString();
    }

    private static boolean nameClashes(MessageType type, String name) {
        boolean withOwnName = type.simpleJavaClassName()
                                  .value()
                                  .equals(name);
        if (withOwnName) {
            return true;
        }
        boolean withNestedName = type
                .nestedDeclarations()
                .stream()
                .anyMatch(nested -> nested.simpleJavaClassName()
                                          .value()
                                          .equals(name));
        return withNestedName;
    }

    public TypeSpec generateClass() {
        MethodSpec validateMethod = MethodSpec
                .methodBuilder(VALIDATE_METHOD)
                .addModifiers(STATIC)
                .returns(listOfViolations)
                .addParameter(bestGuess(messageSimpleName.value()), MESSAGE_PARAMETER)
                .addCode(validator())
                .build();
        MethodSpec ctor = MethodSpec
                .constructorBuilder()
                .addModifiers(PRIVATE)
                .addJavadoc("Prevents validator class instantiation.")
                .build();
        GeneratedBySpine bySpine = GeneratedBySpine.instance();
        AnnotationSpec generated = AnnotationSpec
                .builder(Generated.class)
                .addMember(bySpine.fieldName(), bySpine.codeBlock())
                .build();
        TypeSpec type = TypeSpec
                .classBuilder(validatorSimpleName)
                .addAnnotation(generated)
                .addModifiers(PRIVATE, FINAL)
                .addMethod(ctor)
                .addMethod(validateMethod)
                .build();
        return type;
    }

    private CodeBlock validator() {
        CodeBlock.Builder body = CodeBlock
                .builder()
                .addStatement("$1T $2N = new $1T()", arrayListOfViolations, VIOLATIONS);
        Function<ViolationTemplate, Expression> violationAccumulator =
                violation -> Expression.of("violations.add(" + violation + ')');
        Expression msg = Expression.of(MESSAGE_PARAMETER);
        FieldValidatorFactories factories = new FieldValidatorFactories(msg);
        for (FieldDeclaration field : type.fields()) {
            FieldValidatorFactory factory = factories.forField(field);
            Optional<CodeBlock> fieldValidation = factory.generate(violationAccumulator);
            fieldValidation.ifPresent(body::add);
        }
        body.addStatement(RETURN_N, VIOLATIONS);
        return body.build();
    }

    public MethodSpec generateValidate() {
        CodeBlock body = CodeBlock.of("return $T.validate(this);", bestGuess(validatorSimpleName));
        return MethodSpec
                .methodBuilder(VALIDATE_METHOD)
                .addModifiers(PUBLIC)
                .addAnnotation(Override.class)
                .returns(listOfViolations)
                .addCode(body)
                .build();
    }

    public MethodSpec generateVBuild() {
        ClassName messageClass = bestGuess(messageSimpleName.value());
        Class<ValidationException> exceptionClass = ValidationException.class;
        CodeBlock body = CodeBlock
                .builder()
                .addStatement("$T $N = build()",
                              messageClass, MESSAGE_VARIABLE)
                .addStatement("$T $N = $N.validate()",
                              listOfViolations,
                              VIOLATIONS,
                              MESSAGE_VARIABLE)
                .beginControlFlow("if (!$N.isEmpty())", VIOLATIONS)
                .addStatement("throw new $T($N)", exceptionClass, VIOLATIONS)
                .endControlFlow()
                .addStatement(RETURN_N, MESSAGE_VARIABLE)
                .build();
        return MethodSpec
                .methodBuilder("vBuild")
                .addAnnotation(Override.class)
                .addModifiers(PUBLIC)
                .returns(messageClass)
                .addException(exceptionClass)
                .addCode(body)
                .build();
    }
}
