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

import com.google.common.annotations.VisibleForTesting;
import com.google.protobuf.compiler.PluginProtos.CodeGeneratorResponse.File;
import io.spine.code.java.SourceFile;
import io.spine.tools.protoc.AbstractCompilerOutput;
import io.spine.type.MessageType;
import io.spine.type.Type;

import static java.lang.String.format;

/**
 * A {@link io.spine.tools.protoc.CompilerOutput CompilerOutput} item, which alters a generated
 * message class to implement a given interface.
 */
final class InsertionPoint extends AbstractCompilerOutput {

    @VisibleForTesting
    static final String INSERTION_POINT_IMPLEMENTS = "message_implements:%s";

    private InsertionPoint(File file) {
        super(file);
    }

    /**
     * Creates a new instance of {@code InsertionPoint}.
     *
     * @param type
     *         the type declaration of which should be altered
     * @param messageInterface
     *         the interface to implement
     * @return new instance of {@code InsertionPoint}
     */
    static InsertionPoint implementInterface(MessageType type, MessageInterface messageInterface) {
        File.Builder file = prepareFile(type);
        String insertionPoint = format(INSERTION_POINT_IMPLEMENTS, type.name());
        String content =
                messageInterface.name() + initGenericParams(messageInterface, type) + ',';
        File result = file.setInsertionPoint(insertionPoint)
                          .setContent(content)
                          .build();
        return new InsertionPoint(result);
    }

    private static File.Builder prepareFile(Type<?, ?> type) {
        String fileName = SourceFile.forType(type)
                                    .toString();
        // Protoc consumes only `/` path separators.
        String uriStyleName = fileName.replace('\\', '/');
        File.Builder srcFile = File.newBuilder()
                                   .setName(uriStyleName);
        return srcFile;
    }

    private static String initGenericParams(MessageInterface messageInterface,
                                            Type<?, ?> type) {
        MessageInterfaceParameters parameters = messageInterface.parameters();
        String result = parameters.getAsStringFor(type);
        return result;
    }
}
