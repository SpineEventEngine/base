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

import com.google.common.collect.ImmutableList;
import io.spine.option.OptionsProto;
import io.spine.type.MessageType;

import java.util.Optional;

public final class ColumnOption extends FieldOption<Boolean> {

    /**
     * Prevents instantiation from outside.
     *
     * <p>Use {@link #isColumn(FieldDeclaration)}.
     */
    private ColumnOption() {
        super(OptionsProto.column);
    }

    public static boolean hasColumns(MessageType messageType) {
        return !columnsOf(messageType).isEmpty();
    }

    public static ImmutableList<FieldDeclaration> columnsOf(MessageType messageType) {
        return ImmutableList.of();
    }

    public static boolean isColumn(FieldDeclaration field) {
        ColumnOption option = new ColumnOption();
        Optional<Boolean> value = option.valueFrom(field.descriptor());
        Boolean isColumn = value.orElse(false);
        return isColumn;
    }
}
