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

import com.google.common.base.Optional;
import com.google.common.base.Predicate;
import com.google.protobuf.DescriptorProtos.DescriptorProto;
import com.google.protobuf.DescriptorProtos.FileDescriptorProto;
import com.google.protobuf.DescriptorProtos.FileOptions;
import com.google.protobuf.DescriptorProtos.MessageOptions;
import com.google.protobuf.DescriptorProtos.UninterpretedOption;
import com.google.protobuf.DescriptorProtos.UninterpretedOption.NamePart;
import com.google.protobuf.compiler.PluginProtos.CodeGeneratorResponse.File;
import com.google.protobuf.compiler.PluginProtos.Version;
import io.spine.type.TypeName;

import javax.annotation.Nullable;
import java.util.Iterator;
import java.util.List;

import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Preconditions.checkNotNull;
import static com.google.common.collect.Iterables.any;
import static com.google.common.collect.Iterables.find;
import static com.google.protobuf.compiler.PluginProtos.CodeGeneratorRequest;
import static com.google.protobuf.compiler.PluginProtos.CodeGeneratorResponse;
import static java.lang.String.format;

/**
 * @author Dmytro Dashenkov
 */
public final class CodeGenerator {

    private static final String EVERY_IS_OPTION_NAME = null;
    private static final String IS_OPTION_NAME = null;

    private static final String INSERTION_POINT_IMPLEMENTS =
            "@@protoc_insertion_point(message_implements:%s)";

    private CodeGenerator() {
        // Prevent utility class instantiation.
    }

    public static CodeGeneratorResponse generate(CodeGeneratorRequest request) {
        checkNotNull(request);
        final Version protocVersion = request.getCompilerVersion();
        checkArgument(protocVersion.getMajor() >= 3,
                      "Use protoc of version 3.X.X or higher to generate the Spine sources.");
        final List<String> requestedFiles = request.getFileToGenerateList();
        checkArgument(!requestedFiles.isEmpty(), "No files to generate provided.");
        final CodeGeneratorResponse response = scan(request.getProtoFileList(), requestedFiles);
        return response;
    }

    private static CodeGeneratorResponse scan(Iterable<FileDescriptorProto> descriptors,
                                                        Iterable<String> filePaths) {
        final Iterator<FileDescriptorProto> descriptorsIterator = descriptors.iterator();
        for (Iterator<String> files = filePaths.iterator(); files.hasNext(); ) {
            final String filePath = files.next();
            final FileDescriptorProto descriptor = descriptorsIterator.next();
            final Optional<String> value = getEveryIs(descriptor);
            if (value.isPresent()) {
                // ...
            } else {
                // ...
            }
        }
    }

    private static File implementInterface(File srcFile,
                                           String interfaceTypeName,
                                           TypeName messageType) {
        final String insertionPoint = format(INSERTION_POINT_IMPLEMENTS, messageType);
        final File result = srcFile.toBuilder()
                                   .setInsertionPoint(insertionPoint)
                                   .setContent(interfaceTypeName + ',')
                                   .build();
        return result;
    }

    private static File carryOn(File srcFile, TypeName messageType) {
        final String insertionPoint = format(INSERTION_POINT_IMPLEMENTS, messageType);
        final File result = srcFile.toBuilder()
                                   .setInsertionPoint(insertionPoint)
                                   .build();
        return result;
    }

    private static String prepareInterfaceFqn() {

    }

    private static File prepareFile(String path) {
        return File.newBuilder()
                   .setName(path)

                   .build();
    }

    private static Optional<String> getEveryIs(FileDescriptorProto descriptor) {
        final FileOptions options = descriptor.getOptions();
        final List<UninterpretedOption> unknownOptions = options.getUninterpretedOptionList();
        return getOptionWithName(unknownOptions, EVERY_IS_OPTION_NAME);
    }

    private static Optional<String> getIs(DescriptorProto descriptor) {
        final MessageOptions options = descriptor.getOptions();
        final List<UninterpretedOption> unknownOptions = options.getUninterpretedOptionList();
        return getOptionWithName(unknownOptions, IS_OPTION_NAME);
    }

    private static Optional<String> getOptionWithName(Iterable<UninterpretedOption> unknownOptions,
                                                      String expectedName) {
        for (UninterpretedOption option : unknownOptions) {
            if (any(option.getNameList(), WithName.equalTo(expectedName))) {
                final String value = option.getIdentifierValue();
                return Optional.of(value);
            }
        }
        return Optional.absent();
    }

    private static class WithName implements Predicate<NamePart> {

        private final String name;

        private static Predicate<NamePart> equalTo(String name) {
            checkNotNull(name);
            return new WithName(name);
        }

        private WithName(String name) {
            this.name = name;
        }

        @Override
        public boolean apply(@Nullable NamePart input) {
            checkNotNull(input);
            final boolean matches = input.getNamePart()
                                         .equals(name);
            return matches;
        }
    }
}
