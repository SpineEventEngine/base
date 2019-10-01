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

package io.spine.code.dart.lexeme;

import io.spine.code.dart.FileName;

import static com.google.common.base.Preconditions.checkNotNull;

/**
 * An import of a Dart file.
 */
public final class Import extends Lexeme {

    private static final long serialVersionUID = 0L;

    /**
     * Creates a new {@code Import}.
     *
     * @param uri
     *         import URI, either a relative path, or a package and a path
     * @param alias
     *         import alias
     */
    private Import(StringLiteral uri, Reference alias) {
        super("import %s as %s;", uri, alias);
    }

    public static Import fileBased(FileName fileName, Reference alias) {
        checkNotNull(fileName);
        checkNotNull(alias);

        return new Import(new StringLiteral(fileName.value()), alias);
    }
}
