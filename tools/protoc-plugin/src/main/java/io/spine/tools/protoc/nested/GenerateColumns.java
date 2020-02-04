/*
 * Copyright 2020, TeamDev. All rights reserved.
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

package io.spine.tools.protoc.nested;

import com.google.common.collect.ImmutableList;
import io.spine.code.gen.java.ColumnFactory;
import io.spine.tools.protoc.CompilerOutput;
import io.spine.type.MessageType;

import static com.google.common.base.Preconditions.checkNotNull;
import static io.spine.code.proto.ColumnOption.hasColumns;

/**
 * Generates nested classes for the supplied queryable message type based on the passed
 * configuration.
 */
final class GenerateColumns extends NestedClassGenerationTask {

    private final boolean generate;

    GenerateColumns(boolean generate) {
        super(new ColumnFactory());
        this.generate = generate;
    }

    /**
     * Applies the column factory if the type is eligible for column generation.
     */
    @Override
    public ImmutableList<CompilerOutput> generateFor(MessageType type) {
        checkNotNull(type);
        if (!generate || !isEntityWithColumns(type)) {
            return ImmutableList.of();
        }
        return generateNestedClassesFor(type);
    }

    private static boolean isEntityWithColumns(MessageType type) {
        return type.isEntityState() && hasColumns(type);
    }
}
