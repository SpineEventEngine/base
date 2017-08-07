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

import java.nio.file.Path;
import java.util.Set;

import static com.google.common.base.Strings.isNullOrEmpty;
import static com.google.common.collect.Sets.newHashSet;
import static io.spine.option.OptionsProto.everyIs;
import static io.spine.option.OptionsProto.is;
import static java.lang.String.format;

/**
 * @author Dmytro Dashenkov
 */
public class NarrowMessageInterfaceGenerator extends SpineProtoOptionProcessor {

    @VisibleForTesting
    static final String INSERTION_POINT_IMPLEMENTS = "message_implements:%s";
    private static final String PACKAGE_DELIMITER = ".";

    private final Set<MarkerInterfaceSpec> markerInterfaces = newHashSet();
    private final Path generatedSrcPath;

    public NarrowMessageInterfaceGenerator(Path generatedSrcPath) {
        this.generatedSrcPath = generatedSrcPath;
    }

    @Override
    protected Optional<File> processMessage(FileDescriptorProto file, DescriptorProto message) {
        final Optional<File> fromFileOption = scanFileOption(file, message);
        if (fromFileOption.isPresent()) {
            return fromFileOption;
        }
        final Optional<File> fromMsgOption = scanMsgOption(file, message);
        if (fromMsgOption.isPresent()) {
            return fromMsgOption;
        }
        return Optional.absent();
    }

    private Optional<File> scanFileOption(FileDescriptorProto file, DescriptorProto msg) {
        final Optional<String> everyIs = getEveryIs(file);
        if (everyIs.isPresent()) {
            final File resultingFile = generateFile(file, msg, everyIs.get());
            return Optional.of(resultingFile);
        } else {
            return Optional.absent();
        }
    }

    private Optional<File> scanMsgOption(FileDescriptorProto file, DescriptorProto msg) {
        final Optional<String> everyIs = getIs(msg);
        if (everyIs.isPresent()) {
            final File resultingFile = generateFile(file, msg, everyIs.get());
            return Optional.of(resultingFile);
        } else {
            return Optional.absent();
        }
    }

    private File generateFile(FileDescriptorProto file, DescriptorProto msg,
                                     String optionValue) {
        final String fileName = file.getOptions()
                                    .getJavaMultipleFiles()
                ? msg.getName()
                : resolveName(file);
        final String javaPackage = resolvePackage(file);
        final File.Builder srcFile = prepareFile(fileName, javaPackage);
        final MarkerInterfaceSpec interfaceSpec = prepareInterfaceFqn(optionValue, file);
        final String messageFqn = file.getPackage() + PACKAGE_DELIMITER + msg.getName();
        final File messageFile = implementInterface(srcFile,
                                                    interfaceSpec.getFqn(),
                                                    messageFqn);
        markerInterfaces.add(interfaceSpec);
        return messageFile;
    }

    @Override
    protected void onProcessingFinished() {
        final MarkerInterfaceGenerator generator = new MarkerInterfaceGenerator(generatedSrcPath);
        for (MarkerInterfaceSpec spec : markerInterfaces) {
            generator.generate(spec.getPackageName(), spec.getName());
        }
    }

    private static Optional<String> getEveryIs(FileDescriptorProto descriptor) {
        final FileOptions options = descriptor.getOptions();
        return getOption(options, everyIs);
    }

    private static Optional<String> getIs(DescriptorProto descriptor) {
        final MessageOptions options = descriptor.getOptions();
        return getOption(options, is);
    }

    private static <T extends ExtendableMessage<T>> Optional<String>
    getOption(T options, GeneratedExtension<T, String> extension) {
        final String value = options.getExtension(extension);
        if (isNullOrEmpty(value)) {
            return Optional.absent();
        } else {
            return Optional.of(value);
        }
    }

    private static File implementInterface(File.Builder srcFile,
                                           String interfaceTypeName,
                                           String messageTypeName) {
        final String insertionPoint = format(INSERTION_POINT_IMPLEMENTS, messageTypeName);
        final File result = srcFile.setInsertionPoint(insertionPoint)
                                   .setContent(interfaceTypeName + ',')
                                   .build();
        return result;
    }

    private static MarkerInterfaceSpec prepareInterfaceFqn(String optionValue,
                                                           FileDescriptorProto srcFile) {
        final MarkerInterfaceSpec spec;
        if (optionValue.contains(PACKAGE_DELIMITER)) {
            spec = MarkerInterfaceSpec.from(optionValue);
        } else {
            final String javaPackage = resolvePackage(srcFile);
            spec = MarkerInterfaceSpec.newInstance(javaPackage, optionValue);
        }
        return spec;
    }

    private static String resolvePackage(FileDescriptorProto fileDescriptor) {
        String javaPackage = fileDescriptor.getOptions()
                                           .getJavaPackage();
        if (isNullOrEmpty(javaPackage)) {
            javaPackage = fileDescriptor.getPackage();
        }
        return javaPackage;
    }

    private static String resolveName(FileDescriptorProto fileDescriptor) {
        String name = fileDescriptor.getOptions()
                                    .getJavaOuterClassname();
        if (isNullOrEmpty(name)) {
            name = fileDescriptor.getName();
        }
        return name;
    }

    private static File.Builder prepareFile(String messageName, String javaPackage) {
        final String nameFqn = (javaPackage + PACKAGE_DELIMITER + messageName).replace('.', '/');
        final File.Builder srcFile = File.newBuilder()
                                         .setName(nameFqn);
        return srcFile;
    }

    private static class MarkerInterfaceSpec {

        private final String packageName;
        private final String name;

        private MarkerInterfaceSpec(String packageName, String name) {
            this.packageName = packageName;
            this.name = name;
        }

        private static MarkerInterfaceSpec newInstance(String packageName, String name) {
            return new MarkerInterfaceSpec(packageName, name);
        }

        private static MarkerInterfaceSpec from(String fullName) {
            final int index = fullName.lastIndexOf(PACKAGE_DELIMITER);
            final String name = fullName.substring(index + 1);
            final String packageName = fullName.substring(0, index);
            return new MarkerInterfaceSpec(packageName, name);
        }

        public String getPackageName() {
            return packageName;
        }

        public String getName() {
            return name;
        }

        public String getFqn() {
            return packageName + PACKAGE_DELIMITER + name;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) {
                return true;
            }
            if (o == null || getClass() != o.getClass()) {
                return false;
            }
            MarkerInterfaceSpec that = (MarkerInterfaceSpec) o;
            return Objects.equal(getPackageName(), that.getPackageName()) &&
                    Objects.equal(getName(), that.getName());
        }

        @Override
        public int hashCode() {
            return Objects.hashCode(getPackageName(), getName());
        }

        @Override
        public String toString() {
            return getFqn();
        }
    }
}
