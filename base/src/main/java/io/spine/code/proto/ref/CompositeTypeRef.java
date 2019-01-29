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

package io.spine.code.proto.ref;

import com.google.common.base.Joiner;
import com.google.common.collect.ImmutableList;
import com.google.errorprone.annotations.Immutable;
import com.google.protobuf.Descriptors.Descriptor;

import java.io.Serializable;
import java.util.Objects;
import java.util.Optional;

import static com.google.common.base.Preconditions.checkArgument;

/**
 * A type reference consisting of two or more type references.
 */
@Immutable
public class CompositeTypeRef implements TypeRef, Serializable {

    private static final long serialVersionUID = 0L;

    private final ImmutableList<TypeRef> elements;

    CompositeTypeRef(Iterable<TypeRef> elements) {
        this.elements = ImmutableList.copyOf(elements);
        int size = this.elements.size();
        checkArgument(size > 1, "Composite type reference must have two or more elements.");
    }

    @Override
    public boolean test(Descriptor message) {
        Optional<TypeRef> found =
                elements.stream()
                        .filter(e -> test(message))
                        .findFirst();
        return found.isPresent();
    }

    @Override
    public int hashCode() {
        return Objects.hash(elements);
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (!(obj instanceof CompositeTypeRef)) {
            return false;
        }
        final CompositeTypeRef other = (CompositeTypeRef) obj;
        return Objects.equals(this.elements, other.elements);
    }

    @Override
    public String value() {
        return Joiner.on(',').join(elements);
    }

    @Override
    public String toString() {
        return '[' + value() + ']';
    }
}
