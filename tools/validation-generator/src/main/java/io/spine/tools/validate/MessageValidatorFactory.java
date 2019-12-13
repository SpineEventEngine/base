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

import com.google.common.collect.ImmutableList;
import com.google.common.reflect.TypeToken;
import com.squareup.javapoet.AnnotationSpec;
import com.squareup.javapoet.ClassName;
import com.squareup.javapoet.CodeBlock;
import com.squareup.javapoet.MethodSpec;
import com.squareup.javapoet.TypeSpec;
import io.spine.annotation.Beta;
import io.spine.code.gen.java.GeneratedBySpine;
import io.spine.code.gen.java.NestedClassName;
import io.spine.type.MessageType;
import io.spine.validate.ConstraintViolation;
import io.spine.validate.Constraints;
import io.spine.validate.ValidationException;

import javax.annotation.Generated;
import java.lang.reflect.Type;
import java.util.Set;

import static com.squareup.javapoet.ClassName.bestGuess;
import static javax.lang.model.element.Modifier.FINAL;
import static javax.lang.model.element.Modifier.PRIVATE;
import static javax.lang.model.element.Modifier.PUBLIC;
import static javax.lang.model.element.Modifier.STATIC;

/**
 * A factory of message validation code.
 */
public final class MessageValidatorFactory {

    private static final String VALIDATE_METHOD = "validate";
    private static final String MESSAGE_VARIABLE = "msg";
    private static final String VIOLATIONS = "constraintViolations";

    @SuppressWarnings("UnstableApiUsage")
    static final Type immutableListOfViolations =
            new TypeToken<ImmutableList<ConstraintViolation>>() {}.getType();

    private final MessageType type;
    private final NestedClassName messageSimpleName;
    private final String validatorSimpleName;

    /**
     * Creates a new {@code MessageValidatorFactory} for the given type.
     *
     * @param type
     *         type of the message to validate
     */
    public MessageValidatorFactory(MessageType type) {
        this.type = type;
        this.messageSimpleName = NestedClassName.from(type.javaClassName());
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

    /**
     * Generates the {@code Validator} class for the associated message type.
     *
     * <p>The {@code Validator} class is supposed to be nested inside the message class. It is
     * declared {@code private} and {@code static}.
     *
     * <p>The name of the class is most often {@code Validator}. However, when a name clash occurs,
     * the name is padded with the {@code $} (dollar sign). For instance if the message itself is
     * called {@code Validator}, the validator class will be called {@code Validator$}. Since
     * the class is {@code private}, this naming is not exposed to the outer scope.
     *
     * <p>The only method of the class is:
     * <pre>
     * private static {@literal List<ConstraintViolation>} validate(MyMsg msg) { ... }
     * </pre>
     *
     * <p>In this example, {@code MyMsg} is the type of the validated message.
     *
     * <p>The class is marked with the {@link Generated} annotation so that static code analysis can
     * ignore it.
     *
     * @return the validator class
     */
    public TypeSpec generateClass() {
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
                .addModifiers(PRIVATE, STATIC, FINAL)
                .addMethod(ctor)
                .addMethods(validatorMethods())
                .build();
        return type;
    }

    private Set<MethodSpec> validatorMethods() {
        Constraints constraints = Constraints.of(type);
        Set<MethodSpec> methods = constraints
                .runThrough(new ConstraintCompiler(VALIDATE_METHOD, type));
        return methods;
    }

    /**
     * Generates the {@code validate()} method for the message class.
     *
     * <p>The method is {@code public} and returns a list of {@link ConstraintViolation}s if
     * the message is not valid. For the full contract, see
     * {@link io.spine.protobuf.MessageWithConstraints#validate()}.
     *
     * @return {@code validate()} method
     */
    public MethodSpec generateValidate() {
        CodeBlock body = CodeBlock.of("return $T.$N(this);",
                                      bestGuess(validatorSimpleName), VALIDATE_METHOD);
        return MethodSpec
                .methodBuilder(VALIDATE_METHOD)
                .addModifiers(PUBLIC)
                .addAnnotation(Beta.class)
                .addAnnotation(Override.class)
                .returns(immutableListOfViolations)
                .addCode(body)
                .build();
    }

    /**
     * Generates the {@code vBuild()} method for the message builder class.
     *
     * @return {@code vBuild()} method
     * @see io.spine.protobuf.ValidatingBuilder#vBuild() for the full contract.
     */
    public MethodSpec generateVBuild() {
        ClassName messageClass = bestGuess(messageSimpleName.value());
        Class<ValidationException> exceptionClass = ValidationException.class;
        CodeBlock body = CodeBlock
                .builder()
                .addStatement("$T $N = build()",
                              messageClass, MESSAGE_VARIABLE)
                .addStatement("$T $N = $N.$N()",
                              immutableListOfViolations,
                              VIOLATIONS,
                              MESSAGE_VARIABLE,
                              VALIDATE_METHOD)
                .beginControlFlow("if (!$N.isEmpty())", VIOLATIONS)
                .addStatement("throw new $T($N)", exceptionClass, VIOLATIONS)
                .endControlFlow()
                .addStatement("return $N", MESSAGE_VARIABLE)
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
