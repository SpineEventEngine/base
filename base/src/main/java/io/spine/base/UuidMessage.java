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

package io.spine.base;

import com.google.protobuf.Descriptors;
import com.google.protobuf.Message;
import io.spine.protobuf.Messages;

import java.util.List;

import static com.google.common.base.Preconditions.checkState;

/**
 * A message containing a single string field with the {@linkplain #FIELD_NAME name}.
 */
class UuidMessage<I extends Message> {

    private static final String FIELD_NAME = "uuid";

    private final Class<I> idClass;
    private final Descriptors.FieldDescriptor uuidField;

    private UuidMessage(Class<I> idClass, Descriptors.FieldDescriptor uuidField) {
        this.idClass = idClass;
        this.uuidField = uuidField;
    }

    /**
     * Creates a new instance by the specified class.
     *
     * @param idClass
     *         the class of the Protobuf message
     * @param <I>
     *         the type of the Protobuf message
     * @return a new instance
     */
    static <I extends Message> UuidMessage<I> of(Class<I> idClass) {
        Descriptors.Descriptor message = Messages.newInstance(idClass)
                                                 .getDescriptorForType();
        List<Descriptors.FieldDescriptor> fields = message.getFields();
        checkState(fields.size() == 1, "A UUID message should have a single field.");
        Descriptors.FieldDescriptor uuidField = fields.get(0);
        checkUuidField(uuidField);
        return new UuidMessage<>(idClass, uuidField);
    }

    /**
     * Generates an instance of the UUID message.
     *
     * @return a message instance with the initialized {@code uuid} field
     */
    @SuppressWarnings("unchecked" /* It is OK as the builder is obtained by the specified class. */)
    I generate() {
        Message initializedId = Messages.builderFor(idClass)
                                        .setField(uuidField, Identifier.newUuid())
                                        .build();
        return (I) initializedId;
    }

    private static void checkUuidField(Descriptors.FieldDescriptor field) {
        boolean nameMatches = field.getName()
                                   .equals(FIELD_NAME);
        boolean typeMatches = field.getType() == Descriptors.FieldDescriptor.Type.STRING;
        boolean isUuidField = nameMatches && typeMatches;
        checkState(isUuidField,
                   "A UUID message should have a single string field named %s.", FIELD_NAME);
    }
}
