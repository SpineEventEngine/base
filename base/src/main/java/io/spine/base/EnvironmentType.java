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

package io.spine.base;

import com.google.common.base.Objects;

/**
 * A type of environment.
 *
 * <p>Some examples may be {@code STAGING} or {@code LOCAL} environments.
 *
 * @implNote developers are encouraged to make their environment types singletons, such
 *         that their API is consistent with the env types provided by the {@code base} library:
 *         {@link Production}, {@link Tests}.
 */
public abstract class EnvironmentType {

    /**
     * Returns {@code true} if the underlying system is currently in this environment type.
     *
     * <p>For example, if an application is deployed to a fleet of virtual machines, an environment
     * variable may be set for every virtual machine. Application developer may use this type of
     * knowledge to determine the current environment.
     */
    protected abstract boolean enabled();

    /**
     * @inheritDoc <p>By default, environments types are compared based on their classes.
     * @implNote This class deliberately breaks the substitution principle for the {@code
     *         equals} method. Extenders are not encouraged to have {@code EnvironmentType}
     *         hierarchies. If they decide to have them anyway, they are free to
     *         override {@code equals} and {@code hashCode} accordingly.
     */
    @Override
    @SuppressWarnings("EqualsGetClass" /* see @implNote */)
    public boolean equals(Object obj) {
        return this.getClass()
                   .equals(obj.getClass());
    }

    /**
     * @inheritDoc
     *
     * <p>By default, adheres to the {@code equals} and {@code hashCode} contract, assuming that
     * the implementation of the {@code equals} is the {@linkplain EnvironmentType#equals(Object)
     * default one}.
     */
    @Override
    public int hashCode() {
        return Objects.hashCode(getClass());
    }
}
