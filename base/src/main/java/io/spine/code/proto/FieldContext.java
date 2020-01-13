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

import com.google.errorprone.annotations.Immutable;
import com.google.protobuf.Descriptors.FieldDescriptor;
import io.spine.base.FieldPath;
import org.checkerframework.checker.nullness.qual.Nullable;

import java.util.Objects;
import java.util.Optional;

import static com.google.common.base.Preconditions.checkNotNull;

/**
 * Provides information about a proto field in the nesting hierarchy.
 */
@Immutable
public final class FieldContext {

    /**
     * The descriptor of the current proto field.
     *
     * <p>Is {@code null} if this field context is empty.
     */
    private final @Nullable FieldDescriptor target;

    /**
     * The context of the parent field context.
     *
     * <p>Is {@code null} if the {@linkplain #target target field} has no parent.
     */
    private final @Nullable FieldContext parent;

    private FieldContext() {
        this.target = null;
        this.parent = null;
    }

    private FieldContext(FieldDescriptor target) {
        this.target = target;
        this.parent = null;
    }

    private FieldContext(FieldContext parent, FieldDescriptor target) {
        this.target = target;
        this.parent = parent;
    }

    /**
     * Creates descriptor context for the specified field.
     *
     * @param field
     *         the field of the context to create
     * @return the field context
     */
    public static FieldContext create(FieldDescriptor field) {
        return new FieldContext(field);
    }

    /**
     * Creates empty descriptor context.
     *
     * @return the descriptor context
     */
    public static FieldContext empty() {
        return new FieldContext();
    }

    /**
     * Obtains {@code FieldContext} for the specified child.
     *
     * @param child
     *         the child descriptor
     * @return the child descriptor context
     */
    public FieldContext forChild(FieldDescriptor child) {
        return new FieldContext(this, child);
    }

    /**
     * Obtains {@code FieldContext} for the specified child.
     *
     * @param child
     *         the child declaration
     * @return the child declaration context
     */
    public FieldContext forChild(FieldDeclaration child) {
        return forChild(child.descriptor());
    }

    /**
     * Obtains target of this context.
     *
     * @return the target descriptor
     */
    public FieldDescriptor target() {
        return checkNotNull(target, "Empty context cannot have a target.");
    }

    /**
     * Obtains target of this context as a {@link FieldDeclaration}.
     *
     * @return the target declaration
     */
    public FieldDeclaration targetDeclaration() {
        FieldDescriptor target = target();
        return new FieldDeclaration(target);
    }

    private Optional<FieldDescriptor> targetParent() {
        return parent == null ? Optional.empty() : Optional.ofNullable(parent.target);
    }

    /**
     * Obtains field path for the target of the context.
     *
     * @return the field path
     */
    public FieldPath fieldPath() {
        if (target == null) {
            return FieldPath.getDefaultInstance();
        }
        if (parent == null) {
            return FieldPath.newBuilder()
                            .addFieldName(target.getName())
                            .build();
        }
        return parent.fieldPath()
                     .toBuilder()
                     .addFieldName(target.getName())
                     .build();
    }

    /**
     * Determines whether this context has the same target and
     * the same parent as the specified context.
     *
     * @param other
     *         the context to check
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

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        FieldContext context = (FieldContext) o;
        return Objects.equals(targetNameOrEmpty(), context.targetNameOrEmpty())
                && Objects.equals(parent, context.parent);
    }

    @Override
    public int hashCode() {
        return Objects.hash(targetNameOrEmpty(), parent);
    }

    private String targetNameOrEmpty() {
        return target != null ? target.getFullName() : "";
    }
}
