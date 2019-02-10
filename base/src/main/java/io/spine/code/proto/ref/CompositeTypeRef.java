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
import com.google.common.base.Splitter;
import com.google.common.collect.ImmutableList;
import com.google.errorprone.annotations.Immutable;
import com.google.protobuf.Descriptors.Descriptor;

import java.util.Objects;
import java.util.Optional;

import static com.google.common.base.Preconditions.checkArgument;
import static io.spine.util.Exceptions.newIllegalArgumentException;

/**
 * A type reference consisting of two or more type references.
 */
@Immutable
public class CompositeTypeRef implements TypeRef {

    private static final long serialVersionUID = 0L;

    /** Separator for two or more type references. */
    static final String SEPARATOR = ",";

    /** A splitter for type references separated by comma. */
    private static final Splitter splitter = Splitter.on(SEPARATOR);

    /** Two or more type references. */
    private final ImmutableList<TypeRef> elements;

    /**
     * Parses the passed value returning composite type reference, if the passed value
     * is comma-separated. Otherwise returns empty {@code Optional}.
     */
    static Optional<TypeRef> parse(String value) {
        if (!value.contains(SEPARATOR)) {
            return Optional.empty();
        }
        Optional<TypeRef> result = Optional.of(doParse(value));
        return result;
    }

    /**
     * Parses a value of a composite type references.
     *
     * <p>The value must contain two or more type references separated with comma.
     * Each reference must be a valid type reference.
     *
     * @param value
     *         the string to parse
     * @return a new instance of the composite reference
     * @throws IllegalArgumentException
     *         if the passed string contains only one type reference, or one of the strings
     *         from the reference is not a valid type reference
     */
    static CompositeTypeRef doParse(String value) {
        checkContainsComma(value);
        Iterable<String> parts = splitter.split(value);
        ImmutableList.Builder<TypeRef> builder = ImmutableList.builder();
        for (String part : parts) {
            TypeRef ref = parsePart(part);
            builder.add(ref);
        }
        CompositeTypeRef result = new CompositeTypeRef(builder.build());
        return result;
    }

    private static TypeRef parsePart(String part) {
        Parsing parsing = new Parsing(part, InPackage::parse, DirectTypeRef::parse);
        TypeRef result =
                parsing.parse()
                       .orElseThrow(() -> newIllegalArgumentException(
                               "The value (`%s`) cannot be used in a composite type reference.",
                               part
                       ));
        return result;
    }

    private static void checkContainsComma(String value) {
        checkArgument(
                value.contains(SEPARATOR),
                "The value (`%s`) is not a composite type reference." +
                " A composite type reference must contain two or more type references" +
                " separated with commas."
        );
    }

    CompositeTypeRef(Iterable<TypeRef> elements) {
        this.elements = ImmutableList.copyOf(elements);
        int size = this.elements.size();
        checkArgument(size > 1, "Composite type reference must have two or more elements.");
    }

    @Override
    public boolean test(Descriptor message) {
        Optional<TypeRef> found =
                elements.stream()
                        .filter(e -> e.test(message))
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
