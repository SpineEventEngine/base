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

import com.google.errorprone.annotations.Immutable;
import com.google.protobuf.Descriptors.Descriptor;

import java.io.Serializable;
import java.util.function.Predicate;

import static com.google.common.base.Preconditions.checkNotNull;
import static io.spine.util.Exceptions.newIllegalArgumentException;

/**
 * References one or more message types, and can tell if a message type
 * {@linkplain Predicate#test(Object) matches} the reference.
 */
@Immutable
public interface TypeRef extends Predicate<Descriptor>, Serializable {

    /**
     * Obtains the value of the reference.
     */
    String value();

    /**
     * Creates a type reference by parsing the passed string.
     *
     * <p>The passed string may contain a reference to:
     * <ul>
     *     <li>a simple type name (e.g. "MyType");
     *     <li>a fully-qualified type name (e.g. "some.qualified.TypeName");
     *     <li>several types under a package (e.g. "google.protobuf.*");
     *     <li>combination of such references separated with commas.
     * </ul>
     *
     * @param value a type reference to one or more types
     * @return an instance of type reference
     * @throws IllegalArgumentException if the passed string is not a valid type reference
     */
    static TypeRef parse(String value) {
        checkNotNull(value);
        ParsingChain parsing = new ParsingChain(
                value,
                BuiltIn::parseAll,
                InPackage::parse,
                DirectTypeRef::parse
        );
        TypeRef result =
                parsing.parse()
                       .orElseThrow(() -> newIllegalArgumentException(
                               "Unable to parse type reference from the value: `%s`.", value
                       ));
        return result;
    }
}
