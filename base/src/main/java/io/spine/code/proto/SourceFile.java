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

package io.spine.code.proto;

import com.google.common.collect.ImmutableList;
import com.google.protobuf.DescriptorProtos.DescriptorProto;
import com.google.protobuf.DescriptorProtos.FileDescriptorProto;
import com.google.protobuf.Descriptors.Descriptor;
import com.google.protobuf.Descriptors.FileDescriptor;
import io.spine.code.AbstractSourceFile;
import io.spine.code.java.SimpleClassName;
import io.spine.logging.Logging;
import io.spine.type.MessageType;
import io.spine.type.RejectionType;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.function.Predicate;

import static com.google.common.base.Preconditions.checkNotNull;

/**
 * A Protobuf file which also gives access to its {@link FileDescriptorProto descriptor}.
 */
public class SourceFile extends AbstractSourceFile implements Logging {

    private final FileDescriptor descriptor;

    SourceFile(FileDescriptor descriptor) {
        super(toPath(descriptor));
        this.descriptor = descriptor;
    }

    /**
     * Creates a new instance for the passed file descriptor.
     */
    public static SourceFile from(FileDescriptor file) {
        return new SourceFile(file);
    }

    private static Path toPath(FileDescriptor file) {
        checkNotNull(file);
        Path result = Paths.get(file.getName());
        return result;
    }

    /**
     * Returns {@code true} if the source file matches conventions for rejection files.
     *
     * <p>A valid rejections file must have:
     * <ul>
     *     <li>The file name which ends on
     *         {@link io.spine.base.MessageFile#REJECTIONS “rejections.proto”}.
     *     <li>The option {@code java_multiple_files} set to {@code false}.
     *     <li>
     * </ul>
     * Also such file does not have the option {@code java_outer_classname} or value which ends
     * with {@linkplain RejectionType#isValidOuterClassName(SimpleClassName)} “Rejections”}.
     */
    public boolean isRejections() {
        // By convention, rejections are generated into one file.
        if (descriptor.getOptions()
                      .getJavaMultipleFiles()) {
            return false;
        }
        Optional<SimpleClassName> outerClass = SimpleClassName.declaredOuterClassName(descriptor);

        if (!outerClass.isPresent()) {
            // There's no outer class name given in options.
            // Assuming the file name ends with `rejections.proto`, it's a good rejections file.
            return true;
        }

        boolean result = RejectionType.isValidOuterClassName(outerClass.get());
        return result;
    }

    /**
     * Obtains descriptor of the file.
     */
    public FileDescriptor getDescriptor() {
        return descriptor;
    }

    /**
     * Obtains all message declarations that match the passed predicate.
     */
    public List<MessageType> allThat(Predicate<DescriptorProto> predicate) {
        ImmutableList.Builder<MessageType> result = ImmutableList.builder();
        for (Descriptor messageType : descriptor.getMessageTypes()) {
            MessageType declaration = new MessageType(messageType);
            _debug("Testing {} to match {}", declaration, predicate);
            if (predicate.test(messageType.toProto())) {
                result.add(declaration);
            }
            Collection<MessageType> allNested =
                    declaration.nestedTypesThat(predicate);
            result.addAll(allNested);
        }
        return result.build();
    }

}
