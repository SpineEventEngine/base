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

import com.google.common.collect.ImmutableList;
import com.google.errorprone.annotations.Immutable;
import com.google.protobuf.Descriptors.Descriptor;

import java.util.Optional;
import java.util.function.Predicate;
import java.util.function.Supplier;

import static com.google.common.base.Preconditions.checkNotNull;
import static io.spine.util.Exceptions.newIllegalArgumentException;

/**
 * References one or more message types, and can tell if a message type
 * {@linkplain Predicate#test(Object) matches} the reference.
 */
@Immutable
public interface TypeRef extends Predicate<Descriptor> {

    /**
     * Obtains the value of the reference.
     */
    String value();

    static TypeRef parse(String value) {
        checkNotNull(value);

        ImmutableList<Supplier<Optional<TypeRef>>> suppliers = ImmutableList.of(
                () -> BuiltIn.find(value),
                () -> InPackage.parse(value),
                () -> Direct.parse(value)
        );

        for (Supplier<Optional<TypeRef>> supplier : suppliers) {
            Optional<TypeRef> found = supplier.get();
            if (found.isPresent()) {
                return found.get();
            }
        }

        throw newIllegalArgumentException(
                "Unable to parse type reference from the value: `%s`.",
                value
        );
    }
}
