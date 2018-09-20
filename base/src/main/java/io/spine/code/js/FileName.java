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

package io.spine.code.js;

import com.google.protobuf.DescriptorProtos.FileDescriptorProto;
import com.google.protobuf.Descriptors.FileDescriptor;
import io.spine.code.AbstractFileName;

import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Preconditions.checkNotNull;
import static io.spine.util.Preconditions2.checkNotEmptyOrBlank;

/**
 * The JavaScript source file name.
 *
 * <p>When being created from {@link FileDescriptor}, the {@code .proto} extension is replaced by
 * {@code _pb.js} suffix, as per Protobuf standards.
 *
 * <p>The {@code FileName} is always relative to the sources root, e.g. generated proto's root.
 *
 * @author Dmytro Kuzmin
 */
public final class FileName extends AbstractFileName<FileName> {

    private static final long serialVersionUID = 0L;

    /** The suffix automatically appended to the files generated by Protobuf JS. */
    private static final String SUFFIX = "_pb";

    /** The standard file extension. */
    private static final String EXTENSION = ".js";

    /**
     * The path separator used in Javascript imports. Not platform-dependant.
     */
    private static final String PATH_SEPARATOR = "/";

    private FileName(String value) {
        super(value);
    }

    /**
     * Creates new JavaScript file name with the passed value.
     */
    @SuppressWarnings("ResultOfMethodCallIgnored") // Method annotated with `@CanIgnoreReturnValue`.
    public static FileName of(String value) {
        checkNotEmptyOrBlank(value);
        checkArgument(value.endsWith(EXTENSION));
        return new FileName(value);
    }

    /**
     * Gets the JS proto definition name for the proto file.
     *
     * @param descriptor
     *         the descriptor of the file
     * @return the JS proto file name
     */
    public static FileName from(FileDescriptorProto descriptor) {
        checkNotNull(descriptor);
        io.spine.code.proto.FileName protoFileName = io.spine.code.proto.FileName.from(descriptor);
        String fileName = protoFileName.nameWithoutExtension() + SUFFIX + EXTENSION;
        return of(fileName);
    }

    /**
     * Obtains the file name from the passed descriptor.
     */
    public static FileName from(FileDescriptor descriptor) {
        checkNotNull(descriptor);
        return from(descriptor.toProto());
    }

    public String[] pathElements() {
        String[] result = value().split(PATH_SEPARATOR);
        return result;
    }
}
