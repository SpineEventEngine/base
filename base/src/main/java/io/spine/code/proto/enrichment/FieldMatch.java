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

import com.google.common.collect.ImmutableBiMap;
import com.google.common.collect.ImmutableList;
import com.google.errorprone.annotations.Immutable;
import com.google.protobuf.Descriptors.FieldDescriptor;
import io.spine.type.MessageType;

import java.util.Objects;

import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Preconditions.checkNotNull;
import static com.google.common.collect.ImmutableBiMap.toImmutableBiMap;

/**
 * Matches fields an enrichment source to the fields of the enrichment.
 */
@Immutable
final class FieldMatch {

    /**
     * The source type of the enrichment.
     */
    private final MessageType sourceType;

    /**
     * The target type of the enrichment.
     */
    private final MessageType targetType;

    /**
     * Maps the descriptor of the enrichment field, to instruction of how to obtain the value
     * of the enrichment field.
     */
    private final ImmutableBiMap<FieldDescriptor, FieldSource> targetToSource;

    FieldMatch(MessageType sourceType, MessageType targetType, ImmutableList<FieldDef> fields) {
        checkNotNull(sourceType);
        checkNotNull(fields);
        checkArgument(
                !fields.isEmpty(),
                "Enrichment type (`%s`) must have at least one field.", targetType
        );
        this.sourceType = sourceType;
        this.targetType = targetType;
        this.targetToSource =
                fields.stream()
                      .collect(toImmutableBiMap(
                              FieldDef::descriptor,
                              f -> f.find(sourceType.descriptor())
                      ));
    }

    FieldSource sourceOf(FieldDescriptor target) {
        FieldSource source = targetToSource.get(target);
        checkNotNull(source,
                     "Unable to find source field for the target field `%s`.",
                     target.getFullName());
        return source;
    }

    @Override
    public int hashCode() {
        return Objects.hash(sourceType, targetType, targetToSource);
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (!(obj instanceof FieldMatch)) {
            return false;
        }
        final FieldMatch other = (FieldMatch) obj;
        return Objects.equals(sourceType, other.sourceType)
                && Objects.equals(targetType, other.targetType)
                && Objects.equals(targetToSource, other.targetToSource);
    }
}
