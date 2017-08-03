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
 * that are separated by the {@linkplain #VALUES_SEPARATOR separator}.
 *
 * @param <O> {@inheritDoc}
 * @param <D> {@inheritDoc}
 * @param <R> {@inheritDoc}
 * @author Dmytro Grankin
 */
public abstract class RawListParser<O extends ExtendableMessage, D extends GeneratedMessageV3, R>
        implements OptionParser<O, D, R> {

    /**
     * The separator for the list values.
     *
     * <p>This class defines a concrete separator, because it used across
     * all {@linkplain OptionsProto Spine options}.
     */
    private static final String VALUES_SEPARATOR = ",";
    private static final Pattern PATTERN_VALUES_SEPARATOR = compile(VALUES_SEPARATOR);

    private final GeneratedExtension<O, String> option;

    protected RawListParser(GeneratedExtension<O, String> option) {
        this.option = checkNotNull(option);
    }

    /**
     * Splits the specified value using {@link #VALUES_SEPARATOR}.
     *
     * @param value the option value to split
     * @return the separated parts of the value
     */
    protected static Collection<String> splitOptionValue(CharSequence value) {
        final String[] parts = PATTERN_VALUES_SEPARATOR.split(value);
        return ImmutableList.copyOf(parts);
    }

    protected int getOptionNumber() {
        return option.getNumber();
    }

    public static String getValuesSeparator() {
        return VALUES_SEPARATOR;
    }
}
