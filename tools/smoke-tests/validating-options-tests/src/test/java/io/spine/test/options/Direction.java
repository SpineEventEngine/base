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

package io.spine.test.options;

import com.google.common.collect.ImmutableList;
import com.google.protobuf.ByteString;
import io.spine.validate.FieldValue;
import io.spine.validate.option.Constraint;
import io.spine.validate.option.FieldValidatingOption;

import static io.spine.test.options.BytesDirectionOptionProto.direction;

/**
 * A custom validation option for {@code bytes}.
 *
 * <p>This option is used for testing the custom options loading. The constraint produced by this
 * option cannot be violated.
 */
public final class Direction extends FieldValidatingOption<BytesDirection, ByteString> {

    private static final Constraint<FieldValue<ByteString>> NO_OP_CONSTRAINT =
            byteString -> ImmutableList.of();

    Direction() {
        super(direction);
    }

    @Override
    public Constraint<FieldValue<ByteString>> constraintFor(FieldValue<ByteString> value) {
        return NO_OP_CONSTRAINT;
    }
}
