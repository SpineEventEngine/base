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

package io.spine.code.proto;

import com.google.common.base.Objects;
import com.google.errorprone.annotations.Immutable;
import com.google.protobuf.Any;
import com.google.protobuf.DescriptorProtos.FieldDescriptorProto;
import com.google.protobuf.Descriptors.Descriptor;
import com.google.protobuf.Descriptors.FieldDescriptor;
import com.google.protobuf.Descriptors.FieldDescriptor.JavaType;
import com.google.protobuf.Descriptors.FileDescriptor;
import com.google.protobuf.Message;
import io.spine.annotation.Internal;
import io.spine.base.MessageFile;
import io.spine.code.java.ClassName;
import io.spine.option.EntityOption;
import io.spine.option.OptionsProto;
import io.spine.protobuf.Messages;
import io.spine.type.EnumType;
import io.spine.type.KnownTypes;
import io.spine.type.MessageType;
import io.spine.type.TypeName;
import io.spine.type.TypeUrl;
import io.spine.type.UnknownTypeException;

import java.util.Optional;

import static com.google.common.base.Preconditions.checkNotNull;
import static com.google.common.base.Preconditions.checkState;
import static com.google.protobuf.DescriptorProtos.DescriptorProto.FIELD_FIELD_NUMBER;
import static com.google.protobuf.Descriptors.FieldDescriptor.Type.ENUM;
import static com.google.protobuf.Descriptors.FieldDescriptor.Type.MESSAGE;
import static com.google.protobuf.Descriptors.FieldDescriptor.Type.STRING;
import static io.spine.util.Exceptions.newIllegalStateException;
import static java.lang.String.format;

/**
 * Declaration of a Protobuf message field.
 */
@Immutable
@SuppressWarnings("ClassWithTooManyMethods") // OK as isSomething() methods are mutually exclusive.
public final class FieldDeclaration {

    private final MessageType declaringMessage;
    private final FieldDescriptor field;

    /**
     * Creates a new instance.
     *
     * @param field
     *         the descriptor of a field
     */
    public FieldDeclaration(FieldDescriptor field) {
        this.field = checkNotNull(field);
        this.declaringMessage = new MessageType(field.getContainingType());
    }

    /**
     * Creates a new instance which potentially can have leading comments.
     */
    public FieldDeclaration(FieldDescriptor field, MessageType message) {
        this.field = checkNotNull(field);
        this.declaringMessage = checkNotNull(message);
    }

    /**
     * Obtains the name of the field.
     */
    public FieldName name() {
        return FieldName.of(field.toProto());
    }

    /**
     * Obtains the Protobuf field number.
     */
    public int number() {
        return field.getNumber();
    }

    /**
     * Obtains descriptor of the field.
     */
    public FieldDescriptor descriptor() {
        return field;
    }

    /**
     * Obtains the declaring message type if known.
     */
    public MessageType declaringType() {
        return declaringMessage;
    }

    /**
     * Checks if the given value is the default value for this field.
     *
     * @param fieldValue
     *         the value of the field
     * @return {@code true} if the given value is default for this field, {@code false} otherwise
     */
    public boolean isDefault(Object fieldValue) {
        checkNotNull(fieldValue);
        if (isMessage()) {
            if (fieldValue instanceof Message) {
                Message message = (Message) fieldValue;
                return Messages.isDefault(message) && sameMessageType(message);
            } else {
                return false;
            }
        } else {
            return fieldValue.equals(field.getDefaultValue());
        }
    }

    private boolean sameMessageType(Message msg) {
        String messageClassName = msg.getClass()
                                     .getName();
        String fieldClassName = messageClassName();
        return fieldClassName.equals(messageClassName);
    }

    /**
     * Obtains fully-qualified canonical name of the Java class that corresponds to the declared
     * type of the field.
     *
     * <p>If the field is {@code repeated}, obtains the name of the elements.
     *
     * <p>If the field is a {@code map}, obtains the name of the values.
     */
    public String javaTypeName() {
        return isMap()
               ? javaTypeName(valueDeclaration().field)
               : javaTypeName(this.field);
    }

    private static String javaTypeName(FieldDescriptor field) {
        FieldDescriptor.Type fieldType = field.getType();
        if (fieldType == MESSAGE) {
            MessageType messageType =
                    new MessageType(field.getMessageType());
            return messageType.javaClassName()
                              .canonicalName();
        }

        if (fieldType == ENUM) {
            EnumType enumType = EnumType.create(field.getEnumType());
            return enumType.javaClassName()
                           .canonicalName();
        }

        return ScalarType.javaTypeName(field.toProto().getType());
    }

    private String messageClassName() {
        TypeName typeName = TypeName.from(field.getMessageType());
        KnownTypes knownTypes = KnownTypes.instance();
        try {
            TypeUrl fieldType = typeName.toUrl();
            ClassName className = knownTypes.classNameOf(fieldType);
            return className.value();
        } catch (UnknownTypeException e) {
            String allTypeUrls = knownTypes.printAllTypes();
            throw newIllegalStateException(
                    e,
                    "Cannot find a type %s in the list of known types:%n%s", typeName, allTypeUrls
            );
        }
    }

    /**
     * Determines whether the field is an entity ID.
     *
     * <p>An entity ID satisfies the following conditions:
     * <ul>
     *     <li>Declared as the first field.
     *     <li>Declared inside an {@linkplain EntityOption#getKind() entity state message}.
     *     <li>Is not a map or a repeated field.
     * </ul>
     *
     * @return {@code true} if the field is an entity ID, {@code false} otherwise
     * @see #isId()
     */
    public boolean isEntityId() {
        return isFirstField() && isEntityField() && isNotCollection();
    }

    /**
     * Determines whether the field is an ID.
     *
     * <p>An ID satisfies the following conditions:
     * <ul>
     *     <li>Declared as the first field.
     *     <li>Declared inside an {@linkplain EntityOption#getKind() entity state message} or
     *         a {@linkplain io.spine.base.CommandMessage command message};
     *     <li>Is not a map or a repeated field.
     * </ul>
     *
     * @return {@code true} if the field is an entity ID, {@code false} otherwise
     */
    public boolean isId() {
        boolean fieldMatches = isFirstField() && isNotCollection();
        return fieldMatches && (isCommandsFile() || isEntityField());
    }

    /**
     * Tells if the field is of scalar type.
     */
    public boolean isScalar() {
        return ScalarType.isScalarType(field.toProto());
    }

    /**
     * Tells if the field is of {@code string} type.
     */
    public boolean isString() {
        return field.getType() == STRING;
    }

    /**
     * Tells if the field is of an enum type.
     */
    public boolean isEnum() {
        return field.getType() == ENUM;
    }

    /**
     * Tells if the field is of a message type.
     */
    public boolean isMessage() {
        return field.getType() == MESSAGE;
    }

    /**
     * Tells if the field is of type {@code google.protobuf.Any}.
     */
    public boolean isAny() {
        return isMessage() && field.getMessageType()
                                   .getFullName()
                                   .equals(Any.getDescriptor().getFullName());
    }

    /**
     * Tells if the field is a singular field of message type.
     */
    public boolean isSingularMessage() {
        return isMessage() && isNotCollection();
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

    /**
     * Returns the message type of the field.
     *
     * @throws IllegalStateException
     *         if the field is of non-{@link Message} type
     */
    @Internal
    public MessageType messageType() {
        checkState(isMessage());
        Descriptor messageType = descriptor().getMessageType();
        return new MessageType(messageType);
    }

    /** Obtains the descriptor of the value of a map. */
    public FieldDeclaration valueDeclaration() {
        FieldDescriptor valueDescriptor = FieldTypes.valueDescriptor(field);
        return new FieldDeclaration(valueDescriptor);
    }

    private boolean isEntityField() {
        EntityOption entityOption =
                field.getContainingType()
                     .getOptions()
                     .getExtension(OptionsProto.entity);
        EntityOption.Kind entityKind = entityOption.getKind();
        return entityKind.getNumber() > 0;
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
        boolean result = MessageFile.COMMANDS.test(file.toProto());
        return result;
    }

    /**
     * Returns the name of the getter generated by the Protobuf Java plugin for the field.
     */
    public String javaGetterName() {
        String camelCasedName = name().toCamelCase();
        String result = format("get%s", camelCasedName);
        return result;
    }

    /**
     * Obtains comments going before the field.
     *
     * @return the leading field comments or {@code Optional.empty()} if there are no comments
     */
    public Optional<String> leadingComments() {
        LocationPath fieldPath = fieldPath();
        return declaringMessage.leadingComments(fieldPath);
    }

    /**
     * Returns the path to the field inside a message declaration.
     *
     * <p>Protobuf extensions are not supported.
     *
     * @return the field location path
     */
    private LocationPath fieldPath() {
        LocationPath locationPath = new LocationPath();
        locationPath.addAll(declaringMessage.path());
        locationPath.add(FIELD_FIELD_NUMBER);
        int fieldIndex = fieldIndex();
        locationPath.add(fieldIndex);
        return locationPath;
    }

    private int fieldIndex() {
        FieldDescriptorProto proto = this.field.toProto();
        return declaringMessage.descriptor()
                               .toProto()
                               .getFieldList()
                               .indexOf(proto);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof FieldDeclaration)) {
            return false;
        }
        FieldDeclaration that = (FieldDeclaration) o;
        return Objects.equal(declaringMessage, that.declaringMessage) &&
                Objects.equal(field.getFullName(), that.field.getFullName());
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(declaringMessage, field.getFullName());
    }

    /**
     * Obtains qualified name of this field.
     *
     * <p>Example: {@code spine.net.Uri.protocol}.
     */
    @Override
    public String toString() {
        return field.getFullName();
    }
}
