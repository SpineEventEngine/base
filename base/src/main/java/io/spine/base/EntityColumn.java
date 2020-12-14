/*
 * Copyright 2020, TeamDev. All rights reserved.
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

package io.spine.base;

import io.spine.annotation.Internal;
import io.spine.code.proto.FieldName;
import io.spine.value.ValueHolder;

/**
 * A queryable column of an entity which can be passed to the query filters.
 *
 * <p>Normally, instances of this class are provided by the Spine-generated column enumeration and
 * should not be constructed in the user code directly.
 *
 * <p>For each message which represents an entity state and has columns, the Spine routines will
 * generate a nested {@code Column} class which exposes all columns of the entity as
 * {@code EntityColumn} instances. Example:
 * <pre>
 * // Given a message declaration.
 * message ProjectDetails {
 *     option (entity).kind = PROJECTION;
 *
 *     ProjectId id = 1;
 *     ProjectName name = 2 [(column) = true];
 *     int32 task_count = 3 [(column) = true];
 * }
 *
 * // The following Java class will be generated.
 * public final class ProjectDetails // implements Message, etc. {
 *
 *     // ...
 *
 *     public static final class Column {
 *
 *         private Column() {
 *             // Prevent instantiation.
 *         }
 *
 *         public static io.spine.base.EntityColumn name() {...}
 *
 *         public static io.spine.base.EntityColumn taskCount() {...}
 *     }
 * }
 * </pre>
 *
 * <p>The values retrieved via {@code static} methods of the {@code Column} type may then be passed
 * to a client to form a query request.
 *
 * <p>See the Spine code generation routines in {@code tool-base} for extensive details on how the
 * types are generated.
 */
public final class EntityColumn extends ValueHolder<FieldName> {

    private static final long serialVersionUID = 0L;

    public EntityColumn(String fieldName) {
        super(FieldName.of(fieldName));
    }

    @Internal
    public FieldName name() {
        return value();
    }
}