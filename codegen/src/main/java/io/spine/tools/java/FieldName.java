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

package io.spine.tools.java;

import io.spine.type.StringTypeValue;

import java.util.Iterator;

import static com.google.common.base.Preconditions.checkNotNull;

/**
 * A name of a field declared in a Java class.
 * 
 * @author Alexander Yevsyukov
 */
public final class FieldName extends StringTypeValue {

    private FieldName(String value) {
        super(value);
    }

    /**
     * Creates Java field name that corresponds to the passed Proto field name.
     */
    public static FieldName from(io.spine.tools.proto.FieldName protoField) {
        checkNotNull(protoField);
        final Iterator<String> words = protoField.words()
                                                 .iterator();
        // A field name must have at least one word.
        final StringBuilder builder = new StringBuilder(words.next());

        while (words.hasNext()) {
            final String word = words.next();
            builder.append(Character.toUpperCase(word.charAt(0)))
                   .append(word.substring(1));
        }

        final FieldName result = new FieldName(builder.toString());
        return result;
    }

    //TODO:2018-02-09:alexander.yevsyukov: Sort out the matter with `capitalizeFirst`.
    // This looks strange.

    /**
     * Transforms Protobuf-style field name into a corresponding Java-style field name.
     *
     * <p>For example, "seat_assignment_id" becomes "SeatAssignmentId"
     *
     * @param protoFieldName  Protobuf field name
     * @param capitalizeFirst indicates if the first letter should be capitalized
     * @return a field name
     */
    public static String toJavaFieldName(String protoFieldName, boolean capitalizeFirst) {
        checkNotNull(protoFieldName);

        final FieldName javaField = from(io.spine.tools.proto.FieldName.of(protoFieldName));

        String result = javaField.value();

        if (capitalizeFirst) {
            result = Character.toUpperCase(result.charAt(0)) + result.substring(1);
        }
        return result;
    }
}
