/*
 * Copyright 2018, TeamDev Ltd. All rights reserved.
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
package io.spine.tools.gradle.compiler.enrichment;

import io.spine.tools.compiler.enrichment.EnrichmentFinder;
import io.spine.tools.gradle.GradleTask;
import io.spine.tools.gradle.SpinePlugin;
import org.gradle.api.Action;
import org.gradle.api.Project;
import org.gradle.api.Task;
import org.slf4j.Logger;

import java.io.File;

import static io.spine.tools.gradle.TaskName.COMPILE_JAVA;
import static io.spine.tools.gradle.TaskName.COMPILE_TEST_JAVA;
import static io.spine.tools.gradle.TaskName.FIND_ENRICHMENTS;
import static io.spine.tools.gradle.TaskName.FIND_TEST_ENRICHMENTS;
import static io.spine.tools.gradle.TaskName.PROCESS_RESOURCES;
import static io.spine.tools.gradle.TaskName.PROCESS_TEST_RESOURCES;
import static io.spine.tools.gradle.compiler.Extension.getMainDescriptorSetPath;
import static io.spine.tools.gradle.compiler.Extension.getMainTargetGenResourcesDir;
import static io.spine.tools.gradle.compiler.Extension.getTestDescriptorSetPath;
import static io.spine.tools.gradle.compiler.Extension.getTestTargetGenResourcesDir;

/**
 * Finds event enrichment Protobuf definitions and creates a {@code .properties} file,
 * which contains entries like:
 *
 * <p>{@code ENRICHMENT_TYPE_NAME=EVENT_TO_ENRICH_TYPE_NAME}
 *
 * <p>There can be several event types:
 *
 * <p>{@code ENRICHMENT_TYPE_NAME=FIRST_EVENT_TYPE_NAME,SECOND_EVENT_TYPE_NAME}
 *
 * @author Alexander Litus
 * @author Alex Tymchenko
 */
public class EnrichmentLookupPlugin extends SpinePlugin {

    @Override
    public void apply(final Project project) {
        final Action<Task> mainScopeAction = mainScopeActionFor(project);
        logDependingTask(FIND_ENRICHMENTS, PROCESS_RESOURCES, COMPILE_JAVA);
        final GradleTask findEnrichments =
                newTask(FIND_ENRICHMENTS,
                        mainScopeAction).insertAfterTask(COMPILE_JAVA)
                                        .insertBeforeTask(PROCESS_RESOURCES)
                                        .applyNowTo(project);
        final Action<Task> testScopeAction = testScopeActionFor(project);
        logDependingTask(FIND_TEST_ENRICHMENTS, PROCESS_TEST_RESOURCES, COMPILE_TEST_JAVA);
        final GradleTask findTestEnrichments =
                newTask(FIND_TEST_ENRICHMENTS,
                        testScopeAction).insertAfterTask(COMPILE_TEST_JAVA)
                                        .insertBeforeTask(PROCESS_TEST_RESOURCES)
                                        .applyNowTo(project);

        final String msg = "Enrichment lookup phase initialized with tasks: {}, {}";
        log().debug(msg, findEnrichments, findTestEnrichments);
    }

    private Action<Task> testScopeActionFor(final Project project) {
        log().debug("Initializing the enrichment lookup for the \"test\" source code");
        return new Action<Task>() {
            @Override
            public void execute(Task task) {
                findEnrichmentsAndWriteProps(getTestDescriptorSetPath(project),
                                             getTestTargetGenResourcesDir(project)
                );
            }
        };
    }

    private Action<Task> mainScopeActionFor(final Project project) {
        log().debug("Initializing the enrichment lookup for the \"main\" source code");
        return new Action<Task>() {
            @Override
            public void execute(Task task) {
                findEnrichmentsAndWriteProps(getMainDescriptorSetPath(project),
                                             getMainTargetGenResourcesDir(project)
                );
            }
        };
    }

    private void findEnrichmentsAndWriteProps(String descriptorSetFile, String targetDir) {
        final Logger log = log();
        log.debug("Enrichment lookup started");

        final File file = new File(descriptorSetFile);

        if (file.exists()) {
            if (EnrichmentFinder.processDescriptorSetFile(file, targetDir)) {
                return;
            }
        } else {
            logMissingDescriptorSetFile(file);
        }

        log.debug("Enrichment lookup complete");
    }
}
