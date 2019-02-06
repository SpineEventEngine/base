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
import com.google.common.base.Splitter;
import com.google.common.collect.ImmutableList;
import com.google.protobuf.DescriptorProtos.FieldDescriptorProto;
import com.google.protobuf.Descriptors.Descriptor;
import com.google.protobuf.Descriptors.FieldDescriptor;
import io.spine.value.StringTypeValue;
import org.checkerframework.checker.nullness.qual.Nullable;

import java.util.List;
import java.util.Optional;

import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Preconditions.checkNotNull;
import static io.spine.code.proto.ref.BuiltIn.EVENT_CONTEXT;
import static io.spine.code.proto.ref.BuiltIn.SELF;
import static io.spine.util.Preconditions2.checkNotEmptyOrBlank;

/**
 * A reference to a field found in the {@code "by"} option value.
 */
public final class FieldRef extends StringTypeValue {

    private static final long serialVersionUID = 0L;

    /**
     * A delimiter between a field name and its qualifier, which refers to another message
     * like {@code "context"}.
     */
    private static final String TYPE_SEPARATOR = ".";

    /**
     * Separates a type name from a field name.
     */
    private static final Splitter fieldNameSplit = Splitter.on(TYPE_SEPARATOR);

    /**
     * Reference value parts separated by {@link #fieldNameSplit}.
     */
    private final ImmutableList<String> parts;

    /**
     * Reference to a proto type.
     */
    private final TypeRef typeRef;

    @VisibleForTesting
    FieldRef(String value) {
        super(checkValue(value));
        this.parts = split(value);
        this.typeRef = TypeRef.parse(typeRef(value));
    }

    /**
     * Ensures that the passed value is not null, empty or blank.
     */
    private static String checkValue(String value) {
        checkNotEmptyOrBlank(value);
        checkArgument(
                !value.contains("*"),
                "Field reference cannot be wildcard. Found: `%s`.",
                value
        );
        List<String> parts = split(value);
        checkThat(!parts.isEmpty(), value);
        parts.forEach(v -> checkThat(!v.trim()
                                       .isEmpty(), value));
        return value;
    }

    private static void checkThat(boolean b, String value) {
        checkArgument(b, "The value (`%s`) is not a valid field reference.", value);
    }

    private static ImmutableList<String> split(String value) {
        List<String> elements = fieldNameSplit.splitToList(value);
        return ImmutableList.copyOf(elements);
    }

    /**
     * Obtains references found in the passed field.
     */
    public static ImmutableList<FieldRef> allFrom(FieldDescriptorProto field) {
        ImmutableList<String> refs = ByOption.allFrom(field);
        ImmutableList.Builder<FieldRef> result = ImmutableList.builder();
        for (String ref : refs) {
            result.add(new FieldRef(ref));
        }
        return result.build();
    }

    /**
     * Verifies if the reference is to a field from the same type.
     */
    public boolean isInner() {
        boolean result = typeRef.equals(SELF);
        return result;
    }

    /**
     * Tells if the reference is for a message context field.
     */
    public boolean isContext() {
        boolean result = typeRef.equals(EVENT_CONTEXT);
        return result;
    }

    /**
     * Obtains type reference part from the field reference string.
     */
    private static String typeRef(String value) {
        int index = value.lastIndexOf(TYPE_SEPARATOR);
        if (index == -1) {
            return "";
        }
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
     * Obtains the descriptor of the field with the name {@linkplain #fieldName()} referenced}
     * by this instance in the passed message.
     *
     * @param message the message in which to find the field
     * @return the descriptor of the field, or empty {@code Optional} if there is no a field with
     * the {@linkplain #fieldName()} referenced name}
     */
    public Optional<FieldDescriptor> find(Descriptor message) {
        checkNotNull(message);
        @Nullable FieldDescriptor result = message.findFieldByName(fieldName());
        return Optional.ofNullable(result);
    }
}
