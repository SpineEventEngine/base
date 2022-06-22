/*
 * Copyright 2022, TeamDev. All rights reserved.
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

/**
 * Marks entities that manually implement their columns.
 *
 * <p>The manual column implementation is done with the help of custom {@code XWithColumns}
 * interfaces that extend this type and are generated by Spine Model Compiler.
 *
 * <p>The interfaces declare the column getters and may be implemented by the corresponding entity
 * types to override the column values.
 *
 * <p>For example, given message:
 * <pre>
 * message UserProfile {
 *     option (entity).kind = PROJECTION;
 *
 *     // ...
 *
 *     int32 year_of_registration = 8 [(column) = true];
 * }
 * </pre>
 *
 * The following interface will be generated in Java:
 * <pre>
 * interface UserProfileWithColumns extends EntityWithColumns {
 *
 *     int getYearOfRegistration();
 * }
 * </pre>
 *
 * The interface may then be implemented by the entity to manually specify the column value:
 * <pre>
 * class UserProfileProjection
 *        {@literal extends Projection<UserId, UserProfile, UserProfile.Builder>}
 *         implements UserProfileWithColumns {
 *
 *    {@literal @Override}
 *     int getYearOfRegistration() {
 *         return 2019;
 *     }
 * }
 * </pre>
 *
 * <p>In the example above, the value of {@code year_of_registration}  column will always be
 * {@code 2019} as a result of {@code getYearOfRegistration()} execution.
 *
 * <p>The values received in such way will always override the "manual" entity state updates.
 *
 * <p>This interface itself is used by internal Spine routines and should
 * never be implemented in the user code directly.
 */
@SuppressWarnings("InterfaceNeverImplemented") // Implemented in generated code.
public interface EntityWithColumns {
}
