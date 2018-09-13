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

package io.spine.tools.protojs.field;

import static io.spine.tools.protojs.field.Fields.capitalizedName;
import static io.spine.tools.protojs.message.MessageHandler.MESSAGE;

/**
 * The handler of the ordinary proto field (i.e. non-{@code repeated} and non-{@code map}).
 *
 * @author Dmytro Kuzmin
 */
final class SingularFieldHandler extends AbstractFieldHandler {

    private SingularFieldHandler(Builder builder) {
        super(builder);
    }

    @Override
    public void generateJs() {
        String jsObject = acquireJsObject();
        checkNotUndefined(jsObject);
        mergeFieldValue(jsObject);
        exitUndefinedCheck();
    }

    /**
     * {@inheritDoc}
     *
     * <p>The merge format for a singular field is just calling the corresponding field setter on
     * the message object.
     */
    @Override
    String mergeFormat() {
        String fieldName = capitalizedName(field());
        String setterName = "set" + fieldName;
        String setFieldFormat = MESSAGE + '.' + setterName + "(%s);";
        return setFieldFormat;
    }

    /**
     * Generates the code which will check the provided {@code jsObject} for not being
     * {@code undefined}.
     *
     * @param jsObject
     *         the name of the variable which holds the JS object to check
     */
    private void checkNotUndefined(String jsObject) {
        jsOutput().ifNotUndefined(jsObject);
    }

    /**
     * Generates the code which exists all block entered when checking for undefined.
     *
     * <p>Returns the cursor to the {@code fromObject} method level.
     */
    private void exitUndefinedCheck() {
        jsOutput().exitBlock();
    }

    static Builder newBuilder() {
        return new Builder();
    }

    static class Builder extends AbstractFieldHandler.Builder<Builder> {

        @Override
        Builder self() {
            return this;
        }

        @Override
        SingularFieldHandler build() {
            return new SingularFieldHandler(this);
        }
    }
}
