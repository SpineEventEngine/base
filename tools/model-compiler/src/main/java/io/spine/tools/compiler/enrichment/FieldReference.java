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

import com.google.common.collect.ImmutableList;
import com.google.protobuf.DescriptorProtos.FieldDescriptorProto;
import io.spine.code.proto.FieldName;
import io.spine.value.StringTypeValue;

import java.util.List;
import java.util.regex.Pattern;

import static com.google.common.base.Preconditions.checkState;
import static com.google.common.base.Strings.isNullOrEmpty;
import static io.spine.option.OptionsProto.by;
import static io.spine.util.Exceptions.newIllegalArgumentException;
import static java.util.regex.Pattern.compile;

/**
 * A reference to a field found in the {@code "by"} option value.
 *
 * @author Alexander Yevsyukov
 */
class FieldReference extends StringTypeValue {

    private static final long serialVersionUID = 0L;
    /**
     * Wildcard option used in {@code "by"} field option.
     *
     * <p>{@code string enrichment_value [(by) = "*.my_event_id"];} tells that this enrichment
     * may have any target event types. That's why an FQN of the target type is replaced by
     * this wildcard option.
     */
    static final String ANY_BY_OPTION_TARGET = "*";

    private static final String PIPE_SEPARATOR = "|";
    private static final Pattern PATTERN_PIPE_SEPARATOR = compile("\\|");

    private FieldReference(String value) {
        super(value);
    }

    static List<FieldReference> allFrom(FieldDescriptorProto field) {
        String[] found = parse(field);

        ImmutableList.Builder<FieldReference> result = ImmutableList.builder();
        for (String ref : found) {
            result.add(new FieldReference(ref));
        }
        return result.build();
    }

    private static String[] parse(FieldDescriptorProto field) {
        String byArgument = field.getOptions()
                                 .getExtension(by);
        if (isNullOrEmpty(byArgument)) {
            throw newIllegalArgumentException("There is no `by` option in the passed field %s",
                                              field.getName());
        }

        String[] result;
        result = byArgument.contains(PIPE_SEPARATOR)
                 ? PATTERN_PIPE_SEPARATOR.split(byArgument)
                 : new String[]{byArgument};
        return result;
    }

    boolean isWildcard() {
        boolean result = value().startsWith(ANY_BY_OPTION_TARGET);
        return result;
    }

    boolean isInner() {
        boolean result = !value().contains(FieldName.TYPE_SEPARATOR);
        return result;
    }

    /**
     * Obtains the type name from the reference.
     */
    String getType() {
        String value = value();
        int index = value.lastIndexOf(FieldName.TYPE_SEPARATOR);
        checkState(index > 0, "The field reference does not have the type (`%s`)", value);
        String result = value.substring(0, index)
                             .trim();
        return result;
    }
}
