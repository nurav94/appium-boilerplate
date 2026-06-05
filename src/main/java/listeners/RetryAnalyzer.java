package listeners;

import org.testng.IRetryAnalyzer;
import org.testng.ITestResult;

/**
 * RetryAnalyzer — Retries failed tests once before marking as failed
 *
 * Helps catch genuinely flaky tests (network, timing) vs real failures.
 * If a test fails twice → it is marked as FAILED in the report.
 *
 * Usage — apply to specific test:
 *   @Test(retryAnalyzer = RetryAnalyzer.class)
 *
 * Usage — apply to all tests via RetryListener in testng.xml:
 *   <listener class-name="listeners.RetryListener"/>
 */
public class RetryAnalyzer implements IRetryAnalyzer {

    private int retryCount = 0;
    private static final int MAX_RETRY = 1; // retry once

    @Override
    public boolean retry(ITestResult result) {
        if (retryCount < MAX_RETRY) {
            retryCount++;
            System.out.println("↻ Retrying test: ["
                + result.getMethod().getMethodName()
                + "] — Attempt " + retryCount + " of " + MAX_RETRY);
            return true;
        }
        return false;
    }
}
