package io.spine.gradle.compiler.lookup.valrule;

import io.spine.gradle.compiler.GradleProject;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.Dictionary;
import java.util.List;
import java.util.Properties;

import static io.spine.gradle.TaskName.FIND_VALIDATION_RULES;
import static io.spine.gradle.compiler.Extension.getDefaultMainGenResDir;
import static io.spine.gradle.compiler.lookup.valrule.ValidationRulesFinder.PROTO_TYPE_SEPARATOR;
import static io.spine.util.Exceptions.illegalStateWithCauseOf;
import static org.junit.Assert.assertEquals;

/**
 * @author Dmytro Grankin
 */
public class ValidationRulesLookupPluginShould {

    private static final String PROJECT_NAME = "validation-rules-lookup-plugin-test";

    private static final String PROTO_FILE_PACKAGE = "test.valrule";
    private static final String OUTER_MESSAGE_TYPE = "Outer";
    private static final String VALIDATION_RULE_TYPE = "ValidationRule";
    private static final String VALIDATION_TARGET = PROTO_FILE_PACKAGE + PROTO_TYPE_SEPARATOR +
            OUTER_MESSAGE_TYPE + PROTO_TYPE_SEPARATOR + "field_name";
    private static final List<String> NESTED_VALIDATION_RULE_PROTO =
            Arrays.asList("syntax = \"proto3\";",
                          "package " + PROTO_FILE_PACKAGE + ';',
                          "import \"spine/options.proto\";",

                          "message " + OUTER_MESSAGE_TYPE + " {",

                          "message " + VALIDATION_RULE_TYPE + " {",
                          "option (validation_of) = \"" + VALIDATION_TARGET + "\";",
                          "}",

                          "}"
            );

    @Rule
    public final TemporaryFolder testProjectDir = new TemporaryFolder();

    @Test
    public void findNestedValidationRules() throws Exception {
        final String file = "nested_validation_rule.proto";
        final GradleProject project = newGradleProject(file, NESTED_VALIDATION_RULE_PROTO);
        project.executeTask(FIND_VALIDATION_RULES);

        final String expectedKey = PROTO_FILE_PACKAGE + PROTO_TYPE_SEPARATOR +
                OUTER_MESSAGE_TYPE + PROTO_TYPE_SEPARATOR + VALIDATION_RULE_TYPE;
        final String value = (String) getProperties().get(expectedKey);
        assertEquals(value, VALIDATION_TARGET);
    }

    private Dictionary getProperties() {
        final String projectPath = testProjectDir.getRoot()
                                                 .getAbsolutePath();
        final Path path = Paths.get(projectPath, getDefaultMainGenResDir(),
                                    ValidationRulesLookupPlugin.VALIDATION_PROPS_FILE_NAME);
        try {
            final InputStream inputStream = new FileInputStream(path.toFile());
            final Properties properties = new Properties();
            properties.load(inputStream);
            return properties;
        } catch (IOException e) {
            throw illegalStateWithCauseOf(e);
        }
    }

    private GradleProject newGradleProject(String protoFileName, List<String> protoFileLines) {
        return GradleProject.newBuilder()
                            .setProjectName(PROJECT_NAME)
                            .setProjectFolder(testProjectDir)
                            .createProto(protoFileName, protoFileLines)
                            .build();
    }
}
