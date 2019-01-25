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

package io.spine.code.proto;

import com.google.common.annotations.VisibleForTesting;
import com.google.common.base.Splitter;
import com.google.common.collect.ImmutableList;
import com.google.errorprone.annotations.CanIgnoreReturnValue;
import com.google.protobuf.DescriptorProtos.FieldDescriptorProto;
import io.spine.value.StringTypeValue;

import java.util.List;

import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Preconditions.checkState;
import static io.spine.util.Preconditions2.checkNotEmptyOrBlank;

/**
 * A reference to a field found in the {@code "by"} option value.
 */
public final class FieldReference extends StringTypeValue {

    private static final long serialVersionUID = 0L;

    /**
     * Wildcard option used in {@code "by"} field option.
     *
     * <p>{@code string enrichment_value [(by) = "*.my_event_id"];} tells that this enrichment
     * may have any target event types. That's why an FQN of the target type is replaced by
     * this wildcard option.
     */
    private static final String ANY_BY_OPTION_TARGET = "*";

    /**
     * Separates a type name from a field name.
     */
    private static final Splitter fieldNameSplit = Splitter.on('.');

    /**
     * Reference value parts separated by {@link #fieldNameSplit}.
     */
    private final ImmutableList<String> parts;

    @VisibleForTesting
    FieldReference(String value) {
        super(checkValue(value));
        this.parts = parts(value);
    }

    /**
     * Ensures that the passed value is not null, empty or blank, and if it contains a type
     * reference, it's not the {@linkplain #checkTypeReference(String)} suffix form}.
     */
    private static String checkValue(String value) {
        checkNotEmptyOrBlank(value);
        List<String> parts = parts(value);
        if (parts.size() >= 2) {
            // Contains the type part.
            checkTypeReference(parts.get(0));
        }
        return value;
    }

    private static ImmutableList<String> parts(String value) {
        return ImmutableList.copyOf(fieldNameSplit.splitToList(value));
    }

    /**
     * Ensures that the passed value is:
     * <ol>
     * <li>not null
     * <li>not empty or blank
     * <li>not a wild card type reference in a suffix form
     *  (such as {@code '*CommonEventNameSuffix.field_name'}, which is not currently supported.
     * </ol>
     */
    @CanIgnoreReturnValue
    private static String checkTypeReference(String typeReference) {
        checkNotEmptyOrBlank(typeReference);
        if (typeReference.startsWith(ANY_BY_OPTION_TARGET)) {
            checkArgument(
                    typeReference.equals(ANY_BY_OPTION_TARGET),
                    "Referencing types with a suffix form (`%s`) in wildcard reference " +
                            "is not supported . " +
                            "Please use '*.<field_name>' when referencing a field of many types.",
                    typeReference);
        }
        return typeReference;
    }

    /**
     * Obtains references found in the passed field.
     */
    public static ImmutableList<FieldReference> allFrom(FieldDescriptorProto field) {
        return ByOption.allFrom(field);
    }

    /**
     * Tells if the passed value reference all types.
     */
    public static boolean isWildcard(String typeReference) {
        checkTypeReference(typeReference);
        return ANY_BY_OPTION_TARGET.equals(typeReference);
    }

    /**
     * Verifies if the reference is to a field in all types having a field with the referenced name.
     */
    public boolean isWildcard() {
        boolean result = value().startsWith(ANY_BY_OPTION_TARGET);
        return result;
    }

    /**
     * Verifies if the reference is to a field from the same type.
     */
    public boolean isInner() {
        boolean result = !value().contains(FieldName.TYPE_SEPARATOR);
        return result;
    }

    /**
     * Tells if the reference is for a message context field.
     */
    public boolean isContext() {
        boolean result = value().startsWith(Via.context.name());
        return result;
    }

    /**
     * Obtains the type name from the reference.
     */
    public String typeName() {
        String value = value();
        checkState(hasType(), "The field reference (`%s`) does not have the type.", value);
        int index = value.lastIndexOf(FieldName.TYPE_SEPARATOR);
        String result = value.substring(0, index)
                             .trim();
        return result;
    }

    /**
     * Verifies if the reference contains a type name part.
     */
    public boolean hasType() {
        return parts.size() >= 2;
    }

    /**
     * Obtains the field name part of the reference.
     */
    public String fieldName() {
        return parts.get(parts.size() - 1);
    }

    /**
     * Enumeration of references to instances of specific message.
     */
    public enum Via {

        /**
         * The reference to an event context used in the {@code (by)} field option.
         */
        context
    }
}
