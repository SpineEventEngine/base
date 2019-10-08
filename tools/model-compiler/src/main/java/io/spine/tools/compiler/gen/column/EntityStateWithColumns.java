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

package io.spine.tools.compiler.gen.column;

import com.google.common.collect.ImmutableList;
import com.squareup.javapoet.TypeSpec;
import io.spine.code.java.PackageName;
import io.spine.code.proto.FieldDeclaration;
import io.spine.tools.compiler.gen.GeneratedTypeSpec;
import io.spine.type.MessageType;

public final class EntityStateWithColumns implements GeneratedTypeSpec {

    private final MessageType messageType;
    private final ImmutableList<FieldDeclaration> columns;

    public EntityStateWithColumns(MessageType messageType) {
        this.messageType = messageType;
        this.columns = columnsOf(messageType);
    }

    private static ImmutableList<FieldDeclaration> columnsOf(MessageType messageType) {
        return null;
    }

    @Override
    public PackageName packageName() {
        return null;
    }

    @Override
    public TypeSpec typeSpec() {
        return null;
    }

    private String className() {
        return "";
    }
}
