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

import com.google.protobuf.FieldMask;
import io.spine.query.Either;
import io.spine.tools.query.ProjectId;
import io.spine.tools.query.ProjectView;

import static io.spine.query.given.Lifecycle.ARCHIVED;
import static io.spine.query.given.Lifecycle.DELETED;
import static io.spine.tools.query.Project.Status.DONE;
import static io.spine.tools.query.ProjectView.Column.daysSinceStarted;
import static io.spine.tools.query.ProjectView.Column.projectName;
import static io.spine.tools.query.ProjectView.Column.status;

/**
 * Test values for {@link io.spine.query.EntityQueryTest}.
 */
public final class EntityQueryTestEnv {

    private EntityQueryTestEnv() {
    }

    /**
     * Produces an entity query which has all its fields set.
     */
    public static ProjectView.Query givenQuery() {
        Either<ProjectView.QueryBuilder> startedMoreThanMonthAgo = b -> b.daysSinceStarted()
                                                                         .isGreaterThan(30);
        Either<ProjectView.QueryBuilder> isDone = b -> b.status()
                                                        .is(DONE);
        Either<ProjectView.QueryBuilder> isDeleted = b -> b.where(DELETED.column(), true);
        FieldMask nameAndStatus = maskNameAndStatus();
        ProjectView.Query query =
                ProjectView.query()
                           .projectId()
                           .in(ProjectId.generate(), ProjectId.generate())
                           .where(ARCHIVED.column(), false)
                           .either(startedMoreThanMonthAgo, isDone, isDeleted)
                           .sortAscendingBy(projectName())
                           .sortDescendingBy(daysSinceStarted())
                           .limit(10)
                           .withMask(nameAndStatus)
                           .build();
        return query;
    }

    private static FieldMask maskNameAndStatus() {
        FieldMask mask = FieldMask.newBuilder()
                                  .addPaths(projectName().name()
                                                         .value())
                                  .addPaths(status().name()
                                                    .value())
                                  .build();
        return mask;
    }
}
