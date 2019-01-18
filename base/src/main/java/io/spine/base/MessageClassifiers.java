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

import com.google.common.collect.ImmutableMap;
import io.spine.annotation.Internal;
import io.spine.code.proto.MessageType;

import static io.spine.util.Exceptions.newIllegalStateException;

/**
 * A provider of {@link MessageClassifier} instances.
 */
@Internal
public final class MessageClassifiers {

    private static final ImmutableMap<Class<?>, MessageClassifier> classifiers = ImmutableMap.of(
            CommandMessage.class, MessageType::isCommand,
            EventMessage.class, MessageType::isEvent,
            RejectionMessage.class, MessageType::isRejection,
            UuidValue.class, new UuidValueClassifier()
    );

    private MessageClassifiers() {
    }

    /**
     * Obtains a {@code MessageClassifier} for the given interface class.
     *
     * <p>Throws {@link IllegalStateException} if such class does not have a registered classifier.
     */
    public static MessageClassifier forInterface(Class<?> interfaceClass) {
        MessageClassifier classifier = classifiers.get(interfaceClass);
        if (classifier == null) {
            throw newIllegalStateException("There is no classifier for Message type %s",
                                           interfaceClass.getCanonicalName());
        }
        return classifier;
    }
}
