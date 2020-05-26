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

/**
 * A type of an environment that knows whether it is enabled.
 *
 * <p>Useful examples may include distinguishable {@code STAGING} or {@code LOCAL} environments.
 * The base library provides {@linkplain BaseEnvironmentType default environment types}.
 */
public interface EnvironmentType {

    /**
     * Returns {@code true} if the underlying system is currently in this environment type.
     *
     * <p>For example, let's say that an application is deployed to a fleet of virtual machines.
     * Let's say an environment variable is set for every virtual machine.
     * An application developer may use this type of knowledge to determine the current environment.
     */
    boolean currentlyOn();

    /**
     * Makes it so the underlying system no longer has this environment type.
     *
     * <p>Be careful, since this method may mutate the state that is outside the scope of the
     * Java application, such as clear an environment variable, remove a file, etc.
     */
    void reset();

    /**
     * Makes it so the underlying system has this environment type.
     *
     * <p>Be careful, since this method may mutate the state that is outside the scope of the
     * Java application, such as set an environment variable, create or move a file, etc.
     */
    void setTo();
}

