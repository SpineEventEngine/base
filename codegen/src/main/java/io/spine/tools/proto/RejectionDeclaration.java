/*
 * Copyright 2018, TeamDev Ltd. All rights reserved.
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

package io.spine.tools.proto;

import com.google.common.annotations.VisibleForTesting;
import io.spine.tools.java.SimpleClassName;

import javax.annotation.Nullable;
import java.util.Objects;

import static com.google.protobuf.DescriptorProtos.DescriptorProto;
import static com.google.protobuf.DescriptorProtos.FileDescriptorProto;

/**
 * A code generation metadata on a rejection.
 *
 * @author Dmytro Grankin
 * @author Alexander Yevsyukov
 */
public final class RejectionDeclaration extends AbstractMessageDeclaration {

    /**
     * The suffix for the outer class name for the generated rejection messages.
     */
    @VisibleForTesting
    static final String OUTER_CLASS_NAME_SUFFIX = "Rejections";

    private final SimpleClassName outerJavaClass;

    /**
     * Creates a new instance.
     *
     * @param message the declaration of the rejection message
     * @param file    the file that contains the rejection
     */
    RejectionDeclaration(DescriptorProto message, FileDescriptorProto file) {
        super(message, file);
        this.outerJavaClass = SimpleClassName.outerOf(file);
    }

    /**
     * Returns {@code true} if the class name ends with {@code “Rejections”},
     * {@code false} otherwise.
     */
    public static boolean isValidOuterClassName(SimpleClassName className) {
        final boolean result = className.value()
                                        .endsWith(OUTER_CLASS_NAME_SUFFIX);
        return result;
    }

    public SimpleClassName getOuterJavaClass() {
        return outerJavaClass;
    }

    @Override
    public int hashCode() {
        return 31 * super.hashCode() + Objects.hash(outerJavaClass);
    }

    @Override
    public boolean equals(@Nullable Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null || getClass() != obj.getClass()) {
            return false;
        }
        if (!super.equals(obj)) {
            return false;
        }
        final RejectionDeclaration other = (RejectionDeclaration) obj;
        return Objects.equals(this.outerJavaClass, other.outerJavaClass);
    }
}
