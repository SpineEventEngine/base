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

import com.google.protobuf.Descriptors.Descriptor;

import java.util.Optional;

import static com.google.common.base.Preconditions.checkNotNull;

/**
 * A direct reference to a proto message type.
 */
final class Direct extends AbstractTypeRef {

    private static final long serialVersionUID = 0L;

    /**
     * Parses the passed value for the subject of direct type reference.
     *
     * @return an instance if the passed value is not a wildcard reference or an empty string,
     *         and empty {@code Optional} otherwise
     */
    static Optional<TypeRef> parse(String value) {
        checkNotNull(value);
        if (value.isEmpty()) {
            return Optional.empty();
        }
        if (value.contains("*")) {
            return Optional.empty();
        }
        TypeRef result = new Direct(value);
        return Optional.of(result);
    }

    private Direct(String value) {
        super(value);
    }

    @Override
    public boolean test(Descriptor message) {
        //TODO:2019-01-29:alexander.yevsyukov: Handle package.
        boolean result = value().endsWith(message.getName());
        return result;
    }
}
