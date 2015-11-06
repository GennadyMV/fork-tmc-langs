package fi.helsinki.cs.tmc.langs.rust;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import fi.helsinki.cs.tmc.langs.abstraction.ValidationError;
import fi.helsinki.cs.tmc.langs.abstraction.ValidationResult;
import fi.helsinki.cs.tmc.langs.domain.RunResult;
import fi.helsinki.cs.tmc.langs.domain.SpecialLogs;
import fi.helsinki.cs.tmc.langs.utils.TestUtils;

import org.junit.Before;
import org.junit.Test;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

public class CargoPluginTest {

    private CargoPlugin cargoPlugin;

    @Before
    public void setUp() {
        cargoPlugin = new CargoPlugin();
    }

    @Test
    public void testIsExerciseCorrectTypeDoesntBreakByDirectoryNamedCargoToml() throws IOException {
        Path parent = Files.createTempDirectory("tmc-cargo-test");
        Path cargoToml = parent.resolve("Cargo.toml");
        Files.createDirectory(cargoToml);

        assertFalse(cargoPlugin.isExerciseTypeCorrect(parent));

        Files.delete(cargoToml);
        Files.delete(parent);
    }

    @Test
    public void testProjectWithPassingTestCompilesAndPassesTests() {
        Path path = TestUtils.getPath(getClass(), "passing");
        RunResult result = cargoPlugin.runTests(path);

        assertEquals(RunResult.Status.PASSED, result.status);
        assertEquals(1, result.testResults.size());
        assertTrue(result.testResults.get(0).passed);
    }

    @Test
    public void testProjectWithMultiplePassingTestCompilesAndPassesTests() {
        Path path = TestUtils.getPath(getClass(), "multiPassing");
        RunResult result = cargoPlugin.runTests(path);

        assertEquals(RunResult.Status.PASSED, result.status);
        assertEquals(2, result.testResults.size());
        assertTrue(result.testResults.get(0).passed);
        assertTrue(result.testResults.get(1).passed);
    }

    @Test
    public void testProjectWithFailingTestCompilesAndFailsTest() {
        Path path = TestUtils.getPath(getClass(), "failing");
        RunResult result = cargoPlugin.runTests(path);

        assertEquals(RunResult.Status.TESTS_FAILED, result.status);
        assertEquals(1, result.testResults.size());
        assertFalse(result.testResults.get(0).passed);
    }

    @Test
    public void testProjectPartiallyFailingTestCompilesAndFailsTest() {
        Path path = TestUtils.getPath(getClass(), "semiFailing");
        RunResult result = cargoPlugin.runTests(path);

        assertEquals(RunResult.Status.TESTS_FAILED, result.status);
        assertEquals(2, result.testResults.size());
        boolean first = result.testResults.get(0).passed;
        if (first) {
            assertFalse(result.testResults.get(1).passed);
        } else {
            assertTrue(result.testResults.get(1).passed);
        }
    }

    @Test
    public void testTryingToCheatByAddingTestFails() {
        Path path = TestUtils.getPath(getClass(), "testCheat");
        RunResult result = cargoPlugin.runTests(path);

        assertEquals(RunResult.Status.TESTS_FAILED, result.status);
        assertEquals(1, result.testResults.size());
        assertFalse(result.testResults.get(0).passed);
    }

    @Test
    public void compilationFailurePreserversCompilationOutput() {
        Path path = TestUtils.getPath(getClass(), "compileFail");
        RunResult result = cargoPlugin.runTests(path);

        assertEquals(RunResult.Status.COMPILE_FAILED, result.status);
        assertTrue(result.logs.containsKey(SpecialLogs.COMPILER_OUTPUT));
        assertTrue(new String(result.logs.get(SpecialLogs.COMPILER_OUTPUT))
                .contains("aborting due to previous error"));
        assertEquals(0, result.testResults.size());
    }

    @Test
    public void lintingWorksWithOneError() {
        Path path = TestUtils.getPath(getClass(), "warning");
        ValidationResult result = cargoPlugin.checkCodeStyle(path);
        Map<File, List<ValidationError>> errors = result.getValidationErrors();
        assertEquals(1, errors.size());
        assertEquals(Paths.get("src", "lib.rs"), errors.keySet().iterator().next().toPath());
        assertEquals(1, errors.values().iterator().next().size());
    }

    @Test
    public void lintingHasRightErrorWithOneError() {
        Path path = TestUtils.getPath(getClass(), "warning");
        ValidationResult result = cargoPlugin.checkCodeStyle(path);
        ValidationError validation = result
                .getValidationErrors().values()
                .iterator().next().get(0);
        assertEquals(7, validation.getLine());
        assertEquals(1, validation.getColumn());
        assertTrue(validation.getMessage().contains("snake case"));
        assertTrue(validation.getSourceName().contains("lib.rs"));
    }

    @Test
    public void lintingWorksWithTwoErrors() {
        Path path = TestUtils.getPath(getClass(), "warnings");
        ValidationResult result = cargoPlugin.checkCodeStyle(path);
        Map<File, List<ValidationError>> errors = result.getValidationErrors();
        assertEquals(1, errors.size());
        assertEquals(Paths.get("src", "lib.rs"), errors.keySet().iterator().next().toPath());
        assertEquals(2, errors.values().iterator().next().size());
    }

    @Test
    public void lintingHasRightErrorsWithTwoErrors() {
        Path path = TestUtils.getPath(getClass(), "warnings");
        ValidationResult result = cargoPlugin.checkCodeStyle(path);
        List<ValidationError> errors = result.getValidationErrors().values()
                .iterator().next();
        ValidationError validation1 = errors.get(0);
        ValidationError validation2 = errors.get(1);
        if (validation2.getMessage().contains("snake case")) {
            ValidationError tmp = validation1;
            validation1 = validation2;
            validation2 = tmp;
        }
        assertEquals(7, validation1.getLine());
        assertEquals(1, validation1.getColumn());
        assertTrue(validation1.getMessage().contains("snake case"));
        assertTrue(validation1.getSourceName().contains("lib.rs"));

        assertEquals(8, validation2.getLine());
        assertEquals(9, validation2.getColumn());
        assertTrue(validation2.getMessage().contains("unused"));
        assertTrue(validation2.getSourceName().contains("lib.rs"));
    }

    @Test
    public void lintingWorksWithTwoFiles() {
        Path path = TestUtils.getPath(getClass(), "warningFiles");
        ValidationResult result = cargoPlugin.checkCodeStyle(path);
        Map<File, List<ValidationError>> errors = result.getValidationErrors();
        assertEquals(2, errors.size());
        Iterator<Entry<File, List<ValidationError>>> errorIt = errors.entrySet().iterator();
        Entry<File, List<ValidationError>> error1 = errorIt.next();
        Entry<File, List<ValidationError>> error2 = errorIt.next();
        if (error2.getKey().getPath().contains("lib.rs")) {
            Entry<File, List<ValidationError>> tmp = error1;
            error1 = error2;
            error2 = tmp;
        }
        assertEquals(Paths.get("src", "lib.rs"), error1.getKey().toPath());
        assertEquals(Paths.get("src", "xor_adder.rs"), error2.getKey().toPath());
        assertEquals(1, error1.getValue().size());
        assertEquals(1, error2.getValue().size());
    }

    @Test
    public void lintingHasRightErrorsWithTwoFiles() {
        Path path = TestUtils.getPath(getClass(), "warningFiles");
        ValidationResult result = cargoPlugin.checkCodeStyle(path);
        Map<File, List<ValidationError>> errors = result.getValidationErrors();
        Iterator<Entry<File, List<ValidationError>>> errorIt = errors.entrySet().iterator();
        Entry<File, List<ValidationError>> error1 = errorIt.next();
        Entry<File, List<ValidationError>> error2 = errorIt.next();
        if (error2.getKey().getPath().contains("lib.rs")) {
            Entry<File, List<ValidationError>> tmp = error1;
            error1 = error2;
            error2 = tmp;
        }
        assertEquals(4, error1.getValue().get(0).getLine());
        assertEquals(9, error1.getValue().get(0).getColumn());
        assertTrue(error1.getValue().get(0).getMessage().contains("unused"));
        assertTrue(error1.getValue().get(0).getSourceName().contains("lib.rs"));

        assertEquals(1, error2.getValue().get(0).getLine());
        assertEquals(1, error2.getValue().get(0).getColumn());
        assertTrue(error2.getValue().get(0).getMessage().contains("snake case"));
        assertTrue(error2.getValue().get(0).getSourceName().contains("xor_adder.rs"));
    }
}
