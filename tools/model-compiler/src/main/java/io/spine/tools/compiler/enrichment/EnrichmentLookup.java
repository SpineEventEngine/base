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

package io.spine.tools.compiler.enrichment;

import com.google.protobuf.DescriptorProtos.DescriptorProto;
import com.google.protobuf.DescriptorProtos.FileDescriptorProto;
import io.spine.Resources;
import io.spine.code.properties.PropertiesWriter;
import io.spine.logging.Logging;
import io.spine.type.TypeName;

import java.io.File;
import java.util.List;
import java.util.Map;

import static com.google.common.collect.Maps.newHashMap;
import static io.spine.code.proto.FileDescriptors.parseSkipGoogle;

/**
 * Parses descriptor set file and creates enrichments map for discovered enrichment
 * definitions.
 */
public final class EnrichmentLookup implements Logging {

    private final File descriptorSetFile;

    /**
     * Creates new instance for generating rejections obtained from the passed descriptor set file.
     */
    public EnrichmentLookup(File descriptorSetFile) {
        this.descriptorSetFile = descriptorSetFile;
    }

    /**
     * Takes enrichment information from all the proto types found in the passed descriptor
     * set file and generates the {@linkplain Resources#ENRICHMENTS resource file} in the
     * specified directory.
     */
    public void collectTo(String targetDir) {
        List<FileDescriptorProto> files = parseSkipGoogle(descriptorSetFile);

        Map<String, String> propsMap = findAll(files);

        if (propsMap.isEmpty()) {
            _debug("Enrichment lookup complete. No enrichments found.");
            return;
        }

        writeTo(propsMap, targetDir);
    }

    private Map<String, String> findAll(Iterable<FileDescriptorProto> files) {
        Map<String, String> propsMap = newHashMap();
        for (FileDescriptorProto file : files) {
            Map<String, String> enrichments = findAllIn(file);
            propsMap.putAll(enrichments);
        }
        return propsMap;
    }

    /**
     * Finds enrichment definitions in the proto files represented by the
     * passed descriptor set file.
     *
     * @return a map from enrichment type name to the enriched types
     */
    private Map<String, String> findAllIn(FileDescriptorProto file) {
        _debug("Looking up for the enrichments in {}", file.getName());

        List<DescriptorProto> messages = file.getMessageTypeList();
        String packagePrefix = file.getPackage() + TypeName.PACKAGE_SEPARATOR;
        Map<String, String> result =
                new EnrichmentMapBuilder(packagePrefix)
                        .addAll(messages)
                        .toMap();

        _debug("Found {} enrichments", result.size());
        return result;
    }

    private void writeTo(Map<String, String> propsMap, String targetDir) {
        _debug("Writing the enrichment description to {}/{}",
                    targetDir, Resources.ENRICHMENTS);
        PropertiesWriter writer = new PropertiesWriter(targetDir, Resources.ENRICHMENTS);
        writer.write(propsMap);
    }
}
