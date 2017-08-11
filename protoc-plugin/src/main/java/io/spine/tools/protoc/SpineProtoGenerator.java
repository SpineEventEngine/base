/*
 * Copyright 2017, TeamDev Ltd. All rights reserved.
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

import com.google.protobuf.DescriptorProtos.DescriptorProto;
import com.google.protobuf.DescriptorProtos.FileDescriptorProto;
import com.google.protobuf.compiler.PluginProtos.CodeGeneratorRequest;
import com.google.protobuf.compiler.PluginProtos.CodeGeneratorResponse;
import com.google.protobuf.compiler.PluginProtos.CodeGeneratorResponse.File;
import com.google.protobuf.compiler.PluginProtos.Version;

import java.util.Collection;
import java.util.List;

import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Preconditions.checkNotNull;
import static com.google.common.collect.Sets.newHashSet;

/**
 * An abstract base for the Protobuf code generator.
 *
 * <p>A generator takes a {@link DescriptorProto} for a message and optionally generates some Java
 * code in response for it.
 *
 * @author Dmytro Dashenkov
 */
public abstract class SpineProtoGenerator {

    protected SpineProtoGenerator() {
        super();
    }

    /**
     * Processes a single message type and generates from zero to many {@link File} instances in
     * response to the message type.
     *
     * <p>The output {@linkplain File Files} may contain
     * the {@linkplain File#getInsertionPoint() insertion points}.
     *
     * <p>The output {@link Collection} may be empty.
     *
     * <p>The output {@link Collection} may contain extra types to generate for the given message
     * declaration.
     *
     * <p>If this method produces duplicate entries over a single or multiple invocations,
     * the duplication will be excluded by the rules of {@link java.util.Set}. Though,
     * the implementor should take care of excluding the duplicate entries which cannot be
     * identified by the standard means. Such duplicates are e.g. the {@link File} instances that
     * are not equal in terms of {@link Object#equals equals()} method, but target the same file to
     * be generated (excluding the {@linkplain File files} which target insertion points).
     *
     * <p>Please see <a href="https://developers.google.com/protocol-buffers/docs/reference/cpp/google.protobuf.compiler.plugin.pb">
     * the Google documentation page</a> for more detained description of what a {@link File}
     * instance should and should not contain.
     *
     * @param file    the message type enclosing file
     * @param message the message type to process
     * @return optionally a {@link Collection} of {@linkplain File Files} to generate or an empty
     * {@code Collection}
     */
    protected abstract Collection<File> processMessage(FileDescriptorProto file,
                                                       DescriptorProto message);

    /**
     * Processes the given compiler request and generates the response to the compiler.
     *
     * @param request the compiler request
     * @return the response to the compiler
     * @see #processMessage(FileDescriptorProto, DescriptorProto) for more detaineed behavior
     *                                                            description
     */
    public final CodeGeneratorResponse process(CodeGeneratorRequest request) {
        checkNotNull(request);
        checkNotNull(request);
        final Version protocVersion = request.getCompilerVersion();
        checkArgument(protocVersion.getMajor() >= 3,
                      "Use protoc of version 3.X.X or higher to generate the Spine sources.");
        final List<FileDescriptorProto> descriptors = request.getProtoFileList();
        checkArgument(!descriptors.isEmpty(), "No files to generate provided.");
        final CodeGeneratorResponse response = scan(descriptors);
        return response;
    }

    private CodeGeneratorResponse scan(Iterable<FileDescriptorProto> files) {
        final Collection<File> generatedFiles = newHashSet();
        for (FileDescriptorProto file : files) {
            generatedFiles.addAll(scanFile(file));
        }
        final CodeGeneratorResponse response = CodeGeneratorResponse.newBuilder()
                                                                    .addAllFile(generatedFiles)
                                                                    .build();
        return response;
    }

    private Collection<File> scanFile(FileDescriptorProto file) {
        final Collection<File> result = newHashSet();
        for (DescriptorProto message : file.getMessageTypeList()) {
            final Collection<File> processedFile = processMessage(file, message);
            result.addAll(processedFile);
        }
        return result;
    }
}
