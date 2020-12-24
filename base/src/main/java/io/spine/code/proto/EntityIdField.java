/*
 * Copyright 2020, TeamDev. All rights reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
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

import com.google.common.collect.ImmutableList;
import io.spine.type.MessageType;

import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Preconditions.checkNotNull;
import static com.google.common.base.Preconditions.checkState;
import static io.spine.code.proto.ColumnOption.isColumn;

/**
 * The first field of the Protobuf message marked with {@code (entity)} option, representing
 * its identifier.
 */
public final class EntityIdField {

    private final FieldDeclaration declaration;

    private EntityIdField(FieldDeclaration field) {
        this.declaration = field;
    }

    /**
     * Creates an instance of {@code EntityIdField} for the given message type.
     *
     * @param messageType the message type which is marked as {@code (entity)}
     * @return a new instance of {@code EntityIdField}
     */
    public static EntityIdField of(MessageType messageType) {
        checkNotNull(messageType);
        checkArgument(messageType.isEntityState(),
                      "`EntityIdField` expected an `EntityState` descendant, " +
                              "but got `%s`.", messageType.javaClassName());
        ImmutableList<FieldDeclaration> fields = messageType.fields();
        checkState(fields.size() > 0, "At least one field is expected to be declared " +
                "in the `EntityState` message of type `%s`.", messageType.javaClassName());

        FieldDeclaration declaration = fields.get(0);
        checkState(!isColumn(declaration), "`EntityIdField` must not be marked as `(column)`." +
                " Please check the declaration of `%s` type.", messageType.toProto().getName());
        return new EntityIdField(declaration);
    }

    /**
     * Returns the declaration of the field in scope of the Protobuf message.
     */
    public FieldDeclaration declaration() {
        return declaration;
    }

    /**
     * Returns the name of the ID field.
     */
    public FieldName name() {
        return declaration.name();
    }
}
