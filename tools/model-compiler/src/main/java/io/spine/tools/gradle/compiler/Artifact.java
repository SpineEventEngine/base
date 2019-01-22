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

import org.checkerframework.checker.nullness.qual.Nullable;

import static com.google.common.base.Preconditions.checkNotNull;

public final class Artifact {

    public static final String SPINE_TOOLS_GROUP = "io.spine.tools";

    private static final char COLON = ':';
    private static final char AT = '@';

    private final String group;
    private final String name;
    private final String version;
    private final @Nullable String classifier;
    private final @Nullable String extension;

    private Artifact(Builder builder) {
        this.group = builder.group;
        this.name = builder.name;
        this.version = builder.version;
        this.classifier = builder.classifier;
        this.extension = builder.extension;
    }
    
    String notation() {
        StringBuilder result = new StringBuilder(group)
                .append(COLON)
                .append(name)
                .append(COLON)
                .append(version);
        if (classifier != null) {
            result.append(COLON)
                  .append(classifier);
        }
        if (extension != null) {
            result.append(AT)
                  .append(extension);
        }
        return result.toString();
    }

    @Override
    public String toString() {
        return notation();
    }

    /**
     * Creates a new instance of {@code Builder} for {@code Artifact} instances.
     * 
     * @return new instance of {@code Builder}
     */
    static Builder newBuilder() {
        return new Builder();
    }
    
    /**
     * A builder for the {@code Artifact} instances.
     */
    static final class Builder {

        private String group;
        private String name;
        private String version;
        private String classifier;
        private String extension;
    
        /**
         * Prevents direct instantiation.
         */
        private Builder() {
        }

        Builder setGroup(String group) {
            this.group = group;
            return this;
        }

        Builder useSpineToolsGroup() {
            return setGroup(SPINE_TOOLS_GROUP);
        }

        Builder setName(String name) {
            this.name = name;
            return this;
        }

        Builder setVersion(String version) {
            this.version = version;
            return this;
        }

        Builder setClassifier(String classifier) {
            this.classifier = classifier;
            return this;
        }

        Builder setExtension(String extension) {
            this.extension = extension;
            return this;
        }

        /**
         * Creates a new instance of {@code Artifact}.
         * 
         * @return new instance of {@code Artifact}
         */
        Artifact build() {
            checkNotNull(group);
            checkNotNull(name);
            checkNotNull(version);

            return new Artifact(this);
        }
    }
}
