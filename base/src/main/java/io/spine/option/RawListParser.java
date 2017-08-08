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

import com.google.common.collect.ImmutableList;
import com.google.protobuf.GeneratedMessage.GeneratedExtension;
import com.google.protobuf.GeneratedMessageV3;
import com.google.protobuf.GeneratedMessageV3.ExtendableMessage;

import java.util.Collection;
import java.util.regex.Pattern;

import static com.google.common.collect.Lists.newLinkedList;
import static io.spine.util.Exceptions.newIllegalStateException;
import static io.spine.validate.Validate.checkNotEmptyOrBlank;
import static java.util.regex.Pattern.compile;

/**
 * A parser for the options of the {@code String} type.
 *
 * <p>These options represent a list of values,
 * that are separated by the {@linkplain #VALUE_SEPARATOR separator}.
 *
 * @param <O> the type of the option handled by the parser
 * @param <D> the type of the descriptor to obtain the option
 * @param <R> the type of an element to be returned after parsing
 * @author Dmytro Grankin
 */
public abstract class RawListParser<O extends ExtendableMessage, D extends GeneratedMessageV3, R>
        extends UnknownOptionParser<O, D, R> {

    /**
     * The separator for the list values.
     *
     * <p>This separator is used across the framework.
     */
    private static final String VALUE_SEPARATOR = ",";
    private static final Pattern PATTERN_VALUES_SEPARATOR = compile(VALUE_SEPARATOR);
    private static final Pattern PATTERN_SPACE = compile(" ");

    RawListParser(GeneratedExtension<O, String> option) {
        super(option);
    }

    @Override
    public Collection<R> parse(String optionValue) {
        checkNotEmptyOrBlank(optionValue, "option value");
        final Collection<String> parts = splitOptionValue(optionValue);
        return parseElements(parts);
    }

    /**
     * Parses the {@linkplain #splitOptionValue(CharSequence) split} parts of an option value.
     *
     * @param optionParts the option parts to parse
     * @return the collection of parsed elements
     */
    private Collection<R> parseElements(Iterable<String> optionParts) {
        final Collection<R> result = newLinkedList();
        for (String part : optionParts) {
            final R element = asElement(part);
            result.add(element);
        }
        return result;
    }

    /**
     * Obtains the parsed element from the specified value.
     *
     * @param singleItemValue the item from the option value
     * @return the parsed element
     */
    protected abstract R asElement(String singleItemValue);

    /**
     * Splits the specified value using the {@linkplain #VALUE_SEPARATOR values separator}.
     *
     * @param value the option value to split
     * @return the separated parts of the value
     */
    private static Collection<String> splitOptionValue(CharSequence value) {
        final String valueWithoutSpaces = PATTERN_SPACE.matcher(value)
                                                       .replaceAll("");
        final String[] parts = PATTERN_VALUES_SEPARATOR.split(valueWithoutSpaces);
        for (String part : parts) {
            if (part.isEmpty()) {
                final String errMsg = "A blank part was found in the option `%s`.";
                throw newIllegalStateException(errMsg, value);
            }
        }
        return ImmutableList.copyOf(parts);
    }

    public static String getValueSeparator() {
        return VALUE_SEPARATOR;
    }
}
