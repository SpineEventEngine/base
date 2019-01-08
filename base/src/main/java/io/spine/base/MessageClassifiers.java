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

import io.spine.annotation.Internal;

import java.util.HashMap;
import java.util.Map;

import static io.spine.util.Exceptions.newIllegalStateException;

/**
 * A provider of {@link MessageClassifier} instances.
 */
@Internal
public final class MessageClassifiers {

    private static final Map<Class<?>, MessageClassifier> classifiers = classifiers();

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

    private static Map<Class<?>, MessageClassifier> classifiers() {
        Map<Class<?>, MessageClassifier> classifiers = new HashMap<>();

        classifiers.put(CommandMessage.class, new CommandMessageClassifier());
        classifiers.put(EventMessage.class, new EventMessageClassifier());
        classifiers.put(RejectionMessage.class, new RejectionMessageClassifier());
        classifiers.put(UuidValue.class, new UuidValueClassifier());

        return classifiers;
    }
}
