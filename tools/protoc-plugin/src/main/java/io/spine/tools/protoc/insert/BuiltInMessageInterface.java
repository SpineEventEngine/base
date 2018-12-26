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

package io.spine.tools.protoc.insert;

import com.google.protobuf.Message;
import io.spine.base.CommandMessage;
import io.spine.base.EventMessage;
import io.spine.base.RejectionMessage;
import io.spine.base.UuidValue;
import io.spine.tools.protoc.CompilerOutput;

import java.util.Optional;
import java.util.function.BiPredicate;
import java.util.stream.Stream;

import static com.google.common.base.Preconditions.checkNotNull;
import static com.google.protobuf.DescriptorProtos.DescriptorProto;
import static com.google.protobuf.DescriptorProtos.FileDescriptorProto;
import static io.spine.tools.protoc.insert.InsertionPoint.implementInterface;
import static java.util.Optional.empty;

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

    private static BuiltInMessageInterface from(Type type) {
        checkNotNull(type);
        return new BuiltInMessageInterface(type.interfaceClass, type.interfaceParams);
    }

    /**
     * Obtains the {@link CompilerOutput} item which generates Java sources for the given message
     * to implement a built-in interface.
     *
     * @param file
     *         the file containing the given {@code message} definition
     * @param message
     *         the message type to check
     * @return the compiler output item or {@link Optional#empty()} if the given message should not
     *         implement a built-in interface
     */
    static Optional<CompilerOutput> scanForBuiltIns(FileDescriptorProto file,
                                                    DescriptorProto message) {
        Optional<Type> foundInterface =
                Stream.of(Type.values())
                      .filter(contract -> contract.matches(message, file))
                      .findFirst();
        if (!foundInterface.isPresent()) {
            return empty();
        }
        Type type = foundInterface.get();
        MessageInterface messageInterface = from(type);
        InsertionPoint insertionPoint = implementInterface(file, message, messageInterface);
        return Optional.of(insertionPoint);
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
    private enum Type {

        EVENT_MESSAGE(EventMessage.class, EventMessage.predicate()),
        COMMAND_MESSAGE(CommandMessage.class, CommandMessage.predicate()),
        REJECTION_MESSAGE(RejectionMessage.class, RejectionMessage.predicate()),

        UUID_VALUE(UuidValue.class, UuidValue.predicate(), new IdentityParameter());

        private final Class<? extends Message> interfaceClass;
        private final BiPredicate<DescriptorProto, FileDescriptorProto> predicate;
        private final MessageInterfaceParameters interfaceParams;

        Type(Class<? extends Message> interfaceClass,
             BiPredicate<DescriptorProto, FileDescriptorProto> predicate,
             MessageInterfaceParameter... interfaceParams) {
            this.interfaceClass = interfaceClass;
            this.predicate = predicate;
            this.interfaceParams = MessageInterfaceParameters.of(interfaceParams);
        }

        /**
         * Checks if a given message declaration matches the contract of this interface.
         *
         * @param message
         *         the descriptor of a message to check
         * @param file
         *         the descriptor of the message's declaring file
         * @return {@code true} if the message declaration matches the interface contract,
         *         {@code false} otherwise
         */
        public boolean matches(DescriptorProto message, FileDescriptorProto file) {
            return predicate.test(message, file);
        }
    }
}
