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

package io.spine.code.proto;

import com.google.common.io.Files;
import com.google.common.io.MoreFiles;
import com.google.common.io.RecursiveDeleteOption;
import io.spine.code.proto.DescriptorReference.ResourceReference;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.file.Path;
import java.util.Iterator;
import java.util.List;

import static com.google.common.collect.Lists.newArrayList;
import static io.spine.code.proto.given.DescriptorReferenceTestEnv.toKnownTypes;
import static io.spine.code.proto.given.DescriptorReferenceTestEnv.toSmokeTestModelCompiler;
import static org.junit.Assert.assertEquals;

@DisplayName("Descriptor reference should")
class DescriptorReferenceTest {

    private static final Path PATH = Files.createTempDir()
                                          .toPath();

    @AfterEach
    void tearDown() throws IOException {
        MoreFiles.deleteRecursively(PATH, RecursiveDeleteOption.ALLOW_INSECURE);
    }

    @Test
    @DisplayName("be unaffected by Windows line separator")
    void unaffectedByDuplicates() {
        DescriptorReference knownTypes = toKnownTypes().withCrLf();
        DescriptorReference smokeTestModelCompiler = toSmokeTestModelCompiler().withCrLf();
        knownTypes.writeTo(PATH);
        smokeTestModelCompiler.writeTo(PATH);
        knownTypes.writeTo(PATH);
        Iterator<ResourceReference> existingDescriptors = DescriptorReference.loadAll();
        List<ResourceReference> result = newArrayList(existingDescriptors);
        assertEquals(3, result.size());
    }
}
