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

package io.spine.code.gen.java.column;

import com.google.common.collect.ImmutableList;
import com.squareup.javapoet.CodeBlock;
import com.squareup.javapoet.MethodSpec;
import com.squareup.javapoet.TypeSpec;
import io.spine.code.gen.java.GeneratedJavadoc;
import io.spine.code.gen.java.GeneratedTypeSpec;
import io.spine.code.java.PackageName;
import io.spine.code.javadoc.JavadocText;
import io.spine.code.proto.FieldDeclaration;
import io.spine.query.EntityColumn;
import io.spine.type.MessageType;

import static com.google.common.base.Preconditions.checkNotNull;
import static com.google.common.collect.ImmutableList.toImmutableList;
import static io.spine.code.gen.java.Annotations.generatedBySpineModelCompiler;
import static io.spine.code.gen.java.EmptyPrivateCtor.spec;
import static io.spine.code.proto.ColumnOption.columnsOf;
import static javax.lang.model.element.Modifier.FINAL;
import static javax.lang.model.element.Modifier.PUBLIC;
import static javax.lang.model.element.Modifier.STATIC;

/**
 * A spec of a generated type which exposes columns of an entity as strongly-typed values.
 *
 * <p>For the given entity state type, the spec defines a {@code Column} class which:
 * <ol>
 *     <li>Exposes all entity columns through the {@code static} methods with names that match
 *         the column names in {@code javaCase}.
 *     <li>Is non-instantiable.
 * </ol>
 *
 * <p>For example:
 * <pre>
 * // Given a message declaration.
 * message ProjectDetails {
 *     option (entity).kind = PROJECTION;
 *
 *     ProjectId id = 1;
 *     ProjectName name = 2 [(column) = true];
 *     int32 task_count = 3 [(column) = true];
 * }
 *
 * // The following Java class will be generated.
 * public static final class Column {
 *
 *     private Column() {
 *         // Prevent instantiation.
 *     }
 *
 *     public static io.spine.query.EntityColumn name() {...}
 *
 *     public static io.spine.query.EntityColumn taskCount() {...}
 * }
 * </pre>
 *
 * <p>The {@code EntityColumn} instances retrieved from the {@code Column} methods can be passed
 * to the query filters to form an entity query.
 *
 * <p>The nested columns are ignored during the class generation as they are currently not
 * supported on the server side.
 */
public final class ColumnContainerSpec implements GeneratedTypeSpec {

    private static final String CLASS_NAME = "Column";

    private final MessageType messageType;
    private final ImmutableList<FieldDeclaration> columns;

    private ColumnContainerSpec(MessageType messageType) {
        this.messageType = messageType;
        this.columns = columnsOf(messageType);
    }

    public static ColumnContainerSpec of(MessageType messageType) {
        checkNotNull(messageType);
        return new ColumnContainerSpec(messageType);
    }

    @Override
    public PackageName packageName() {
        return messageType.javaPackage();
    }

    @Override
    public TypeSpec typeSpec() {
        TypeSpec result = TypeSpec
                .classBuilder(CLASS_NAME)
                .addJavadoc(javadoc().spec())
                .addAnnotation(generatedBySpineModelCompiler())
                .addModifiers(PUBLIC, STATIC, FINAL)
                .addMethod(spec())
                .addMethods(columns())
                .build();
        return result;
    }

    /**
     * Generates the methods which return entity columns as {@link EntityColumn}
     * instances.
     */
    private ImmutableList<MethodSpec> columns() {
        ImmutableList<MethodSpec> result =
                columns.stream()
                       .map(ColumnAccessor::new)
                       .map(ColumnAccessor::methodSpec)
                       .collect(toImmutableList());
        return result;
    }

    /**
     * Obtains the class Javadoc.
     */
    private static GeneratedJavadoc javadoc() {
        return GeneratedJavadoc.twoParagraph(
                CodeBlock.of("A listing of all entity columns of the type."),
                CodeBlock.of("Use static methods of this class to access the columns of the " +
                                     "entity$L which can then be used for query filters creation.",
                             JavadocText.lineSeparator())
        );
    }
}
