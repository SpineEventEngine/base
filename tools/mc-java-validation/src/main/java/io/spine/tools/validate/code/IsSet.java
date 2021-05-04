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

package io.spine.tools.validate.code;

import com.google.protobuf.Descriptors.FieldDescriptor.JavaType;
import com.squareup.javapoet.CodeBlock;
import com.squareup.javapoet.MethodSpec;
import com.squareup.javapoet.TypeName;
import io.spine.code.proto.FieldDeclaration;
import io.spine.tools.validate.FieldAccess;
import io.spine.tools.validate.MessageAccess;
import io.spine.validate.Alternative;

import static com.google.common.base.Preconditions.checkNotNull;
import static com.google.common.base.Preconditions.checkState;
import static com.squareup.javapoet.ClassName.bestGuess;
import static io.spine.tools.validate.code.BooleanExpression.falseLiteral;
import static io.spine.tools.validate.code.BooleanExpression.trueLiteral;
import static java.lang.String.format;
import static javax.lang.model.element.Modifier.PRIVATE;
import static javax.lang.model.element.Modifier.STATIC;

/**
 * A factory of code which check is a given field is set.
 */
public final class IsSet {

    private static final String MESSAGE = "msg";

    private final FieldDeclaration field;
    private final String methodName;
    private final CodeBlock methodBody;
    private boolean alwaysTrue = false;

    public IsSet(FieldDeclaration field) {
        this.field = checkNotNull(field);
        this.methodName = format("is%sSet", field.name()
                                                 .toCamelCase());
        this.methodBody = methodBody();
    }

    /**
     * Produces an expression which invokes the method which checks if the value is set.
     *
     * @param parameter
     *         the parameter to the method - the message containing the field
     * @return an expression which checks if the field is set
     * @see #method()
     */
    public BooleanExpression invocation(MessageAccess parameter) {
        checkNotNull(parameter);
        return alwaysTrue
               ? trueLiteral()
               : BooleanExpression.fromCode("$N($L)", methodName, parameter);
    }

    /**
     * Creates a method which checks if the value is set.
     *
     * @see #invocation(MessageAccess)
     */
    public MethodSpec method() {
        TypeName messageType = bestGuess(field.declaringType()
                                              .javaClassName()
                                              .canonicalName());
        return MethodSpec
                .methodBuilder(methodName)
                .addModifiers(PRIVATE, STATIC)
                .returns(boolean.class)
                .addParameter(messageType, MESSAGE)
                .addCode(methodBody)
                .build();
    }

    private CodeBlock methodBody() {
        MessageAccess message = MessageAccess.of(MESSAGE);
        FieldAccess fieldAccess = message.get(field);
        return field.isCollection()
               ? methodBodyForCollection(fieldAccess)
               : methodBodyForSingular(fieldAccess);
    }

    private CodeBlock methodBodyForSingular(FieldAccess fieldAccess) {
        BooleanExpression expression = valueIsPresent(fieldAccess);
        alwaysTrue = expression.isConstant() && expression.isConstantTrue();
        return expression.returnStatement();
    }

    private CodeBlock methodBodyForCollection(FieldAccess fieldAccess) {
        BooleanExpression collectionIsNotEmpty = Containers
                .isEmpty(fieldAccess)
                .negate();
        Expression<?> elementAccess = Expression.of("el");
        BooleanExpression elementIsSet = valueIsPresent(elementAccess);
        if (elementIsSet.isConstant()) {
            checkState(elementIsSet.isConstantTrue(), "The field `%s` can never be non-default.");
            return collectionIsNotEmpty.returnStatement();
        } else {
            String nonDefaultField = "nonDefault";
            return CodeBlock
                    .builder()
                    .beginControlFlow("if ($L)", collectionIsNotEmpty)
                    .addStatement("$T $N = false", boolean.class, nonDefaultField)
                    .beginControlFlow("for ($L $N : $L)",
                                      field.javaTypeName(),
                                      elementAccess.toString(),
                                      fieldAccess)
                    .addStatement("$N |= $L", nonDefaultField, elementIsSet)
                    .endControlFlow()
                    .add(Expression.of(nonDefaultField)
                                   .returnStatement())
                    .nextControlFlow("else")
                    .add(falseLiteral().returnStatement())
                    .endControlFlow()
                    .build();
        }
    }

    /**
     * Produces an expression which tests if a single value of the field is present.
     *
     * <p>If the field is singular, the expression is equivalent (but not the same) as the one
     * produced by {@link #invocation(MessageAccess)}. If the field is a collection however,
     * the produced expression only tests a single value (i.e. element of the collection).
     *
     * @param valueAccess
     *         the value to be tested
     * @return the boolean expression testing if the value is non-default
     */
    public BooleanExpression valueIsPresent(Expression<?> valueAccess) {
        return valueIsNotSet(valueAccess).negate();
    }

    /**
     * Produces an expression which tests if a single value of the field is not set.
     *
     * @param valueAccess
     *         the value to be tested
     * @return the boolean expression testing if the value is default
     * @see #valueIsPresent(Expression)
     */
    public BooleanExpression valueIsNotSet(Expression<?> valueAccess) {
        JavaType javaType = field.isMap()
                            ? field.valueDeclaration()
                                   .javaType()
                            : field.javaType();
        switch (javaType) {
            case STRING:
            case BYTE_STRING:
                return Containers
                        .isEmpty(valueAccess);
            case ENUM:
            case MESSAGE:
                return Containers
                        .isDefault(valueAccess);
            case BOOLEAN:
            case INT:
            case LONG:
            case FLOAT:
            case DOUBLE:
            default:
                return falseLiteral();
        }
    }

    /**
     * Produces an expression which checks if a given {@linkplain Alternative set of fields} is set.
     *
     * <p>For the resulting expression to yield {@code true}, all the fields in the given
     * {@link Alternative} must be set.
     *
     * @param alternative
     *         fields to check
     * @param messageAccess
     *         message containing the fields
     */
    public static BooleanExpression
    alternativeIsSet(Alternative alternative, MessageAccess messageAccess) {
        BooleanExpression alternativeMatched =
                alternative.fields()
                           .stream()
                           .map(IsSet::new)
                           .reduce(trueLiteral(),
                                   (condition, isSet) ->
                                           condition.and(isSet.invocation(messageAccess)),
                                   BooleanExpression::and);
        return alternativeMatched;
    }
}
