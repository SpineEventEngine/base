/*
 * Copyright 2020, TeamDev. All rights reserved.
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

package io.spine.tools.protoc.iface;

import com.google.protobuf.compiler.PluginProtos.CodeGeneratorResponse.File;
import io.spine.tools.protoc.AbstractCompilerOutput;
import io.spine.tools.protoc.InsertionPoint;
import io.spine.tools.protoc.ProtocPluginFiles;
import io.spine.tools.protoc.TypeParameters;
import io.spine.type.MessageType;
import io.spine.type.Type;

/**
 * A {@link io.spine.tools.protoc.CompilerOutput CompilerOutput} item, which alters a generated
 * message class to implement a given interface.
 */
public final class MessageImplements extends AbstractCompilerOutput {

    private MessageImplements(File file) {
        super(file);
    }

    /**
     * Creates a new instance of {@code MessageImplements}.
     *
     * @param type
     *         the type declaration that should be altered
     * @param messageInterface
     *         the interface to implement
     * @return new instance of {@code MessageImplements}
     */
    public static MessageImplements implementInterface(MessageType type,
                                                       MessageInterface messageInterface) {
        String insertionPoint = InsertionPoint.message_implements.forType(type);
        String content = buildContent(type, messageInterface);
        File.Builder file = ProtocPluginFiles.prepareFile(type);
        File result = file.setInsertionPoint(insertionPoint)
                          .setContent(content)
                          .build();
        return new MessageImplements(result);
    }

    /**
     * Creates an {@code implement INTERFACE_NAME,} string.
     *
     * <p>It is assumed that any Protobuf message always implements at least its own parent
     * interface.
     */
    private static String buildContent(MessageType type, MessageInterface messageInterface) {
        String result = messageInterface.name() + initGenericParams(messageInterface, type) + ',';
        return result;
    }

    private static String initGenericParams(MessageInterface messageInterface,
                                            Type<?, ?> type) {
        TypeParameters parameters = messageInterface.parameters();
        String result = parameters.asStringFor(type);
        return result;
    }
}
