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

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.function.Predicate;

import static io.spine.tools.protoc.MessageSelectorFactory.prefix;
import static io.spine.tools.protoc.MessageSelectorFactory.regex;
import static io.spine.tools.protoc.MessageSelectorFactory.suffix;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

@DisplayName("`GeneratedNestedClasses` should")
class GeneratedNestedClassesTest {

    public static final String FACTORY = "io.spine.test.NestedClassFactory";

    @Test
    @DisplayName("convert to proper Protoc configuration")
    void convertToProperProtocConfiguration() {
        GeneratedNestedClasses classes = new GeneratedNestedClasses();
        MessageSelectorFactory messages = classes.messages();
        classes.applyFactory(FACTORY, messages.inFiles(suffix("_test.proto")));
        AddNestedClasses config = classes.asProtocConfig();

        assertEquals(FACTORY, config.getFactoryByPattern(0)
                                    .getValue());
    }

    @Test
    @DisplayName("add multiple file patterns")
    void addMultipleFilePatterns() {
        String pattern = "file_name_pattern";

        GeneratedNestedClasses defaults = new GeneratedNestedClasses();
        MessageSelectorFactory messages = defaults.messages();
        defaults.applyFactory(FACTORY, messages.inFiles(suffix(pattern)));
        defaults.applyFactory(FACTORY, messages.inFiles(prefix(pattern)));
        defaults.applyFactory(FACTORY, messages.inFiles(regex(pattern)));

        assertTrue(hasSuffixConfig(pattern, FACTORY, defaults.asProtocConfig()));
        assertTrue(hasPrefixConfig(pattern, FACTORY, defaults.asProtocConfig()));
        assertTrue(hasRegexConfig(pattern, FACTORY, defaults.asProtocConfig()));
    }

    private static boolean
    hasSuffixConfig(String suffix, String factoryName, AddNestedClasses config) {
        return hasConfig(config, factoryName, pattern -> suffix.equals(pattern.getSuffix()));
    }

    private static boolean
    hasPrefixConfig(String prefix, String factoryName, AddNestedClasses config) {
        return hasConfig(config, factoryName, pattern -> prefix.equals(pattern.getPrefix()));
    }

    private static boolean
    hasRegexConfig(String regex, String factoryName, AddNestedClasses config) {
        return hasConfig(config, factoryName, pattern -> regex.equals(pattern.getRegex()));
    }

    private static boolean hasConfig(AddNestedClasses config,
                                     String factoryName,
                                     Predicate<? super FilePattern> patternPredicate) {
        return config
                .getFactoryByPatternList()
                .stream()
                .filter(byPattern -> factoryName.equals(byPattern.getValue()))
                .map(ConfigByPattern::getPattern)
                .anyMatch(patternPredicate);
    }
}
