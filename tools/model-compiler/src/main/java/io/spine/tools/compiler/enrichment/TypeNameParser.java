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

package io.spine.tools.compiler.enrichment;

import com.google.common.annotations.VisibleForTesting;
import com.google.common.base.Joiner;
import com.google.common.base.Splitter;
import com.google.common.collect.ImmutableList;
import com.google.protobuf.DescriptorProtos.DescriptorProto;
import com.google.protobuf.DescriptorProtos.MessageOptions;
import com.google.protobuf.Extension;
import io.spine.option.Options;
import io.spine.option.OptionsProto;
import io.spine.type.TypeName;

import static com.google.common.collect.ImmutableList.toImmutableList;
import static io.spine.option.OptionsProto.enrichment;
import static io.spine.option.OptionsProto.enrichmentFor;
import static java.util.Collections.emptyList;
import static java.util.Comparator.comparing;

/**
 * A parser of {@link TypeName}s contained in a message option.
 */
final class TypeNameParser {

    private static final char TYPE_NAME_SEPARATOR = ',';

    /**
     * A joiner for Protobuf types.
     *
     * <p>Joins strings with the {@code ,} (comma) character.
     *
     * @see #splitter
     */
    static final Joiner joiner = Joiner.on(TYPE_NAME_SEPARATOR);

    /**
     * A splitter for Protobuf types.
     *
     * <p>Splits strings by the {@code ,} (comma) character.
     *
     * @see #joiner
     */
    private static final Splitter splitter = Splitter.on(TYPE_NAME_SEPARATOR);

    private static final String PACKAGE_SEPARATOR = String.valueOf(TypeName.PACKAGE_SEPARATOR);

    private final Extension<MessageOptions, String> option;
    private final String packagePrefix;

    /**
     * Obtains the parser for the {@link OptionsProto#enrichmentFor} option values.
     */
    static TypeNameParser ofEnrichmentFor(String packagePrefix) {
        return new TypeNameParser(enrichmentFor, packagePrefix);
    }

    /**
     * Obtains the instance for the {@link OptionsProto#enrichment} option values.
     */
    static TypeNameParser ofEnrichment(String packagePrefix) {
        return new TypeNameParser(enrichment, packagePrefix);
    }

    private TypeNameParser(Extension<MessageOptions, String> option, String packagePrefix) {
        this.option = option;
        this.packagePrefix = packagePrefix;
    }

    /**
     * Parses the {@linkplain #option} from the given message descriptor and
     * {@linkplain #splitter splits} it into separate {@link TypeName}s.
     *
     * <p>If a type name is not an FQN, the {@code packagePrefix} is added to it.
     *
     * @param descriptor the descriptor to parse
     * @return the list of parsed message types or an empty list if the option is absent or empty
     */
    ImmutableList<TypeName> parse(DescriptorProto descriptor) {
        ImmutableList<TypeName> result =
                Options.option(descriptor, option)
                       .map(splitter::splitToList)
                       .orElse(emptyList())
                       .stream()
                       .map(this::toQualified)
                       .sorted(comparing(TypeName::value))
                       .collect(toImmutableList());
        return result;
    }

    @VisibleForTesting
    TypeName toQualified(String value) {
        //TODO:2019-01-25:alexander.yevsyukov: Checking only by having the dot in the name is
        // the limitation which prevents from referencing nested types.
        boolean isQualified = value.contains(PACKAGE_SEPARATOR);
        String typeNameValue = isQualified
                               ? value
                               : packagePrefix + value;
        return TypeName.of(typeNameValue);
    }
}
