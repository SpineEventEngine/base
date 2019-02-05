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

package io.spine.gradle.compiler.lookup.enrichments;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

import static org.junit.jupiter.api.Assertions.assertEquals;

@DisplayName("EnrichmentLookupPlugin should")
class EnrichmentLookupPluginTest {

    private static final String GENERATED_ENRICHMENTS = "generated/test/resources/enrichments.properties";

    private static final String EXPECTED_ENRICHMENTS = "src/test/resources/expected_enrichments.properties";

    @DisplayName("generate proper enrichments")
    @Test
    void generateProperEnrichments() {
        Properties generatedEnrichments = loadProperties(GENERATED_ENRICHMENTS);
        Properties expectedEnrichments = loadProperties(EXPECTED_ENRICHMENTS);

        assertEquals(expectedEnrichments, generatedEnrichments);
    }

    private static Properties loadProperties(String pathname) {
        File propFile = new File(pathname);
        try (InputStream input = new FileInputStream(propFile)) {
            Properties props = new Properties();
            props.load(input);
            return props;
        } catch (IOException e) {
            throw new IllegalStateException(e);
        }
    }
}
