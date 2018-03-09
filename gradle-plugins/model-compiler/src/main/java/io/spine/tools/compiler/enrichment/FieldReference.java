/*
 * Copyright 2018, TeamDev Ltd. All rights reserved.
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

import com.google.protobuf.DescriptorProtos.FieldDescriptorProto;
import io.spine.type.StringTypeValue;

import java.util.regex.Pattern;

import static io.spine.option.OptionsProto.BY_FIELD_NUMBER;
import static io.spine.option.UnknownOptions.getUnknownOptionValue;
import static java.util.regex.Pattern.compile;

/**
 * A reference to a field found in the {@code "by"} option value.
 *
 * @author Alexander Yevsyukov
 */
class FieldReference extends StringTypeValue {

    private static final String PIPE_SEPARATOR = "|";
    private static final Pattern PATTERN_PIPE_SEPARATOR = compile("\\|");

    FieldReference(String value) {
        super(value);
    }

    static String[] allFrom(FieldDescriptorProto field) {
        final String byArgument = getUnknownOptionValue(field, BY_FIELD_NUMBER);
        final String[] result;
        result = byArgument.contains(PIPE_SEPARATOR)
                ? PATTERN_PIPE_SEPARATOR.split(byArgument)
                : new String[]{byArgument};
        return result;
    }
}
