
package fi.helsinki.cs.tmc.langs.testrunner;

import org.junit.Ignore;
import org.junit.Test;

import static org.junit.Assert.fail;

@Ignore
public class TestRunnerTestSubject {
    @Test
    public void successfulTestCase() {
    }

    @Test
    public void failingTestCase() {
        fail("too bad"); // This must be on line 17 of the source file, or the test must be updated.
    }
}
