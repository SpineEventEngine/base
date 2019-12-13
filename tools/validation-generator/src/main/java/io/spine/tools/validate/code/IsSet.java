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

package io.spine.tools.validate.code;

import com.google.protobuf.Descriptors.FieldDescriptor.JavaType;
import com.squareup.javapoet.CodeBlock;
import com.squareup.javapoet.MethodSpec;
import com.squareup.javapoet.TypeName;
import io.spine.code.proto.FieldDeclaration;
import io.spine.tools.validate.FieldAccess;
import io.spine.tools.validate.MessageAccess;
import io.spine.tools.validate.field.Containers;

import static com.google.common.base.Preconditions.checkNotNull;
import static com.google.common.base.Preconditions.checkState;
import static com.squareup.javapoet.ClassName.bestGuess;
import static io.spine.tools.validate.FieldAccess.fieldOfMessage;
import static io.spine.tools.validate.code.BooleanExpression.falseLiteral;
import static io.spine.tools.validate.code.BooleanExpression.trueLiteral;
import static java.lang.String.format;
import static javax.lang.model.element.Modifier.PRIVATE;
import static javax.lang.model.element.Modifier.STATIC;

public final class IsSet {

    private static final String MESSAGE = "msg";

    private final FieldDeclaration field;
    private final String methodName;
    private final CodeBlock methodBody;
    private boolean alwaysTrue = false;

    public IsSet(FieldDeclaration field) {
        this.field = checkNotNull(field);
        this.methodName = format("is%sSet", field.name().toCamelCase());
        this.methodBody = methodBody();
    }

    public BooleanExpression invocation(MessageAccess parameter) {
        checkNotNull(parameter);
        return alwaysTrue
               ? trueLiteral()
               : BooleanExpression.fromCode("$N($L)",
                                            methodName,
                                            parameter);
    }

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
        FieldAccess fieldAccess = fieldOfMessage(new MessageAccess(Expression.of(MESSAGE)), field);
        return field.isCollection()
               ? methodBodyForCollection(fieldAccess)
               : methodBodyForSingular(fieldAccess);
    }

    private CodeBlock methodBodyForSingular(FieldAccess fieldAccess) {
        BooleanExpression expression = singularIsSet(fieldAccess.expression());
        alwaysTrue = expression.isConstant() && expression.isConstantTrue();
        return expression.returnStatement();
    }

    private CodeBlock methodBodyForCollection(FieldAccess fieldAccess) {
        BooleanExpression collectionIsNotEmpty = Containers
                .isEmpty(fieldAccess.expression())
                .negate();
        Expression<?> elementAccess = Expression.of("el");
        BooleanExpression elementIsSet = singularIsSet(elementAccess);
        if (elementIsSet.isConstant()) {
            checkState(elementIsSet.isConstantTrue(), "Field `%s` can never be non-default.");
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
                    .add(Expression.of(nonDefaultField).returnStatement())
                    .nextControlFlow("else")
                    .add(falseLiteral().returnStatement())
                    .endControlFlow()
                    .build();
        }
    }

    private BooleanExpression singularIsSet(Expression<?> fieldAccess) {
        JavaType javaType = field.isMap()
                            ? field.valueDeclaration().javaType()
                            : field.javaType();
        switch (javaType) {
            case STRING:
            case BYTE_STRING:
                return Containers
                        .isEmpty(fieldAccess)
                        .negate();
            case ENUM:
            case MESSAGE:
                return Containers
                        .isDefault(fieldAccess)
                        .negate();
            case BOOLEAN:
            case INT:
            case LONG:
            case FLOAT:
            case DOUBLE:
            default:
                return trueLiteral();
        }
    }
}
