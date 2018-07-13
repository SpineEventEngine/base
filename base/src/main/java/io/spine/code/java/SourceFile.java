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

package io.spine.code.java;

import com.google.protobuf.DescriptorProtos.DescriptorProto;
import com.google.protobuf.DescriptorProtos.EnumDescriptorProto;
import com.google.protobuf.DescriptorProtos.FileDescriptorProto;
import com.google.protobuf.DescriptorProtos.ServiceDescriptorProto;
import io.spine.code.AbstractSourceFile;

import java.nio.file.Path;

import static com.google.common.base.Preconditions.checkNotNull;
import static io.spine.util.Exceptions.newIllegalStateException;

/**
 * A Java source code file.
 *
 * @author Alexander Yevsyukov
 */
public final class SourceFile extends AbstractSourceFile {

    private SourceFile(Path path) {
        super(path);
    }

    static SourceFile of(Path path) {
        checkNotNull(path);
        SourceFile result = new SourceFile(path);
        return result;
    }

    /**
     * Obtains the generated file {@link Path} for the specified file descriptor.
     *
     * @param file the proto file descriptor
     * @return the relative file path
     */
    public static SourceFile forOuterClassOf(FileDescriptorProto file) {
        checkNotNull(file);
        FileName filename = SimpleClassName.outerOf(file)
                                                 .toFileName();
        SourceFile result = getFolder(file).resolve(filename);
        return result;
    }

    /**
     * Obtains the {@link Path} to a folder, that contains
     * a generated file from the file descriptor.
     *
     * @param file the proto file descriptor
     * @return the relative folder path
     */
    private static Directory getFolder(FileDescriptorProto file) {
        checkNotNull(file);
        PackageName packageName = PackageName.resolve(file);
        Directory result = packageName.toDirectory();
        return result;
    }

    /**
     * Obtains the generated file {@link Path} for the specified message descriptor.
     *
     * @param  message   the descriptor of the message type for which we obtain the source code file
     * @param  orBuilder indicates if a {@code MessageOrBuilder} path for the message should
     *                   be returned
     * @param  file      the descriptor of the proto file which contains the declaration of the
     *                   message type
     * @return the relative file path
     */
    public static SourceFile forMessage(DescriptorProto message,
                                        boolean orBuilder,
                                        FileDescriptorProto file) {
        checkNotNull(file);
        checkNotNull(message);
        String typeName = message.getName();
        if (!file.getMessageTypeList()
                 .contains(message)) {
            throw invalidNestedDefinition(file.getName(), typeName);
        }

        if (!file.getOptions()
                 .hasJavaMultipleFiles()) {
            SourceFile result = forOuterClassOf(file);
            return result;
        }

        FileName filename = FileName.forMessage(message, orBuilder);
        SourceFile result = getFolder(file).resolve(filename);
        return result;
    }

    private static IllegalStateException invalidNestedDefinition(String filename,
                                                                 String nestedDefinitionName) {
        throw newIllegalStateException("`%s` does not contain nested definition `%s`.",
                                       filename, nestedDefinitionName);
    }

    /**
     * Obtains the generated file {@link Path} for the specified enum descriptor.
     *
     * @param enumType the enum descriptor to get path
     * @param file the file descriptor containing the enum descriptor
     * @return the relative file path
     */
    public static SourceFile forEnum(EnumDescriptorProto enumType, FileDescriptorProto file) {
        checkNotNull(file);
        checkNotNull(enumType);
        if (!file.getEnumTypeList()
                 .contains(enumType)) {
            throw invalidNestedDefinition(file.getName(), enumType.getName());
        }

        if (!file.getOptions()
                 .hasJavaMultipleFiles()) {
            SourceFile result = forOuterClassOf(file);
            return result;
        }

        FileName filename = FileName.forEnum(enumType);
        SourceFile result = getFolder(file).resolve(filename);
        return result;
    }

    public static SourceFile forService(ServiceDescriptorProto service, FileDescriptorProto file) {
        checkNotNull(service);
        checkNotNull(file);
        String serviceType = service.getName();
        if (!file.getServiceList()
                 .contains(service)) {
            throw invalidNestedDefinition(file.getName(), serviceType);
        }

        FileName filename = FileName.forService(service);
        SourceFile result = getFolder(file).resolve(filename);
        return result;
    }

    /**
     * Obtains a file path for the source code file of the give type in the passed package.
     */
    public static SourceFile forType(String javaPackage, String typename) {
        SourceFile result = PackageName.of(javaPackage)
                                             .toDirectory()
                                             .resolve(FileName.forType(typename));
        return result;
    }
}
