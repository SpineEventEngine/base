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
import java.util.stream.IntStream;

import static com.google.common.collect.ImmutableSet.toImmutableSet;
import static com.google.common.truth.Truth.assertThat;
import static io.spine.query.ComparisonOperator.EQUALS;
import static io.spine.query.ComparisonOperator.LESS_OR_EQUALS;
import static io.spine.query.ComparisonOperator.LESS_THAN;
import static io.spine.query.ComparisonOperator.NOT_EQUALS;
import static io.spine.query.Direction.ASC;
import static io.spine.query.Direction.DESC;
import static io.spine.query.LogicalOperator.AND;
import static io.spine.query.LogicalOperator.OR;
import static io.spine.query.given.RecordQueryBuilderTestEnv.ManufacturerColumns.isTraded;
import static io.spine.query.given.RecordQueryBuilderTestEnv.ManufacturerColumns.isin;
import static io.spine.query.given.RecordQueryBuilderTestEnv.ManufacturerColumns.whenFounded;
import static io.spine.query.given.RecordQueryBuilderTestEnv.assertHasParamValue;
import static io.spine.query.given.RecordQueryBuilderTestEnv.fieldMaskWith;
import static io.spine.query.given.RecordQueryBuilderTestEnv.manufacturerBuilder;
import static io.spine.query.given.RecordQueryBuilderTestEnv.manufacturerId;
import static io.spine.query.given.RecordQueryBuilderTestEnv.subjectWithNoPredicates;

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
            RecordQuery<ManufacturerId, Manufacturer> actual = manufacturerBuilder().build();
            Subject<ManufacturerId, Manufacturer> subject = subjectWithNoPredicates(actual);
            assertThat(subject.id()
                              .values()).isEmpty();
            RecordQueryBuilderTestEnv.assertNoOrderingMaskLimit(actual);
        }

        @Test
        @DisplayName("which hold the type of the queried record")
        void withRecordType() {
            RecordQuery<ManufacturerId, Manufacturer> query = manufacturerBuilder().build();
            Subject<ManufacturerId, Manufacturer> subject = query.subject();
            assertThat(subject.recordType()).isEqualTo(Manufacturer.class);
        }

        @Test
        @DisplayName("by a single identifier value")
        void byId() {
            ManufacturerId expectedId = manufacturerId();
            RecordQuery<ManufacturerId, Manufacturer> query =
                    manufacturerBuilder()
                            .id()
                            .is(expectedId)
                            .build();
            Subject<ManufacturerId, Manufacturer> subject = subjectWithNoPredicates(query);

            IdParameter<ManufacturerId> actualIdParam = subject.id();
            assertThat(actualIdParam.values()).containsExactly(expectedId);
        }

        @Test
        @DisplayName("by several identifier values")
        void bySeveralIds() {
            ImmutableSet<ManufacturerId> expectedValues =
                    IntStream.range(0, 24)
                             .mapToObj((i) -> manufacturerId())
                             .collect(toImmutableSet());
            RecordQuery<ManufacturerId, Manufacturer> query =
                    manufacturerBuilder()
                            .id()
                            .with(expectedValues)
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
                    manufacturerBuilder().where(isin)
                                         .isNot(isinValue)
                                         .where(whenFounded)
                                         .isLessOrEqualTo(THURSDAY)
                                         .where(isTraded)
                                         .is(stocksAreTraded)
                                         .build();

            ImmutableList<QueryPredicate<Manufacturer>> predicates = query.subject()
                                                                          .predicates();
            assertThat(predicates).hasSize(1);
            QueryPredicate<Manufacturer> predicate = predicates.get(0);
            assertThat(predicate.operator()).isEqualTo(AND);

            ImmutableList<SubjectParameter<Manufacturer, ?, ?>> params = predicate.parameters();
            assertThat(params).hasSize(3);
            assertHasParamValue(params, isin, NOT_EQUALS, isinValue);
            assertHasParamValue(params, whenFounded, LESS_OR_EQUALS, THURSDAY);
            assertHasParamValue(params, isTraded, EQUALS, stocksAreTraded);
        }

        @Test
        @DisplayName("by the value of either of the columns")
        void byEitherColumn() {
            String isinValue = "JP 3899800001";
            boolean stocksAreTraded = true;
            RecordQuery<ManufacturerId, Manufacturer> query =
                    manufacturerBuilder()
                            .either((r) -> r.where(isin)
                                            .is(isinValue),
                                    (r) -> r.where(isTraded)
                                            .is(stocksAreTraded))
                            .where(whenFounded)
                            .isLessThan(THURSDAY)
                            .build();
            ImmutableList<QueryPredicate<Manufacturer>> predicates = query.subject()
                                                                          .predicates();
            assertThat(predicates).hasSize(2);

            QueryPredicate<Manufacturer> actualEither = predicates.get(0);
            assertThat(actualEither.operator()).isEqualTo(OR);
            List<SubjectParameter<Manufacturer, ?, ?>> orParams = actualEither.parameters();
            assertThat(orParams).hasSize(2);
            assertHasParamValue(orParams, isin, EQUALS, isinValue);
            assertHasParamValue(orParams, isTraded, EQUALS, stocksAreTraded);

            QueryPredicate<Manufacturer> actualAnd = predicates.get(1);
            assertThat(actualAnd.operator()).isEqualTo(AND);
            List<SubjectParameter<Manufacturer, ?, ?>> andParams = actualAnd.parameters();
            assertThat(andParams).hasSize(1);
            assertHasParamValue(andParams, whenFounded, LESS_THAN, THURSDAY);

        }

        @Test
        @DisplayName("with the field mask")
        void withFieldMask() {
            FieldMask mask = fieldMaskWith(isTraded);
            RecordQuery<ManufacturerId, Manufacturer> query = manufacturerBuilder().withMask(mask)
                                                                                   .build();

            assertThat(query.mask()).isEqualTo(mask);
        }

        @Test
        @DisplayName("ordered by several fields")
        void withOrdering() {
            RecordQuery<ManufacturerId, Manufacturer> query =
                    manufacturerBuilder().orderBy(whenFounded, ASC)
                                         .orderBy(isin, ASC)
                                         .orderBy(isTraded, DESC)
                                         .build();

            ImmutableList<OrderBy<?, Manufacturer>> ordering = query.ordering();
            assertThat(ordering).hasSize(3);
            assertThat(ordering.get(0)).isEqualTo(new OrderBy<>(whenFounded, ASC));
            assertThat(ordering.get(1)).isEqualTo(new OrderBy<>(isin, ASC));
            assertThat(ordering.get(2)).isEqualTo(new OrderBy<>(isTraded, DESC));
        }

        @Test
        @DisplayName("ordered by several fields with the record limit")
        void withLimitAndOrdering() {
            int tenRecords = 10;
            RecordQuery<ManufacturerId, Manufacturer> query =
                    manufacturerBuilder().orderBy(isin, DESC)
                                         .limit(tenRecords)
                                         .build();
            OrderBy<?, Manufacturer> orderBy = query.ordering()
                                                    .get(0);
            assertThat(orderBy).isEqualTo(new OrderBy<>(isin, DESC));
            assertThat(query.limit()).isEqualTo(tenRecords);
        }

        @Test
        @DisplayName("which return the same `Builder` instance if asked")
        void returnSameBuilder() {
            RecordQueryBuilder<ManufacturerId, Manufacturer> builder =
                    manufacturerBuilder().where(whenFounded)
                                         .isGreaterThan(THURSDAY)
                                         .where(isin)
                                         .isNot("JP 49869009911")
                                         .orderBy(whenFounded, ASC)
                                         .limit(150);
            RecordQuery<ManufacturerId, Manufacturer> query = builder.build();
            RecordQueryBuilder<ManufacturerId, Manufacturer> actualBuilder = query.toBuilder();
            assertThat(actualBuilder).isSameInstanceAs(builder);
        }
    }
}
