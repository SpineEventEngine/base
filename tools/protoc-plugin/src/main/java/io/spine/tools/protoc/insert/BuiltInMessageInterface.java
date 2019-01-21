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

package io.spine.tools.protoc.insert;

import com.google.protobuf.Message;
import io.spine.base.CommandMessage;
import io.spine.base.EventMessage;
import io.spine.base.MessageClassifier;
import io.spine.base.RejectionMessage;
import io.spine.base.SerializableMessage;
import io.spine.base.UuidValue;
import io.spine.code.proto.MessageType;
import io.spine.tools.protoc.CompilerOutput;

import java.util.Optional;
import java.util.stream.Stream;

import static com.google.common.base.Preconditions.checkNotNull;
import static io.spine.base.MessageClassifiers.forInterface;
import static io.spine.tools.protoc.insert.InsertionPoint.implementInterface;

/**
 * A built-in message interface.
 *
 * <p>Such interfaces mark special message types, such as unique IDs, events, commands, and are
 * assigned to messages automatically.
 */
final class BuiltInMessageInterface implements MessageInterface {

    private final Class<? extends Message> type;
    private final MessageInterfaceParameters genericParams;

    private BuiltInMessageInterface(Class<? extends Message> type,
                                    MessageInterfaceParameters genericParams) {
        this.type = type;
        this.genericParams = genericParams;
    }

    private static BuiltInMessageInterface from(BuiltIn builtIn) {
        checkNotNull(builtIn);
        return new BuiltInMessageInterface(builtIn.interfaceClass, builtIn.interfaceParams);
    }

    /**
     * Obtains the {@link CompilerOutput} item which generates Java sources for the given message
     * to implement a built-in interface.
     *
     * @param type
     *         the Proto type to check
     * @return the compiler output item or {@link Optional#empty()} if the given message should not
     *         implement a built-in interface
     */
    static Optional<CompilerOutput> scanForBuiltIns(MessageType type) {
        Optional<BuiltIn> foundInterface =
                Stream.of(BuiltIn.values())
                      .filter(contract -> contract.matches(type))
                      .findFirst();
        return foundInterface.map(builtIn -> implementInterface(type, from(builtIn)));
    }

    @Override
    public String name() {
        return type.getName();
    }

    @Override
    public MessageInterfaceParameters parameters() {
        return genericParams;
    }

    /**
     * The enumeration of built-in interfaces.
     */
    @SuppressWarnings("NonSerializableFieldInSerializableClass") // OK for this enum.
    private enum BuiltIn {

        EVENT_MESSAGE(EventMessage.class),
        COMMAND_MESSAGE(CommandMessage.class),
        REJECTION_MESSAGE(RejectionMessage.class),

        UUID_VALUE(UuidValue.class, new IdentityParameter());

        private final Class<? extends Message> interfaceClass;
        private final MessageClassifier classifier;
        private final MessageInterfaceParameters interfaceParams;

        BuiltIn(Class<? extends SerializableMessage> interfaceClass,
                MessageInterfaceParameter... interfaceParams) {
            this.interfaceClass = interfaceClass;
            this.classifier = forInterface(interfaceClass);
            this.interfaceParams = MessageInterfaceParameters.of(interfaceParams);
        }

        /**
         * Checks if a given type declaration matches the contract of this interface.
         *
         * @param type
         *         the type to check
         * @return {@code true} if the type declaration matches the interface contract,
         *         {@code false} otherwise
         */
        public boolean matches(MessageType type) {
            return classifier.test(type);
        }
    }
}
