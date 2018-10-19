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

package io.spine.validate;

import com.google.protobuf.DescriptorProtos.FieldOptions;
import com.google.protobuf.Descriptors.FieldDescriptor;
import com.google.protobuf.Descriptors.FileDescriptor;
import com.google.protobuf.GeneratedMessage.GeneratedExtension;
import io.spine.base.CommandMessage;
import io.spine.code.proto.Option;
import io.spine.option.EntityOption;
import io.spine.option.OptionsProto;

import java.util.Optional;

import static io.spine.validate.rule.ValidationRuleOptions.getOptionValue;

/**
 * Declaration of a Protobuf field.
 *
 * <p>The field can be declared in a message or enum.
 *
 * <p>Unlike {@link io.spine.code.proto.FieldDeclaration}, the class uses
 * {@link com.google.protobuf.Descriptors} instead of {@link com.google.protobuf.DescriptorProtos}.
 * The former descriptors provide a more powerful API.
 */
final class FieldDeclaration {

    private final FieldDescriptor field;
    private final FieldContext context;

    FieldDeclaration(FieldContext context) {
        this.field = context.getTarget();
        this.context = context;
    }

    /**
     * Obtains the desired option for the field.
     *
     * @param extension
     *         an extension key used to obtain an option
     * @param <T>
     *         the type of the option value
     */
    <T> Option<T> option(GeneratedExtension<FieldOptions, T> extension) {
        Optional<Option<T>> validationRuleOption = getOptionValue(context, extension);
        if (validationRuleOption.isPresent()) {
            return validationRuleOption.get();
        }

        Option<T> ownOption = Option.from(field, extension);
        return ownOption;
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
    boolean isEntityId() {
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
    boolean isCommandId() {
        return isFirstField() && isCommandsFile();
    }

    boolean isNotRepeatedOrMap() {
        return !isRepeated()
                && !field.isMapField();
    }

    boolean isRepeated() {
        return field.isRepeated();
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
        return name.equals("id") || name.endsWith("_id");
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
