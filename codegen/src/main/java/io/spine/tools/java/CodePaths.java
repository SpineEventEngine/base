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

import java.io.File;
import java.nio.file.Path;
import java.nio.file.Paths;

import static com.google.common.base.Preconditions.checkNotNull;
import static java.lang.String.format;

/**
 * Utilities for working with the Java sources, generated from {@code .proto} files.
 *
 * @author Dmytro Grankin
 * @author Alexander Yevsyukov
 */
public final class CodePaths {

    public static final String PACKAGE_DELIMITER = ".";
    private static final String OR_BUILDER_SUFFIX = "OrBuilder";
    private static final String GRPC_CLASSNAME_SUFFIX = "Grpc";

    static final String FILE_EXTENSION = ".java";

    /** Prevent instantiation of this utility class. */
    private CodePaths() {
    }

    /**
     * Obtains the generated file {@link Path} for the specified file descriptor.
     *
     * @param file the proto file descriptor
     * @return the relative file path
     */
    public static Path getFile(FileDescriptorProto file) {
        checkNotNull(file);
        final Path folderPath = getFolder(file);
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
     * @param  file
     *         the file descriptor containing the message descriptor
     * @return the relative file path
     */
    public static Path getFile(DescriptorProto messageDescriptor,
                               boolean messageOrBuilder,
                               FileDescriptorProto file) {
        checkNotNull(file);
        checkNotNull(messageDescriptor);
        final String typeName = messageDescriptor.getName();
        if (!file.getMessageTypeList()
                           .contains(messageDescriptor)) {
            throw invalidNestedDefinition(file.getName(), typeName);
        }

        if (!file.getOptions()
                           .hasJavaMultipleFiles()) {
            return getFile(file);
        }

        final Path folderPath = getFolder(file);

        final String filename;
        filename = messageOrBuilder
                   ? typeName + OR_BUILDER_SUFFIX + FILE_EXTENSION
                   : typeName + FILE_EXTENSION;
        return folderPath.resolve(filename);
    }

    /**
     * Obtains the generated file {@link Path} for the specified enum descriptor.
     *
     * @param enumType the enum descriptor to get path
     * @param file the file descriptor containing the enum descriptor
     * @return the relative file path
     */
    public static Path getFile(EnumDescriptorProto enumType, FileDescriptorProto file) {
        checkNotNull(file);
        checkNotNull(enumType);
        if (!file.getEnumTypeList()
                           .contains(enumType)) {
            throw invalidNestedDefinition(file.getName(), enumType.getName());
        }

        if (!file.getOptions()
                           .hasJavaMultipleFiles()) {
            return getFile(file);
        }

        final Path folderPath = getFolder(file);
        final String filename = enumType.getName() + FILE_EXTENSION;
        return folderPath.resolve(filename);
    }

    public static Path getFile(ServiceDescriptorProto service, FileDescriptorProto file) {
        checkNotNull(service);
        checkNotNull(file);
        final String serviceType = service.getName();
        if (!file.getServiceList()
                           .contains(service)) {
            throw invalidNestedDefinition(file.getName(), serviceType);
        }

        final Path folderPath = getFolder(file);
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

    /**
     * Obtains the {@link Path} to a folder, that contains
     * a generated file from the file descriptor.
     *
     * @param file the proto file descriptor
     * @return the relative folder path
     */
    private static Path getFolder(FileDescriptorProto file) {
        checkNotNull(file);
        final PackageName packageName = PackageName.resolve(file);
        final String packageDir = packageName.value()
                                             .replace('.', File.separatorChar);
        return Paths.get(packageDir);
    }

    public static String toFileName(String javaPackage, String typename) {
        return (javaPackage + PACKAGE_DELIMITER + typename).replace('.', '/') + FILE_EXTENSION;
    }
}
