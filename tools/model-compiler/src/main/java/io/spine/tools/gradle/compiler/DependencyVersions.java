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

package io.spine.tools.gradle.compiler;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.util.Properties;

import static com.google.common.base.Charsets.UTF_8;
import static com.google.common.base.Preconditions.checkNotNull;
import static io.spine.util.Exceptions.illegalStateWithCauseOf;

final class DependencyVersions {

    private static final String RESOURCE_NAME = "/versions.properties";

    private final String spineBase;
    private final String protobuf;
    private final String grpc;

    private DependencyVersions(String spineBase, String protobuf, String grpc) {
        this.spineBase = spineBase;
        this.protobuf = protobuf;
        this.grpc = grpc;
    }

    static DependencyVersions load() {
         try (InputStream resource = DependencyVersions.class.getResourceAsStream(RESOURCE_NAME)) {
             checkNotNull(resource, "%s is not available.", RESOURCE_NAME);
             Reader reader = new InputStreamReader(resource, UTF_8);
             Properties properties = new Properties();
             properties.load(reader);
             return loadFrom(properties);
         } catch (IOException e) {
             throw illegalStateWithCauseOf(e);
         }
    }

    private static DependencyVersions loadFrom(Properties properties) {
        return new DependencyVersions(Property.SPINE.from(properties),
                                      Property.PROTOBUF.from(properties),
                                      Property.GRPC.from(properties));
    }

    String spineBase() {
        return spineBase;
    }

    String protobuf() {
        return protobuf;
    }

    String grpc() {
        return grpc;
    }

    private enum Property {

        SPINE("baseVersion"),
        PROTOBUF("protobufVersion"),
        GRPC("gRPCVersion");

        private final String name;

        Property(String name) {
            this.name = name;
        }

        private String from(Properties properties) {
            String value = properties.getProperty(name);
            checkNotNull(value, "Property `%s` could not be found.", name);
            return value;
        }
    }
}
