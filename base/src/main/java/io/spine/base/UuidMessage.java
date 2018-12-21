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

import com.google.protobuf.DescriptorProtos.DescriptorProto;
import com.google.protobuf.DescriptorProtos.FieldDescriptorProto;
import com.google.protobuf.Descriptors.Descriptor;
import com.google.protobuf.Descriptors.FieldDescriptor;
import com.google.protobuf.Message;
import io.spine.protobuf.Messages;

import java.util.List;

import static com.google.common.base.Preconditions.checkState;
import static com.google.protobuf.DescriptorProtos.FieldDescriptorProto.Type.TYPE_STRING;

/**
 * A message containing a single string field with the {@linkplain #FIELD_NAME name}.
 */
class UuidMessage<I extends Message> {

    private static final String FIELD_NAME = "uuid";

    private final Class<I> idClass;
    private final FieldDescriptor uuidField;

    private UuidMessage(Class<I> idClass, FieldDescriptor uuidField) {
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
        Descriptor message = Messages.newInstance(idClass)
                                     .getDescriptorForType();
        checkState(isUuidMessage(message),
                   "A UUID message should have a single string field named %s.", FIELD_NAME);
        List<FieldDescriptor> fields = message.getFields();
        FieldDescriptor uuidField = fields.get(0);
        return new UuidMessage<>(idClass, uuidField);
    }

    /**
     * Generates an instance of the UUID message using a random string.
     *
     * @return a message instance with the initialized {@code uuid} field
     */
    I generate() {
        return create(Identifier.newUuid());
    }

    /**
     * Generates an instance of the UUID message from the passed value.
     *
     * @param value
     *         a value to use
     * @return a new instance with the {@code uuid} field initialized to the given value
     */
    @SuppressWarnings("unchecked") // It is OK as the builder is obtained by the specified class.
    I create(String value) {
        Message initializedId = Messages.builderFor(idClass)
                                        .setField(uuidField, value)
                                        .build();
        return (I) initializedId;
    }

    /**
     * Checks if the given proto definition represents a {@code UuidMessage}.
     */
    static boolean isUuidMessage(DescriptorProto message) {
        int fieldCount = message.getFieldCount();
        if (fieldCount != 1) {
            return false;
        }
        FieldDescriptorProto theField = message.getFieldList()
                                               .get(0);
        boolean nameMatches = theField.getName()
                                      .equals(FIELD_NAME);
        boolean typeMatches = theField.getType() == TYPE_STRING;
        return nameMatches && typeMatches;
    }

    private static boolean isUuidMessage(Descriptor message) {
        return isUuidMessage(message.toProto());
    }
}
