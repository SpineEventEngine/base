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

import com.google.protobuf.Timestamp;
import io.spine.base.Time;
import io.spine.query.Either;
import io.spine.query.Manufacturer;
import io.spine.query.ManufacturerId;
import io.spine.query.RecordPredicates;
import io.spine.query.RecordQueryBuilder;

import static io.spine.query.given.RecordQueryBuilderTestEnv.ManufacturerColumns.is_traded;
import static io.spine.query.given.RecordQueryBuilderTestEnv.ManufacturerColumns.isin;
import static io.spine.query.given.RecordQueryBuilderTestEnv.ManufacturerColumns.stock_count;
import static io.spine.query.given.RecordQueryBuilderTestEnv.ManufacturerColumns.when_founded;
import static io.spine.query.given.RecordQueryBuilderTestEnv.queryManufacturer;

/**
 * Test environment for {@link io.spine.query.RecordQueryTest}.
 */
public final class RecordQueryTestEnv {

    private static final Timestamp NOW = Time.currentTime();

    private RecordQueryTestEnv() {
    }

    public static RecordPredicates<ManufacturerId, Manufacturer> conjunctivePredicates() {
        return (builder) -> builder.where(is_traded).is(false)
                                   .where(stock_count).is(0);
    }

    public static RecordPredicates<ManufacturerId, Manufacturer> moreConjunctivePredicates() {
        return (builder) -> builder.where(stock_count).is(15)
                                   .where(isin).is("More JP12341500");
    }

    public static Either<RecordQueryBuilder<ManufacturerId, Manufacturer>> either1() {
        return r -> r.where(stock_count)
                     .is(10);
    }

    public static Either<RecordQueryBuilder<ManufacturerId, Manufacturer>> either2() {
        return r -> r.where(when_founded)
                     .isLessThan(NOW);
    }

    public static Either<RecordQueryBuilder<ManufacturerId, Manufacturer>> either3() {
        return r -> r.where(stock_count)
                     .is(99);
    }

    public static Either<RecordQueryBuilder<ManufacturerId, Manufacturer>> either4() {
        return r -> r.where(when_founded)
                     .isGreaterOrEqualTo(Timestamp.getDefaultInstance());
    }

    @SuppressWarnings("unchecked")
    public static RecordQueryBuilder<ManufacturerId, Manufacturer>
    disjunctiveBuilder(Either<RecordQueryBuilder<ManufacturerId, Manufacturer>>... items) {
        return queryManufacturer()
                .either(items)
                .withMask(isin.name().value())
                .sortAscendingBy(isin)
                .limit(42);
    }

    @SuppressWarnings("unchecked")
    public static RecordPredicates<ManufacturerId, Manufacturer>
    disjunctivePredicates(Either<RecordQueryBuilder<ManufacturerId, Manufacturer>>... items) {
        return (builder) -> builder.either(items);
    }

    public static RecordQueryBuilder<ManufacturerId, Manufacturer> conjunctiveBuilder() {
        var withPredicates = conjunctivePredicates().apply(queryManufacturer());
        return withMaskSortingAndLimit(withPredicates);

    }

    public static RecordQueryBuilder<ManufacturerId, Manufacturer>
    withMaskSortingAndLimit(RecordQueryBuilder<ManufacturerId, Manufacturer> builder) {
        return builder.withMask(isin.name().value())
               .sortAscendingBy(when_founded)
               .limit(18);
    }
}
