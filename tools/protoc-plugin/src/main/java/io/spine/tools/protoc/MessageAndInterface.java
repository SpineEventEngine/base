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
import com.google.common.base.Objects;
import com.google.common.collect.ImmutableSet;
import com.google.protobuf.DescriptorProtos.DescriptorProto;
import com.google.protobuf.DescriptorProtos.FileDescriptorProto;
import com.google.protobuf.compiler.PluginProtos.CodeGeneratorResponse.File;
import com.squareup.javapoet.JavaFile;
import io.spine.code.java.PackageName;
import io.spine.code.java.SourceFile;
import io.spine.option.Options;

import java.util.Optional;
import java.util.Set;

import static com.google.common.base.Strings.isNullOrEmpty;
import static io.spine.code.java.PackageName.delimiter;
import static io.spine.option.OptionsProto.everyIs;
import static io.spine.option.OptionsProto.is;
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
        Optional<String> everyIs = getEveryIs(file);
        if (everyIs.isPresent()) {
            MessageAndInterface resultingFile = generateFile(file, msg, everyIs.get());
            return Optional.of(resultingFile);
        } else {
            return Optional.empty();
        }
    }

    private static MessageAndInterface generateFile(FileDescriptorProto file,
                                                    DescriptorProto msg,
                                                    String optionValue) {
        MarkerInterfaceSpec interfaceSpec = prepareInterface(optionValue, file);
        File.Builder srcFile = prepareFile(file, msg);
        String messageFqn = file.getPackage() + delimiter() + msg.getName();
        File messageFile = implementInterface(srcFile,
                                              interfaceSpec.getFqn(),
                                              messageFqn);
        JavaFile interfaceContent = interfaceSpec.toJavaCode();
        File interfaceFile = File.newBuilder()
                                 .setName(interfaceSpec.toSourceFile()
                                                       .toString())
                                 .setContent(interfaceContent.toString())
                                 .build();
        MessageAndInterface result = new MessageAndInterface(messageFile, interfaceFile);
        return result;
    }

    private static String toTypeName(FileDescriptorProto file, DescriptorProto msg) {
        boolean multipleFiles = file.getOptions()
                                    .getJavaMultipleFiles();
        return multipleFiles
               ? msg.getName()
               : resolveName(file);
    }

    private static Optional<String> getEveryIs(FileDescriptorProto descriptor) {
        Optional<String> value = Options.option(descriptor, everyIs);
        return value;
    }

    private static File implementInterface(File.Builder srcFile,
                                           String interfaceTypeName,
                                           String messageTypeName) {
        String insertionPoint = format(INSERTION_POINT_IMPLEMENTS, messageTypeName);
        File result = srcFile.setInsertionPoint(insertionPoint)
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
        String javaPackage = PackageName.resolve(file)
                                        .value();
        String messageName = toTypeName(file, msg);
        String fileName = SourceFile.forType(javaPackage, messageName)
                                    .toString();
        String uriStyleName = fileName.replace('\\', '/');
        File.Builder srcFile = File.newBuilder()
                                   .setName(uriStyleName);
        return srcFile;
    }

    /**
     * Scans the given {@linkplain DescriptorProto message} for the {@code (is)} option.
     */
    static Optional<MessageAndInterface> scanMsgOption(FileDescriptorProto file,
                                                       DescriptorProto msg) {
        Optional<String> everyIs = getIs(msg);
        if (everyIs.isPresent()) {
            MessageAndInterface resultingFile = generateFile(file, msg, everyIs.get());
            return Optional.of(resultingFile);
        } else {
            return Optional.empty();
        }
    }

    private static Optional<String> getIs(DescriptorProto descriptor) {
        Optional<String> value = Options.option(descriptor, is);
        return value;
    }

    /**
     * Converts the instance into the pair containing a message file and an interface file.
     */
    Set<File> asSet() {
        return ImmutableSet.of(messageFile, interfaceFile);
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
