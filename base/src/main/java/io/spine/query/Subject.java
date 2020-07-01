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

/**
 * Set of criteria for the objects obtained via querying or subscription.
 *
 * <p>Subjects are typically stored records or entities.
 */
public final class Subject<I, P extends SubjectParameter<?, ?>> {

    /**
     * The criteria put on the identifiers of the object of interest.
     */
    private final IdParameter<I> id;

    /**
     * Predicates, being the group of the parameters, against which the actual values
     * of target object fields are compared when querying.
     *
     * <p>The evaluation is done in a conjunction mode. I.e. the object matches the subject
     * if it matches each predicate.
     */
    private final ImmutableList<Predicate<P>> predicates;

    public Subject(IdParameter<I> id, ImmutableList<Predicate<P>> predicates) {
        this.id = id;
        this.predicates = predicates;
    }

    /**
     * Returns the criteria put on the identifiers of matched objects.
     */
    public IdParameter<I> id() {
        return id;
    }

    /**
     * Returns the predicates for the fields of matched objects.
     */
    public ImmutableList<Predicate<P>> predicates() {
        return predicates;
    }
}
