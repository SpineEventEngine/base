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

import com.google.protobuf.Descriptors.Descriptor;
import io.spine.annotation.Internal;
import io.spine.code.proto.MessageType;

import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Preconditions.checkNotNull;
import static io.spine.code.java.ClassName.OUTER_CLASS_DELIMITER;
import static io.spine.code.java.Names.containingClassPrefix;
import static io.spine.code.java.Names.outerClassPrefix;
import static io.spine.code.proto.MessageType.VBUILDER_SUFFIX;

/**
 * Obtains a simple class name for a Validating Builder of a message.
 *
 * <p>If a message is top-level, the validating builder name would be concatenation
 * of the message type name and {@link MessageType#VBUILDER_SUFFIX}.
 *
 * <p>For nested messages and messages with an outer class, the name of the validating builder
 * would be concatenation of simple class names of the nesting hierarchy followed with
 * {@link MessageType#VBUILDER_SUFFIX}.
 */
@Internal
public final class VBuilderClassName {

    private final MessageType type;

    /**
     * Obtains the name of a Validating Builder class that corresponds to the passed message.
     *
     * @throws java.lang.IllegalArgumentException
     *  if the passed message type cannot have a validating builder
     */
    public static SimpleClassName of(MessageType type) {
        checkNotNull(type);
        checkArgument(type.isCustom(), "Validating Builder is not available for `%s`", type.name());

        VBuilderClassName name = new VBuilderClassName(type);
        return name.get();
    }

    private VBuilderClassName(MessageType type) {
        this.type = type;
    }

    private SimpleClassName get() {
        if (type.isTopLevel()) {
            SimpleClassName result = type.javaClassName().toSimple().with(VBUILDER_SUFFIX);
            return result;
        }

        // Nested: either with outer class, or with enclosing message, or both.
        Descriptor descriptor = type.descriptor();
        String outerPrefix = outerClassPrefix(descriptor.getFile())
                .replace(String.valueOf(OUTER_CLASS_DELIMITER), "");
        String enclosingPrefix = containingClassPrefix(descriptor.getContainingType())
                .replace(String.valueOf(OUTER_CLASS_DELIMITER), "");
        String typeName = descriptor.getName();
        return SimpleClassName.create(outerPrefix + enclosingPrefix + typeName + VBUILDER_SUFFIX);
    }
}
