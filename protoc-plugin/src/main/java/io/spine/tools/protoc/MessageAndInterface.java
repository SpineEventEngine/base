/*
 * Copyright 2018, TeamDev Ltd. All rights reserved.
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
import com.google.protobuf.DescriptorProtos;
import com.google.protobuf.DescriptorProtos.DescriptorProto;
import com.google.protobuf.DescriptorProtos.FileDescriptorProto;
import com.google.protobuf.Extension;
import com.google.protobuf.GeneratedMessageV3;
import com.google.protobuf.compiler.PluginProtos.CodeGeneratorResponse.File;
import com.squareup.javapoet.JavaFile;
import io.spine.option.UnknownOptions;
import io.spine.tools.java.PackageName;
import io.spine.tools.java.SourceFile;

import javax.annotation.Nullable;
import java.util.Set;

import static com.google.common.base.Strings.isNullOrEmpty;
import static com.google.common.collect.ImmutableSet.of;
import static io.spine.option.OptionsProto.everyIs;
import static io.spine.option.OptionsProto.is;
import static io.spine.tools.java.PackageName.DELIMITER;
import static io.spine.tools.protoc.MarkerInterfaceSpec.prepareInterface;
import static java.lang.String.format;

/**
 * A tuple of two {@link File} instances representing a message and the marker interface
 * resolved for that message.
 *
 * @author Dmytro Dashenkov
 */
final class MessageAndInterface {

    @VisibleForTesting
    static final String INSERTION_POINT_IMPLEMENTS = "message_implements:%s";

    private final File messageFile;
    private final File interfaceFile;

    private MessageAndInterface(File messageFile, File interfaceFile) {
        this.messageFile = messageFile;
        this.interfaceFile = interfaceFile;
    }

    /**
     * Scans the given {@linkplain FileDescriptorProto file} for the {@code (every_is)} option.
     */
    static Optional<MessageAndInterface> scanFileOption(FileDescriptorProto file,
                                                        DescriptorProto msg) {
        final Optional<String> everyIs = getEveryIs(file);
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
        final MarkerInterfaceSpec interfaceSpec = prepareInterface(optionValue, file);
        final File.Builder srcFile = prepareFile(file, msg);
        final String messageFqn = file.getPackage() + DELIMITER + msg.getName();
        final File messageFile = implementInterface(srcFile,
                                                    interfaceSpec.getFqn(),
                                                    messageFqn);
        final JavaFile interfaceContent = interfaceSpec.toJavaCode();
        final File interfaceFile = File.newBuilder()
                                       .setName(interfaceSpec.toSourceFile()
                                                             .toString())
                                       .setContent(interfaceContent.toString())
                                       .build();
        final MessageAndInterface result = new MessageAndInterface(messageFile, interfaceFile);
        return result;
    }

    private static String toTypeName(FileDescriptorProto file, DescriptorProto msg) {
        final boolean multipleFiles = file.getOptions()
                                              .getJavaMultipleFiles();
        return multipleFiles
                ? msg.getName()
                : resolveName(file);
    }

    private static Optional<String> getEveryIs(FileDescriptorProto descriptor) {
        final String value = UnknownOptions.getUnknownOptionValue(descriptor, everyIs.getNumber());
        return getOptionalOption(value, descriptor.getOptions(), everyIs);
    }

    /**
     * Retrieves the value of the specified extension option.
     *
     * <p>The {@linkplain DescriptorProtos proto descriptor API} behaves
     * differently at the Protobuf compile time and at runtime. Thus, the method receives the value
     * retrieved by the {@link UnknownOptions} utility. If the value is absent, the method tries to
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
    static <O extends GeneratedMessageV3.ExtendableMessage<O>> Optional<String>
    getOptionalOption(@Nullable String initialValue, O options, Extension<O, String> option) {
        final Optional<String> result = isNullOrEmpty(initialValue)
                ? getResolvedOption(options, option)
                : Optional.of(initialValue);
        return result;
    }

    private static <O extends GeneratedMessageV3.ExtendableMessage<O>> Optional<String>
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

    private static String resolveName(FileDescriptorProto fileDescriptor) {
        String name = fileDescriptor.getOptions()
                                    .getJavaOuterClassname();
        if (isNullOrEmpty(name)) {
            name = fileDescriptor.getName();
        }
        return name;
    }

    private static File.Builder prepareFile(FileDescriptorProto file, DescriptorProto msg) {
        final String javaPackage = PackageName.resolve(file)
                                              .value();
        final String messageName = toTypeName(file, msg);

        final String fileName = SourceFile.forType(javaPackage, messageName)
                                          .toString();
        final File.Builder srcFile = File.newBuilder()
                                         .setName(fileName);
        return srcFile;
    }

    /**
     * Scans the given {@linkplain DescriptorProto message} for the {@code (is)} option.
     */
    static Optional<MessageAndInterface> scanMsgOption(FileDescriptorProto file,
                                                       DescriptorProto msg) {
        final Optional<String> everyIs = getIs(msg);
        if (everyIs.isPresent()) {
            final MessageAndInterface resultingFile = generateFile(file, msg, everyIs.get());
            return Optional.of(resultingFile);
        } else {
            return Optional.absent();
        }
    }

    private static Optional<String> getIs(DescriptorProto descriptor) {
        final String value = UnknownOptions.getUnknownOptionValue(descriptor, is.getNumber());
        return getOptionalOption(value, descriptor.getOptions(), is);
    }

    /**
     * Produces an immutable {@link Set} from this tuple.
     */
    Set<File> asSet() {
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
