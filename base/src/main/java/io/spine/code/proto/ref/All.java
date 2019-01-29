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

import com.google.errorprone.annotations.CanIgnoreReturnValue;
import com.google.protobuf.Descriptors;

import static com.google.common.base.Preconditions.checkArgument;
import static io.spine.util.Preconditions2.checkNotEmptyOrBlank;

/**
 * Reference to all message types.
 *
 * <p>Such references are used in the {@code (by)} option of enrichments when referencing all
 * messages with a particular field name.
 */
final class All extends AbstractTypeRef {

    private static final long serialVersionUID = 0L;

    /**
     * Wildcard option used in {@code "by"} field option.
     *
     * <p>{@code string enrichment_value [(by) = "*.my_event_id"];} tells that this enrichment
     * may have any target event types. That's why an FQN of the target type is replaced by
     * this wildcard option.
     */
    static final String WILDCARD = "*";

    All() {
        super(WILDCARD);
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
    static String checkTypeReference(String typeReference) {
        checkNotEmptyOrBlank(typeReference);
        if (typeReference.startsWith(WILDCARD)) {
            checkArgument(
                    typeReference.equals(WILDCARD),
                    "Referencing types with a suffix form (`%s`) in wildcard reference " +
                            "is not supported . " +
                            "Please use '*.<field_name>' when referencing a field of many types.",
                    typeReference);
        }
        return typeReference;
    }

    @Override
    public boolean matches(Descriptors.Descriptor message) {
        return false;
    }
}
