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
import com.google.protobuf.Extension;
import com.google.protobuf.GeneratedMessageV3.ExtendableMessage;
import com.google.protobuf.compiler.PluginProtos.CodeGeneratorResponse.File;
import com.squareup.javapoet.JavaFile;
import io.spine.option.UnknownOptions;

import javax.annotation.Nullable;
import java.util.Collection;
import java.util.Set;

import static com.google.common.base.Strings.isNullOrEmpty;
import static com.google.common.collect.ImmutableSet.of;
import static io.spine.option.OptionsProto.everyIs;
import static io.spine.option.OptionsProto.is;
import static io.spine.tools.protoc.MarkerInterfaces.create;
import static java.lang.String.format;

/**
 * The {@link SpineProtoGenerator} implementation generating the specific interfaces implemented by
 * some message types.
 *
 * <p>The generator produces two types of {@link File CodeGeneratorResponse.File} instances
 * representing:
 * <ul>
 *     <li>the marker interfaces derived from
 *         {@link com.google.protobuf.Message com.google.protobuf.Message} (see
 *         {@link MarkerInterfaces});
 *     <li>the insertion entries to the existing messages (see
 *         {@link File#getInsertionPoint() CodeGeneratorResponse.File.insertionPoint}).
 * </ul>
 *
 * @author Dmytro Dashenkov
 */
public class MarkerInterfaceGenerator extends SpineProtoGenerator {

    @VisibleForTesting
    static final String INSERTION_POINT_IMPLEMENTS = "message_implements:%s";
    private static final String PACKAGE_DELIMITER = ".";

    private MarkerInterfaceGenerator() {
        super();
        // Prevent singleton class instantiation.
    }

    /**
     * Retrieves the single instance of the {@link MarkerInterfaceGenerator} type.
     */
    public static SpineProtoGenerator instance() {
        return Singleton.INSTANCE.value;
    }

    /**
     * {@inheritDoc}
     *
     * <p>The {@code MarkerInterfaceGenerator} implementation performs the message processing
     * as follows:
     * <ol>
     *     <li>Checks the message has {@code (is)} option. If it does, the marker interface name is
     *         extracted from it and both the marker interface and the message insertion point are
     *         generated.
     *     <li>Checks the message file has {@code (every_is)} option. If it does, the marker
     *         interface name is extracted from it and both the marker interface and the message
     *         insertion point are generated.
     *     <li>Otherwise, no compiler response is generated for this message type.
     * </ol>
     */
    @Override
    protected Collection<File> processMessage(FileDescriptorProto file, DescriptorProto message) {
        final Optional<MessageAndInterface> fromMsgOption = scanMsgOption(file, message);
        if (fromMsgOption.isPresent()) {
            return fromMsgOption.get().asSet();
        }
        final Optional<MessageAndInterface> fromFileOption = scanFileOption(file, message);
        if (fromFileOption.isPresent()) {
            return fromFileOption.get().asSet();
        }
        return of();
    }

    /**
     * Scans the given {@linkplain FileDescriptorProto file} for the {@code (every_is)} option.
     */
    private static Optional<MessageAndInterface> scanFileOption(FileDescriptorProto file,
                                                                DescriptorProto msg) {
        final Optional<String> everyIs = getEveryIs(file);
        if (everyIs.isPresent()) {
            final MessageAndInterface resultingFile = generateFile(file, msg, everyIs.get());
            return Optional.of(resultingFile);
        } else {
            return Optional.absent();
        }
    }

    /**
     * Scans the given {@linkplain DescriptorProto message} for the {@code (is)} option.
     */
    private static Optional<MessageAndInterface> scanMsgOption(FileDescriptorProto file,
                                                               DescriptorProto msg) {
        final Optional<String> everyIs = getIs(msg);
        if (everyIs.isPresent()) {
            final MessageAndInterface resultingFile = generateFile(file, msg, everyIs.get());
            return Optional.of(resultingFile);
        } else {
            return Optional.absent();
        }
    }

    private static MessageAndInterface generateFile(FileDescriptorProto file,
                                                    DescriptorProto msg,
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
        final JavaFile interfaceContent = create(interfaceSpec.getPackageName(),
                                                 interfaceSpec.getName());
        final File interfaceFile = File.newBuilder()
                                       .setName(toFileName(interfaceSpec.getPackageName(),
                                                           interfaceSpec.getName()))
                                       .setContent(interfaceContent.toString())
                                       .build();
        final MessageAndInterface result = new MessageAndInterface(messageFile, interfaceFile);
        return result;
    }

    private static Optional<String> getEveryIs(FileDescriptorProto descriptor) {
        final String value = UnknownOptions.getUnknownOptionValue(descriptor, everyIs.getNumber());
        return getOptionalOption(value, descriptor.getOptions(), everyIs);
    }

    private static Optional<String> getIs(DescriptorProto descriptor) {
        final String value = UnknownOptions.getUnknownOptionValue(descriptor, is.getNumber());
        return getOptionalOption(value, descriptor.getOptions(), is);
    }

    /**
     * Retrieves the value of the specified extension option.
     *
     * <p>The {@linkplain com.google.protobuf.DescriptorProtos proto descriptor API} behaves
     * differently at the Protobuf compile time and at runtime. Thus, the method receives the value
     * retrieved by the {@link UnknownOptions} utility. If the value is absent, the method ties to
     * get the option value as a value of a resolved extension option, which is the runtime way.
     *
     * @param initialValue the value parsed from the options unknown fields
     * @param options      the container of the options to get the value from
     * @param option       the desired option
     * @param <O>          the type of the options container
     * @return the value of the option
     *         {@linkplain com.google.common.base.Strings#isNullOrEmpty if any} or
     *         {@link Optional#absent() Optional.absent()} otherwise
     */
    private static <O extends ExtendableMessage<O>> Optional<String>
    getOptionalOption(@Nullable String initialValue, O options, Extension<O, String> option) {
        if (isNullOrEmpty(initialValue)) {
            return getResolvedOption(options, option);
        } else {
            return Optional.of(initialValue);
        }
    }

    private static <O extends ExtendableMessage<O>> Optional<String>
    getResolvedOption(O options, Extension<O, String> resolvedOption) {
        final String value = options.getExtension(resolvedOption);
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
        final String nameFqn = toFileName(javaPackage, messageName);
        final File.Builder srcFile = File.newBuilder()
                                         .setName(nameFqn);
        return srcFile;
    }

    private static String toFileName(String javaPackage, String typename) {
        return (javaPackage + PACKAGE_DELIMITER + typename).replace('.', '/') + ".java";
    }

    /**
     * The specification of the marker interface to be produces by the generator.
     *
     * <p>The specification includes the package name and the type name.
     */
    private static final class MarkerInterfaceSpec {

        private final String packageName;
        private final String name;

        private MarkerInterfaceSpec(String packageName, String name) {
            this.packageName = packageName;
            this.name = name;
        }

        private static MarkerInterfaceSpec newInstance(String packageName, String name) {
            return new MarkerInterfaceSpec(packageName, name);
        }

        /**
         * Parses a {@code MarkerInterfaceSpec} from the given type fully qualified name.
         */
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

    /**
     * A tuple of two {@link File} instances representing a message and the marker interface
     * resolved for that message.
     */
    private static final class MessageAndInterface {

        private final File messageFile;
        private final File interfaceFile;

        private MessageAndInterface(File messageFile, File interfaceFile) {
            this.messageFile = messageFile;
            this.interfaceFile = interfaceFile;
        }

        /**
         * Produces an immutable {@link Set} from this tuple.
         */
        private Set<File> asSet() {
            return of(messageFile, interfaceFile);
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) {
                return true;
            }
            if (o == null || getClass() != o.getClass()) {
                return false;
            }
            MessageAndInterface that = (MessageAndInterface) o;
            return Objects.equal(messageFile, that.messageFile) &&
                    Objects.equal(interfaceFile, that.interfaceFile);
        }

        @Override
        public int hashCode() {
            return Objects.hashCode(messageFile, interfaceFile);
        }
    }

    private enum Singleton {
        INSTANCE;

        @SuppressWarnings("NonSerializableFieldInSerializableClass")
        private final SpineProtoGenerator value = new MarkerInterfaceGenerator();
    }
}
