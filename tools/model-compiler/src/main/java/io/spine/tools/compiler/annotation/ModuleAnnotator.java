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

package io.spine.tools.compiler.annotation;

import com.google.common.collect.ImmutableSet;
import io.spine.code.java.ClassName;

import java.util.Set;

import static com.google.common.base.Preconditions.checkNotNull;
import static com.google.common.collect.Sets.newHashSet;

public final class ModuleAnnotator {

    private final AnnotatorFactory annotatorFactory;
    private final ImmutableSet<Job> jobs;

    private ModuleAnnotator(Builder builder) {
        this.annotatorFactory = builder.annotatorFactory;
        this.jobs = ImmutableSet.copyOf(builder.jobs);
    }

    public void annotate() {
        jobs.forEach(this::execute);
    }

    private void execute(Job job) {
        ClassName annotation = job.javaAnnotation;
        ApiOption option = job.protobufOption;
        annotatorFactory.createFileAnnotator(annotation, option)
                        .annotate();
        annotatorFactory.createMessageAnnotator(annotation, option)
                        .annotate();
        if (option.supportsServices()) {
            annotatorFactory.createServiceAnnotator(annotation, option)
                            .annotate();
        }
        if (option.supportsFields()) {
            annotatorFactory.createFieldAnnotator(annotation, option)
                            .annotate();
        }
    }

    public static JobBuilder translate(ApiOption option) {
        checkNotNull(option);
        return new JobBuilder(option);
    }

    public static final class Job {

        private final ApiOption protobufOption;
        private final ClassName javaAnnotation;

        private Job(ApiOption protobufOption, ClassName javaAnnotation) {
            this.protobufOption = protobufOption;
            this.javaAnnotation = javaAnnotation;
        }
    }

    public static final class JobBuilder {

        private final ApiOption targetOption;

        private JobBuilder(ApiOption targetOption) {
            this.targetOption = targetOption;
        }

        public Job as(ClassName annotation) {
            checkNotNull(annotation);
            return new Job(targetOption, annotation);
        }
    }

    /**
     * Creates a new instance of {@code Builder} for {@code ModuleAnnotator} instances.
     *
     * @return new instance of {@code Builder}
     */
    public static Builder newBuilder() {
        return new Builder();
    }

    /**
     * A builder for the {@code ModuleAnnotator} instances.
     */
    public static final class Builder {

        private final Set<Job> jobs;
        private AnnotatorFactory annotatorFactory;

        /**
         * Prevents direct instantiation.
         */
        private Builder() {
            this.jobs = newHashSet();
        }

        public Builder setAnnotatorFactory(AnnotatorFactory annotatorFactory) {
            this.annotatorFactory = checkNotNull(annotatorFactory);
            return this;
        }

        public Builder add(Job job) {
            checkNotNull(job);
            this.jobs.add(job);
            return this;
        }

        /**
         * Creates a new instance of {@code ModuleAnnotator}.
         *
         * @return new instance of {@code ModuleAnnotator}
         */
        public ModuleAnnotator build() {
            checkNotNull(annotatorFactory);
            return new ModuleAnnotator(this);
        }
    }
}
