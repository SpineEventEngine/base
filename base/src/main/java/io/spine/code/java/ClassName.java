/*
 * Copyright 2019, TeamDev. All rights reserved.
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

import com.google.errorprone.annotations.Immutable;
import com.google.protobuf.Descriptors.Descriptor;
import com.google.protobuf.Descriptors.EnumDescriptor;
import com.google.protobuf.Descriptors.FileDescriptor;
import com.google.protobuf.Descriptors.ServiceDescriptor;
import io.spine.value.StringTypeValue;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.checkerframework.checker.signature.qual.FullyQualifiedName;

import java.util.Deque;

import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Preconditions.checkNotNull;
import static com.google.common.collect.Lists.newLinkedList;
import static io.spine.code.java.SimpleClassName.OR_BUILDER_SUFFIX;

/**
 * A value object holding a fully-qualified Java class name.
 */
@Immutable
public final class ClassName extends StringTypeValue {

    private static final long serialVersionUID = 0L;

    /**
     * Separates nested class name from the name of the outer class in a fully-qualified name.
     */
    public static final char OUTER_CLASS_DELIMITER = '$';

    /**
     * Separates class name from package, and outer class name with nested when such a class is
     * referenced as a parameter.
     */
    static final char DOT_SEPARATOR = '.';

    private ClassName(String value) {
        super(checkNotNull(value));
    }

    private ClassName(Class cls) {
        this(cls.getName());
    }

    /**
     * Creates a new instance with the name of the passed class.
     *
     * @param cls
     *         the class to get name from
     * @return new instance
     */
    public static ClassName of(Class cls) {
        return new ClassName(checkNotNull(cls));
    }

    /**
     * Creates a new instance with the passed class name value.
     *
     * @param className
     *         a fully-qualified Java class name
     * @return new
     */
    public static ClassName of(@FullyQualifiedName String className) {
        checkNotNull(className);
        checkArgument(className.length() > 0, "Class name cannot be empty.");
        return new ClassName(className);
    }

    /**
     * Creates a class name from the specified package the and simple name.
     *
     * @param packageName
     *         the name of the class package
     * @param simpleClassName
     *         the simple name of a the class
     * @return a new instance
     */
    public static ClassName of(PackageName packageName, SimpleClassName simpleClassName) {
        checkNotNull(packageName);
        checkNotNull(simpleClassName);
        return new ClassName(packageName.value() + DOT_SEPARATOR + simpleClassName);
    }

    /**
     * Creates an instance of {@code ClassName} from the given Protobuf message type descriptor.
     *
     * <p>The resulting class name is the name of the Java class which represents the given Protobuf
     * type.
     *
     * @param messageType
     *         the Protobuf message type descriptor
     * @return new instance of {@code ClassName}
     */
    public static ClassName from(Descriptor messageType) {
        checkNotNull(messageType);
        return construct(messageType.getFile(), messageType.getName(),
                         messageType.getContainingType());
    }

    /**
     * Obtains a {@code ClassName} for the outer class of the given Protobuf file.
     *
     * @param file
     *         the file from which the outer class is generated
     * @return new instance of {@code ClassName}
     */
    public static ClassName outerClass(FileDescriptor file) {
        PackageName packageName = PackageName.resolve(file.toProto());
        SimpleClassName simpleName = SimpleClassName.outerOf(file);
        return of(packageName, simpleName);
    }

    /**
     * Creates an instance of {@code ClassName} from the given Protobuf enum type descriptor.
     *
     * <p>The resulting class name is the name of the Java enum which represents the given Protobuf
     * type.
     *
     * @param enumType
     *         the Protobuf enum type descriptor
     * @return new instance of {@code ClassName}
     */
    public static ClassName from(EnumDescriptor enumType) {
        return construct(enumType.getFile(), enumType.getName(),
                         enumType.getContainingType());
    }

    /**
     * Creates an instance of {@code ClassName} from the given Protobuf service descriptor.
     *
     * <p>The resulting class name is the name of the Java gRPC stub class which is generated from
     * the given service type.
     *
     * @param serviceType
     *         the gRPC service descriptor
     * @return new instance of {@code ClassName}
     */
    public static ClassName from(ServiceDescriptor serviceType) {
        return construct(serviceType.getFile(), serviceType.getName(), null);
    }

    private static String javaPackageName(FileDescriptor file) {
        PackageName packageName = PackageName.resolve(file.toProto());
        return packageName.value() + DOT_SEPARATOR;
    }

    /**
     * Obtains outer class prefix, if the file has {@code java_multiple_files} set to {@code false}.
     * If the option is set, returns an empty string.
     */
    private static String outerClassPrefix(FileDescriptor file) {
        checkNotNull(file);
        boolean multipleFiles = file.getOptions()
                                    .getJavaMultipleFiles();
        if (multipleFiles) {
            return "";
        } else {
            String className = SimpleClassName.outerOf(file)
                                              .value();
            return className + OUTER_CLASS_DELIMITER;
        }
    }

    /**
     * Obtains prefix for a type which is enclosed into the passed message.
     * If null value is passed, returns an empty string.
     */
    private static String containingClassPrefix(@Nullable Descriptor containingMessage) {
        if (containingMessage == null) {
            return "";
        }
        Deque<String> parentClassNames = newLinkedList();
        Descriptor current = containingMessage;
        while (current != null) {
            parentClassNames.addFirst(current.getName() + OUTER_CLASS_DELIMITER);
            current = current.getContainingType();
        }
        String result = String.join("", parentClassNames);
        return result;
    }

    /**
     * Obtains the name of a nested class.
     *
     * @param className
     *         the name of the nested class to get
     * @return the nested class name
     */
    public ClassName withNested(SimpleClassName className) {
        checkNotNull(className);
        return of(value() + OUTER_CLASS_DELIMITER + className);
    }

    /**
     * Converts the name which may be a nested class name with {@link #OUTER_CLASS_DELIMITER}
     * to the name separated with dots.
     */
    public ClassName toDotted() {
        String withDots = toDotted(value());
        return of(withDots);
    }

    static String toDotted(String outerDelimited) {
        String result = outerDelimited.replace(OUTER_CLASS_DELIMITER, DOT_SEPARATOR);
        return result;
    }

    public ClassName orBuilder() {
        return of(value() + OR_BUILDER_SUFFIX);
    }

    private static ClassName construct(FileDescriptor file,
                                       String typeName,
                                       @Nullable Descriptor enclosing) {
        String packageName = javaPackageName(file);
        String outerClass = outerClassPrefix(file);
        String enclosingTypes = containingClassPrefix(enclosing);
        String result = packageName + outerClass + enclosingTypes + typeName;
        return of(result);
    }

    /**
     * Converts fully-qualified name to simple name. If the class is nested inside one or more
     * classes, the most nested name will be returned.
     */
    public SimpleClassName toSimple() {
        String fullName = toDotted().value();
        String result = afterDot(fullName);
        return SimpleClassName.create(result);
    }

    static String afterDot(String fullName) {
        int lastDotIndex = fullName.lastIndexOf(DOT_SEPARATOR);
        return fullName.substring(lastDotIndex + 1);
    }

    /**
     * Converts a possibly nested class name into a nested name.
     *
     * <p>If the class is not nested, the returned value would be equivalent to a simple class name.
     */
    public NestedClassName toNested() {
        return NestedClassName.create(this);
    }

    /**
     * Resolves the file which contains the declaration of the associated class.
     *
     * <p>The resulting {@code SourceFile} represents a <strong>relative</strong> path to the Java
     * file starting at the top level package.
     *
     * <p>In the simplest case, the file name is the same as the simple class name. However, if
     * the class is nested, then the file name coincides with the simple name of the top-level
     * class.
     *
     * @return the file in which the Java class is declared
     */
    public SourceFile resolveFile() {
        Directory directory = getPackage().toDirectory();
        SimpleClassName topLevelClass = topLevelClass();
        FileName javaFile = FileName.forType(topLevelClass.value());
        SourceFile sourceFile = directory.resolve(javaFile);
        return sourceFile;
    }

    private PackageName getPackage() {
        String fullName = value();
        int lastDotIndex = fullName.lastIndexOf(DOT_SEPARATOR);
        checkArgument(lastDotIndex > 0, "%s should be qualified.", fullName);
        String result = fullName.substring(0, lastDotIndex);
        return PackageName.of(result);
    }

    private SimpleClassName topLevelClass() {
        String qualifiedClassName = afterDot(value());
        int delimiterIndex = qualifiedClassName.indexOf(OUTER_CLASS_DELIMITER);
        String topLevelClassName = delimiterIndex >= 0
                                   ? qualifiedClassName.substring(0, delimiterIndex)
                                   : qualifiedClassName;
        return SimpleClassName.create(topLevelClassName);
    }
}
