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

package io.spine.tools.java;

import com.google.common.base.Optional;
import com.google.protobuf.DescriptorProtos.DescriptorProto;
import com.google.protobuf.DescriptorProtos.FileDescriptorProto;
import com.google.protobuf.Descriptors.Descriptor;
import io.spine.value.StringTypeValue;

import static com.google.common.base.Preconditions.checkNotNull;
import static io.spine.util.Preconditions2.checkNotEmptyOrBlank;

/**
 * A {@link Class#getSimpleName() simple name} of a class.
 *
 * @author Alexander Yevsyukov
 */
public final class SimpleClassName extends StringTypeValue {

    private static final SimpleClassName BUILDER_CLASS_NAME = new SimpleClassName("Builder");

    private static final String OR_BUILDER_SUFFIX = "OrBuilder";

    private SimpleClassName(String value) {
        super(value);
    }

    /**
     * Creates an instance with the outer class name for the types declared in the file specified
     * by the passed descriptor.
     *
     * <p>The outer class name is calculated according to
     * <a href="https://developers.google.com/protocol-buffers/docs/reference/java-generated#invocation">
     * Protobuf compiler conventions</a>.
     *
     * @param file a descriptor for file for which outer class name will be generated
     * @return outer class name
     */
    public static SimpleClassName outerOf(FileDescriptorProto file) {
        checkNotNull(file);
        final String value = getOuterClassName(file);
        final SimpleClassName result = new SimpleClassName(value);
        return result;
    }

    /**
     * Obtains an outer class name declared in the passed file.
     *
     * @param  file the descriptor of the proto file
     * @return the value declared in the file options, or
     *         {@linkplain Optional#absent() empty Optional} if the option is not set
     */
    public static Optional<SimpleClassName> declaredOuterClassName(FileDescriptorProto file) {
        final String className = file.getOptions()
                                     .getJavaOuterClassname();
        if (className.isEmpty()) {
            return Optional.absent();
        }

        final SimpleClassName result = outerOf(file);
        return Optional.of(result);
    }

    /**
     * Calculates a name of an outer Java class for types declared in the file represented
     * by the passed descriptor.
     *
     * <p>The outer class name is calculated according to
     * <a href="https://developers.google.com/protocol-buffers/docs/reference/java-generated#invocation">
     * Protobuf compiler conventions</a>.
     *
     * @param file a descriptor for file for which outer class name will be generated
     * @return non-qualified outer class name
     */
    private static String getOuterClassName(FileDescriptorProto file) {
        checkNotNull(file);
        String outerClassNameFromOptions = file.getOptions()
                                               .getJavaOuterClassname();
        if (!outerClassNameFromOptions.isEmpty()) {
            return outerClassNameFromOptions;
        }

        final String className = io.spine.tools.proto.FileName.from(file)
                                                              .nameOnlyCamelCase();
        return className;
    }

    /**
     * Obtains default name for a builder class.
     */
    public static SimpleClassName ofBuilder() {
        return BUILDER_CLASS_NAME;
    }

    /**
     * Obtains class name for {@link com.google.protobuf.MessageOrBuilder MessageOrBuilder}
     * descendant for the passed message type.
     */
    public static SimpleClassName messageOrBuilder(String typeName) {
        checkNotEmptyOrBlank(typeName);
        final SimpleClassName result = new SimpleClassName(typeName + OR_BUILDER_SUFFIX);
        return result;
    }

    /**
     * Obtains a Java class name corresponding the proto message declaration.
     */
    public static SimpleClassName ofMessage(DescriptorProto descriptor) {
        checkNotNull(descriptor);
        final SimpleClassName result = new SimpleClassName(descriptor.getName());
        return result;
    }

    /**
     * Obtains a Java class name corresponding the proto message declaration.
     */
    public static SimpleClassName ofMessage(Descriptor descriptor) {
        return ofMessage(descriptor.toProto());
    }

    /**
     * Obtains the name for a file of the class.
     */
    public FileName toFileName() {
        final FileName result = FileName.forType(value());
        return result;
    }
}
