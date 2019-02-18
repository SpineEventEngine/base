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

package io.spine.code.proto.enrichment;

import com.google.common.collect.ImmutableList;
import com.google.errorprone.annotations.Immutable;
import com.google.protobuf.Descriptors.Descriptor;
import com.google.protobuf.Descriptors.FieldDescriptor;

import java.util.Optional;

import static io.spine.util.Exceptions.newIllegalArgumentException;

/**
 * Provides references to fields of messages that serve as input for creating a field of
 * an enrichment message.
 */
@Immutable
final class FieldDef {

    /**
     * The descriptor of the enrichment type field.
     */
    private final FieldDescriptor descriptor;

    /**
     * Field references found in the {@code (by)} option of the field.
     */
    private final ImmutableList<FieldRef> sources;

    FieldDef(FieldDescriptor field) {
        this.descriptor = field;
        ImmutableList<FieldRef> sources = FieldRef.allFrom(field.toProto());
        checkDuplicatingContextRef(field, sources);
        this.sources = sources;
    }

    /**
     * Ensures that field references has at most one reference to a message context field.
     */
    private static
    void checkDuplicatingContextRef(FieldDescriptor field, ImmutableList<FieldRef> sources) {
        long count = sources.stream()
                            .filter(FieldRef::isContext)
                            .count();
        if (count > 1) {
            throw newIllegalArgumentException(
                    "There can be only one `context` field reference per enrichment field." +
                            " The field `%s` has %d references: `%s`.",
                    field.getFullName(), count, sources
            );
        }
    }

    boolean matchesType(Descriptor type) {
        boolean result = sources.stream()
                                .anyMatch(r -> r.matchesType(type));
        return result;
    }

    /**
     * Obtains the field descriptor of the source message field.
     *
     * @param type the passed type must have a {@linkplain #matchesType(Descriptor) matching} field
     * @return the source field descriptor
     */
    FieldSource find(Descriptor type) {
        FieldRef contextReference = null;
        for (FieldRef ref : sources) {
            Optional<FieldDescriptor> field = ref.find(type);
            if (field.isPresent()) {
                return new FieldSource(field.get(), ref);
            }
            if (ref.isContext()) {
                contextReference = ref;
            }
        }
        // None of the field references match the type, but we found context reference.
        // Let's use it as the source.
        if (contextReference != null) {
            return new FieldSource(null, contextReference);
        }
        // None of the field references match. We don't have a context reference either.
        // There is no information on how to construct the enrichment field.
        throw noneFieldMatches(type);
    }

    private IllegalArgumentException noneFieldMatches(Descriptor type) {
        return newIllegalArgumentException(
                "Unable to obtain any of the referenced fields `%s` in the message type `%s`.",
                sources, type.getFullName()
        );
    }

    /**
     * Obtains the descriptor of the enrichment field.
     */
    FieldDescriptor descriptor() {
        return descriptor;
    }
}
