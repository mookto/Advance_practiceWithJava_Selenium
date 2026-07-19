import org.testng.IRetryAnalyzer;
import org.testng.ITestResult;

// ════════════════════════════════════════════════════════════════════════
//  RetryAnalyzer — Flaky test retry করার জন্য
//  Topic3ParallelRetryTest এর 3B test এ use হচ্ছে
//  @Test(retryAnalyzer = RetryAnalyzer.class)
// ════════════════════════════════════════════════════════════════════════

public class RetryAnalyzer implements IRetryAnalyzer {

    // কতবার retry হবে
    private static final int MAX_RETRY = 2;

    // static করা হয়েছে যাতে test3B_FlakyTestWithRetry() থেকে read করা যায়
    public static int retryCount = 0;

    @Override
    public boolean retry(ITestResult result) {
        if (retryCount < MAX_RETRY) {
            retryCount++;
            System.out.println("\n  🔄 RETRYING: " + result.getName() +
                    " | Attempt: " + (retryCount + 1));
            return true; // retry করো
        }
        retryCount = 0; // reset for next test
        return false;   // আর retry করো না
    }
}