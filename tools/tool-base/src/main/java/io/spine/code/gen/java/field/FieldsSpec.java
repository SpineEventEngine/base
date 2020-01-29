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
import io.spine.base.SubscribableField;
import io.spine.code.gen.java.GeneratedTypeSpec;
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
import static io.spine.util.Exceptions.newIllegalArgumentException;
import static java.lang.String.format;
import static javax.lang.model.element.Modifier.FINAL;
import static javax.lang.model.element.Modifier.PUBLIC;
import static javax.lang.model.element.Modifier.STATIC;

/**
 * A spec which defines a type that exposes message fields as strongly-typed values.
 *
 * <p>For the given message type, the spec defines a {@code Fields} class which:
 * <ol>
 *     <li>Exposes all top-level message fields through the static methods with names that match
 *         the field names in {@code javaCase}.
 *     <li>Defines nested classes which expose nested message fields and instances of which are
 *         returned from the higher level methods.
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
 * public static final class Fields {
 *
 *     private Fields {
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
 *         // Instantiation is allowed only inside the `Fields` class.
 *         private OrderIdField(...) {...}
 *
 *         public EntityStateField value() {
 *             return new EntityStateField(...);
 *         }
 *     }
 * }
 * </pre>
 *
 * <p>The values obtained from the `Fields` class can then be passed to subscription filters to
 * form a subscription request.
 *
 * <p>Please note that for {@code repeated} and {@code map} fields the nested fields are not
 * exposed (because targeting them in a filter won't always be processed properly on the server).
 *
 * <p>The descendants of this type differentiate between entity state, event and event context
 * fields allowing to pass a specific field type to the filter builder to form a typed subscription
 * request.
 */
public abstract class FieldsSpec implements GeneratedTypeSpec {

    @SuppressWarnings("DuplicateStringLiteralInspection") // Random duplication.
    private static final String CLASS_NAME = "Fields";

    /**
     * A message type for which the class is generated.
     */
    private final MessageType messageType;

    /**
     * The top-level message fields.
     */
    private final ImmutableList<FieldDeclaration> fields;

    /**
     * The recursively-collected nested field types.
     *
     * @see NestedFieldScanner
     */
    @LazyInit
    private @MonotonicNonNull List<MessageType> nestedFieldTypes;

    FieldsSpec(MessageType messageType) {
        this.messageType = messageType;
        this.fields = messageType.fields();
    }

    /**
     * Creates a {@code FieldsSpec} for the given message type.
     *
     * @throws IllegalArgumentException
     *         if the field generation for the passed message type is not supported
     */
    public static FieldsSpec of(MessageType messageType) {
        checkNotNull(messageType);
        if (messageType.isEntityState() || messageType.isSpineCoreEvent()) {
            return new EntityStateFields(messageType);
        }
        if (messageType.isEvent() || messageType.isRejection()) {
            return new EventMessageFields(messageType);
        }
        if (messageType.isEventContext()) {
            return new EventContextFields(messageType);
        }
        throw newIllegalArgumentException(
                "Unexpected message type during fields generation: %s.", messageType.name()
        );
    }

    @Override
    public PackageName packageName() {
        return messageType.javaPackage();
    }

    @Override
    public TypeSpec typeSpec() {
        TypeSpec result = TypeSpec
                .classBuilder(CLASS_NAME)
                .addJavadoc(javadoc())
                .addModifiers(PUBLIC, STATIC, FINAL)
                .addAnnotation(generatedBySpineModelCompiler())
                .addMethod(spec())
                .addMethods(fields())
                .addTypes(messageTypeFields())
                .build();
        return result;
    }

    /**
     * Generates the static methods which expose the top-level message fields.
     */
    private ImmutableList<MethodSpec> fields() {
        ImmutableList<MethodSpec> result =
                fields.stream()
                      .map(this::topLevelFieldSpec)
                      .map(FieldSpec::methodSpec)
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
                                  .map(type -> new MessageTypedField(type, fieldSupertype()))
                                  .map(MessageTypedField::typeSpec)
                                  .collect(toImmutableList());
        return result;
    }

    private FieldSpec topLevelFieldSpec(FieldDeclaration field) {
        return new TopLevelFieldSpec(field, fieldSupertype());
    }

    private List<MessageType> nestedFieldTypes() {
        if (nestedFieldTypes == null) {
            NestedFieldScanner scanner = new NestedFieldScanner(messageType);
            nestedFieldTypes = scanner.scan();
        }
        return nestedFieldTypes;
    }

    /**
     * Returns the supertype with which all returned fields are marked.
     *
     * <p>The field supertype defines the filter type whose instance is constructed when the field
     * is passed to the filter builder.
     */
    protected abstract Class<? extends SubscribableField> fieldSupertype();

    /**
     * Generates the class Javadoc.
     */
    private static CodeBlock javadoc() {
        CodeBlock firstParagraphText = CodeBlock
                .builder()
                .add("The listing of all fields of the message type.")
                .build();
        JavadocText firstParagraph = JavadocText.fromEscaped(firstParagraphText.toString())
                                                .withNewLine()
                                                .withNewLine();
        String secondParagraphText = format(
                "The fields exposed by this class can be provided to a subscription filter%s" +
                        "on creation.",
                JavadocText.lineSeparator()
        );
        JavadocText secondParagraph = JavadocText.fromEscaped(secondParagraphText)
                                                 .withPTag()
                                                 .withNewLine()
                                                 .withNewLine();
        String thirdParagraphText =
                "Use static methods of this class to access the top-level fields of the message.";
        JavadocText thirdParagraph = JavadocText.fromEscaped(thirdParagraphText)
                                                .withPTag()
                                                .withNewLine()
                                                .withNewLine();
        String fourthParagraphText = format(
                "The nested fields can be accessed using the values returned by the top-level%s" +
                        "field accessors, through method chaining.",
                JavadocText.lineSeparator()
        );
        JavadocText fourthParagraph = JavadocText.fromEscaped(fourthParagraphText)
                                                 .withPTag()
                                                 .withNewLine();
        CodeBlock value = CodeBlock
                .builder()
                .add(firstParagraph.value())
                .add(secondParagraph.value())
                .add(thirdParagraph.value())
                .add(fourthParagraph.value())
                .build();
        return value;
    }
}
