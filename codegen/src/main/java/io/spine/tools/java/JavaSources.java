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

package io.spine.tools.java;

import com.google.protobuf.DescriptorProtos.DescriptorProto;
import com.google.protobuf.DescriptorProtos.EnumDescriptorProto;
import com.google.protobuf.DescriptorProtos.FileDescriptorProto;
import com.google.protobuf.DescriptorProtos.ServiceDescriptorProto;

import java.nio.file.Path;

import static com.google.common.base.Preconditions.checkNotNull;
import static java.lang.String.format;

/**
 * Utilities for working with the Java sources, generated from {@code .proto} files.
 *
 * @author Dmytro Grankin
 */
public class JavaSources {

    private static final String OR_BUILDER_SUFFIX = "OrBuilder";
    private static final String GRPC_CLASSNAME_SUFFIX = "Grpc";
    static final String FILE_EXTENSION = ".java";

    /** Prevent instantiation of this utility class. */
    private JavaSources() {
    }

    /**
     * Obtains the generated file {@link Path} for the specified file descriptor.
     *
     * @param file the proto file descriptor
     * @return the relative file path
     */
    public static Path getFilePath(FileDescriptorProto file) {
        checkNotNull(file);
        final Path folderPath = JavaCode.getFolderPath(file);
        final SimpleClassName className = SimpleClassName.outerOf(file);
        final String filename = className.toFileName();
        return folderPath.resolve(filename);
    }

    /**
     * Obtains the generated file {@link Path} for the specified message descriptor.
     *
     * @param  messageDescriptor
     *         the message descriptor to get path
     * @param  messageOrBuilder
     *         indicates if a {@code MessageOrBuilder} path for the message should be returned
     * @param  fileDescriptor
     *         the file descriptor containing the message descriptor
     * @return the relative file path
     */
    public static Path getFilePath(DescriptorProto messageDescriptor,
                                   boolean messageOrBuilder,
                                   FileDescriptorProto fileDescriptor) {
        checkNotNull(fileDescriptor);
        checkNotNull(messageDescriptor);
        final String typeName = messageDescriptor.getName();
        if (!fileDescriptor.getMessageTypeList()
                           .contains(messageDescriptor)) {
            throw invalidNestedDefinition(fileDescriptor.getName(), typeName);
        }

        if (!fileDescriptor.getOptions()
                           .hasJavaMultipleFiles()) {
            return getFilePath(fileDescriptor);
        }

        final Path folderPath = JavaCode.getFolderPath(fileDescriptor);

        final String filename;
        filename = messageOrBuilder
                   ? typeName + OR_BUILDER_SUFFIX + FILE_EXTENSION
                   : typeName + FILE_EXTENSION;
        return folderPath.resolve(filename);
    }

    /**
     * Obtains the generated file {@link Path} for the specified enum descriptor.
     *
     * @param enumDescriptor the enum descriptor to get path
     * @param fileDescriptor the file descriptor containing the enum descriptor
     * @return the relative file path
     */
    public static Path getFilePath(EnumDescriptorProto enumDescriptor,
                                   FileDescriptorProto fileDescriptor) {
        checkNotNull(fileDescriptor);
        checkNotNull(enumDescriptor);
        if (!fileDescriptor.getEnumTypeList()
                           .contains(enumDescriptor)) {
            throw invalidNestedDefinition(fileDescriptor.getName(), enumDescriptor.getName());
        }

        if (!fileDescriptor.getOptions()
                           .hasJavaMultipleFiles()) {
            return getFilePath(fileDescriptor);
        }

        final Path folderPath = JavaCode.getFolderPath(fileDescriptor);
        final String filename = enumDescriptor.getName() + FILE_EXTENSION;
        return folderPath.resolve(filename);
    }

    public static Path getFilePath(ServiceDescriptorProto serviceDescriptor,
                                   FileDescriptorProto fileDescriptor) {
        checkNotNull(serviceDescriptor);
        checkNotNull(fileDescriptor);
        final String serviceType = serviceDescriptor.getName();
        if (!fileDescriptor.getServiceList()
                           .contains(serviceDescriptor)) {
            throw invalidNestedDefinition(fileDescriptor.getName(), serviceType);
        }

        final Path folderPath = JavaCode.getFolderPath(fileDescriptor);
        final String filename = serviceType + GRPC_CLASSNAME_SUFFIX + FILE_EXTENSION;
        return folderPath.resolve(filename);
    }

    private static IllegalStateException invalidNestedDefinition(String filename,
                                                                 String nestedDefinitionName) {
        final String errMsg = format("`%s` does not contain nested definition `%s`.",
                                     filename, nestedDefinitionName);
        throw new IllegalStateException(errMsg);
    }

    public static String getOrBuilderSuffix() {
        return OR_BUILDER_SUFFIX;
    }
}
