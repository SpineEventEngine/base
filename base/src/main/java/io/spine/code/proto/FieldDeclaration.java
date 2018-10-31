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

import com.google.protobuf.Descriptors.FieldDescriptor;
import com.google.protobuf.Descriptors.FileDescriptor;
import io.spine.base.CommandMessage;
import io.spine.option.EntityOption;
import io.spine.option.OptionsProto;

import static com.google.common.base.Preconditions.checkNotNull;

/**
 * Declaration of a Protobuf field.
 *
 * <p>The field can be declared in a message or enum.
 *
 * <p>Unlike {@link io.spine.code.proto.FieldDeclarationProto}, the class uses
 * {@link com.google.protobuf.Descriptors} instead of {@link com.google.protobuf.DescriptorProtos}.
 * The former descriptors provide a more powerful API.
 */
public final class FieldDeclaration {

    private final FieldDescriptor field;

    public FieldDeclaration(FieldDescriptor field) {
        this.field = checkNotNull(field);
    }

    /**
     * Determines whether the field is an entity ID.
     *
     * <p>An entity ID satisfies the following conditions:
     * <ul>
     *     <li>Declared as the first field.</li>
     *     <li>Named {@code id} or the name ends with {@code _id}.</li>
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
     * Determines whether the declaration is a scalar type.
     *
     * @return {@code true} if the declaration neither map nor repeated, {@code false} otherwise
     */
    public boolean isScalar() {
        return !isMap() && !isRepeated();
    }

    /**
     * Determines whether the declaration is not a scalar type.
     *
     * @return {@code true} if the declaration either map or repeated, {@code false} otherwise
     */
    public boolean isNotScalar() {
        return isMap() || isRepeated();
    }

    public boolean isRepeated() {
        return FieldTypes.isRepeated(field);
    }

    public boolean isMap() {
        return FieldTypes.isMap(field);
    }

    /** The {@link FieldDescriptor.JavaType JavaType} of the declaration. */
    public FieldDescriptor.JavaType javaType() {
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

    private boolean isFirstField() {
        return field.getIndex() == 0;
    }

    private boolean isCommandsFile() {
        FileDescriptor file = field.getFile();
        boolean commandsFile = CommandMessage.File.predicate()
                                                  .test(file);
        return commandsFile;
    }
}
