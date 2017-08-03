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

import javax.annotation.Nullable;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;

import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Preconditions.checkNotNull;
import static com.google.common.base.Strings.isNullOrEmpty;
import static com.google.common.collect.Iterables.any;
import static com.google.common.collect.Lists.newLinkedList;
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

    private static final String PACKAGE_DELIMITER = ".";

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
        final Collection<File> result = newLinkedList();
        final Iterator<FileDescriptorProto> descriptorsIterator = descriptors.iterator();
        for (final String filePath : filePaths) {
            final FileDescriptorProto descriptor = descriptorsIterator.next();
            final File file = prepareFile(filePath);
            final Optional<String> everyIsValue = getEveryIs(descriptor);
            if (everyIsValue.isPresent()) {
                result.addAll(collectMessages(descriptor, file, everyIsValue.get()));
            } else {
                result.addAll(scanMessages(descriptor, file));
            }
        }
        final CodeGeneratorResponse response = CodeGeneratorResponse.newBuilder()
                                                                    .addAllFile(result)
                                                                    .build();
        return response;
    }

    private static Collection<File> scanMessages(FileDescriptorProto file,
                                                 File srcFile) {
        final Collection<File> result = newLinkedList();
        for (DescriptorProto messageType : file.getMessageTypeList()) {
            final Optional<String> isValue = getIs(messageType);
            if (isValue.isPresent()) {
                final String interfaceName = prepareInterfaceFqn(isValue.get(), file);
                final File messageFile = implementInterface(srcFile,
                                                            interfaceName,
                                                            messageType.getName());
                result.add(messageFile);
            }
        }
        return result;
    }

    private static Collection<File> collectMessages(FileDescriptorProto descriptor,
                                                    File file,
                                                    String interfaceName) {
        final Collection<File> result = newLinkedList();
        for (DescriptorProto message : descriptor.getMessageTypeList()) {
            final String interfaceTypeName = prepareInterfaceFqn(interfaceName, descriptor);
            final File messageFile = implementInterface(file, interfaceTypeName, message.getName());
            result.add(messageFile);
        }
        return result;
    }

    private static File implementInterface(File srcFile,
                                           String interfaceTypeName,
                                           String messageTypeName) {
        final String insertionPoint = format(INSERTION_POINT_IMPLEMENTS, messageTypeName);
        final File result = srcFile.toBuilder()
                                   .setInsertionPoint(insertionPoint)
                                   .setContent(interfaceTypeName + ',')
                                   .build();
        return result;
    }

    private static String prepareInterfaceFqn(String optionValue, FileDescriptorProto srcFile) {
        checkNotNull(optionValue);
        final String interfaceFqn;
        if (optionValue.contains(PACKAGE_DELIMITER)) {
            interfaceFqn = optionValue;
        } else {
            String javaPackage = srcFile.getOptions()
                                        .getJavaPackage();
            if (isNullOrEmpty(javaPackage)) {
                javaPackage = srcFile.getPackage();
            }
            interfaceFqn = javaPackage + PACKAGE_DELIMITER + optionValue;
        }
        return interfaceFqn;
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
