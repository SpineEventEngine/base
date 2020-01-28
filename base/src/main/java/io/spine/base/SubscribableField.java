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

package io.spine.base;

import static com.google.common.base.Preconditions.checkNotNull;

/**
 * A subscribable message field which can be passed to subscription filters.
 *
 * <p>Normally this type shouldn't be inherited in the client code and is instead used by the
 * Spine routines which provide generated field enumerations.
 *
 * <p>See the {@code Fields} class in the subscribable message declarations.
 *
 * @apiNote Among others, this class is normally inherited by the generated nested message fields
 *        which declare own properties as public instance methods, for example:
 *        <pre>
 *        public EntityStateField someFieldName() {...}
 *        </pre>
 *        Thus, this class has to avoid name clashes with proto fields declared this way. Hence the
 *        otherwise redundant "get-" prefix on the {@link #getField()} method. For the same reason
 *        the class does not inherit from {@link io.spine.value.ValueHolder}.
 */
@SuppressWarnings("AbstractClassWithoutAbstractMethods")
// Prevent instantiation in favor of concrete subclasses.
public abstract class SubscribableField {

    private final Field field;

    protected SubscribableField(Field field) {
        this.field = checkNotNull(field);
    }

    /**
     * Returns a wrapped field.
     *
     * <p>See the class doc for the naming motivation.
     */
    public Field getField() {
        return field;
    }
}
