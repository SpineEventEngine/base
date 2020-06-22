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

package io.spine.base.entity;

import io.spine.base.query.MessageColumn;

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
 *          //TODO:2020-04-30:alex.tymchenko: add the docs
 *         public static io.spine.base.entity.EntityColumn name() {...}
 *
 *         public static io.spine.base.entity.EntityColumn taskCount() {...}
 *     }
 * }
 * </pre>
 *
 * <p>The values retrieved via {@code static} methods of the {@code Column} type may then be passed
 * to a client to form a query request.
 *
 * <p>See the Spine code generation routines in {@code tool-base} for extensive details on how the
 * types are generated.
 *
 * @param <S> the type of the state of the {@code Entity}
 * @param <V> the type of the column type
 */
public final class EntityColumn<S extends EntityState<?>, V> extends MessageColumn<S, V> {

    private static final long serialVersionUID = 0L;

    public EntityColumn(String fieldName, Class<S> entityStateType, Class<V> valueType) {
        super(fieldName, entityStateType, valueType);
    }
}
