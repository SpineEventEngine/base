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

import com.google.common.base.Function;
import com.google.common.io.Files;
import io.spine.gradle.compiler.model.given.Given;
import io.spine.server.model.DuplicateCommandHandlerError;
import io.spine.tools.model.SpineModel;
import org.gradle.api.Project;
import org.gradle.api.tasks.TaskCollection;
import org.gradle.api.tasks.TaskContainer;
import org.gradle.api.tasks.compile.JavaCompile;
import org.gradle.internal.impldep.com.google.common.collect.Iterators;
import org.gradle.testfixtures.ProjectBuilder;
import org.junit.BeforeClass;
import org.junit.Test;

import java.io.File;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Collections;

import static io.spine.gradle.TaskName.COMPILE_JAVA;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * @author Dmytro Dashenkov
 */
public class ModelVerifierShould {

    private static final Object[] EMPTY_ARRAY = new Object[0];

    private Project project = null;

    @BeforeClass
    public void setUp() {
        project = mock(Project.class);
        when(project.getSubprojects()).thenReturn(Collections.<Project>emptySet());
        when(project.getRootProject()).thenReturn(project);
        final TaskContainer tasks = mock(TaskContainer.class);
        final TaskCollection emptyTaskCollection = mock(TaskCollection.class);
        when(emptyTaskCollection.iterator()).thenReturn(Iterators.emptyIterator());
        when(emptyTaskCollection.toArray()).thenReturn(EMPTY_ARRAY);
        when(tasks.withType(any(Class.class))).thenReturn(emptyTaskCollection);
        when(project.getTasks()).thenReturn(tasks);
    }

    @Test
    public void verify_model_from_classpath() {
        final ModelVerifier verifier = new ModelVerifier(project);

        verify(project).getSubprojects();

        final String commandHandlerTypeName = Given.TestCommandHandler.class.getName();
        final String aggregateTypeName = Given.TestAggregate.class.getName();
        final String procManTypeName = Given.TestProcMan.class.getName();
        final SpineModel spineModel = SpineModel.newBuilder()
                                                .addCommandHandlingTypes(commandHandlerTypeName)
                                                .addCommandHandlingTypes(aggregateTypeName)
                                                .addCommandHandlingTypes(procManTypeName)
                                                .build();
        verifier.verify(spineModel);
    }

    @Test(expected = DuplicateCommandHandlerError.class)
    public void fail_on_duplicate_command_handlers() {
        final ModelVerifier verifier = new ModelVerifier(project);
        final String firstType = Given.TestCommandHandler.class.getName();
        final String secondType = Given.DuplicateCommandHandler.class.getName();

        final SpineModel spineModel = SpineModel.newBuilder()
                                                .addCommandHandlingTypes(firstType)
                                                .addCommandHandlingTypes(secondType)
                                                .build();
        verifier.verify(spineModel);
    }

    @Test(expected = IllegalStateException.class)
    public void not_accept_invalid_class_names() {
        final String invalidClassname = "non.existing.class.Name";
        final SpineModel spineModel = SpineModel.newBuilder()
                                                .addCommandHandlingTypes(invalidClassname)
                                                .build();
        new ModelVerifier(project).verify(spineModel);
    }

    @Test(expected = IllegalArgumentException.class)
    public void not_accept_non_command_handler_types() {
        final String invalidClassname = ModelVerifierShould.class.getName();
        final SpineModel spineModel = SpineModel.newBuilder()
                                                .addCommandHandlingTypes(invalidClassname)
                                                .build();
        new ModelVerifier(project).verify(spineModel);
    }

    @Test
    public void retrieve_compilation_dest_dir_from_task() throws MalformedURLException {
        final JavaCompile compileTask = actualProject().getTasks()
                                                       .withType(JavaCompile.class)
                                                       .getByName(COMPILE_JAVA.getValue());
        final File dest = Files.createTempDir();
        compileTask.setDestinationDir(dest);
        final Function<JavaCompile, URL> func = ModelVerifier.GetDestinationDir.FUNCTION;
        final URL destUrl = dest.toURI().toURL();
        assertEquals(destUrl, func.apply(compileTask));
    }

    @Test
    public void retrieve_null_if_dest_dir_is_null() {
        final JavaCompile compileTask = mock(JavaCompile.class);
        final Function<JavaCompile, URL> func = ModelVerifier.GetDestinationDir.FUNCTION;
        assertNull(func.apply(compileTask));
    }

    private static Project actualProject() {
        final Project result = ProjectBuilder.builder().build();
        result.getPluginManager().apply("java");
        return result;
    }
}
