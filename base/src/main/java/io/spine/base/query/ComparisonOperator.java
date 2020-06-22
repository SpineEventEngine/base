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

package io.spine.base.query;

/**
 * Defines how the queried records are compared against the desired parameter values.
 *
 * @see AbstractQuery
 * @see QueryParameter
 */
public enum ComparisonOperator {

    /**
     * The actual value must be equal to the value of the query parameter.
     */
    EQUALS,

    /**
     * The actual value must be different from the value of the query parameter.
     */
    NOT_EQUALS,

    /**
     * The actual value must be less than the value of the query parameter.
     */
    LESS_THAN,

    /**
     * The actual value must be less or equal to the value of the query parameter.
     */
    LESS_OR_EQUALS,

    /**
     * The actual value must be greater than the value of the query parameter.
     */
    GREATER_THAN,

    /**
     * The actual value must be greater or equal to the value of the query parameter.
     */
    GREATER_OR_EQUALS
}
