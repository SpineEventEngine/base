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

package io.spine.query.given;

import com.google.protobuf.FieldMask;
import com.google.protobuf.StringValue;
import com.google.protobuf.util.Timestamps;
import org.junit.jupiter.params.provider.Arguments;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.stream.Stream;

import static io.spine.protobuf.AnyPacker.pack;

/**
 * Test values used in comparison tests of a {@link io.spine.query.ComparisonOperator}.
 */
@SuppressWarnings("unused") /* Serve as method sources */
public final class ComparisonOperatorTestEnv {

    private ComparisonOperatorTestEnv() {
    }

    public static Stream<Arguments> equalValues() {
        return Stream.of(equalPrimitives(), equalComparables(), equalMessages(), equalTimestamps());
    }

    public static Stream<Arguments> equalOrderableValues() {
        return Stream.of(equalPrimitives(), equalComparables(), equalTimestamps());
    }

    public static Stream<Arguments> notEqualValues() {
        return Stream.of(ascPrimitives(), ascComparables(), ascTimestamps(), differentMessages());
    }

    public static Stream<Arguments> greaterThanValues() {
        return Stream.of(descPrimitives(), descComparables(), descTimestamps());
    }

    public static Stream<Arguments> lessThanValues() {
        return Stream.of(ascPrimitives(), ascComparables(), ascTimestamps());
    }

    private static Arguments equalPrimitives() {
        return Arguments.of(50, 50);
    }

    private static Arguments equalComparables() {
        return Arguments.of(BigDecimal.valueOf(1000), BigDecimal.valueOf(1000));
    }

    private static Arguments equalMessages() {
        var message = pack(FieldMask.getDefaultInstance());
        var anotherMessage = pack(FieldMask.getDefaultInstance());
        return Arguments.of(message, anotherMessage);
    }

    private static Arguments differentMessages() {
        return Arguments.of(StringValue.of("A value"), StringValue.of("A very different value"));
    }

    private static Arguments equalTimestamps() {
        return Arguments.of(Timestamps.fromSeconds(111),
                            Timestamps.fromSeconds(111));
    }

    private static Arguments ascPrimitives() {
        return Arguments.of(1L, 2L);
    }

    private static Arguments ascComparables() {
        return Arguments.of("firstComparable", "secondComparable");
    }

    private static Arguments ascTimestamps() {
        return Arguments.of(Timestamps.fromSeconds(1),
                            Timestamps.fromSeconds(2));
    }

    private static Arguments descPrimitives() {
        return Arguments.of(100.0f, 77.0f);
    }

    private static Arguments descComparables() {
        return Arguments.of(BigInteger.valueOf(500), BigInteger.valueOf(499));
    }

    private static Arguments descTimestamps() {
        return Arguments.of(Timestamps.fromSeconds(44),
                            Timestamps.fromSeconds(22));

    }
}
