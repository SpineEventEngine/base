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

import com.google.common.collect.ImmutableList;
import com.google.errorprone.annotations.concurrent.LazyInit;
import com.squareup.javapoet.CodeBlock;
import com.squareup.javapoet.MethodSpec;
import com.squareup.javapoet.TypeSpec;
import io.spine.base.entity.EntityStateField;
import io.spine.code.gen.java.GeneratedJavadoc;
import io.spine.code.gen.java.GeneratedTypeSpec;
import io.spine.code.java.ClassName;
import io.spine.code.java.PackageName;
import io.spine.code.javadoc.JavadocText;
import io.spine.code.proto.FieldDeclaration;
import io.spine.type.MessageType;
import org.checkerframework.checker.nullness.qual.MonotonicNonNull;

import java.util.List;

import static com.google.common.base.Preconditions.checkNotNull;
import static com.google.common.collect.ImmutableList.toImmutableList;
import static io.spine.code.gen.java.Annotations.generatedBySpineModelCompiler;
import static io.spine.code.gen.java.EmptyPrivateCtor.spec;
import static javax.lang.model.element.Modifier.FINAL;
import static javax.lang.model.element.Modifier.PUBLIC;
import static javax.lang.model.element.Modifier.STATIC;

/**
 * A spec of a generated type which exposes message fields as strongly-typed values.
 *
 * <p>For the given message type, the spec defines a {@code Field} class which:
 * <ol>
 *     <li>Exposes all top-level message fields through the {@code static} methods with names that
 *         match the field names in {@code javaCase}.
 *     <li>Defines nested classes which expose nested message fields and instances of which are
 *         returned from the higher level methods.
 *     <li>Marks all exposed fields with the provided {@link #fieldSupertype}.
 *     <li>Is non-instantiable.
 * </ol>
 *
 * <p>For example:
 * <pre>
 * // Given message declarations.
 * message OrderView {
 *     option (entity).kind = PROJECTION;
 *
 *     OrderId id = 1;
 *     // ...
 * }
 *
 * message OrderId {
 *     string value = 1;
 * }
 *
 * // The following Java class will be generated for the `OrderView` message type.
 * public static final class Field {
 *
 *     private Field {
 *         // Prevent instantiation.
 *     }
 *
 *     // The `OrderIdField` instance can both be passed to the filter builder itself and used for
 *     // obtaining more nested properties.
 *
 *     public static OrderIdField id() {
 *         return new OrderIdField(...);
 *     }
 *
 *     public static final class OrderIdField extends EntityStateField {
 *
 *         private OrderIdField(...) {
 *             // Instantiation is allowed only inside the `Field` class.
 *         }
 *
 *         public EntityStateField value() {
 *             return new EntityStateField(...);
 *         }
 *     }
 * }
 * </pre>
 *
 * <p>The values obtained from the {@code Field} class can then be passed to subscription filters
 * to form a subscription request.
 *
 * <p>Please note that for {@code repeated} and {@code map} fields the nested fields are not
 * exposed (because targeting them in a filter won't always be processed properly on the server).
 */
public final class FieldContainerSpec implements GeneratedTypeSpec {

    @SuppressWarnings("DuplicateStringLiteralInspection") // Random duplication.
    private static final String CLASS_NAME = "Field";

    /**
     * A message type for which the class is generated.
     */
    private final MessageType messageType;

    /**
     * The top-level message fields.
     */
    private final ImmutableList<FieldDeclaration> fields;

    /**
     * A type to mark the generated fields with.
     *
     * <p>An example of such type could be the {@link EntityStateField} or
     * {@link io.spine.base.EventMessageField} along with custom user-defined field types.
     */
    private final ClassName fieldSupertype;

    /**
     * The recursively-collected nested field types.
     *
     * @see NestedFieldScanner
     */
    @LazyInit
    private @MonotonicNonNull List<MessageType> nestedFieldTypes;

    public FieldContainerSpec(MessageType messageType, ClassName fieldSupertype) {
        this.messageType = checkNotNull(messageType);
        this.fields = messageType.fields();
        this.fieldSupertype = checkNotNull(fieldSupertype);
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
                .addModifiers(PUBLIC, STATIC, FINAL)
                .addAnnotation(generatedBySpineModelCompiler())
                .addMethod(spec())
                .addMethods(fields())
                .addTypes(messageTypeFields())
                .build();
        return result;
    }

    /**
     * Generates the {@code static} methods which expose the top-level message fields.
     */
    private ImmutableList<MethodSpec> fields() {
        ImmutableList<MethodSpec> result =
                fields.stream()
                      .map(this::topLevelFieldSpec)
                      .map(FieldAccessor::methodSpec)
                      .collect(toImmutableList());
        return result;
    }

    /**
     * Generates a nested class for each top-level or nested field of the message which is of
     * a {@link com.google.protobuf.Message Message} type itself.
     *
     * <p>Such classes allow to retrieve the nested fields of a message in a strongly-typed manner,
     * building up the required field path through the chain of method calls.
     *
     * @see MessageTypedField
     */
    private ImmutableList<TypeSpec> messageTypeFields() {
        ImmutableList<TypeSpec> result =
                nestedFieldTypes().stream()
                                  .map(type -> new MessageTypedField(type, fieldSupertype))
                                  .map(MessageTypedField::typeSpec)
                                  .collect(toImmutableList());
        return result;
    }

    private FieldAccessor topLevelFieldSpec(FieldDeclaration field) {
        return new TopLevelFieldAccessor(field, fieldSupertype);
    }

    private List<MessageType> nestedFieldTypes() {
        if (nestedFieldTypes == null) {
            NestedFieldScanner scanner = new NestedFieldScanner(messageType);
            nestedFieldTypes = scanner.scan();
        }
        return nestedFieldTypes;
    }

    /**
     * Obtains the class Javadoc.
     */
    private static GeneratedJavadoc javadoc() {
        return GeneratedJavadoc.threeParagraph(
                CodeBlock.of("The listing of all fields of the message type."),
                CodeBlock.of("The fields exposed by this class can be provided to " +
                                     "a subscription filter on creation."),
                CodeBlock.of("Use static methods of this class to access the top-level fields " +
                                     "of the message. The nested$L fields can be accessed using " +
                                     "the values returned by the top-level field accessors, " +
                                     "through$L method chaining.",
                             JavadocText.lineSeparator(), JavadocText.lineSeparator())
        );
    }
}
