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
import static java.util.stream.Collectors.groupingBy;
import static java.util.stream.Collectors.reducing;

/**
 * An abstract base for the Protobuf code generator.
 *
 * <p>A generator consumes a {@link DescriptorProto DescriptorProto} for each message type and
 * optionally generates some Java code in response to it regarding {@linkplain FileDescriptorProto
 * its file}.
 *
 * <a name="contract"></a>
 * <p>Each message type is processed separately. As the result of processing, a generator may
 * produce instances of {@link File CodeGeneratorResponse.File}.
 *
 * <p>The {@code CodeGeneratorResponse.File} has three fields: {@code name}, {@code insertionPoint},
 * and {@code content}.
 *
 * <p>The {@code name} field represents the name of the file to generate. The name is relative to
 * the output directory and should not contain {@code ./} or {@code ../} prefixes.
 *
 * <p>The {@code content} field represents the code snippet to write into the file. This field is
 * required.
 *
 * <p>To make the {@code protoc} generate a new file from the scratch, the the generator should
 * produce {@code CodeGeneratorResponse.File} instance with the {@code name} and {@code content}
 * fields. The {@code insertionPoint} field is omitted in this case.
 *
 * <p>To extend an existing {@code protoc} plugin (e.g. built-in {@code java} plugin), use
 * {@code insertionPoint} field. The value of the field must correspond to an existing insertion
 * point declared by the extended plugin. The insertion points are declared in the generated code
 * as follows:
 * {@code @@protoc_insertion_point(NAME)}, where {@code NAME} is value to set into the field.
 *
 * <p>If the {@code insertionPoint} field is present, the {@code name} field must also be present.
 * The {@code content} field contains the value to insert into the insertion point is this case.
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
     * <p>The output {@linkplain File Files} may:
     * <ul>
     *     <li>contain the {@linkplain File#getInsertionPoint() insertion points};
     *     <li>be empty;
     *     <li>contain extra types to generate for the given message declaration.
     * </ul>
     *
     * <p>Note that this method may produce identical {@link File CodeGeneratorResponse.File}
     * instances (i.e. equal in terms of {@link Object#equals(Object) equals()} method), but should
     * not produce non-equal instances with the same value of
     * {@code CodeGeneratorResponse.File.name} field. Such entries cause {@code protoc} to fail
     * and should be filtered on the early stage.
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
     * <p>Each {@linkplain FileDescriptorProto .proto file} may cause none, one or many
     * generated {@link File CodeGeneratorResponse.File} instances.
     *
     * <p>Note: there are several preconditions for this method to run successfully:
     * <ul>
     *     <li>since Spine relies on 3rd version of Protobuf, the Proto compiler version should be
     *         {@code 3.*} or greater;
     *     <li>there must be at least one {@code .proto} file in the {@link CodeGeneratorRequest}.
     * </ul>
     *
     * @param request the compiler request
     * @return the response to the compiler
     * @see #processMessage(com.google.protobuf.DescriptorProtos.FileDescriptorProto,
     *                      com.google.protobuf.DescriptorProtos.DescriptorProto)
     *                     Javadoc for processMessage() for more detailed description
     */
    public final CodeGeneratorResponse process(CodeGeneratorRequest request) {
        checkNotNull(request);
        checkCompilerVersion(request);
        List<FileDescriptorProto> protoFiles = request.getProtoFileList();
        checkArgument(!protoFiles.isEmpty(), "No files to generate provided.");
        CodeGeneratorResponse response = process(protoFiles);
        return response;
    }

    /**
     * Ensures that the version of the Google Protobuf Compiler is 3.* or higher.
     */
    private void checkCompilerVersion(CodeGeneratorRequest request) {
        Version version = request.getCompilerVersion();
        checkArgument(version.getMajor() >= 3,
                      "Use protoc of version 3.* or higher to run %s",
                      getClass().getName());
    }

    /**
     * Processes all passed proto files.
     */
    private CodeGeneratorResponse process(Iterable<FileDescriptorProto> files) {
        Collection<File> generatedFiles = newHashSet();
        for (FileDescriptorProto file : files) {
            Collection<File> newFiles = generateForTypesIn(file);
            generatedFiles.addAll(newFiles);
        }
        Collection<File> mergedFiles = mergeInsertionPoints(generatedFiles);
        CodeGeneratorResponse response =
                CodeGeneratorResponse.newBuilder()
                                     .addAllFile(mergedFiles)
                                     .build();
        return response;
    }

    private static Collection<File> mergeInsertionPoints(Collection<File> allFiles) {
        File reducingSeed = File.getDefaultInstance();
        Collection<File> merged = allFiles
                .stream()
                .collect(groupingBy(File::getName,
                                    reducing(reducingSeed, SpineProtoGenerator::concatContent)))
                .values();
        return merged;
    }

    private static File concatContent(File left, File right) {
        return left.toBuilder()
                   .setContent(left.getContent() + right.getContent())
                   .build();
    }

    /**
     * Processes the passed proto file.
     */
    private Collection<File> generateForTypesIn(FileDescriptorProto file) {
        Collection<File> result = newHashSet();
        for (DescriptorProto message : file.getMessageTypeList()) {
            Collection<File> processedFile = processMessage(file, message);
            result.addAll(processedFile);
        }
        return result;
    }
}
