/*
 * Copyright 2017, TeamDev Ltd. All rights reserved.
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

package io.spine.gradle.compiler.model;

import io.spine.server.aggregate.Aggregate;
import io.spine.server.command.CommandHandler;
import io.spine.server.model.Model;
import io.spine.server.procman.ProcessManager;
import io.spine.tools.model.SpineModel;

import static io.spine.util.Exceptions.newIllegalArgumentException;

/**
 * @author Dmytro Dashenkov
 */
final class ProcessingStages {

    private static final Model model = Model.getInstance();

    private ProcessingStages() {
        // Prevent utility class instantiation.
    }

    static ProcessingStage validate() {
        return ValidatingProcessingStage.Singleton.INSTANCE.value;
    }

    private static class ValidatingProcessingStage extends AbstractProcessingStage {

        private ValidatingProcessingStage() {
            // Prevent direct instantiation.
        }

        @SuppressWarnings("IfStatementWithTooManyBranches") // OK in this case.
        @Override
        public void process(SpineModel rawModel) {
            for (String commandHandlingClass : rawModel.getCommandHandlingTypesList()) {
                final Class<?> cls;
                try {
                    cls = Class.forName(commandHandlingClass);
                } catch (ClassNotFoundException e) {
                    throw new IllegalStateException(e);
                }
                if (Aggregate.class.isAssignableFrom(cls)) {
                    @SuppressWarnings("unchecked")
                    final Class<? extends Aggregate> aggregateClass =
                            (Class<? extends Aggregate>) cls;
                    model.asAggregateClass(aggregateClass);
                } else if (ProcessManager.class.isAssignableFrom(cls)) {
                    @SuppressWarnings("unchecked")
                    final Class<? extends ProcessManager> aggregateClass =
                            (Class<? extends ProcessManager>) cls;
                    model.asProcessManagerClass(aggregateClass);
                } else if (CommandHandler.class.isAssignableFrom(cls)) {
                    @SuppressWarnings("unchecked")
                    final Class<? extends CommandHandler> aggregateClass =
                            (Class<? extends CommandHandler>) cls;
                    model.asCommandHandlerClass(aggregateClass);
                } else {
                    throw newIllegalArgumentException("Class %s is not a command handling type.",
                                                      cls.getName());
                }
            }
        }

        private enum Singleton {
            INSTANCE;
            @SuppressWarnings("NonSerializableFieldInSerializableClass")
            private final ValidatingProcessingStage value = new ValidatingProcessingStage();
        }
    }
}
