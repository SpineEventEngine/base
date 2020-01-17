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

package io.spine.validate.option;

import com.google.common.collect.ImmutableSet;
import io.spine.annotation.Internal;

import java.util.ServiceLoader;

import static java.util.ServiceLoader.load;

/**
 * Loads the implementations of {@link ValidatingOptionFactory} using a {@link ServiceLoader}.
 *
 * <p>Caches the loaded results and never reloads the services.
 */
@Internal
public enum ValidatingOptionsLoader {

    INSTANCE;

    private final ImmutableSet<ValidatingOptionFactory> implementations;

    ValidatingOptionsLoader() {
        ServiceLoader<ValidatingOptionFactory> loader = load(ValidatingOptionFactory.class);
        this.implementations = ImmutableSet.copyOf(loader);
    }

    /**
     * Obtains all the implementations of {@link ValidatingOptionFactory} available at current runtime.
     *
     * <p>Uses a {@link ServiceLoader} to scan for the SPI implementations.
     *
     * @return a stream of all available {@link ValidatingOptionFactory} implementations
     * @implNote The implementations are actually loaded when the enum instance is created.
     *         This method only accesses the loaded services.
     */
    public ImmutableSet<ValidatingOptionFactory> implementations() {
        return implementations;
    }
}
