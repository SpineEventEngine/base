/*
 * Copyright 2018, TeamDev. All rights reserved.
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

package io.spine.code.proto;

import com.google.common.base.Joiner;
import com.google.protobuf.Descriptors.FieldDescriptor;
import com.google.protobuf.Descriptors.FieldDescriptor.JavaType;
import com.google.protobuf.Descriptors.FileDescriptor;
import io.spine.base.CommandMessage;
import io.spine.logging.Logging;
import io.spine.option.EntityOption;
import io.spine.option.OptionsProto;
import io.spine.type.ClassName;
import io.spine.type.KnownTypes;
import io.spine.type.TypeName;
import io.spine.type.TypeUrl;
import io.spine.type.UnknownTypeException;
import org.checkerframework.checker.nullness.qual.MonotonicNonNull;

import java.util.List;
import java.util.Optional;

import static com.google.common.base.Preconditions.checkNotNull;
import static com.google.protobuf.Descriptors.FieldDescriptor.Type.ENUM;
import static com.google.protobuf.Descriptors.FieldDescriptor.Type.MESSAGE;
import static java.lang.String.format;
import static java.util.stream.Collectors.toList;

/**
 * Declaration of a Protobuf message field.
 */
@SuppressWarnings("ClassWithTooManyMethods") // OK as isSomething() methods are mutually exclusive.
public final class FieldDeclaration implements Logging {

    private final FieldDescriptor field;
    private final @MonotonicNonNull String leadingComments;

    /**
     * Creates a new instance.
     *
     * @param field
     *         the descriptor of a field
     */
    public FieldDeclaration(FieldDescriptor field) {
        this.field = checkNotNull(field);
        this.leadingComments = null;
    }

    /**
     * Creates a new instance which potentially can have leading comments.
     */
    public FieldDeclaration(FieldDescriptor field, MessageType message) {
        this.field = checkNotNull(field);
        this.leadingComments = message.documentation()
                                      .fieldLeadingComments(field.toProto())
                                      .orElse(null);
    }

    /**
     * Obtains the name of the field.
     */
    public FieldName name() {
        return FieldName.of(field.toProto());
    }

    /**
     * Obtains descriptor of the field.
     */
    public FieldDescriptor descriptor() {
        return field;
    }

    /**
     * Obtains fully-qualified name of the Java class that corresponds to the declared type
     * of the field.
     */
    public String javaTypeName() {
        FieldDescriptor.Type fieldType = field.getType();
        if (fieldType == MESSAGE) {
            return getMessageClassName();
        }

        if (fieldType == ENUM) {
            return getEnumClassName();
        }

        return ScalarType.getJavaTypeName(field.toProto()
                                               .getType());
    }

    private String getMessageClassName() {
        TypeName typeName = TypeName.from(field.getMessageType());
        KnownTypes knownTypes = KnownTypes.instance();
        try {
            TypeUrl fieldTypeUrl = typeName.toUrl();
            ClassName className = knownTypes.getClassName(fieldTypeUrl);
            return className.value();
        } catch (UnknownTypeException e) {
            List<String> allUrls =
                    knownTypes.getAllUrls()
                              .stream()
                              .map(TypeUrl::value)
                              .sorted()
                              .collect(toList());
            String newLine = format(",%n");
            String message =
                    format("Cannot find a type %s in the list of known types:%n%s",
                           typeName,
                           Joiner.on(newLine)
                                 .join(allUrls));

            throw new RuntimeException(message, e);
        }
    }

    private String getEnumClassName() {
        EnumType enumType = EnumType.create(field.getEnumType());
        return enumType.javaClassName().value();
    }

    /**
     * Determines whether the field is an entity ID.
     *
     * <p>An entity ID satisfies the following conditions:
     * <ul>
     *     <li>Declared as the first field.
     *     <li>Named {@code id} or the name ends with {@code _id}.
     *     <li>Declared inside an {@linkplain EntityOption#getKind() entity state message}.
     * </ul>
     *
     * @return {@code true} if the field is an entity ID, {@code false} otherwise
     */
    public boolean isEntityId() {
        return isFirstField() && matchesIdName() && isEntityField();
    }

    /**
     * Determines whether the field is a command ID.
     *
     * <p>A command ID is the first field of a message declared in a
     * {@link io.spine.base.CommandMessage.File command file}.
     *
     * @return {@code true} if the field is a command ID, {@code false} otherwise
     */
    public boolean isCommandId() {
        return isFirstField() && isCommandsFile();
    }

    /**
     * Tells if the field is of scalar type.
     */
    public boolean isScalar() {
        return ScalarType.isScalarType(field.toProto());
    }

    /**
     * Tells if the field is of enum type.
     */
    public boolean isEnum() {
        return field.getType() == ENUM;
    }

    public boolean isMessage() {
        return field.getType() == MESSAGE;
    }

    /**
     * Determines whether the declaration is a singular value.
     *
     * @return {@code true} if the declaration neither map nor repeated, {@code false} otherwise
     */
    public boolean isNotCollection() {
        return !isCollection();
    }

    /**
     * Determines whether the declaration is a collection of items.
     *
     * @return {@code true} if the declaration either map or repeated, {@code false} otherwise
     */
    public boolean isCollection() {
        return isMap() || isRepeated();
    }

    /**
     * Determines whether the field marked as {@code repeated}.
     *
     * <p>A map field is not considered repeated.
     *
     * @return {@code true} if the field is repeated, {@code false} otherwise
     */
    public boolean isRepeated() {
        return FieldTypes.isRepeated(field);
    }

    /**
     * Determines whether the field is a {@code map}.
     *
     * @return {@code true} if the field is a {@code map}, {@code false} otherwise
     */
    public boolean isMap() {
        return FieldTypes.isMap(field);
    }

    /** Obtains the Java type of the declaration. */
    public JavaType javaType() {
        return field.getJavaType();
    }

    /** Obtains the descriptor of the value of a map. */
    public FieldDeclaration valueDeclaration() {
        FieldDescriptor valueDescriptor = FieldTypes.valueDescriptor(field);
        return new FieldDeclaration(valueDescriptor);
    }

    private boolean isEntityField() {
        EntityOption entityOption = field.getContainingType()
                                         .getOptions()
                                         .getExtension(OptionsProto.entity);
        EntityOption.Kind entityKind = entityOption.getKind();
        return entityKind.getNumber() > 0;
    }

    private boolean matchesIdName() {
        String name = field.getName();
        return "id".equals(name) || name.endsWith("_id");
    }

    /**
     * Determines whether the field is the first within a declaration.
     *
     * <p>The first field is declared at the top of the containing message,
     * the last — at the bottom.
     *
     * @return {@code true} if the field is the first in the containing declaration,
     *         {@code false} otherwise
     */
    private boolean isFirstField() {
        return field.getIndex() == 0;
    }

    private boolean isCommandsFile() {
        FileDescriptor file = field.getFile();
        boolean commandsFile = CommandMessage.File.predicate()
                                                  .test(file);
        return commandsFile;
    }

    /**
     * Obtains comments going before the field.
     *
     * @return the leading field comments or {@code Optional.empty()} if there are no comments
     */
    public Optional<String> leadingComments() {
        return Optional.ofNullable(leadingComments);
    }
}
