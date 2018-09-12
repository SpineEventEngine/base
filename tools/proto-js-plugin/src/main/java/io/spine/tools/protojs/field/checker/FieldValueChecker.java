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

import io.spine.tools.protojs.code.JsOutput;

/**
 * The generator of the code which checks the proto field value.
 *
 * @apiNote
 * Like the other handlers and generators of this module, the {@code FieldValueChecker} is meant to
 * operate on the common {@link JsOutput} passed on construction and
 * thus its methods do not return any generated code.
 *
 * @author Dmytro Kuzmin
 */
public interface FieldValueChecker {

    /**
     * Generates the code which checks the given field value for {@code null}.
     *
     * <p>The setter format is specified so the checker can interact with the field itself in case
     * the check passes/fails.
     *
     * @param value
     *         the name of the variable representing the field value to check
     * @param setterFormat
     *         the format of the setter
     */
    void performNullCheck(String value, String setterFormat);

    /**
     * Generates the code to exit all {@code null} check blocks and return to the upper level.
     */
    void exitNullCheck();
}
