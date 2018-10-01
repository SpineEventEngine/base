/*
 * Copyright 2018, TeamDev. All rights reserved.
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

package io.spine.code.proto;

import com.google.common.collect.ImmutableList;
import com.google.protobuf.DescriptorProtos.FieldDescriptorProto;
import io.spine.code.AbstractFieldName;

import java.util.List;

import static com.google.common.base.Preconditions.checkNotNull;
import static io.spine.util.Preconditions2.checkNotEmptyOrBlank;

/**
 * A name of a message field.
 *
 * @author Alexander Yevsyukov
 */
public final class FieldName extends AbstractFieldName implements UnderscoredName {

    private static final long serialVersionUID = 0L;

    /** A delimiter between a type name and a field name. */
    public static final String TYPE_SEPARATOR = ".";

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

    /**
     * Obtains immutable list of words used in the name of the field.
     */
    @Override
    public List<String> words() {
        String[] words = value().split(WORD_SEPARATOR);
        ImmutableList<String> result = ImmutableList.copyOf(words);
        return result;
    }

    /**
     * Obtains the field name in {@code CamelCase}.
     */
    public String toCamelCase() {
        String result = CamelCase.convert(this);
        return result;
    }

    /**
     * Obtains the field name in {@code javaCase}.
     */
    public String javaCase() {
        String camelCase = toCamelCase();
        String result = Character.toLowerCase(camelCase.charAt(0)) + camelCase.substring(1);
        return result;
    }
}
