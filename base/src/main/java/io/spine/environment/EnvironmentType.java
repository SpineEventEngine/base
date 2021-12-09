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

package io.spine.environment;

/**
 * A type of environment.
 *
 * <p>Some examples may be {@code STAGING} or {@code LOCAL} environments.
 *
 * @implNote Not an {@code interface} to limit the access level of {@link #enabled()}
 * @see Environment
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
     * Returns the {@code hashCode()} of the class.
     */
    @Override
    public final int hashCode() {
        return getClass().hashCode();
    }

    /**
     * Returns {@code true} if this and passed objects are of the same class,
     * otherwise {@code false}.
     *
     * @implNote The derived classes are meant to emulate enums in the sense that all
     *         instances of them are interchangeable. Therefore, we are interested only in the class
     *         information for the comparison.
     */
    @Override
    @SuppressWarnings("EqualsGetClass" /* see @implNote */)
    public final boolean equals(Object obj) {
        if (obj == null) {
            return false;
        }
        var result = getClass().equals(obj.getClass());
        return result;
    }
}
