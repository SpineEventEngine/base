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

package io.spine.tools.proto;

import com.google.common.collect.ImmutableList;
import com.google.protobuf.DescriptorProtos.FieldDescriptorProto;
import io.spine.type.StringTypeValue;

import java.util.List;

import static com.google.common.base.Preconditions.checkNotNull;
import static io.spine.tools.CodePreconditions.checkNotEmptyOrBlank;

/**
 * A name of a message field.
 *
 * @author Alexander Yevsyukov
 */
public final class FieldName extends StringTypeValue implements UnderscoredName {

    private static final String WORD_SEPARATOR = "_";

    private FieldName(String value) {
        super(value);
    }

    /**
     * Creates a field name with the passed value.
     */
    public static FieldName of(String value) {
        checkNotEmptyOrBlank(value);
        return new FieldName(value);
    }

    /**
     * Creates a field name for the passed descriptor.
     */
    public static FieldName of(FieldDescriptorProto field) {
        checkNotNull(field);
        return new FieldName(field.getName());
    }

    //TODO:2018-02-10:alexander.yevsyukov: Eliminate this method in favor of MethodName class.
    /**
     * Transforms Protobuf-style field name into a {@code CamelCase} name.
     *
     * <p>For example, "seat_assignment_id" becomes "SeatAssignmentId"
     *
     * @param protoFieldName  Protobuf field name
     * @param capitalizeFirst indicates if the first letter should be capitalized
     * @return a field name
     */
    public static String toCamelCase(String protoFieldName, boolean capitalizeFirst) {
        checkNotEmptyOrBlank(protoFieldName);

        String result = of(protoFieldName).toCamelCase();
        if (!capitalizeFirst) {
            result = Character.toLowerCase(result.charAt(0)) + result.substring(1);
        }
        return result;
    }

    /**
     * Obtains immutable list of words used in the name of the field.
     */
    @Override
    public List<String> words() {
        final String[] words = value().split(WORD_SEPARATOR);
        final ImmutableList<String> result = ImmutableList.copyOf(words);
        return result;
    }

    /**
     * Obtains the field name in {@code CamelCase}.
     */
    public String toCamelCase() {
        final String result = CamelCase.convert(this);
        return result;
    }

    /**
     * Obtains the field name in {@code javaCase}.
     */
    public String javaCase() {
        final String camelCase = toCamelCase();
        final String result = Character.toLowerCase(camelCase.charAt(0)) + camelCase.substring(1);
        return result;
    }
}
