/*
 * Copyright 2017, TeamDev Ltd. All rights reserved.
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

package io.spine.option;

import com.google.protobuf.DescriptorProtos.DescriptorProto;
import com.google.protobuf.DescriptorProtos.MessageOptions;
import com.google.protobuf.GeneratedMessage.GeneratedExtension;
import io.spine.type.TypeName;

import java.util.Collection;
import java.util.regex.Pattern;

import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Preconditions.checkNotNull;
import static com.google.common.base.Preconditions.checkState;
import static com.google.common.base.Strings.isNullOrEmpty;
import static com.google.common.collect.Lists.newLinkedList;
import static io.spine.option.UnknownOptions.getUnknownOptionValue;
import static java.util.Collections.emptyList;
import static java.util.regex.Pattern.compile;

/**
 * A parser for the options, that represent {@linkplain RawListParser raw list} of type names.
 *
 * @author Dmytro Grankin
 */
public class TypeNamesParser extends RawListParser<MessageOptions, DescriptorProto, TypeName> {

    private static final String PACKAGE_SEPARATOR = ".";
    private static final Pattern PATTERN_SPACE = compile(" ");

    /**
     * A package prefix to supply unqualified type names.
     */
    private final String packagePrefix;

    public TypeNamesParser(GeneratedExtension<MessageOptions, String> option,
                           String packagePrefix) {
        super(option);
        this.packagePrefix = checkNotNull(packagePrefix);
    }

    /**
     * Obtains the option value and {@linkplain #parse(String) parses it}.
     *
     * @param descriptor the descriptor to obtain the option value
     * @return the parsed value
     */
    @Override
    public Collection<TypeName> parse(DescriptorProto descriptor) {
        final String optionValue = getUnknownOptionValue(descriptor, getOptionNumber());
        if (optionValue == null) {
            return emptyList();
        }

        return parse(optionValue);
    }

    /**
     * Obtains type names from the specified option value and
     * supplies them with the package prefix if it is not present.
     *
     * @param optionValue the option value to parse
     * @return the type names
     */
    @Override
    public Collection<TypeName> parse(String optionValue) {
        checkArgument(!isNullOrEmpty(optionValue));
        final String optionValueWithoutSpaces = PATTERN_SPACE.matcher(optionValue)
                                                             .replaceAll("");
        final Collection<String> typeNames = splitOptionValue(optionValueWithoutSpaces);
        return normalizeTypeNames(typeNames);
    }

    /**
     * Supplies and wraps the specified type names with the package prefix if it is not present.
     *
     * @param rawTypeNames the raw type names to normalize
     * @return a collection of normalized type names
     */
    private Collection<TypeName> normalizeTypeNames(Iterable<String> rawTypeNames) {
        final Collection<TypeName> normalizedNames = newLinkedList();
        for (String rawTypeName : rawTypeNames) {
            checkState(!rawTypeName.trim()
                                   .isEmpty(), "Empty type name");
            final boolean isFqn = rawTypeName.contains(PACKAGE_SEPARATOR);
            final String typeNameValue = isFqn
                                         ? rawTypeName
                                         : packagePrefix + rawTypeName;
            final TypeName typeName = TypeName.of(typeNameValue);
            normalizedNames.add(typeName);
        }
        return normalizedNames;
    }
}
