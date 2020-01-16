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

package io.spine.validate;

import io.spine.validate.option.ValidatingOptionFactory;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.Set;
import java.util.function.Function;

import static com.google.common.truth.Truth.assertThat;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;

@DisplayName("Interface `ValidatingOptionFactory` should")
class ValidatingOptionFactoryTest {

    private ValidatingOptionFactory options;

    @BeforeEach
    void setUp() {
        options = new ValidatingOptionFactory() {};
    }

    @Test
    @DisplayName("have no abstract methods")
    void noAbstract() {
        // Actually, just has to at least compile.
        assertDoesNotThrow(() -> new ValidatingOptionFactory() {});
    }

    @Test
    @DisplayName("provide empty sets of options for all types by default")
    void provideEmptySets() {
        assetEmpty(ValidatingOptionFactory::forBoolean);
        assetEmpty(ValidatingOptionFactory::forByteString);
        assetEmpty(ValidatingOptionFactory::forDouble);
        assetEmpty(ValidatingOptionFactory::forEnum);
        assetEmpty(ValidatingOptionFactory::forFloat);
        assetEmpty(ValidatingOptionFactory::forInt);
        assetEmpty(ValidatingOptionFactory::forLong);
        assetEmpty(ValidatingOptionFactory::forMessage);
        assetEmpty(ValidatingOptionFactory::forString);
    }

    private void assetEmpty(Function<ValidatingOptionFactory, Set<?>> typeSelector) {
        Set<?> result = typeSelector.apply(options);
        assertThat(result).isEmpty();
    }
}
