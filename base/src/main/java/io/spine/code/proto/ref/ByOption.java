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

import com.google.common.annotations.VisibleForTesting;
import com.google.common.collect.ImmutableList;
import com.google.protobuf.DescriptorProtos.FieldOptions;
import com.google.protobuf.Descriptors.FieldDescriptor;
import io.spine.code.proto.StringOption;
import io.spine.option.OptionsProto;

import java.util.Collection;
import java.util.regex.Pattern;

import static com.google.common.collect.ImmutableList.toImmutableList;
import static java.util.regex.Pattern.compile;

/**
 * Obtains source type names from the {@code "by"} field option of a message.
 */
public final class ByOption extends StringOption<Collection<FieldRef>,
                                                 FieldDescriptor,
                                                 FieldOptions> {

    /**
     * Separates two or more alternative references.
     */
    private static final String PIPE_SEPARATOR = "|";
    private static final Pattern PATTERN_PIPE_SEPARATOR = compile("\\|");
    private static final Pattern SPACE = compile(" ", Pattern.LITERAL);

    /** Prevents instantiation of this utility class. */
    private ByOption() {
        super(OptionsProto.by);
    }

    /**
     * Parses the string with the value of the {@code (by)} option.
     *
     * @see #allFrom(FieldDescriptor)
     */
    @VisibleForTesting
    static ImmutableList<String> parse(String rawValue) {
        String byArgument = SPACE.matcher(rawValue)
                                 .replaceAll("");
        String[] result;
        result = byArgument.contains(PIPE_SEPARATOR)
                 ? PATTERN_PIPE_SEPARATOR.split(byArgument)
                 : new String[]{byArgument};
        return ImmutableList.copyOf(result);
    }

    @Override
    protected Collection<FieldRef> parsedValueFrom(FieldDescriptor field) {
        String byOptionExpression = valueFrom(field).orElse("");
        return parseExpression(byOptionExpression);
    }

    /** Obtains all of the fields referenced in the {@code (by)} option of the specified field. */
    static ImmutableList<FieldRef> allFrom(FieldDescriptor field) {
        ByOption option = new ByOption();
        Collection<FieldRef> result = option.parsedValueFrom(field);
        return ImmutableList.copyOf(result);
    }

    private static Collection<FieldRef> parseExpression(String byExpression) {
        String trimmed = byExpression.trim();
        ImmutableList<String> rawFieldRefs = parse(trimmed);
        return rawFieldRefs.stream()
                           .map(FieldRef::new)
                           .collect(toImmutableList());
    }

    @Override
    protected FieldOptions optionsFrom(FieldDescriptor field) {
        return field.getOptions();
    }
}
