/*
 * Copyright 2020, TeamDev. All rights reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
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
import io.spine.code.proto.FileName;
import io.spine.code.proto.FileSet;
import io.spine.code.proto.TypeSet;
import io.spine.tools.type.MoreKnownTypes;
import io.spine.type.Type;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Preconditions.checkNotNull;
import static com.google.common.collect.Lists.newArrayListWithExpectedSize;
import static java.util.stream.Collectors.groupingBy;
import static java.util.stream.Collectors.partitioningBy;
import static java.util.stream.Collectors.reducing;
import static java.util.stream.Collectors.toList;
import static java.util.stream.Collectors.toSet;

/**
 * An abstract base for the Protobuf to Java code generator.
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
 */
public abstract class CodeGenerator {

    protected CodeGenerator() {
    }

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
     * @param request
     *         the compiler request
     * @return the response to the compiler
     * @see #generate Javadoc for generate(...) for more detailed description
     */
    public final CodeGeneratorResponse process(CodeGeneratorRequest request) {
        checkNotNull(request);
        checkCompilerVersion(request);
        checkNotEmpty(request);
        FileSet fileSet = FileSet.of(request.getProtoFileList());
        MoreKnownTypes.extendWith(fileSet);
        Set<FileName> requestedFileNames = request.getFileToGenerateList()
                                                  .stream()
                                                  .map(FileName::of)
                                                  .collect(toSet());
        FileSet requestedFiles = fileSet.find(requestedFileNames);
        TypeSet typeSet = TypeSet.from(requestedFiles);
        CodeGeneratorResponse response = process(typeSet);
        return response;
    }

    /**
     * Processes a single type and generates from zero to many {@link CompilerOutput} instances in
     * response to the type.
     *
     * <p>The output {@linkplain File Files} may:
     * <ul>
     *     <li>contain the {@linkplain File#getInsertionPoint() insertion points};
     *     <li>be empty;
     *     <li>contain extra types to generate for the given message declaration.
     * </ul>
     *
     * @param type
     *         the Protobuf type to process
     * @return optionally a {@link Collection} of {@linkplain CompilerOutput CompilerOutputs}
     *         to write or an empty {@code Collection}
     * @apiNote This method may produce identical {@link CompilerOutput} instances (i.e.
     *         equal in terms of {@link Object#equals(Object) equals()} method), but should
     *         not produce non-equal instances with the same value of
     *         {@code CodeGeneratorResponse.File.name}. Such entries cause {@code protoc}
     *         to fail and should be filtered on an early stage.
     */
    protected abstract Collection<CompilerOutput> generate(Type<?, ?> type);

    private static void checkNotEmpty(CodeGeneratorRequest request)
            throws IllegalArgumentException {
        checkArgument(request.getFileToGenerateCount() > 0, "No files to generate provided.");
    }

    /**
     * Processes all passed proto files.
     */
    private CodeGeneratorResponse process(TypeSet types) {
        Set<CompilerOutput> rawOutput = generate(types);
        Collection<File> mergedFiles = mergeFiles(rawOutput);
        CodeGeneratorResponse response = CodeGeneratorResponse
                .newBuilder()
                .addAllFile(mergedFiles)
                .build();
        return response;
    }

    /**
     * Generates code for the supplied types.
     */
    private Set<CompilerOutput> generate(TypeSet types) {
        return types.allTypes()
                    .stream()
                    .map(this::generate)
                    .flatMap(Collection::stream)
                    .collect(toSet());

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

    private static Collection<File> mergeFiles(Collection<CompilerOutput> allFiles) {
        Map<Boolean, List<File>> partitionedFiles = allFiles
                .stream()
                .map(CompilerOutput::asFile)
                .collect(partitioningBy(File::hasInsertionPoint));
        Collection<File> insertionPoints = mergeInsertionPoints(partitionedFiles.get(true));
        Collection<File> completeFiles = partitionedFiles.get(false);

        Collection<File> merged = newArrayListWithExpectedSize(allFiles.size());
        merged.addAll(insertionPoints);
        merged.addAll(completeFiles);

        return merged;
    }

    private static Collection<File> mergeInsertionPoints(Collection<File> insertionPoints) {
        File emptyFile = File.getDefaultInstance();
        List<File> merged = insertionPoints
                .stream()
                .collect(groupingBy(File::getInsertionPoint,
                                    reducing(CodeGenerator::concatContent)))
                .values()
                .stream()
                .map(file -> file.orElse(emptyFile))
                .collect(toList());
        return merged;
    }

    private static File concatContent(File left, File right) {
        return left.toBuilder()
                   .setContent(left.getContent() + right.getContent())
                   .build();
    }
}
