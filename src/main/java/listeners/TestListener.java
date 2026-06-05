package listeners;

import org.testng.ITestContext;
import org.testng.ITestListener;
import org.testng.ITestResult;

/**
 * TestListener — Hooks into TestNG lifecycle for logging and reporting
 *
 * Register in testng.xml:
 *   <listener class-name="listeners.TestListener"/>
 *
 * Or on test class:
 *   @Listeners({TestListener.class})
 *   public class LoginTest extends Base { ... }
 *
 * Extend this class to add Slack, email, or database notifications.
 */
public class TestListener implements ITestListener {

    @Override
    public void onTestStart(ITestResult result) {
        System.out.println("\n▶ STARTED  : " + getTestName(result));
    }

    @Override
    public void onTestSuccess(ITestResult result) {
        System.out.println("✓ PASSED   : " + getTestName(result)
            + " (" + getDuration(result) + "ms)");
    }

    @Override
    public void onTestFailure(ITestResult result) {
        System.out.println("✗ FAILED   : " + getTestName(result)
            + " (" + getDuration(result) + "ms)");
        System.out.println("  Reason   : " + result.getThrowable().getMessage());
    }

    @Override
    public void onTestSkipped(ITestResult result) {
        System.out.println("⊘ SKIPPED  : " + getTestName(result));
    }

    @Override
    public void onTestFailedButWithinSuccessPercentage(ITestResult result) {
        System.out.println("⚠ PARTIAL  : " + getTestName(result));
    }

    @Override
    public void onStart(ITestContext context) {
        System.out.println("\n═══════════════════════════════════════════════");
        System.out.println("  SUITE STARTED : " + context.getName());
        System.out.println("═══════════════════════════════════════════════");
    }

    @Override
    public void onFinish(ITestContext context) {
        int passed  = context.getPassedTests().size();
        int failed  = context.getFailedTests().size();
        int skipped = context.getSkippedTests().size();
        int total   = passed + failed + skipped;

        System.out.println("\n═══════════════════════════════════════════════");
        System.out.println("  SUITE FINISHED: " + context.getName());
        System.out.println("  Total  : " + total);
        System.out.println("  Passed : " + passed  + " ✓");
        System.out.println("  Failed : " + failed  + " ✗");
        System.out.println("  Skipped: " + skipped + " ⊘");
        System.out.println("═══════════════════════════════════════════════\n");

        // ─────────────────────────────────────────────────────────────────
        // TODO: Add Slack / Email / Teams notification here
        // Example Slack message:
        //   sendSlackMessage("Suite: " + context.getName()
        //     + " | P:" + passed + " F:" + failed + " S:" + skipped);
        // ─────────────────────────────────────────────────────────────────
    }

    // ===== Helpers =====
    private String getTestName(ITestResult result) {
        return result.getTestClass().getRealClass().getSimpleName()
            + " > " + result.getMethod().getMethodName();
    }

    private long getDuration(ITestResult result) {
        return result.getEndMillis() - result.getStartMillis();
    }
}
