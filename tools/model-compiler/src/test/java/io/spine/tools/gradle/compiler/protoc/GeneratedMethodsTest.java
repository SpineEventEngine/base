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

package io.spine.tools.gradle.compiler.protoc;

import io.spine.tools.protoc.AddMethods;
import io.spine.tools.protoc.ConfigByPattern;
import io.spine.tools.protoc.FilePattern;
import io.spine.tools.protoc.UuidConfig;
import io.spine.tools.protoc.method.uuid.UuidMethodFactory;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.util.function.Predicate;

import static com.google.common.truth.Truth.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;

@DisplayName("GeneratedMethods should")
final class GeneratedMethodsTest {

    @DisplayName("prepare default generated config for")
    @Nested
    class Default {

        @DisplayName("UUID value")
        @Test
        void uuid() {
            AddMethods config = Methods.withDefaults()
                                       .asProtocConfig();
            UuidConfig uuid = config.getUuidFactory();
            assertThat(uuid.getValue())
                    .isEqualTo(UuidMethodFactory.class.getName());
        }
    }

    @DisplayName("convert to proper Protoc configuration")
    @Test
    void convertToProperProtocConfiguration() {
        String testMethodFactory = "io.spine.test.MethodFactory";
        Methods methods = Methods.withDefaults();
        methods.useFactory(testMethodFactory, methods.uuidMessage());
        methods.useFactory(testMethodFactory, methods.filePattern()
                                                     .endsWith("_test.proto"));
        AddMethods config = methods.asProtocConfig();

        assertEquals(testMethodFactory, config.getUuidFactory()
                                              .getValue());
        assertEquals(testMethodFactory, config.getFactoryByPattern(0)
                                              .getValue());
    }

    @DisplayName("add multiple file patterns")
    @Test
    void addMultipleFilePatterns() {
        String pattern = "testPattern";
        String interfaceName = "io.spine.test.TestInterface";

        Methods defaults = Methods.withDefaults();
        FileSelectorFactory filePattern = defaults.filePattern();
        defaults.useFactory(interfaceName, filePattern.endsWith(pattern));
        defaults.useFactory(interfaceName, filePattern.startsWith(pattern));
        defaults.useFactory(interfaceName, filePattern.matches(pattern));

        assertTrue(hasPostfixConfig(pattern, interfaceName, defaults.asProtocConfig()));
        assertTrue(hasPrefixConfig(pattern, interfaceName, defaults.asProtocConfig()));
        assertTrue(hasRegexConfig(pattern, interfaceName, defaults.asProtocConfig()));
    }

    @DisplayName("be able to ignore UUID message configuration")
    @Test
    void ignoreUuidMessageConfig() {
        Methods methods = Methods.withDefaults();
        methods.ignore(methods.uuidMessage());
        assertSame(UuidConfig.getDefaultInstance(), methods.asProtocConfig()
                                                           .getUuidFactory());
    }

    private static boolean
    hasPostfixConfig(String postfix, String factoryName, AddMethods config) {
        return hasConfig(config, factoryName, pattern -> postfix.equals(pattern.getFilePostfix()));
    }

    private static boolean
    hasPrefixConfig(String prefix, String factoryName, AddMethods config) {
        return hasConfig(config, factoryName, pattern -> prefix.equals(pattern.getFilePrefix()));
    }

    private static boolean
    hasRegexConfig(String regex, String factoryName, AddMethods config) {
        return hasConfig(config, factoryName, pattern -> regex.equals(pattern.getRegex()));
    }

    private static boolean hasConfig(AddMethods config,
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
