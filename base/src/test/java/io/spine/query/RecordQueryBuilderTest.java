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

package io.spine.query;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
import com.google.protobuf.FieldMask;
import com.google.protobuf.Timestamp;
import com.google.protobuf.util.Timestamps;
import io.spine.query.given.RecordQueryBuilderTestEnv;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Optional;

import static com.google.common.truth.Truth.assertThat;
import static com.google.common.truth.Truth8.assertThat;
import static io.spine.query.ComparisonOperator.EQUALS;
import static io.spine.query.ComparisonOperator.GREATER_OR_EQUALS;
import static io.spine.query.ComparisonOperator.LESS_OR_EQUALS;
import static io.spine.query.ComparisonOperator.LESS_THAN;
import static io.spine.query.Direction.ASC;
import static io.spine.query.Direction.DESC;
import static io.spine.query.LogicalOperator.AND;
import static io.spine.query.LogicalOperator.OR;
import static io.spine.query.given.RecordQueryBuilderTestEnv.ManufacturerColumns.is_traded;
import static io.spine.query.given.RecordQueryBuilderTestEnv.ManufacturerColumns.isin;
import static io.spine.query.given.RecordQueryBuilderTestEnv.ManufacturerColumns.when_founded;
import static io.spine.query.given.RecordQueryBuilderTestEnv.assertHasParamValue;
import static io.spine.query.given.RecordQueryBuilderTestEnv.fieldMaskWith;
import static io.spine.query.given.RecordQueryBuilderTestEnv.generateIds;
import static io.spine.query.given.RecordQueryBuilderTestEnv.manufacturerId;
import static io.spine.query.given.RecordQueryBuilderTestEnv.queryManufacturer;
import static io.spine.query.given.RecordQueryBuilderTestEnv.subjectWithNoPredicates;
import static org.junit.jupiter.api.Assertions.assertThrows;

@DisplayName("`RecordQueryBuilder` should")
class RecordQueryBuilderTest {

    /**
     * The Epoch Thursday.
     */
    private static final Timestamp THURSDAY = Timestamps.fromSeconds(0);

    @Nested
    @DisplayName("create `RecordQuery` instances")
    final class CreateQuery {

        @Test
        @DisplayName("with no parameters")
        void empty() {
            RecordQuery<ManufacturerId, Manufacturer> actual = queryManufacturer().build();
            Subject<ManufacturerId, Manufacturer> subject = subjectWithNoPredicates(actual);
            assertThat(subject.id()
                              .values()).isEmpty();
            RecordQueryBuilderTestEnv.assertNoSortingMaskLimit(actual);
        }

        @Test
        @DisplayName("which hold the type of the queried record and the type of its ID")
        void withRecordType() {
            RecordQuery<ManufacturerId, Manufacturer> query = queryManufacturer().build();
            Subject<ManufacturerId, Manufacturer> subject = query.subject();
            assertThat(subject.recordType()).isEqualTo(Manufacturer.class);
            assertThat(subject.idType()).isEqualTo(ManufacturerId.class);
        }

        @Test
        @DisplayName("by a single identifier value")
        void byId() {
            ManufacturerId expectedId = manufacturerId();
            RecordQuery<ManufacturerId, Manufacturer> query =
                    queryManufacturer()
                            .id().is(expectedId)
                            .build();
            Subject<ManufacturerId, Manufacturer> subject = subjectWithNoPredicates(query);

            IdParameter<ManufacturerId> actualIdParam = subject.id();
            assertThat(actualIdParam.values()).containsExactly(expectedId);
        }

        @Test
        @DisplayName("by several identifier values")
        void bySeveralIds() {
            ImmutableSet<ManufacturerId> expectedValues = generateIds(24);
            RecordQuery<ManufacturerId, Manufacturer> query =
                    queryManufacturer()
                            .id().in(expectedValues)
                            .build();
            Subject<ManufacturerId, Manufacturer> subject = subjectWithNoPredicates(query);

            IdParameter<ManufacturerId> actualIdParam = subject.id();
            assertThat(actualIdParam.values()).isEqualTo(expectedValues);
        }

        @Test
        @DisplayName("by the values of several columns")
        void byColumnValues() {
            boolean stocksAreTraded = true;
            String isinValue = "JP 3633400001";
            RecordQuery<ManufacturerId, Manufacturer> query =
                    queryManufacturer().where(isin).is(isinValue)
                                       .where(when_founded).isLessOrEqualTo(THURSDAY)
                                       .where(is_traded).is(stocksAreTraded)
                                       .build();

            ImmutableList<QueryPredicate<Manufacturer>> predicates = query.subject()
                                                                          .predicates();
            assertThat(predicates).hasSize(1);
            QueryPredicate<Manufacturer> predicate = predicates.get(0);
            assertThat(predicate.operator()).isEqualTo(AND);

            ImmutableList<SubjectParameter<Manufacturer, ?, ?>> params = predicate.parameters();
            assertThat(params).hasSize(3);
            assertHasParamValue(params, isin, EQUALS, isinValue);
            assertHasParamValue(params, when_founded, LESS_OR_EQUALS, THURSDAY);
            assertHasParamValue(params, is_traded, EQUALS, stocksAreTraded);
        }

        @Test
        @DisplayName("by the value of either of the columns")
        void byEitherColumn() {
            String isinValue = "JP 3899800001";
            boolean stocksAreTraded = true;
            RecordQuery<ManufacturerId, Manufacturer> query =
                    queryManufacturer()
                            .where(when_founded).isLessThan(THURSDAY)
                            .either((r) -> r.where(isin).is(isinValue),
                                    (r) -> r.where(is_traded).is(stocksAreTraded))
                            .build();
            ImmutableList<QueryPredicate<Manufacturer>> predicates = query.subject()
                                                                          .predicates();
            assertThat(predicates).hasSize(2);

            QueryPredicate<Manufacturer> actualAnd = predicates.get(0);
            assertThat(actualAnd.operator()).isEqualTo(AND);
            List<SubjectParameter<Manufacturer, ?, ?>> andParams = actualAnd.parameters();
            assertThat(andParams).hasSize(1);
            assertHasParamValue(andParams, when_founded, LESS_THAN, THURSDAY);

            QueryPredicate<Manufacturer> actualEither = predicates.get(1);
            assertThat(actualEither.operator()).isEqualTo(OR);
            List<SubjectParameter<Manufacturer, ?, ?>> orParams = actualEither.parameters();
            assertThat(orParams).hasSize(2);
            assertHasParamValue(orParams, isin, EQUALS, isinValue);
            assertHasParamValue(orParams, is_traded, EQUALS, stocksAreTraded);
        }

        @Test
        @DisplayName("with the field mask")
        void withFieldMask() {
            FieldMask mask = fieldMaskWith(is_traded);
            RecordQuery<ManufacturerId, Manufacturer> query =
                    queryManufacturer().withMask(mask)
                                       .build();

            assertThat(query.mask()).isEqualTo(mask);
        }

        @Test
        @DisplayName("with the field mask defined by the paths")
        @SuppressWarnings("DuplicateStringLiteralInspection")   /* Field names just for tests. */
        void withMaskPaths() {
            String isin = "isin";
            String whenFounded = "when_founded";
            RecordQuery<ManufacturerId, Manufacturer> query =
                    queryManufacturer().withMask(isin, whenFounded)
                                       .build();
            FieldMask expected = FieldMask.newBuilder()
                                          .addPaths(isin)
                                          .addPaths(whenFounded)
                                          .build();
            assertThat(query.mask()).isEqualTo(expected);
        }

        @Test
        @DisplayName("sorted by the values of several columns")
        void withSorting() {
            RecordQuery<ManufacturerId, Manufacturer> query =
                    queryManufacturer().sortAscendingBy(when_founded)
                                       .sortAscendingBy(isin)
                                       .sortDescendingBy(is_traded)
                                       .build();

            ImmutableList<SortBy<?, Manufacturer>> sorting = query.sorting();
            assertThat(sorting).hasSize(3);
            assertThat(sorting.get(0)).isEqualTo(new SortBy<>(when_founded, ASC));
            assertThat(sorting.get(1)).isEqualTo(new SortBy<>(isin, ASC));
            assertThat(sorting.get(2)).isEqualTo(new SortBy<>(is_traded, DESC));
        }

        @Test
        @DisplayName("sorted by the values of several columns with the record limit")
        void withLimitAndSorting() {
            int tenRecords = 10;
            RecordQuery<ManufacturerId, Manufacturer> query =
                    queryManufacturer().sortDescendingBy(isin)
                                       .sortAscendingBy(when_founded)
                                       .limit(tenRecords)
                                       .build();
            ImmutableList<SortBy<?, Manufacturer>> sorting = query.sorting();
            assertThat(sorting.get(0)).isEqualTo(new SortBy<>(isin, DESC));
            assertThat(sorting.get(1)).isEqualTo(new SortBy<>(when_founded, ASC));
            assertThat(query.limit()).isEqualTo(tenRecords);
        }

        @Test
        @DisplayName("which return the same `Builder` instance if asked")
        void returnSameBuilder() {
            RecordQueryBuilder<ManufacturerId, Manufacturer> builder =
                    queryManufacturer().where(when_founded).isGreaterThan(THURSDAY)
                                       .where(isin).is("JP 49869009911")
                                       .sortAscendingBy(when_founded)
                                       .limit(150);
            RecordQuery<ManufacturerId, Manufacturer> query = builder.build();
            RecordQueryBuilder<ManufacturerId, Manufacturer> actualBuilder = query.toBuilder();
            assertThat(actualBuilder).isSameInstanceAs(builder);
        }
    }

    @Nested
    @DisplayName("prevent")
    final class Prevent {

        @Test
        @DisplayName("building queries with the record limit set without the sorting specified")
        void fromUsingLimitWithoutSorting() {
            assertThrows(IllegalStateException.class,
                         () -> queryManufacturer().limit(100)
                                                  .build());

        }
    }

    @Nested
    @DisplayName("return previously specified values")
    final class ReturnValues {

        @Test
        @DisplayName("of a single ID parameter")
        void ofId() {
            ManufacturerId value = manufacturerId();
            assertThat(queryManufacturer().id().is(value)
                                          .whichIds()
                                          .values()).containsExactly(value);
        }

        @Test
        @DisplayName("of several IDs")
        void ofSeveralIds() {
            ImmutableSet<ManufacturerId> ids = generateIds(3);
            assertThat(queryManufacturer().id().in(ids)
                                          .whichIds()
                                          .values()).isEqualTo(ids);
        }

        @Test
        @DisplayName("of parameters")
        void ofParameterValues() {
            String isinValue = "JP 3496600002";
            List<QueryPredicate<Manufacturer>> predicates =
                    queryManufacturer().where(isin).is(isinValue)
                                       .where(when_founded).isGreaterOrEqualTo(THURSDAY)
                                       .predicates();
            assertThat(predicates).hasSize(1);
            QueryPredicate<Manufacturer> predicate = predicates.get(0);
            assertThat(predicate.operator()).isEqualTo(AND);

            ImmutableList<SubjectParameter<Manufacturer, ?, ?>> parameters = predicate.parameters();
            assertHasParamValue(parameters, isin, EQUALS, isinValue);
            assertHasParamValue(parameters, when_founded, GREATER_OR_EQUALS, THURSDAY);
        }

        @Test
        @DisplayName("of a field mask")
        void ofFieldMask() {
            FieldMask mask = fieldMaskWith(isin);
            Optional<FieldMask> maybeMask = queryManufacturer().withMask(mask)
                                                               .whichMask();
            assertThat(maybeMask).isPresent();
            assertThat(maybeMask.get()).isEqualTo(mask);
        }

        @Test
        @DisplayName("of a record limit")
        void ofLimit() {
            int limit = 55;
            assertThat(queryManufacturer().limit(limit)
                                          .whichLimit()).isEqualTo(limit);

        }

        @Test
        @DisplayName("of the sorting directives")
        void ofSorting() {
            assertThat(queryManufacturer().sortDescendingBy(isin)
                                          .sortAscendingBy(when_founded)
                                          .sorting())
                    .isEqualTo(ImmutableList.of(new SortBy<>(isin, DESC),
                                                new SortBy<>(when_founded, ASC))
                    );
        }
    }

    @Test
    @DisplayName("allow transforming the built `RecordQuery` instance" +
            " into an object of choice in the same call chain")
    void transform() {
        int predicateSize = queryManufacturer()
                .where(is_traded).is(false)
                .build((q) -> q.subject()
                               .predicates()
                               .size());
        assertThat(predicateSize).isEqualTo(1);
    }
}
