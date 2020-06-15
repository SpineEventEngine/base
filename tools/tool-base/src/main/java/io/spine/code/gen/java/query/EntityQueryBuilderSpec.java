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

package io.spine.code.gen.java.query;

import com.google.common.collect.ImmutableList;
import com.squareup.javapoet.ClassName;
import com.squareup.javapoet.MethodSpec;
import com.squareup.javapoet.ParameterizedTypeName;
import com.squareup.javapoet.TypeName;
import com.squareup.javapoet.TypeSpec;
import io.spine.base.entity.EntityQueryBuilder;
import io.spine.code.gen.java.JavaPoetName;
import io.spine.code.java.SimpleClassName;
import io.spine.code.proto.EntityIdField;
import io.spine.code.proto.FieldDeclaration;
import io.spine.type.MessageType;

import static com.google.common.collect.ImmutableList.toImmutableList;
import static io.spine.code.gen.java.Annotations.generatedBySpineModelCompiler;
import static io.spine.code.proto.ColumnOption.columnsOf;
import static javax.lang.model.element.Modifier.FINAL;
import static javax.lang.model.element.Modifier.PUBLIC;
import static javax.lang.model.element.Modifier.STATIC;

/**
 * Assembles the specification for the {@code QueryBuilder} class generated for entity state types.
 */
public final class EntityQueryBuilderSpec extends AbstractEntityQuerySpec {

    private static final SimpleClassName CLASS_NAME = SimpleClassName.create("QueryBuilder");

    private final EntityIdField idField;
    private final ImmutableList<FieldDeclaration> columns;
    private final TypeName typeOfSelf;

    public EntityQueryBuilderSpec(MessageType type) {
        super(type);
        this.idField = EntityIdField.of(type);
        this.columns = columnsOf(type);
        this.typeOfSelf = JavaPoetName.of(CLASS_NAME)
                                      .value();
    }

    @Override
    public TypeSpec typeSpec() {
        TypeSpec result = TypeSpec
                .classBuilder(CLASS_NAME.value())
                .superclass(ParameterizedTypeName.get(ClassName.get(EntityQueryBuilder.class),
                                                      stateClassName()))
                .addAnnotation(generatedBySpineModelCompiler())
                .addModifiers(PUBLIC, STATIC, FINAL)
                .addMethod(id())
                .addMethods(columns())
                .build();
        return result;
    }

    /**
     * Generates the methods which allow to specify restrictions put on the entity columns
     * to use in the {@link io.spine.base.entity.EntityQuery EntityQuery}.
     */
    private ImmutableList<MethodSpec> columns() {
        ImmutableList<MethodSpec> result =
                columns.stream()
                       .map((c) -> new EntityColumnSpec(c, typeOfSelf))
                       .map(EntityColumnSpec::methodSpec)
                       .collect(toImmutableList());
        return result;
    }

    /**
     * Generates the method returning the {@link io.spine.base.entity.IdCriterion IdCriterion}
     * for this query builder.
     */
    private MethodSpec id() {
        return new IdColumnSpec(idField, typeOfSelf).methodSpec();
    }
}
