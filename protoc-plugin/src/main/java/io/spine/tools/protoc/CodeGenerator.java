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

import com.google.common.annotations.VisibleForTesting;
import com.google.common.base.Objects;
import com.google.common.base.Optional;
import com.google.protobuf.DescriptorProtos.DescriptorProto;
import com.google.protobuf.DescriptorProtos.FileDescriptorProto;
import com.google.protobuf.DescriptorProtos.FileOptions;
import com.google.protobuf.DescriptorProtos.MessageOptions;
import com.google.protobuf.GeneratedMessage.GeneratedExtension;
import com.google.protobuf.GeneratedMessageV3.ExtendableMessage;
import com.google.protobuf.compiler.PluginProtos.CodeGeneratorResponse.File;
import com.google.protobuf.compiler.PluginProtos.Version;

import java.nio.file.Path;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Preconditions.checkNotNull;
import static com.google.common.base.Strings.isNullOrEmpty;
import static com.google.common.collect.Lists.newLinkedList;
import static com.google.protobuf.compiler.PluginProtos.CodeGeneratorRequest;
import static com.google.protobuf.compiler.PluginProtos.CodeGeneratorResponse;
import static io.spine.option.OptionsProto.everyIs;
import static io.spine.option.OptionsProto.is;
import static java.lang.String.format;

/**
 * @author Dmytro Dashenkov
 */
public final class CodeGenerator {

    @VisibleForTesting
    static final String INSERTION_POINT_IMPLEMENTS = "message_implements:%s";
    private static final String PACKAGE_DELIMITER = ".";

    private final Set<InterfaceSpec> markerInterfaces = new HashSet<>();

    public CodeGeneratorResponse generate(CodeGeneratorRequest request, Path genDir) {
        checkNotNull(request);
        final Version protocVersion = request.getCompilerVersion();
        checkArgument(protocVersion.getMajor() >= 3,
                      "Use protoc of version 3.X.X or higher to generate the Spine sources.");
        final List<FileDescriptorProto> descriptors = request.getProtoFileList();
        checkArgument(!descriptors.isEmpty(), "No files to generate provided.");
        final CodeGeneratorResponse response = scan(descriptors);
        writeMarkerInterfaces(genDir);
        return response;
    }

    private CodeGeneratorResponse scan(Iterable<FileDescriptorProto> descriptors) {
        final Collection<File> result = newLinkedList();
        for (final FileDescriptorProto descriptor : descriptors) {
            result.addAll(scanFile(descriptor));
        }
        final CodeGeneratorResponse response = CodeGeneratorResponse.newBuilder()
                                                                    .addAllFile(result)
                                                                    .build();
        return response;
    }

    private Collection<File> scanFile(FileDescriptorProto descriptor) {
        final Collection<File> result = newLinkedList();
        final Optional<String> everyIsValue = getEveryIs(descriptor);
        if (everyIsValue.isPresent()) {
            result.addAll(collectMessages(descriptor, everyIsValue.get()));
        } else {
            result.addAll(scanMessages(descriptor));
        }
        return result;
    }

    private Collection<File> scanMessages(FileDescriptorProto file) {
        final Collection<File> result = newLinkedList();
        final String javaPackage = resolvePackage(file);
        for (DescriptorProto message : file.getMessageTypeList()) {
            final Optional<String> isValue = getIs(message);
            if (isValue.isPresent()) {
                final String fileName = file.getOptions().getJavaMultipleFiles()
                        ? message.getName()
                        : resolveName(file);
                final File.Builder srcFile = prepareFile(fileName, javaPackage);
                final String interfaceName = prepareInterfaceFqn(isValue.get(), file);
                final String messageFqn = file.getPackage() + PACKAGE_DELIMITER + message.getName();
                final File messageFile = implementInterface(srcFile,
                                                            interfaceName,
                                                            messageFqn);
                result.add(messageFile);
            }
        }
        return result;
    }

    private Collection<File> collectMessages(FileDescriptorProto file,
                                                    String interfaceName) {
        final Collection<File> result = newLinkedList();
        final String javaPackage = resolvePackage(file);
        for (DescriptorProto message : file.getMessageTypeList()) {
            final String fileName = file.getOptions().getJavaMultipleFiles()
                    ? message.getName()
                    : resolveName(file);
            final File.Builder srcFile = prepareFile(fileName, javaPackage);
            final String interfaceTypeName = prepareInterfaceFqn(interfaceName, file);
            final String messageFqn = file.getPackage() + PACKAGE_DELIMITER + message.getName();
            final File messageFile = implementInterface(srcFile, interfaceTypeName, messageFqn);
            result.add(messageFile);
        }
        return result;
    }

    private File implementInterface(File.Builder srcFile,
                                           String interfaceTypeName,
                                           String messageTypeName) {
        final String insertionPoint = format(INSERTION_POINT_IMPLEMENTS, messageTypeName);
        final File result = srcFile.setInsertionPoint(insertionPoint)
                                   .setContent(interfaceTypeName + ',')
                                   .build();
        return result;
    }

    private String prepareInterfaceFqn(String optionValue, FileDescriptorProto srcFile) {
        checkNotNull(optionValue);
        final String interfaceFqn;
        if (optionValue.contains(PACKAGE_DELIMITER)) {
            interfaceFqn = optionValue;
        } else {
            final String javaPackage = resolvePackage(srcFile);
            interfaceFqn = javaPackage + PACKAGE_DELIMITER + optionValue;
        }
        return interfaceFqn;
    }

    private String resolvePackage(FileDescriptorProto fileDescriptor) {
        String javaPackage = fileDescriptor.getOptions().getJavaPackage();
        if (isNullOrEmpty(javaPackage)) {
            javaPackage = fileDescriptor.getPackage();
        }
        return javaPackage;
    }

    private String resolveName(FileDescriptorProto fileDescriptor) {
        String name = fileDescriptor.getOptions().getJavaOuterClassname();
        if (isNullOrEmpty(name)) {
            name = fileDescriptor.getName();
        }
        return name;
    }

    private File.Builder prepareFile(String messageName, String javaPackage) {
        final String nameFqn = (javaPackage + PACKAGE_DELIMITER + messageName).replace('.', '/');
        final File.Builder srcFile = File.newBuilder()
                                         .setName(nameFqn);
        return srcFile;
    }

    private Optional<String> getEveryIs(FileDescriptorProto descriptor) {
        final FileOptions options = descriptor.getOptions();
        return getOption(options, everyIs);
    }

    private Optional<String> getIs(DescriptorProto descriptor) {
        final MessageOptions options = descriptor.getOptions();
        return getOption(options, is);
    }

    private <T extends ExtendableMessage<T>> Optional<String>
    getOption(T options, GeneratedExtension<T, String> extension) {
        final String value = options.getExtension(extension);
        if (isNullOrEmpty(value)) {
            return Optional.absent();
        } else {
            return Optional.of(value);
        }
    }

    private void writeMarkerInterfaces(Path genDir) {
        final MarkerInterfaceGenerator generator = new MarkerInterfaceGenerator(genDir.toFile());
        for (InterfaceSpec spec : markerInterfaces) {
            generator.generate(spec.getPackageName(), spec.getName());
        }
    }

    private static class InterfaceSpec {

        private final String packageName;
        private final String name;

        private InterfaceSpec(String packageName, String name) {
            this.packageName = packageName;
            this.name = name;
        }

        private static InterfaceSpec newInstance(String packageName, String name) {
            return new InterfaceSpec(packageName, name);
        }

        private static InterfaceSpec from(String fullName) {
            final int index = fullName.lastIndexOf(PACKAGE_DELIMITER);
            final String name = fullName.substring(index + 1);
            final String packageName = fullName.substring(0, index);
            return new InterfaceSpec(packageName, name);
        }

        public String getPackageName() {
            return packageName;
        }

        public String getName() {
            return name;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) {
                return true;
            }
            if (o == null || getClass() != o.getClass()) {
                return false;
            }
            InterfaceSpec that = (InterfaceSpec) o;
            return Objects.equal(getPackageName(), that.getPackageName()) &&
                    Objects.equal(getName(), that.getName());
        }

        @Override
        public int hashCode() {
            return Objects.hashCode(getPackageName(), getName());
        }
    }
}
