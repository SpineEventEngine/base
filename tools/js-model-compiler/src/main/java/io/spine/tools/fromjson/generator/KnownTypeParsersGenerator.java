/*
 * Copyright 2018, TeamDev. All rights reserved.
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

package io.spine.tools.fromjson.generator;

import com.google.common.collect.ImmutableList;
import com.google.protobuf.Any;
import com.google.protobuf.BytesValue;
import com.google.protobuf.DoubleValue;
import com.google.protobuf.Duration;
import com.google.protobuf.Empty;
import com.google.protobuf.FieldMask;
import com.google.protobuf.FloatValue;
import com.google.protobuf.Int32Value;
import com.google.protobuf.Int64Value;
import com.google.protobuf.ListValue;
import com.google.protobuf.StringValue;
import com.google.protobuf.Timestamp;
import com.google.protobuf.UInt32Value;
import com.google.protobuf.UInt64Value;
import com.google.protobuf.Value;
import io.spine.tools.fromjson.js.KnownTypeParsersCode;
import io.spine.type.TypeUrl;
import org.gradle.api.Project;

import java.io.File;
import java.nio.file.Path;
import java.nio.file.Paths;

import static io.spine.type.TypeUrl.of;

public class KnownTypeParsersGenerator {

    static final String FILE_NAME = "known_type_parsers";

    // todo make it so we don't need to call map name from file import and just can call "get"
    static final String MAP_NAME = KnownTypeParsersCode.mapName();
    static final ImmutableList<TypeUrl> WELL_KNOWN_TYPES = wellKnownTypes();

    private static final String JS_FILE_NAME = FILE_NAME + ".js";

    private final Project project;

    public KnownTypeParsersGenerator(Project project) {
        this.project = project;
    }

    public Path composeFilePath() {
        File projectDir = project.getProjectDir();
        String absolutePath = projectDir.getAbsolutePath();
        Path filePath = Paths.get(absolutePath, "proto", "test", "js", JS_FILE_NAME);
        return filePath;
    }

    public String createFileContent() {
        String content = KnownTypeParsersCode.get();
        return content;
    }

    // todo make it common with Known Type Parsers js file somehow
    @SuppressWarnings("OverlyCoupledMethod")
    private static ImmutableList<TypeUrl> wellKnownTypes() {
        ImmutableList<TypeUrl> wellKnownTypes = ImmutableList.of(
                of(BytesValue.class),
                of(DoubleValue.class),
                of(FloatValue.class),
                of(Int32Value.class),
                of(Int64Value.class),
                of(StringValue.class),
                of(UInt32Value.class),
                of(UInt64Value.class),
                of(Value.class),
                of(ListValue.class),
                of(Empty.class),
                of(Timestamp.class),
                of(Duration.class),
                of(FieldMask.class),
                of(Any.class)
        );
        return wellKnownTypes;
    }
}
