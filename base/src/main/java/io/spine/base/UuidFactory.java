/*
 * Copyright 2019, TeamDev. All rights reserved.
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

import com.google.protobuf.Descriptors.Descriptor;
import com.google.protobuf.Descriptors.FieldDescriptor;
import com.google.protobuf.Internal;
import com.google.protobuf.Message;
import io.spine.protobuf.Messages;

import java.util.List;
import java.util.UUID;

import static com.google.common.base.Preconditions.checkNotNull;
import static com.google.common.base.Preconditions.checkState;
import static io.spine.base.UuidValueClassifier.FIELD_NAME;
import static io.spine.protobuf.Messages.defaultInstance;
import static io.spine.util.Exceptions.newIllegalArgumentException;
import static io.spine.util.Preconditions2.checkNotEmptyOrBlank;

/**
 * A factory for creating UUID-based identifiers of the given {@link Message} type.
 *
 * <p>The passed message type must contain a single {@code string} field named 'uuid'.
 *
 * @param <I>
 *         the type of created messages
 */
final class UuidFactory<I extends Message> {

    private static final String ERROR_MESSAGE =
            "A UUID message should have a single string field named %s.";
    private static final String INVALID_STRING_MESSAGE = "Invalid UUID string: %s";

    private final Class<I> idClass;
    private final FieldDescriptor uuidField;

    private UuidFactory(Class<I> idClass, FieldDescriptor uuidField) {
        this.idClass = idClass;
        this.uuidField = uuidField;
    }

    /**
     * Creates a new factory for the specified {@code Message} class.
     *
     * @param idClass
     *         the class of the Protobuf message
     * @param <I>
     *         the type of the Protobuf message
     * @return a new factory instance
     * @throws IllegalStateException
     *         if the passed ID class does not obey {@link UuidValue} contract
     */
    static <I extends Message> UuidFactory<I> forClass(Class<I> idClass) {
        checkNotNull(idClass);
        Descriptor message = defaultInstance(idClass).getDescriptorForType();
        checkState(isUuidMessage(message), ERROR_MESSAGE, FIELD_NAME);
        List<FieldDescriptor> fields = message.getFields();
        FieldDescriptor uuidField = fields.get(0);
        return new UuidFactory<>(idClass, uuidField);
    }

    /**
     * Generates an instance of the UUID message using a random string.
     *
     * @return a message instance with the initialized {@code uuid} field
     */
    I newUuid() {
        return newUuidOf(Identifier.newUuid());
    }

    /**
     * Creates an instance of the UUID message from the passed value.
     *
     * @param value
     *         a value to use
     * @return a new message instance with the {@code uuid} field initialized to the given value
     */
    @SuppressWarnings("unchecked") // It is OK as the builder is obtained by the specified class.
    I newUuidOf(String value) {
        checkIsUuid(value);
        Message initializedId = Messages
                .builderFor(idClass)
                .setField(uuidField, value)
                .build();
        return (I) initializedId;
    }

    private static boolean isUuidMessage(Descriptor message) {
        return new UuidValueClassifier().test(message);
    }

    /**
     * Checks that the passed value is a UUID-based string.
     *
     * <p>The check utilizes the Standard Java {@code UUID}
     * {@linkplain java.util.UUID#fromString(String) check on construction}.
     *
     * @throws IllegalArgumentException
     *         if the passed value is not a valid UUID string
     */
    @SuppressWarnings({"CheckReturnValue", "ResultOfMethodCallIgnored"})
    // Just verify that the object is constructed without errors.
    private static void checkIsUuid(String value) {
        checkNotEmptyOrBlank(value);
        try {
            UUID.fromString(value);
        } catch (NumberFormatException e) {
            throw newIllegalArgumentException(e, INVALID_STRING_MESSAGE, value);
        }
    }
}
