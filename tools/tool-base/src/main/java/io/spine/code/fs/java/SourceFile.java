/*
 * Copyright 2020, TeamDev. All rights reserved.
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

package io.spine.code.fs.java;

import com.google.protobuf.DescriptorProtos.DescriptorProto;
import com.google.protobuf.DescriptorProtos.EnumDescriptorProto;
import com.google.protobuf.DescriptorProtos.FileDescriptorProto;
import com.google.protobuf.DescriptorProtos.ServiceDescriptorProto;
import io.spine.code.AbstractSourceFile;
import io.spine.code.java.ClassName;
import io.spine.code.java.PackageName;
import io.spine.code.java.SimpleClassName;
import io.spine.type.Type;

import java.nio.file.Path;
import java.util.function.Function;

import static com.google.common.base.Preconditions.checkNotNull;
import static io.spine.util.Exceptions.newIllegalStateException;

/**
 * A Java source code file.
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
     * Obtains the generated file {@link Path} for the specified type.
     *
     * @param type
     *         the type from which the file is generated
     * @return a relative file path
     */
    public static SourceFile forType(Type<?, ?> type) {
        SourceFile classFile = whichDeclares(type.javaClassName());
        return classFile;
    }

    /**
     * Resolves the file which contains the declaration of the given class.
     *
     * <p>The resulting {@code SourceFile} represents a <strong>relative</strong> path to the Java
     * file starting at the top level package.
     *
     * <p>In the simplest case, the file name is the same as the simple class name. However, if
     * the class is nested, then the file name coincides with the simple name of the top-level
     * class.
     *
     * @param javaClass
     *         the name of the class to resolve
     * @return the file in which the Java class is declared
     */
    public static SourceFile whichDeclares(ClassName javaClass) {
        checkNotNull(javaClass);
        Directory directory = Directory.of(javaClass.packageName());
        SimpleClassName topLevelClass = javaClass.topLevelClass();
        FileName javaFile = FileName.forType(topLevelClass.value());
        SourceFile sourceFile = directory.resolve(javaFile);
        return sourceFile;
    }

    /**
     * Obtains the generated file {@link Path} for the specified file descriptor.
     *
     * @param file
     *         the proto file descriptor
     * @return the relative file path
     */
    public static SourceFile forOuterClassOf(FileDescriptorProto file) {
        checkNotNull(file);
        FileName filename = FileName.forType(SimpleClassName.outerOf(file).value());
        SourceFile result = getGeneratedFolder(file).resolve(filename);
        return result;
    }

    /**
     * Obtains the {@link Path} to a folder, that contains
     * a generated file from the file descriptor.
     *
     * @param file
     *         the proto file descriptor
     * @return the relative folder path
     */
    private static Directory getGeneratedFolder(FileDescriptorProto file) {
        checkNotNull(file);
        PackageName packageName = PackageName.resolve(file);
        Directory result = Directory.of(packageName);
        return result;
    }

    /**
     * Obtains the generated file for the specified message descriptor.
     *
     * @param message
     *         the descriptor of the message type for which we obtain the source code file
     * @param file
     *         the descriptor of the proto file which contains the declaration of the
     *         message type
     * @return the relative file path
     */
    public static SourceFile forMessage(DescriptorProto message,
                                        FileDescriptorProto file) {
        return forMessageOrInterface(message, file, FileName::forMessage);
    }

    /**
     * Obtains the generated file for the {@code MessageOrBuilder} interface of the specified
     * message descriptor.
     *
     * @param message
     *         the descriptor of the message type for which we obtain the source code file
     * @param file
     *         the descriptor of the proto file which contains the declaration of the
     *         message type
     * @return the relative file path
     */
    public static SourceFile forMessageOrBuilder(DescriptorProto message,
                                                 FileDescriptorProto file) {
        return forMessageOrInterface(message, file, FileName::forMessageOrBuilder);
    }

    private static SourceFile forMessageOrInterface(DescriptorProto message,
                                                    FileDescriptorProto file,
                                                    Function<DescriptorProto, FileName> fileName) {
        checkNotNull(file);
        checkNotNull(message);
        String typeName = message.getName();
        if (!file.getMessageTypeList()
                 .contains(message)) {
            throw invalidNestedDefinition(file.getName(), typeName);
        }
        if (file.getOptions()
                .getJavaMultipleFiles()) {
            FileName filename = fileName.apply(message);
            SourceFile result = getGeneratedFolder(file).resolve(filename);
            return result;
        } else {
            SourceFile result = forOuterClassOf(file);
            return result;
        }

    }

    private static IllegalStateException invalidNestedDefinition(String filename,
                                                                 String nestedDefinitionName) {
        throw newIllegalStateException("`%s` does not contain nested definition `%s`.",
                                       filename, nestedDefinitionName);
    }

    /**
     * Obtains the generated file for the specified enum descriptor.
     *
     * @param enumType
     *         the enum descriptor to get the file for
     * @param file
     *         the file descriptor containing the enum descriptor
     * @return the relative file path
     */
    public static SourceFile forEnum(EnumDescriptorProto enumType, FileDescriptorProto file) {
        checkNotNull(file);
        checkNotNull(enumType);
        if (!file.getEnumTypeList()
                 .contains(enumType)) {
            throw invalidNestedDefinition(file.getName(), enumType.getName());
        }
        if (file.getOptions()
                .getJavaMultipleFiles()) {
            FileName filename = FileName.forEnum(enumType);
            SourceFile result = getGeneratedFolder(file).resolve(filename);
            return result;
        } else {
            SourceFile result = forOuterClassOf(file);
            return result;
        }
    }

    /**
     * Obtains the generated file for the specified service descriptor.
     *
     * @param service
     *         the service descriptor to get the file for
     * @param file
     *         the file descriptor containing the enum descriptor
     * @return the relative file path
     */
    public static SourceFile forService(ServiceDescriptorProto service, FileDescriptorProto file) {
        checkNotNull(service);
        checkNotNull(file);
        String serviceType = service.getName();
        if (!file.getServiceList()
                 .contains(service)) {
            throw invalidNestedDefinition(file.getName(), serviceType);
        }

        FileName filename = FileName.forService(service);
        SourceFile result = getGeneratedFolder(file).resolve(filename);
        return result;
    }

    /**
     * Obtains a file path for the source code file of the give type in the passed package.
     */
    public static SourceFile forType(String javaPackage, String typename) {
        PackageName packageName = PackageName.of(javaPackage);
        SourceFile result = Directory.of(packageName)
                                     .resolve(FileName.forType(typename));
        return result;
    }

    /**
     * Obtains a source file of the specified class.
     */
    public static SourceFile of(Class cls) {
        PackageName packageName = PackageName.of(cls);
        Directory directory = Directory.of(packageName);
        return forType(directory.toString(), cls.getSimpleName());
    }
}
