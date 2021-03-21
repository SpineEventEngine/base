/*
 * Copyright 2021, TeamDev. All rights reserved.
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

import com.google.errorprone.annotations.Immutable;
import io.spine.annotation.GeneratedMixin;
import io.spine.validate.BuiltMessage;
import io.spine.validate.ValidatingBuilder;

/**
 * A common interface for entity state messages.
 *
 * <p>Any message that defines an {@code (entity)} option with a valid {@code kind} is marked with
 * this interface by the Model Compiler.
 *
 * <p>The first field of the entity state message is treated as its identifier. It is a convention
 * that has two goals:
 *
 * <ol>
 *     <li>The definition of an entity state always starts with its ID with no extra Protobuf
 *     options. This way it's feels easy and more natural to read the code.

 *     <li>Developers don't forget to specify which of the fields declared in Protobuf corresponds
 *     to the entity ID.
 * </ol>
 *
 * <p>At codegen-time, the Model Compiler substitutes the generic parameter {@code <I>} with
 * an actual type of the first field of the entity state message.
 *
 * @param <I>
 *         the type of entity identifiers
 * @param <B>
 *         the type of the builder for this entity state
 * @param <M>
 *         the type of this entity state, which is used for binding of the builder type
 * @see io.spine.code.proto.EntityStateOption
 */
@SuppressWarnings("unused") /* Used in the generated code. */
@Immutable
@GeneratedMixin
public interface EntityState<I, B extends ValidatingBuilder<M>, M extends EntityState<I, B, M>>
        extends BuiltMessage<B, M> {

}
