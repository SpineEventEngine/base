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
import io.spine.tools.protoc.CompilerOutput;
import io.spine.tools.protoc.ExternalClassLoader;
import io.spine.tools.protoc.SubscribableConfig;
import io.spine.type.MessageType;

import static com.google.common.base.Preconditions.checkNotNull;

/**
 * Generates nested classes for the supplied subscribable message type based on the
 * {@link SubscribableConfig passed configuration}.
 */
final class GenerateFields extends NestedClassGenerationTask {

    GenerateFields(ExternalClassLoader<NestedClassFactory> classLoader, SubscribableConfig config) {
        super(classLoader, config.getValue());
    }

    /**
     * Applies the field factory if the passed type is eligible for field generation.
     *
     * <p>Apart from the types that can be subscription targets, i.e. entity states and events,
     * there are also a number of built-in types to which the field generation is applied, as
     * required by the Spine routines.
     */
    @Override
    public ImmutableList<CompilerOutput> generateFor(MessageType type) {
        checkNotNull(type);
        if (!eligibleForFieldsGeneration(type)) {
            return ImmutableList.of();
        }
        return generateNestedClassesFor(type);
    }

    private static boolean eligibleForFieldsGeneration(MessageType type) {
        return type.isEntityState()
                || type.isEvent()
                || type.isRejection()
                || type.isSpineCoreEvent()
                || type.isEventContext();
    }
}
