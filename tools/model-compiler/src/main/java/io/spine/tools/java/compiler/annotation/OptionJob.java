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

package io.spine.tools.java.compiler.annotation;

import com.google.common.flogger.FluentLogger;
import io.spine.code.java.ClassName;

/**
 * An annotation {@link Job} which covers Java sources generated from Protobuf marked with
 * a certain {@link ApiOption}.
 */
final class OptionJob implements Job {

    private final ApiOption protobufOption;
    private final ClassName javaAnnotation;

    OptionJob(ApiOption protobufOption, ClassName javaAnnotation) {
        this.protobufOption = protobufOption;
        this.javaAnnotation = javaAnnotation;
    }

    @SuppressWarnings("FloggerSplitLogStatement")
    // See: https://github.com/SpineEventEngine/base/issues/612
    @Override
    public void execute(AnnotatorFactory factory) {
        FluentLogger.Api debug = _debug();
        debug.log("Annotating sources marked as `%s` with `%s`.",
                  protobufOption, javaAnnotation);
        debug.log("Annotating by the file option.");
        factory.createFileAnnotator(javaAnnotation, protobufOption)
               .annotate();
        debug.log("Annotating by the message option.");
        factory.createMessageAnnotator(javaAnnotation, protobufOption)
               .annotate();
        if (protobufOption.supportsServices()) {
            debug.log("Annotating by the service option.");
            factory.createServiceAnnotator(javaAnnotation, protobufOption)
                   .annotate();
        }
        if (protobufOption.supportsFields()) {
            debug.log("Annotating by the field option.");
            factory.createFieldAnnotator(javaAnnotation, protobufOption)
                   .annotate();
        }
        debug.log("Option `%s` processed.", protobufOption);
    }
}
