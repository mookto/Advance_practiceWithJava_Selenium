import org.openqa.selenium.By;
import org.openqa.selenium.Keys;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.testng.Assert;
import org.testng.annotations.Test;

public class Topic3ParallelRetryTest extends DriverManager {

    // ── TEST 3A-1: Parallel Homepage Load ─────────────────────────────
    @Test(description = "3A-1 VISUAL — Parallel: Homepage load")
    public void test3A1_ParallelHomepage() throws InterruptedException {
        System.out.println("\n 🔀 PARALLEL [3A-1]");
        driver.get("https://playwright.dev");
        showStep(1, "Thread 3A-1 চলছে...");
        highlightElement(By.cssSelector("nav"), "#3498db");
        Assert.assertTrue(driver.getTitle().contains("Playwright"));
        showStatus("✅ 3A-1 PASSED!", "success");
        Thread.sleep(1000);
    }

    // ── TEST 3A-2: Parallel Docs Page ─────────────────────────────────
    @Test(description = "3A-2 VISUAL — Parallel: Docs page")
    public void test3A2_ParallelDocs() throws InterruptedException {
        System.out.println("\n 🔀 PARALLEL [3A-2]");
        driver.get("https://playwright.dev/docs/intro");
        showStep(1, "Thread 3A-2 চলছে...");
        wait.until(ExpectedConditions.visibilityOfElementLocated(By.cssSelector("h1")));
        highlightElement(By.cssSelector("h1"), "#f39c12");
        showStatus("✅ 3A-2 PASSED!", "success");
        Thread.sleep(1000);
    }

    // ── TEST 3A-3: Parallel API Page ──────────────────────────────────
    @Test(description = "3A-3 VISUAL — Parallel: API page")
    public void test3A3_ParallelAPI() throws InterruptedException {
        System.out.println("\n 🔀 PARALLEL [3A-3]");
        driver.get("https://playwright.dev/docs/api/class-page");
        showStep(1, "Thread 3A-3 চলছে...");
        wait.until(ExpectedConditions.visibilityOfElementLocated(By.cssSelector("h1")));
        highlightElement(By.cssSelector("h1"), "#27ae60");
        showStatus("✅ 3A-3 PASSED!", "success");
        Thread.sleep(1000);
    }


    // ── TEST 3B: Flaky Test with Retry ─────────────────────────────────
    // Playwright: test.info().retry → TestNG: @Test(retryAnalyzer = RetryAnalyzer.class)
    @Test(description = "3B VISUAL — Flaky Test with Retry",
            retryAnalyzer = RetryAnalyzer.class)
    public void test3B_FlakyTestWithRetry() throws InterruptedException {
        int attemptNum = RetryAnalyzer.retryCount + 1;
        System.out.println("\n 🔄 TEST 3B: Attempt " + attemptNum);
        driver.get("https://playwright.dev");
        showStep(1, "Attempt #" + attemptNum);

        js.executeScript(
                "var d = document.createElement('div');" +
                        "d.style.cssText = 'position:fixed;bottom:10px;left:10px;z-index:99999;" +
                        "background:#9b59b6;color:white;padding:10px 16px;" +
                        "border-radius:8px;font:bold 13px monospace;';" +
                        "d.textContent = '🔄 Attempt: " + attemptNum + "';" +
                        "document.body.appendChild(d);"
        );

        highlightElement(By.cssSelector("nav"), "#9b59b6");
        wait.until(ExpectedConditions.visibilityOfElementLocated(By.cssSelector("nav")));
        showStatus("✅ 3B PASSED! Attempt " + attemptNum, "success");
        Thread.sleep(1000);
    }


    // ── TEST 3C: Test Info + Retry ─────────────────────────────────────
    @Test(description = "3C VISUAL — Test Info + Retry")
    public void test3C_TestInfo() throws InterruptedException {
        driver.get("https://playwright.dev");
        showStep(1, "Test info দেখাচ্ছি...");

        // Playwright: test.info().retry + workerIndex → TestNG: ITestResult / Thread ID
        long workerId = Thread.currentThread().getId();
        System.out.println(" 📊 Info: {worker: " + workerId + "}");

        js.executeScript(
                "var d = document.createElement('div');" +
                        "d.style.cssText = 'position:fixed;bottom:10px;right:10px;z-index:99999;" +
                        "background:#2c3e50;color:#ecf0f1;padding:12px 16px;" +
                        "border-radius:8px;font:13px monospace;line-height:1.6;';" +
                        "d.innerHTML = '<b style=\"color:#3498db;\">📊 Test Info</b><br>" +
                        "Worker Thread: " + workerId + "';" +
                        "document.body.appendChild(d);"
        );

        highlightElement(By.cssSelector("main"), "#3498db");
        showStatus("✅ 3C PASSED! Worker: " + workerId, "success");
        Thread.sleep(1000);
    }


    // ── TEST 3D: Parallel Users (3 users simultaneously) ──────────────
    // Playwright: test.describe.parallel → TestNG: parallel="methods" in testng.xml
    @Test(description = "3D-A VISUAL — User A: Browse Desktops")
    public void test3D_UserA_BrowseDesktops() throws InterruptedException {
        System.out.println("\n 👤 USER A...");
        driver.get("https://opencart.abstracta.us/");
        Thread.sleep(1000);

        showStep(1, "User A: Desktops browse...");
        wait.until(ExpectedConditions.visibilityOfElementLocated(
                By.cssSelector("#logo a, .navbar-header a")));
        highlightElement(By.id("logo"), "#e74c3c");

        wait.until(ExpectedConditions.elementToBeClickable(
                By.xpath("//a[contains(text(),'Desktops')]")
        )).click();

        wait.until(ExpectedConditions.visibilityOfElementLocated(By.id("content")));

        // ✅ FIX: "#content h2" নেই এই page এ
        //    highlightElement এর বদলে JS দিয়ে #content highlight করো
        //    এতে findElement call হবে না → NoSuchElementException আসবে না
        js.executeScript(
                "var el = document.getElementById('content');" +
                        "if(el){" +
                        "  el.style.cssText += 'outline:4px solid #27ae60!important;" +
                        "  box-shadow:0 0 20px #27ae60!important;';" +
                        "  setTimeout(function(){ el.style.outline=''; el.style.boxShadow=''; }, 1500);" +
                        "}"
        );
        Thread.sleep(1500);

        showStatus("✅ User A: Done!", "success");
        System.out.println(" ✅ User A DONE!");
    }


    @Test(description = "3D-B VISUAL — User B: Search laptop")
    public void test3D_UserB_SearchLaptop() throws InterruptedException {
        System.out.println("\n 👤 USER B...");
        driver.get("https://opencart.abstracta.us/");
        Thread.sleep(1000);

        showStep(1, "User B: Laptop search...");
        // Playwright: page.locator('input[name="search"]').first()
        WebElement searchInput = wait.until(
                ExpectedConditions.visibilityOfElementLocated(By.cssSelector("input[name='search']"))
        );
        highlightElement(By.cssSelector("input[name='search']"), "#f39c12");

        searchInput.sendKeys("MacBook");
        searchInput.sendKeys(Keys.ENTER);

        wait.until(ExpectedConditions.visibilityOfElementLocated(By.id("content")));

        showStatus("✅ User B: Done!", "success");
        System.out.println(" ✅ User B DONE!");
    }

    @Test(description = "3D-C VISUAL — User C: Visit homepage")
    public void test3D_UserC_VisitHomepage() throws InterruptedException {
        System.out.println("\n 👤 USER C...");
        driver.get("https://opencart.abstracta.us/");
        Thread.sleep(1000);

        showStep(1, "User C: Homepage দেখছে...");
        // Playwright: page.locator('header, #menu, .nav').first()
        WebElement topHeader = wait.until(
                ExpectedConditions.visibilityOfElementLocated(By.cssSelector("header, #menu, .nav"))
        );
        highlightElement(By.cssSelector("header"), "#9b59b6");

        showStatus("✅ User C: Done!", "success");
        System.out.println(" ✅ User C DONE!");
    }
}
 