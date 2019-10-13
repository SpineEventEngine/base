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

import static com.google.common.collect.ImmutableList.toImmutableList;

public final class ColumnOption extends FieldOption<Boolean> {

    /**
     * Prevents instantiation from outside.
     *
     * <p>Use the static methods of this class to extract the column value(s).
     */
    private ColumnOption() {
        super(OptionsProto.column);
    }

    public static boolean hasColumns(MessageType messageType) {
        boolean result = messageType.fields()
                                    .stream()
                                    .anyMatch(ColumnOption::isColumn);
        return result;
    }

    public static ImmutableList<FieldDeclaration> columnsOf(MessageType messageType) {
        ImmutableList<FieldDeclaration> result = messageType.fields()
                                                            .stream()
                                                            .filter(ColumnOption::isColumn)
                                                            .collect(toImmutableList());
        return result;
    }

    public static boolean isColumn(FieldDeclaration field) {
        ColumnOption option = new ColumnOption();
        Optional<Boolean> value = option.valueFrom(field.descriptor());
        Boolean isColumn = value.orElse(false);
        return isColumn;
    }
}
