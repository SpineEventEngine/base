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

package io.spine.tools.mc.java.protoc.message;

import com.google.protobuf.compiler.PluginProtos.CodeGeneratorResponse.File;
import io.spine.tools.mc.java.protoc.AbstractCompilerOutput;
import io.spine.tools.mc.java.protoc.InsertionPoint;
import io.spine.tools.mc.java.protoc.ProtocPluginFiles;
import io.spine.type.MessageType;

/**
 * A compiler output which makes a message class implement a given interface.
 */
public final class Implement extends AbstractCompilerOutput {

    private Implement(File file) {
        super(file);
    }

    /**
     * Creates a new instance.
     *
     * @param type
     *         the type declaration that should be altered
     * @param iface
     *         the interface to implement
     * @return new instance of {@code MessageImplements}
     */
    public static Implement interfaceFor(MessageType type, Interface iface) {
        String insertionPoint = InsertionPoint.message_implements.forType(type);
        String content = buildContent(type, iface);
        File.Builder file = ProtocPluginFiles.prepareFile(type);
        File result = file.setInsertionPoint(insertionPoint)
                          .setContent(content)
                          .build();
        return new Implement(result);
    }

    /**
     * Creates an {@code implement INTERFACE_NAME,} string.
     *
     * <p>The trailing comma is added assuming that a Protobuf message also implements
     * a generated {@code MessageOrBuilder} interface.
     */
    private static String buildContent(MessageType type, Interface iface) {
        String params = iface.parameters()
                             .asStringFor(type);
        String result = iface.name() + params + ',';
        return result;
    }
}
