/*
 * Copyright 2019, TeamDev. All rights reserved.
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

package io.spine.validate;

import java.util.ServiceLoader;
import java.util.Spliterator;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

enum ValidatorFactoryLoader {

    INSTANCE;

    private final ServiceLoader<ValidatorFactory> loader;

    ValidatorFactoryLoader() {
        this.loader = ServiceLoader.load(ValidatorFactory.class);
    }

    /**
     * Obtains all the implementations of {@link ValidatorFactory} available at current runtime.
     *
     * <p>Uses a {@link ServiceLoader} to scan for the SPI implementations.
     *
     * @return a stream of all available {@link ValidatorFactory} implementations
     */
    Stream<ValidatorFactory> implementations() {
        ServiceLoader<ValidatorFactory> loader = ServiceLoader.load(ValidatorFactory.class);
        Spliterator<ValidatorFactory> spliterator = loader.spliterator();
        return StreamSupport.stream(spliterator, false);
    }
}
