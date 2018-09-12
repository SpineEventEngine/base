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

package io.spine.tools.protojs.field.checker;

import io.spine.tools.protojs.code.JsGenerator;

import static com.google.common.base.Preconditions.checkNotNull;

/**
 * The value checker for the Protobuf primitive fields.
 *
 * <p>All Protobuf fields which are not of message type are considered primitive and thus are
 * handled by this checker.
 *
 * @author Dmytro Kuzmin
 */
public class PrimitiveFieldChecker implements FieldValueChecker {

    private final JsGenerator jsGenerator;

    /**
     * Creates a new {@code PrimitiveFieldChecker}.
     *
     * @param jsGenerator
     *         the {@code JsGenerator} which accumulates all the generated code
     */
    PrimitiveFieldChecker(JsGenerator jsGenerator) {
        this.jsGenerator = jsGenerator;
    }

    /**
     * {@inheritDoc}
     *
     * <p>In case of the primitive field, the {@code null} values are not allowed and thus not set.
     */
    @Override
    public void performNullCheck(String value, String setterFormat) {
        checkNotNull(value);
        checkNotNull(setterFormat);
        jsGenerator.ifNotNull(value);
    }

    @Override
    public void exitNullCheck() {
        jsGenerator.exitBlock();
    }
}
