/*
 * Copyright 2018, TeamDev Ltd. All rights reserved.
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

package io.spine.tools.compiler.enrichment;

import com.google.protobuf.DescriptorProtos.FileDescriptorProto;
import io.spine.Resources;
import io.spine.tools.properties.PropertiesWriter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.util.Collection;
import java.util.Map;

import static com.google.common.collect.Maps.newHashMap;
import static io.spine.tools.proto.FileDescriptors.parseSkipStandard;

/**
 * Parses descriptor set file and creates enrichments map for discovered enrichment
 * definitions.
 *
 * @author Alexander Yevsyukov
 */
public class EnrichmentLookup {

    /** Prevents instantiation of this utility class. */
    private EnrichmentLookup() {
    }

    public static void processDescriptorSetFile(File setFile, String targetDir) {
        final Collection<FileDescriptorProto> files = parseSkipStandard(setFile.getPath());

        final Map<String, String> propsMap = findAll(files);

        if (propsMap.isEmpty()) {
            log().debug("Enrichment lookup complete. No enrichments found.");
            return;
        }

        writeTo(propsMap, targetDir);
    }

    private static Map<String, String> findAll(Iterable<FileDescriptorProto> files) {
        final Map<String, String> propsMap = newHashMap();
        for (FileDescriptorProto file : files) {
            final EnrichmentFinder lookup = new EnrichmentFinder(file);
            final Map<String, String> enrichments = lookup.findEnrichments();
            propsMap.putAll(enrichments);
        }
        return propsMap;
    }

    private static void writeTo(Map<String, String> propsMap, String targetDir) {
        log().debug("Writing the enrichment description to {}/{}",
                                     targetDir, Resources.ENRICHMENTS);

        final PropertiesWriter writer =
                new PropertiesWriter(targetDir, Resources.ENRICHMENTS);
        writer.write(propsMap);
    }

    private enum LogSingleton {
        INSTANCE;

        @SuppressWarnings("NonSerializableFieldInSerializableClass")
        private final Logger value = LoggerFactory.getLogger(EnrichmentLookup.class);
    }

    private static Logger log() {
        return LogSingleton.INSTANCE.value;
    }
}
