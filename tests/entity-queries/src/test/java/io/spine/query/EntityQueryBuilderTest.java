/*
 * Copyright 2021, TeamDev. All rights reserved.
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
import com.google.common.truth.Truth8;
import com.google.protobuf.FieldMask;
import io.spine.tools.query.Project;
import io.spine.tools.query.ProjectId;
import io.spine.tools.query.ProjectView;
import io.spine.tools.query.ProjectView.Field;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.util.Optional;

import static com.google.common.truth.Truth.assertThat;
import static io.spine.query.ComparisonOperator.EQUALS;
import static io.spine.query.ComparisonOperator.GREATER_THAN;
import static io.spine.query.ComparisonOperator.LESS_THAN;
import static io.spine.query.Direction.ASC;
import static io.spine.query.Direction.DESC;
import static io.spine.query.LogicalOperator.AND;
import static io.spine.query.LogicalOperator.OR;
import static io.spine.query.given.EntityQueryBuilderTestEnv.assertHasParamValue;
import static io.spine.query.given.EntityQueryBuilderTestEnv.assertNoSortingMaskLimit;
import static io.spine.query.given.EntityQueryBuilderTestEnv.fieldMaskWith;
import static io.spine.query.given.EntityQueryBuilderTestEnv.generateIds;
import static io.spine.query.given.EntityQueryBuilderTestEnv.projectId;
import static io.spine.query.given.EntityQueryBuilderTestEnv.subjectWithNoParameters;
import static io.spine.query.given.Lifecycle.DELETED;
import static io.spine.tools.query.Project.Status.CREATED;
import static io.spine.tools.query.Project.Status.DONE;
import static io.spine.tools.query.Project.Status.STARTED;
import static io.spine.tools.query.ProjectView.Column.daysSinceStarted;
import static io.spine.tools.query.ProjectView.Column.projectName;
import static io.spine.tools.query.ProjectView.Column.status;
import static io.spine.tools.query.ProjectView.Column.wasReassigned;
import static org.junit.jupiter.api.Assertions.assertThrows;

@DisplayName("`EntityQueryBuilder` should")
class EntityQueryBuilderTest {

    @Nested
    @DisplayName("create `EntityQuery` instances")
    final class CreateQuery {

        @Test
        @DisplayName("without parameters")
        void empty() {
            ProjectView.Query query = ProjectView.query()
                                                 .build();
            Subject<ProjectId, ProjectView> subject = subjectWithNoParameters(query);
            assertThat(subject.id()
                              .values()).isEmpty();
            assertNoSortingMaskLimit(query);
        }

        @Test
        @DisplayName("which hold the types of the queried entity state and entity ID")
        void withRecordType() {
            Subject<ProjectId, ProjectView> subject = ProjectView.query()
                                                                 .build()
                                                                 .subject();
            assertThat(subject.recordType()).isEqualTo(ProjectView.class);
            assertThat(subject.idType()).isEqualTo(ProjectId.class);
        }

        @Test
        @DisplayName("by a single ID value")
        void byId() {
            ProjectId expectedId = projectId();
            ProjectView.Query query = ProjectView.query()
                                                 .projectId().is(expectedId)
                                                 .build();
            Subject<ProjectId, ProjectView> subject = subjectWithNoParameters(query);
            IdParameter<ProjectId> actualIdParam = subject.id();
            assertThat(actualIdParam.values()).containsExactly(expectedId);
        }

        @Test
        @DisplayName("by several ID values")
        void bySeveralIds() {
            ImmutableSet<ProjectId> expectedValues = generateIds(24);
            ProjectView.Query query = ProjectView.query()
                                                 .projectId().in(expectedValues)
                                                 .build();
            Subject<ProjectId, ProjectView> subject = subjectWithNoParameters(query);
            IdParameter<ProjectId> actualIdParam = subject.id();
            assertThat(actualIdParam.values()).isEqualTo(expectedValues);
        }

        @Test
        @DisplayName("by the values of several entity columns")
        void byColumnValues() {
            Project.Status statusValue = DONE;
            int daysSinceStarted = 15;
            ProjectView.Query query = ProjectView.query()
                    .status().is(statusValue)
                    .daysSinceStarted().isLessThan(daysSinceStarted)
                    .build();
            QueryPredicate<ProjectView> rootPredicate = query.subject().predicate();
            assertThat(rootPredicate.operator()).isEqualTo(AND);

            ImmutableList<QueryPredicate<ProjectView>> children = rootPredicate.children();
            assertThat(children).hasSize(0);
            assertThat(rootPredicate.parameters()).hasSize(2);

            assertHasParamValue(rootPredicate, status(), EQUALS, statusValue);
            assertHasParamValue(rootPredicate, daysSinceStarted(), LESS_THAN, daysSinceStarted);
        }

        @Test
        @DisplayName("by the value of either of the entity columns")
        void byEitherColumn() {
            int daysSinceStarted = 30;
            Project.Status statusValue = DONE;
            boolean deletedValue = true;

            Either<ProjectView.QueryBuilder> startedMoreThanMonthAgo =
                    project -> project.daysSinceStarted().isGreaterThan(daysSinceStarted);
            Either<ProjectView.QueryBuilder> isDone =
                    project -> project.status().is(statusValue);
            Either<ProjectView.QueryBuilder> isDeleted =
                    project -> project.where(DELETED.column(), deletedValue);
            ProjectView.Query query =
                    ProjectView.query()
                               .either(startedMoreThanMonthAgo, isDone, isDeleted)
                               .build();

            QueryPredicate<ProjectView> rootPredicate = query.subject()
                                                          .predicate();
            ImmutableList<QueryPredicate<ProjectView>> predicates = rootPredicate.children();
            assertThat(predicates).hasSize(0);
            assertThat(rootPredicate.operator()).isEqualTo(OR);
            assertThat(rootPredicate.allParams()).hasSize(3);

            assertHasParamValue(rootPredicate, daysSinceStarted(), GREATER_THAN, daysSinceStarted);
            assertHasParamValue(rootPredicate, status(), EQUALS, statusValue);
            assertHasParamValue(rootPredicate, DELETED.column(), EQUALS, deletedValue);
        }

        @Test
        @DisplayName("with the specified field mask")
        void withFieldMask() {
            FieldMask mask = fieldMaskWith(status());
            ProjectView.Query query = ProjectView.query()
                                                 .withMask(mask)
                                                 .build();
            assertThat(query.mask()).isEqualTo(mask);
        }

        @Test
        @DisplayName("with the field mask defined by the paths")
        @SuppressWarnings("DuplicateStringLiteralInspection")   /* Field names just for tests. */
        void withMaskPaths() {
            String status = "status";
            String assignee = "assignee";
            ProjectView.Query query = ProjectView.query()
                    .withMask(status, assignee)
                    .build();
            FieldMask expected = FieldMask.newBuilder()
                                          .addPaths(status)
                                          .addPaths(assignee)
                                          .build();
            assertThat(query.mask()).isEqualTo(expected);
        }

        @Test
        @DisplayName("with the field mask defined by the generated `SubscribableField`s")
        @SuppressWarnings("DuplicateStringLiteralInspection")   /* Field names just for tests. */
        void withMaskDefinedBySubscribableFields() {
            ProjectView.Query query = ProjectView.query()
                    .withMask(Field.status(), Field.assignee())
                    .build();
            FieldMask expected = FieldMask.newBuilder()
                                          .addPaths("status")
                                          .addPaths("assignee")
                                          .build();
            assertThat(query.mask()).isEqualTo(expected);
        }

        @Test
        @DisplayName("with the field mask defined by the `Field`s declared in Proto message")
        @SuppressWarnings("DuplicateStringLiteralInspection")   /* Field names just for tests. */
        void withMaskDefinedByFields() {
            ProjectView.Query query = ProjectView.query()
                    .withMask(Field.projectName().getField(), Field.assignee().getField())
                    .build();
            FieldMask expected = FieldMask.newBuilder()
                                          .addPaths("project_name")
                                          .addPaths("assignee")
                                          .build();
            assertThat(query.mask()).isEqualTo(expected);
        }

        @Test
        @DisplayName("sorted by several entity columns")
        void withSorting() {
            ProjectView.Query query = ProjectView.query()
                    .sortAscendingBy(daysSinceStarted())
                    .sortAscendingBy(projectName())
                    .sortDescendingBy(wasReassigned())
                    .build();
            ImmutableList<SortBy<?, ProjectView>> sorting = query.sorting();
            assertThat(sorting).hasSize(3);
            assertThat(sorting.get(0)).isEqualTo(new SortBy<>(daysSinceStarted(), ASC));
            assertThat(sorting.get(1)).isEqualTo(new SortBy<>(projectName(), ASC));
            assertThat(sorting.get(2)).isEqualTo(new SortBy<>(wasReassigned(), DESC));
        }

        @Test
        @DisplayName("sorted by an entity column values with the record limit")
        void withLimitAndSorting() {
            int dozenOfRecords = 10;
            ProjectView.Query query = ProjectView.query()
                    .sortDescendingBy(daysSinceStarted())
                    .limit(dozenOfRecords)
                    .build();
            SortBy<?, ProjectView> sortBy = query.sorting()
                                                 .get(0);
            assertThat(sortBy).isEqualTo(new SortBy<>(daysSinceStarted(), DESC));
            assertThat(query.limit()).isEqualTo(dozenOfRecords);
        }

        @Test
        @DisplayName("which return the same query builder instance if asked")
        void returnSameBuilder() {
            ProjectView.QueryBuilder builder = ProjectView.query()
                    .status().is(STARTED)
                    .daysSinceStarted().isGreaterOrEqualTo(5);
            ProjectView.Query query = builder.build();
            ProjectView.QueryBuilder actualBuilder = query.toBuilder();
            assertThat(actualBuilder).isSameInstanceAs(builder);
        }
    }

    @Nested
    @DisplayName("prevent from")
    final class Prevent {

        @Test
        @DisplayName("building entity queries with the record limit set with no sorting specified")
        void fromUsingLimitWithoutSorting() {
            assertThrows(IllegalStateException.class,
                         () -> ProjectView.query()
                                          .limit(100)
                                          .build());

        }
    }

    @Nested
    @DisplayName("return previously specified entity column values")
    final class ReturnValues {

        @Test
        @DisplayName("of a single identifier parameter")
        void ofId() {
            ProjectId value = projectId();
            assertThat(ProjectView.query()
                                  .projectId().is(value)
                                  .whichIds().values()).containsExactly(value);
        }

        @Test
        @DisplayName("of several IDs")
        void ofSeveralIds() {
            ImmutableSet<ProjectId> ids = generateIds(3);
            assertThat(ProjectView.query()
                                  .projectId().in(ids)
                                  .whichIds().values()).isEqualTo(ids);
        }

        @Test
        @DisplayName("of the column parameters")
        void ofParameterValues() {
            Project.Status statusValue = CREATED;
            int daysSinceStarted = 1;
            ProjectView.Query query = ProjectView.query()
                    .status().is(statusValue)
                    .daysSinceStarted().isGreaterThan(daysSinceStarted)
                    .build();
            QueryPredicate<ProjectView> rootPredicate = query.subject()
                                                             .predicate();
            assertThat(rootPredicate.operator()).isEqualTo(AND);

            assertHasParamValue(rootPredicate, status(), EQUALS, statusValue);
            assertHasParamValue(rootPredicate, daysSinceStarted(), GREATER_THAN, daysSinceStarted);
        }

        @Test
        @DisplayName("of the field mask")
        void ofFieldMask() {
            FieldMask mask = fieldMaskWith(daysSinceStarted());
            Optional<FieldMask> maybeMask = ProjectView.query()
                                                       .withMask(mask)
                                                       .whichMask();
            Truth8.assertThat(maybeMask)
                  .isPresent();
            assertThat(maybeMask.get()).isEqualTo(mask);
        }

        @Test
        @DisplayName("of the record limit")
        void ofLimit() {
            int limit = 92;
            assertThat(ProjectView.query()
                                  .limit(limit)
                                  .whichLimit()).isEqualTo(limit);

        }

        @Test
        @DisplayName("of the column sorting directives")
        void ofSorting() {
            assertThat(ProjectView.query()
                                  .sortDescendingBy(daysSinceStarted())
                                  .sortAscendingBy(projectName())
                                  .sorting())
                    .isEqualTo(ImmutableList.of(new SortBy<>(daysSinceStarted(), DESC),
                                                new SortBy<>(projectName(), ASC))
                    );
        }
    }

    @Test
    @DisplayName("allow transforming the built `EntityQuery` instance into an object of choice" +
            " in the same call chain")
    void transform() {
        int predicateSize = ProjectView.query()
                .status().is(CREATED)
                .build((q) -> q.subject()
                               .predicate()
                               .allParams()
                               .size());
        assertThat(predicateSize).isEqualTo(1);
    }
}
