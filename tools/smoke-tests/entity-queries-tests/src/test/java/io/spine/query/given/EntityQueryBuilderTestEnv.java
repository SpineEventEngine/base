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

package io.spine.query.given;

import com.google.common.collect.ImmutableSet;
import com.google.protobuf.FieldMask;
import io.spine.query.Column;
import io.spine.query.ComparisonOperator;
import io.spine.query.RecordColumn;
import io.spine.query.Subject;
import io.spine.query.SubjectParameter;
import io.spine.tools.query.ProjectId;
import io.spine.tools.query.ProjectView;

import java.util.List;
import java.util.stream.IntStream;

import static com.google.common.collect.ImmutableSet.toImmutableSet;
import static com.google.common.truth.Truth.assertThat;
import static io.spine.testing.TestValues.nullRef;

/**
 * Test environment and utilities for {@link io.spine.query.EntityQueryBuilderTest}.
 */
public final class EntityQueryBuilderTestEnv {

    private EntityQueryBuilderTestEnv() {
    }

    /**
     * Generates a new {@code ProjectId} with the random value.
     */
    public static ProjectId projectId() {
        return ProjectId.generate();
    }

    /**
     * Generates the given number of {@code ProjectId} instances.
     */
    public static ImmutableSet<ProjectId> generateIds(int howMany) {
        return IntStream.range(0, howMany)
                        .mapToObj((i) -> projectId())
                        .collect(toImmutableSet());
    }

    /**
     * Checks that the query is not {@code null} as well as has no predicates and returns it.
     */
    public static Subject<ProjectId, ProjectView>
    subjectWithNoPredicates(ProjectView.Query query) {
        assertThat(query).isNotNull();
        Subject<ProjectId, ProjectView> subject = query.subject();
        assertThat(subject.predicates()).isEmpty();
        return subject;
    }

    /**
     * Asserts that the given query has no sorting, field mask and limit parameters set.
     */
    public static void assertNoSortingMaskLimit(ProjectView.Query query) {
        assertThat(query.sorting()).isEmpty();
        assertThat(query.mask()).isEqualTo(FieldMask.getDefaultInstance());
        assertThat(query.limit()).isEqualTo(nullRef());
    }

    /**
     * Creates a new {@code FieldMask} with the name of the given column as a path.
     */
    public static FieldMask fieldMaskWith(RecordColumn<ProjectView, ?> column) {
        return FieldMask.newBuilder()
                        .addPaths(column.name()
                                        .value())
                        .build();
    }

    /**
     * Asserts the given list of the subject parameters has the parameter with the given properties.
     *
     * <p>In case there are several parameters for the same column, this method checks them all.
     *
     * @param list
     *         the list of all parameters
     * @param column
     *         the column for which the parameter value is asserted
     * @param operator
     *         the operator of the asserted parameter
     * @param value
     *         value of the paramater
     */
    public static void assertHasParamValue(List<SubjectParameter<ProjectView, ?, ?>> list,
                                           Column<?, ?> column,
                                           ComparisonOperator operator,
                                           Object value) {
        boolean parameterFound = false;
        for (SubjectParameter<ProjectView, ?, ?> parameter : list) {
            if (parameter.column()
                         .equals(column)) {
                ComparisonOperator actualOperator = parameter.operator();
                Object actualValue = parameter.value();
                if (actualOperator == operator && value.equals(actualValue)) {
                    parameterFound = true;
                }
            }
        }
        assertThat(parameterFound).isTrue();
    }
}
