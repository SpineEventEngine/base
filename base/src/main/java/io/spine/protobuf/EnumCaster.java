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

package io.spine.protobuf;

import com.google.protobuf.EnumValue;
import com.google.protobuf.ProtocolMessageEnum;

import static com.google.common.base.Preconditions.checkNotNull;
import static io.spine.util.Exceptions.newIllegalArgumentException;

/**
 * Converts {@link EnumValue} to {@link Enum} and back.
 */
final class EnumCaster extends MessageCaster<EnumValue, Enum<? extends ProtocolMessageEnum>> {

    private final Class<? extends Enum<? extends ProtocolMessageEnum>> type;

    /**
     * Creates a new caster for the specified {@code type}.
     */
    EnumCaster(Class<? extends Enum<? extends ProtocolMessageEnum>> type) {
        super();
        this.type = checkNotNull(type);
    }

    @Override
    protected Enum<? extends ProtocolMessageEnum> toObject(EnumValue input) {
        String name = input.getName();
        if (name.isEmpty()) {
            int number = input.getNumber();
            return convertByNumber(number);
        } else {
            return convertByName(name);
        }
    }

    private Enum<? extends ProtocolMessageEnum> convertByNumber(int number) {
        Enum<? extends ProtocolMessageEnum>[] constants = type.getEnumConstants();
        for (Enum<? extends ProtocolMessageEnum> constant : constants) {
            ProtocolMessageEnum asProtoEnum = (ProtocolMessageEnum) constant;
            int valueNumber = asProtoEnum.getNumber();
            if (number == valueNumber) {
                return constant;
            }
        }
        throw unknownNumber(number);
    }

    @SuppressWarnings({"unchecked", "rawtypes"}) // Checked at runtime.
    private Enum<? extends ProtocolMessageEnum> convertByName(String name) {
        Enum result = Enum.valueOf((Class<? extends Enum>) type, name);
        return (Enum<? extends ProtocolMessageEnum>) result;
    }

    @Override
    protected EnumValue toMessage(Enum<? extends ProtocolMessageEnum> input) {
        String name = input.name();
        ProtocolMessageEnum asProtoEnum = (ProtocolMessageEnum) input;
        EnumValue value = EnumValue
                .newBuilder()
                .setName(name)
                .setNumber(asProtoEnum.getNumber())
                .build();
        return value;
    }

    private IllegalArgumentException unknownNumber(int number) {
        throw newIllegalArgumentException(
                "Could not find a enum value of type `%s` for number `%d`.",
                type.getCanonicalName(), number
        );
    }
}
