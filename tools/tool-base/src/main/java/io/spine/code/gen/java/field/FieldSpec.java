/*
 * Copyright 2020, TeamDev. All rights reserved.
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

package io.spine.code.gen.java.field;

import com.squareup.javapoet.CodeBlock;
import com.squareup.javapoet.MethodSpec;
import io.spine.base.SubscribableField;
import io.spine.code.gen.java.FieldJavadoc;
import io.spine.code.gen.java.GeneratedJavadoc;
import io.spine.code.gen.java.GeneratedMethodSpec;
import io.spine.code.gen.java.JavaPoetName;
import io.spine.code.java.ClassName;
import io.spine.code.java.SimpleClassName;
import io.spine.code.proto.FieldDeclaration;
import io.spine.code.proto.FieldName;

import javax.lang.model.element.Modifier;

import static com.google.common.base.Preconditions.checkState;

/**
 * A spec of the method which returns a {@linkplain SubscribableField strongly-typed message field}.
 *
 * <p>The name of the method matches the field name in {@code javaCase}.
 *
 * <p>The descendants of this class differentiate between top-level and nested fields to enable the
 * correct field path propagation.
 */
@SuppressWarnings("DuplicateStringLiteralInspection")
// Random duplication of some generated code elements.
abstract class FieldSpec implements GeneratedMethodSpec {

    private final FieldDeclaration field;
    private final Class<? extends SubscribableField> fieldSupertype;

    FieldSpec(FieldDeclaration field, Class<? extends SubscribableField> fieldSupertype) {
        this.field = field;
        this.fieldSupertype = fieldSupertype;
    }

    @Override
    public MethodSpec methodSpec(Modifier... modifiers) {
        MethodSpec result = MethodSpec
                .methodBuilder(fieldName().javaCase())
                .addJavadoc(javadoc())
                .addModifiers(modifiers)
                .returns(returnType().value())
                .addStatement(methodBody())
                .build();
        return result;
    }

    /**
     * Returns the field name as defined in Protobuf.
     */
    FieldName fieldName() {
        return field.name();
    }

    /**
     * Obtains the method return type.
     */
    JavaPoetName returnType() {
        return shouldExposeNestedFields()
               ? nestedFieldsContainer()
               : simpleField();
    }

    /**
     * Obtains the method body.
     */
    private CodeBlock methodBody() {
        return shouldExposeNestedFields()
               ? returnNestedFieldsContainer()
               : returnSimpleField();
    }

    /**
     * Checks if the wrapped field has nested fields and should expose them to subscribers.
     */
    private boolean shouldExposeNestedFields() {
        return field.isSingularMessage();
    }

    /**
     * Obtains a JavaPoet name for the type representing a nested field container which is
     * returned from this method.
     */
    private JavaPoetName nestedFieldsContainer() {
        JavaPoetName type = JavaPoetName.of(fieldTypeName().with("Field"));
        return type;
    }

    /**
     * Obtains a JavaPoet name for the simple field (i.e. the one which doesn't expose nested
     * ones) returned by this method.
     */
    private JavaPoetName simpleField() {
        JavaPoetName type = JavaPoetName.of(fieldSupertype);
        return type;
    }

    /**
     * A return statement which instantiates a nested fields container.
     */
    abstract CodeBlock returnNestedFieldsContainer();

    /**
     * A return statement which instantiates a simple field.
     */
    abstract CodeBlock returnSimpleField();

    /**
     * A simple name of the field type.
     *
     * <p>Assumes the wrapped field is a {@link com.google.protobuf.Message Message}.
     */
    private SimpleClassName fieldTypeName() {
        checkState(field.isMessage());
        String fieldTypeName = field.javaTypeName();
        SimpleClassName result = ClassName.of(fieldTypeName)
                                          .toSimple();
        return result;
    }

    /**
     * The supertype from which the returned field should inherit.
     *
     * <p>Enables the typed filter creation on the client side.
     */
    final Class<? extends SubscribableField> fieldSupertype() {
        return fieldSupertype;
    }

    /**
     * Generates the method Javadoc.
     */
    private CodeBlock javadoc() {
        GeneratedJavadoc javadoc = new FieldJavadoc(this.field, "field");
        return javadoc.spec();
    }
}
