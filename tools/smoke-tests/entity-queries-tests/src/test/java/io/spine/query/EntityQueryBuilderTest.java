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

import io.spine.tools.query.Project;
import io.spine.tools.query.ProjectName;
import io.spine.tools.query.ProjectView;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static com.google.common.truth.Truth.assertThat;
import static io.spine.query.given.Lifecycle.ARCHIVED;
import static io.spine.query.given.Lifecycle.DELETED;

@DisplayName("`EntityQueryBuilder` should")
class EntityQueryBuilderTest {

    @DisplayName("create `EntityQuery` instances")
    @Test
    void createEntityQueries() {
        ProjectName empty = ProjectName.getDefaultInstance();
        Either<ProjectView.QueryBuilder> startedMoreThanMonthAgo =
                b -> b.daysSinceStarted()
                      .isGreaterThan(30);
        Either<ProjectView.QueryBuilder> isDone =
                b -> b.status()
                      .is(Project.Status.DONE);
        Either<ProjectView.QueryBuilder> isDeleted =
                b -> b.lifecycle((builder) -> DELETED.column()
                                                     .in(builder)
                                                     .is(true));
        ProjectView.Query query =
                ProjectView.newQuery()
                           .lifecycle((b) -> ARCHIVED.column()
                                                     .in(b)
                                                     .is(false))
                           .either(startedMoreThanMonthAgo, isDone, isDeleted)
                           .projectName()
                           .isNot(empty)
                           .build();
        assertThat(query).isNotNull();
    }
}
