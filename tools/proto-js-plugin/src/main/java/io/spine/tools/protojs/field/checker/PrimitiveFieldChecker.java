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

import com.google.common.annotations.VisibleForTesting;
import io.spine.tools.protojs.code.JsGenerator;

import static com.google.common.base.Preconditions.checkNotNull;

@VisibleForTesting
public class PrimitiveFieldChecker implements FieldValueChecker {

    private final JsGenerator jsGenerator;

    PrimitiveFieldChecker(JsGenerator jsGenerator) {
        this.jsGenerator = jsGenerator;
    }

    @Override
    public void performNullCheck(String fieldValue, String setterFormat) {
        checkNotNull(fieldValue);
        checkNotNull(setterFormat);
        jsGenerator.ifNotNull(fieldValue);
    }

    @Override
    public void exitNullCheck() {
        jsGenerator.exitBlock();
    }
}
