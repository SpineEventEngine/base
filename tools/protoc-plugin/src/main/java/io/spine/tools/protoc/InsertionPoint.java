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

import com.google.common.annotations.VisibleForTesting;
import com.google.protobuf.DescriptorProtos.DescriptorProto;
import com.google.protobuf.DescriptorProtos.FileDescriptorProto;
import com.google.protobuf.compiler.PluginProtos.CodeGeneratorResponse.File;
import io.spine.code.java.SourceFile;

import static io.spine.code.java.PackageName.delimiter;
import static java.lang.String.format;

/**
 * A {@link io.spine.tools.protoc.CompilerOutput CompilerOutput} item, which alters a generated
 * message class to implement a given marker interface.
 *
 * @author Dmytro Dashenkov
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
     * @param containingFile
     *         the file which contains the given {@code message}
     * @param message
     *         the message to mark with an interface
     * @param markerInterface
     *         the interface to implement
     * @return new instance of {@code InsertionPoint}
     */
    static InsertionPoint implementInterface(FileDescriptorProto containingFile,
                                             DescriptorProto message,
                                             MarkerInterface markerInterface) {
        File.Builder file = prepareFile(containingFile, message);
        String messageFqn = containingFile.getPackage() + delimiter() + message.getName();
        String insertionPoint = format(INSERTION_POINT_IMPLEMENTS, messageFqn);
        File result = file.setInsertionPoint(insertionPoint)
                          .setContent(markerInterface.name() + ',')
                          .build();
        return new InsertionPoint(result);
    }

    private static File.Builder prepareFile(FileDescriptorProto containingFile,
                                            DescriptorProto message) {
        String fileName = SourceFile.forMessage(message, containingFile)
                                    .toString();
        String uriStyleName = fileName.replace('\\', '/');
        File.Builder srcFile = File.newBuilder()
                                   .setName(uriStyleName);
        return srcFile;
    }
}
