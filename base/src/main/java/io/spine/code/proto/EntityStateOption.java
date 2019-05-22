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

import com.google.errorprone.annotations.Immutable;
import com.google.protobuf.Descriptors.Descriptor;
import io.spine.option.EntityOption;
import io.spine.option.OptionsProto;

import java.util.Optional;

/**
 * An option that, for a message representing a state of the entity, defines its kind (of which
 * there are four: Aggregate, Projection, Process Manager and Entity) and its visibility to queries.
 */
@Immutable
public final class EntityStateOption extends MessageOption<EntityOption> {

    private EntityStateOption() {
        super(OptionsProto.entity);
    }

    /**
     * Obtains the value of the {@code entity} option from the specified message.
     *
     * @param message
     *         message to obtain the option value from
     * @return either an {@code Optional} containing the value of the {@code entity} option,
     *         or an empty {@code Optional}.
     * @apiNote This method is just a shorthand for
     *      <pre>
     *      {@code
     *      EntityStateOption option = new EntityStateOption();
     *      option.valueFrom(messageDescriptor);
     *      }
     *      </pre>
     * to avoid instantiating an object.
     */
    public static Optional<EntityOption> valueOf(Descriptor message) {
        EntityStateOption option = new EntityStateOption();
        return option.valueFrom(message);
    }
}
