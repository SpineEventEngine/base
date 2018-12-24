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

import com.google.errorprone.annotations.Immutable;
import com.google.protobuf.Descriptors.Descriptor;
import com.google.protobuf.Descriptors.EnumDescriptor;
import com.google.protobuf.Descriptors.FileDescriptor;
import io.spine.value.StringTypeValue;
import org.checkerframework.checker.nullness.qual.Nullable;

import java.util.Deque;

import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Preconditions.checkNotNull;
import static com.google.common.collect.Lists.newLinkedList;
import static io.spine.util.Preconditions2.checkNotEmptyOrBlank;

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
    private static final char DOT_SEPARATOR = '.';

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
    public static ClassName of(String className) {
        checkNotNull(className);
        checkArgument(className.length() > 0, "Class name cannot me empty");
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
     * @param descriptor
     *         the Protobuf message type descriptor
     * @return new instance of {@code ClassName}
     */
    public static ClassName from(Descriptor descriptor) {
        return construct(descriptor.getName(),
                         descriptor.getFile(),
                         descriptor.getContainingType());
    }

    /**
     * Creates an instance of {@code ClassName} from the given Protobuf enum type descriptor.
     *
     * <p>The resulting class name is the name of the Java enum which represents the given Protobuf
     * type.
     *
     * @param descriptor
     *         the Protobuf enum type descriptor
     * @return new instance of {@code ClassName}
     */
    public static ClassName from(EnumDescriptor descriptor) {
        return construct(descriptor.getName(),
                         descriptor.getFile(),
                         descriptor.getContainingType());
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

    /**
     * Generates new class name taking this name and appending the passed suffix.
     *
     * @param suffix non-empty suffix
     * @return new class name
     */
    public ClassName with(String suffix) {
        checkNotEmptyOrBlank(suffix);
        return of(value() + suffix);
    }

    private static ClassName construct(String typeName,
                                       FileDescriptor file,
                                       @Nullable Descriptor parent) {
        String packageName = javaPackageName(file);
        String outerClass = outerClassPrefix(file);
        String parentTypes = parentClassPrefix(parent);
        String result = packageName + outerClass + parentTypes + typeName;
        return of(result);
    }

    private static String javaPackageName(FileDescriptor file) {
        String javaPackage = file.getOptions()
                                 .getJavaPackage()
                                 .trim();
        String packageName = javaPackage.isEmpty()
                             ? file.getPackage()
                             : javaPackage;
        String result = packageName.isEmpty()
                        ? ""
                        : packageName + DOT_SEPARATOR;
        return result;
    }

    private static String parentClassPrefix(@Nullable Descriptor parent) {
        if (parent == null) {
            return "";
        }
        Deque<String> parentClassNames = newLinkedList();
        Descriptor current = parent;
        while (current != null) {
            parentClassNames.addFirst(current.getName() + OUTER_CLASS_DELIMITER);
            current = current.getContainingType();
        }
        String result = String.join("", parentClassNames);
        return result;
    }

    private static String outerClassPrefix(FileDescriptor file) {
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
}
