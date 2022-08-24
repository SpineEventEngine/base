/*
 * Copyright 2022, TeamDev. All rights reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
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

package io.spine.type;

import static com.google.common.base.Preconditions.checkNotNull;
import static java.lang.String.format;

/**
 * Thrown when a Protobuf type having the {@code internal_type} option is sent
 * to a bounded context which declares it. Or, when the declaring bounded context attempts
 * to send this type outside.
 *
 * <p>A Java type corresponding to a Protobuf type with the {@code internal_type} option
 * is expected to be annotated as {@link io.spine.annotation.Internal Internal}.
 *
 * @see io.spine.annotation.Internal
 */
public class UnpublishedLanguageException extends RuntimeException {

    private static final long serialVersionUID = 0L;

    public UnpublishedLanguageException(TypeName type) {
        super(formatMsg(type));
    }

    private static String formatMsg(TypeName type) {
        checkNotNull(type);
        return format(
                "The type `%s` is not a part of the published language" +
                        " of its bounded context.", type
        );
    }
}
