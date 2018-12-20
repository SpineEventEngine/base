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

package io.spine.tools.protoc;

import com.google.protobuf.Message;
import io.spine.base.CommandMessage;
import io.spine.base.EventMessage;
import io.spine.base.MessageAcceptor;
import io.spine.base.RejectionMessage;
import io.spine.base.UniqueId;
import io.spine.code.proto.MessageDeclaration;

import java.util.Optional;

import static com.google.common.base.Preconditions.checkNotNull;
import static com.google.protobuf.DescriptorProtos.DescriptorProto;
import static com.google.protobuf.DescriptorProtos.FileDescriptorProto;
import static io.spine.tools.protoc.InsertionPoint.implementInterface;
import static java.util.Optional.empty;
import static java.util.stream.Stream.of;

/**
 * A built-in marked interface.
 *
 * <p>This interface marks special message types, such as unique IDs, events, commands, etc.
 */
public class BuiltInMarkerInterface implements MarkerInterface {

    private final Class<? extends Message> type;

    private BuiltInMarkerInterface(Class<? extends Message> type) {
        this.type = type;
    }

    private static BuiltInMarkerInterface from(Class<? extends Message> type) {
        checkNotNull(type);
        return new BuiltInMarkerInterface(type);
    }

    /**
     * Obtains the {@link CompilerOutput} item which generates Java sources for the given message
     * to implement a built-in marker interface.
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
        MessageDeclaration declaration = MessageDeclaration.create(message, file);
        Optional<Type> foundInterface =
                of(Type.values())
                        .filter(contract -> contract.matches(declaration))
                        .findFirst();
        if (!foundInterface.isPresent()) {
            return empty();
        }
        Type type = foundInterface.get();
        MarkerInterface markerInterface = from(type.interfaceClass);
        InsertionPoint insertionPoint = implementInterface(file, message, markerInterface);
        return Optional.of(insertionPoint);
    }

    @Override
    public String name() {
        return type.getName();
    }

    /**
     * The enumeration of built-in interfaces.
     */
    private enum Type {

        EVENT_MESSAGE(EventMessage.class, EventMessage.predicate()),
        COMMAND_MESSAGE(CommandMessage.class, CommandMessage.predicate()),
        REJECTION_MESSAGE(RejectionMessage.class, RejectionMessage.predicate()),

        UNIQUE_ID(UniqueId.class, UniqueId.predicate());

        private final Class<? extends Message> interfaceClass;
        private final MessageAcceptor acceptor;

        Type(Class<? extends Message> interfaceClass, MessageAcceptor acceptor) {
            this.interfaceClass = interfaceClass;
            this.acceptor = acceptor;
        }

        public boolean matches(MessageDeclaration declaration) {
            return acceptor.test(declaration);
        }
    }
}
