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

package io.spine.protobuf;

import com.google.protobuf.EnumValue;
import com.google.protobuf.ProtocolMessageEnum;

import static com.google.common.base.Preconditions.checkNotNull;
import static io.spine.util.Exceptions.newIllegalArgumentException;

/**
 * Handles conversion of Java {@link Enum} objects to respective {@link EnumValue} Protobuf
 * counterpart.
 *
 * <p>Enums are converted by their {@linkplain EnumValue#getName() name} first or by the
 * {@linkplain EnumValue#getNumber() number} if the name is not present.
 */
final class EnumConverter extends ProtoConverter<EnumValue, Enum<? extends ProtocolMessageEnum>> {

    /**
     * A special name of a Protobuf enum entry that denotes an unrecognized value.
     */
    private static final String UNRECOGNIZED_PROTO_ENUM = "UNRECOGNIZED";

    private final Class<? extends Enum<? extends ProtocolMessageEnum>> type;

    /**
     * Creates a new converter for the specified {@code type}.
     */
    EnumConverter(Class<? extends Enum<? extends ProtocolMessageEnum>> type) {
        super();
        this.type = checkNotNull(type);
    }

    @Override
    protected Enum<? extends ProtocolMessageEnum> toObject(EnumValue input) {
        var name = input.getName();
        if (name.isEmpty()) {
            var number = input.getNumber();
            return findByNumber(number);
        } else {
            return findByName(name);
        }
    }

    /**
     * Retrieves the enum constant with the specified {@code number}.
     *
     * @throws IllegalArgumentException
     *         if enum constant with such a number is not present
     */
    private Enum<? extends ProtocolMessageEnum> findByNumber(int number) {
        var constants = type.getEnumConstants();
        for (var constant : constants) {
            var isUnrecognized = isUnrecognized(constant);
            if (isUnrecognized && number == -1) {
                return constant;
            }
            if (isUnrecognized) {
                continue;
            }
            var asProtoEnum = (ProtocolMessageEnum) constant;
            var valueNumber = asProtoEnum.getNumber();
            if (number == valueNumber) {
                return constant;
            }
        }
        throw unknownNumber(number);
    }

    private static boolean isUnrecognized(Enum<? extends ProtocolMessageEnum> constant) {
        return UNRECOGNIZED_PROTO_ENUM.equalsIgnoreCase(constant.name());
    }

    /**
     * {@linkplain Enum#valueOf(Class, String) Retrieves} the enum constant with
     * the specified {@code name}.
     *
     * @throws IllegalArgumentException
     *         if enum constant with such a name is not present
     */
    @SuppressWarnings({"unchecked", "rawtypes"}) // Checked at runtime.
    private Enum<? extends ProtocolMessageEnum> findByName(String name) {
        var result = Enum.valueOf((Class<? extends Enum>) type, name);
        return (Enum<? extends ProtocolMessageEnum>) result;
    }

    @Override
    protected EnumValue toMessage(Enum<? extends ProtocolMessageEnum> input) {
        var name = input.name();
        var asProtoEnum = (ProtocolMessageEnum) input;
        var value = EnumValue.newBuilder()
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
