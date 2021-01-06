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

package io.spine.tools.protoc;

import io.spine.code.java.ClassName;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.function.Predicate;

import static com.google.common.truth.Truth.assertThat;
import static io.spine.tools.protoc.MessageSelectorFactory.prefix;
import static io.spine.tools.protoc.MessageSelectorFactory.regex;
import static io.spine.tools.protoc.MessageSelectorFactory.suffix;
import static org.junit.jupiter.api.Assertions.assertTrue;

@DisplayName("`GeneratedInterfaces` should")
final class GeneratedInterfacesTest {

    @DisplayName("add multiple file patterns")
    @Test
    void addMultipleFilePatterns() {
        String pattern = "testPattern";
        ClassName interfaceName = ClassName.of("io.spine.test.TestInterface");

        GeneratedInterfaces defaults = new GeneratedInterfaces();
        MessageSelectorFactory messages = defaults.messages();
        defaults.mark(messages.inFiles(suffix(pattern)), interfaceName);
        defaults.mark(messages.inFiles(prefix(pattern)), interfaceName);
        defaults.mark(messages.inFiles(regex(pattern)), interfaceName);

        assertTrue(hasSuffixConfig(pattern, interfaceName, defaults.asProtocConfig()));
        assertTrue(hasPrefixConfig(pattern, interfaceName, defaults.asProtocConfig()));
        assertTrue(hasRegexConfig(pattern, interfaceName, defaults.asProtocConfig()));
    }

    @DisplayName("allows asType syntax sugar method")
    @Test
    void allowAsTypeSugar() {
        GeneratedInterfaces interfaces = new GeneratedInterfaces();
        String interfaceName = "MyInterface";
        assertThat(interfaces.asType(interfaceName)).isEqualTo(ClassName.of(interfaceName));
    }

    private static boolean
    hasSuffixConfig(String suffix, ClassName interfaceName, AddInterfaces config) {
        return hasInterface(config, interfaceName,
                            pattern -> suffix.equals(pattern.getSuffix()));
    }

    private static boolean
    hasPrefixConfig(String prefix, ClassName interfaceName, AddInterfaces config) {
        return hasInterface(config, interfaceName,
                            pattern -> prefix.equals(pattern.getPrefix()));
    }

    private static boolean
    hasRegexConfig(String regex, ClassName interfaceName, AddInterfaces config) {
        return hasInterface(config, interfaceName,
                            pattern -> regex.equals(pattern.getRegex()));
    }

    private static boolean hasInterface(AddInterfaces config,
                                        ClassName interfaceName,
                                        Predicate<? super FilePattern> patternPredicate) {
        return config
                .getInterfaceByPatternList()
                .stream()
                .filter(byPattern -> interfaceName.value()
                                                  .equals(byPattern.getValue()))
                .map(ConfigByPattern::getPattern)
                .anyMatch(patternPredicate);
    }
}
