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

package io.spine.dart.generate;

import com.google.common.io.CharStreams;
import io.spine.io.Resource;
import org.apache.tools.ant.filters.ReplaceTokens;
import org.apache.tools.ant.filters.ReplaceTokens.Token;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import static com.google.common.base.Preconditions.checkNotNull;
import static io.spine.util.Exceptions.newIllegalStateException;

/**
 * A Dart code template loaded from a resource.
 */
public class CodeTemplate {

    private final Resource template;
    private final Map<String, String> insertions;

    public CodeTemplate(Resource resource) {
        this.template = checkNotNull(resource);
        this.insertions = new HashMap<>();
    }

    public void insert(String token, String content) {
        checkNotNull(token);
        checkNotNull(content);

        insertions.put(token, content);
    }

    public GeneratedDartFile compile() {
        try (ReplaceTokens reader = new ReplaceTokens(template.openAsText())) {
            insertions.forEach(
                    (key, value) -> reader.addConfiguredToken(token(key, value))
            );
            String text = CharStreams.toString(reader);
            return new GeneratedDartFile(text);
        } catch (IOException e) {
            throw newIllegalStateException(e, "Unable to read resource `%s`.", template);
        }
    }

    private static Token token(String tokenKey, String replaceWith) {
        Token token = new Token();
        token.setKey(tokenKey);
        token.setValue(replaceWith);
        return token;
    }
}
