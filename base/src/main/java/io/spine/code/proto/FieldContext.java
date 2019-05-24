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
import com.google.errorprone.annotations.Immutable;
import com.google.protobuf.Descriptors.FieldDescriptor;
import io.spine.base.FieldPath;

import java.util.Optional;

import static com.google.common.base.Preconditions.checkState;

/**
 * Provides information about a proto field in the nesting hierarchy.
 */
@Immutable
public final class FieldContext {

    /**
     * Descriptors of fields in the nesting hierarchy.
     *
     * <p>The list starts from the top-most field and ends with the descriptor
     * of the target field.
     *
     * <p>Suppose we have the following declarations:
     * <pre>{@code
     * message User {
     *     UserId id = 1;
     * }
     *
     * message UserId {
     *     string value = 1;
     * }
     * }</pre>
     *
     * <p>The value of this field for the {@code value} field of the {@code UserId} is
     * {@code [FieldDescriptor_for_id, FieldDescriptor_for_value]}.
     */
    private final ImmutableList<FieldDescriptor> descriptors;

    /**
     * Provides field names in the nesting hierarchy.
     */
    private final FieldPath fieldPath;

    private FieldContext(ImmutableList<FieldDescriptor> descriptors) {
        this.descriptors = descriptors;
        this.fieldPath = toPath(descriptors);
    }

    /**
     * Creates descriptor context for the specified field.
     *
     * @param field the field of the context to create
     * @return the field context
     */
    public static FieldContext create(FieldDescriptor field) {
        return new FieldContext(ImmutableList.of(field));
    }

    /**
     * Creates empty descriptor context.
     *
     * @return the descriptor context
     */
    public static FieldContext empty() {
        return new FieldContext(ImmutableList.of());
    }

    /**
     * Obtains {@code FieldContext} for the specified child.
     *
     * @param child the child descriptor
     * @return the child descriptor context
     */
    public FieldContext forChild(FieldDescriptor child) {
        return new FieldContext(
                ImmutableList.<FieldDescriptor>builder()
                        .addAll(descriptors)
                        .add(child)
                        .build()
        );
    }

    /**
     * Obtains target of this context.
     *
     * @return the target descriptor
     */
    public FieldDescriptor target() {
        checkState(!descriptors.isEmpty(), "Empty context cannot have a target.");
        int targetIndex = descriptors.size() - 1;
        return descriptors.get(targetIndex);
    }

    private Optional<FieldDescriptor> targetParent() {
        int targetParentIndex = descriptors.size() - 2;
        boolean parentExists = targetParentIndex > -1;
        return parentExists
               ? Optional.of(descriptors.get(targetParentIndex))
               : Optional.empty();
    }

    /**
     * Obtains field path for the target of the context.
     *
     * @return the field path
     */
    public FieldPath fieldPath() {
        return fieldPath;
    }

    /**
     * Determines whether this context has the same target and
     * the same parent as the specified context.
     *
     * @param other the context to check
     * @return {@code true} if this context has the same target and the same parent
     */
    public boolean hasSameTargetAndParent(FieldContext other) {
        String thisTargetName = target().getFullName();
        String otherTargetName = other.target()
                                      .getFullName();
        boolean sameTarget = thisTargetName.equals(otherTargetName);
        if (!sameTarget) {
            return false;
        }
        Optional<String> parentFromThis = targetParent()
                .map(FieldDescriptor::getFullName);
        Optional<String> parentFromOther = other
                .targetParent()
                .map(FieldDescriptor::getFullName);
        boolean bothHaveParents = parentFromThis.isPresent() && parentFromOther.isPresent();
        return bothHaveParents && parentFromThis.get()
                                                .equals(parentFromOther.get());
    }

    private static FieldPath toPath(Iterable<FieldDescriptor> descriptors) {
        FieldPath.Builder builder = FieldPath.newBuilder();
        for (FieldDescriptor descriptor : descriptors) {
            String fieldName = descriptor.getName();
            builder = builder.addFieldName(fieldName);
        }
        return builder.build();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }

        FieldContext that = (FieldContext) o;

        return descriptors.equals(that.descriptors);
    }

    @Override
    public int hashCode() {
        return descriptors.hashCode();
    }
}
