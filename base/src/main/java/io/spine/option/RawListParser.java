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

import static com.google.common.base.Preconditions.checkNotNull;
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
        implements OptionParser<D, R> {

    /**
     * The separator for the list values.
     *
     * <p>This separator is used across the framework.
     */
    private static final String VALUE_SEPARATOR = ",";
    private static final Pattern PATTERN_VALUES_SEPARATOR = compile(VALUE_SEPARATOR);

    private final int optionNumber;

    protected RawListParser(GeneratedExtension<O, String> option) {
        this.optionNumber = checkNotNull(option).getNumber();
    }

    /**
     * Splits the specified value using the {@linkplain #VALUE_SEPARATOR values separator}.
     *
     * @param value the option value to split
     * @return the separated parts of the value
     */
    protected static Collection<String> splitOptionValue(CharSequence value) {
        final String[] parts = PATTERN_VALUES_SEPARATOR.split(value);
        return ImmutableList.copyOf(parts);
    }

    /**
     * Obtains the number of the option to parse.
     *
     * @return the option number
     */
    protected int getOptionNumber() {
        return optionNumber;
    }

    public static String getValueSeparator() {
        return VALUE_SEPARATOR;
    }
}
