/*
 * Copyright 2020, TeamDev. All rights reserved.
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

package io.spine.validate;

import com.google.common.base.Splitter;
import com.google.common.collect.ImmutableSet;
import com.google.errorprone.annotations.Immutable;
import io.spine.code.proto.FieldDeclaration;
import io.spine.type.MessageType;

import java.util.Collection;
import java.util.List;
import java.util.regex.Pattern;

import static com.google.common.collect.ImmutableSet.toImmutableSet;

/**
 * A combination of required fields found in the message value.
 */
@Immutable
public final class Alternative {

    /**
     * The pattern to remove whitespace from the option field value.
     */
    private static final Pattern WHITESPACE = Pattern.compile("\\s+");

    /**
     * Splits Protobuf field names separated with a logical disjunction (OR) literal {@literal |}.
     */
    private static final Splitter orSplitter = Splitter.on('|');

    /**
     * Splits Protobuf field names separated with a logical conjunction (AND) literal {@literal &}.
     */
    private static final Splitter andSplitter = Splitter.on('&');

    private final ImmutableSet<FieldDeclaration> fields;

    private Alternative(ImmutableSet<FieldDeclaration> fields) {
        this.fields = fields;
    }

    /**
     * Parses field combinations from the given raw input.
     *
     * <p>Fields may be combined via `{@code |}` ("or") or `{@code &}` ("and") operators. The "and"
     * operator always has a priority. Brackets are not supported.
     *
     * @param notation
     *         the field expression
     * @param type
     *         the type which declares the fields
     * @return a set of parsed alternatives
     */
    public static ImmutableSet<Alternative> parse(String notation, MessageType type) {
        ImmutableSet.Builder<Alternative> alternatives = ImmutableSet.builder();
        String whiteSpaceRemoved = WHITESPACE.matcher(notation)
                                             .replaceAll("");
        Iterable<String> parts = orSplitter.split(whiteSpaceRemoved);
        for (String part : parts) {
            List<String> fieldNames = andSplitter.splitToList(part);
            alternatives.add(ofCombination(fieldNames, type));
        }
        return alternatives.build();
    }

    private static Alternative ofCombination(Collection<String> fieldNames, MessageType type) {
        ImmutableSet<FieldDeclaration> fields = fieldNames
                .stream()
                .map(type::field)
                .collect(toImmutableSet());
        return new Alternative(fields);
    }

    /**
     * Obtains fields joined in this combination.
     */
    public ImmutableSet<FieldDeclaration> fields() {
        return fields;
    }
}
