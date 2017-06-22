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

package io.spine.gradle.compiler.util;

import com.google.protobuf.DescriptorProtos.DescriptorProto;
import com.google.protobuf.DescriptorProtos.EnumDescriptorProto;
import com.google.protobuf.DescriptorProtos.FileDescriptorProto;
import com.google.protobuf.DescriptorProtos.ServiceDescriptorProto;

import java.io.File;
import java.nio.file.Path;
import java.nio.file.Paths;

import static com.google.common.base.Preconditions.checkNotNull;
import static io.spine.gradle.compiler.util.JavaCode.getOuterClassName;
import static java.lang.String.format;

/**
 * Utilities for working with the Java sources, generated from {@code .proto} files.
 *
 * @author Dmytro Grankin
 */
public class JavaSources {

    private static final char FILE_SEPARATOR = File.separatorChar;
    private static final String OR_BUILDER_SUFFIX = "OrBuilder";
    private static final String BUILDER_CLASS_NAME = "Builder";
    private static final String GRPC_CLASSNAME_SUFFIX = "Grpc";
    private static final String JAVA_EXTENSION = ".java";

    private JavaSources() {
        // Prevent instantiation of this utility class.
    }

    /**
     * Obtains the generated file {@link Path} for the specified file descriptor.
     *
     * @param fileDescriptor the proto file descriptor
     * @return the relative file path
     */
    public static Path getFilePath(FileDescriptorProto fileDescriptor) {
        checkNotNull(fileDescriptor);
        final Path folderPath = getFolderPath(fileDescriptor);
        final String filename = getOuterClassName(fileDescriptor) + JAVA_EXTENSION;
        return folderPath.resolve(filename);
    }

    /**
     * Obtains the generated file {@link Path} for the specified message descriptor.
     *
     * @param messageDescriptor the message descriptor to get path
     * @param messageOrBuilder  indicates if a {@code MessageOrBuilder} path
     *                          for the message should be returned
     * @param fileDescriptor    the file descriptor containing the message descriptor
     * @return the relative file path
     */
    public static Path getFilePath(DescriptorProto messageDescriptor,
                                   boolean messageOrBuilder,
                                   FileDescriptorProto fileDescriptor) {
        checkNotNull(fileDescriptor);
        checkNotNull(messageDescriptor);
        if (!fileDescriptor.getMessageTypeList()
                           .contains(messageDescriptor)) {
            throw invalidNestedDefinition(fileDescriptor.getName(), messageDescriptor.getName());
        }

        if (!fileDescriptor.getOptions()
                           .hasJavaMultipleFiles()) {
            return getFilePath(fileDescriptor);
        }

        final Path folderPath = getFolderPath(fileDescriptor);

        final String filename;
        filename = messageOrBuilder
                   ? messageDescriptor.getName() + OR_BUILDER_SUFFIX + JAVA_EXTENSION
                   : messageDescriptor.getName() + JAVA_EXTENSION;
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

        final Path folderPath = getFolderPath(fileDescriptor);
        final String filename = enumDescriptor.getName() + JAVA_EXTENSION;
        return folderPath.resolve(filename);
    }

    public static Path getFilePath(ServiceDescriptorProto serviceDescriptor,
                                   FileDescriptorProto fileDescriptor) {
        checkNotNull(serviceDescriptor);
        checkNotNull(fileDescriptor);
        if (!fileDescriptor.getServiceList()
                           .contains(serviceDescriptor)) {
            throw invalidNestedDefinition(fileDescriptor.getName(), serviceDescriptor.getName());
        }

        final Path folderPath = getFolderPath(fileDescriptor);
        final String filename = serviceDescriptor.getName() + GRPC_CLASSNAME_SUFFIX + JAVA_EXTENSION;
        return folderPath.resolve(filename);
    }

    /**
     * Obtains the {@link Path} to a folder, that contains
     * a generated file from the file descriptor.
     *
     * @param fileDescriptor the proto file descriptor
     * @return the relative folder path
     */
    public static Path getFolderPath(FileDescriptorProto fileDescriptor) {
        checkNotNull(fileDescriptor);
        final String javaPackage = fileDescriptor.getOptions()
                                                 .getJavaPackage();
        final String packageDir = javaPackage.replace('.', FILE_SEPARATOR);
        return Paths.get(packageDir);
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

    public static String getBuilderClassName() {
        return BUILDER_CLASS_NAME;
    }

    public static String getJavaExtension() {
        return JAVA_EXTENSION;
    }
}
