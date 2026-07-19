import org.openqa.selenium.*;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.FluentWait;
import org.testng.annotations.Test;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.time.Duration;
import java.util.Arrays;
import java.util.List;

public class Topic2VisualRegressionTest extends DriverManager {

    // ── TEST 2A: Full Page Screenshot Compare ──────────────────────────
    @Test(description = "2A VISUAL — Full Page Screenshot Compare")
    public void test2A_FullPageScreenshot() throws InterruptedException, IOException {
        System.out.println("\n━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━");
        System.out.println("📸 TEST 2A: Full Page Screenshot");
        System.out.println("━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━");

        Files.createDirectories(Paths.get("./test-screenshots/2A"));

        driver.get("https://playwright.dev");
        // Playwright: waitForLoadState('networkidle') → Selenium: fluent wait
        new FluentWait<>(driver)
                .withTimeout(Duration.ofSeconds(30))
                .pollingEvery(Duration.ofMillis(500))
                .until(d -> ((JavascriptExecutor) d)
                        .executeScript("return document.readyState").equals("complete"));

        showStep(1, "Page loaded — screenshot নেবো");
        highlightElement(By.cssSelector("nav"), "#3498db");

        showStep(2, "Full page screenshot capture...");
        saveScreenshot("./test-screenshots/2A", "full-page.png");

        showStep(3, "Baseline compare হচ্ছে...");
        // NOTE: Selenium-এ built-in visual comparison নেই।
        // Option 1: Ashot library use করো (Maven: ru.yandex.qatools.ashot)
        // Option 2: Screenshot bytes compare করো (নিচে basic version)

        Path baselinePath = Paths.get("./test-screenshots/2A/baseline.png");
        if (!Files.exists(baselinePath)) {
            // First run: baseline তৈরি করো
            saveScreenshot("./test-screenshots/2A", "baseline.png");
            showStatus("📸 Baseline created! Run again to compare.", "info");
            System.out.println("  📸 Baseline created!");
        } else {
            // Compare করো
            byte[] current  = ((TakesScreenshot) driver).getScreenshotAs(OutputType.BYTES);
            byte[] baseline = Files.readAllBytes(baselinePath);
            boolean matches = Arrays.equals(current, baseline);
            System.out.println("  📊 " + (matches ? "✅ Matches!" : "⚠️ Differences found!"));
            showStatus(matches ? "✅ 2A PASSED!" : "⚠️ Differences detected!", matches ? "success" : "warning");
        }

        Thread.sleep(2000);
        System.out.println("\n  ✅ 2A COMPLETE!\n");
    }


    // ── TEST 2B: Component Level Screenshot ───────────────────────────
    @Test(description = "2B VISUAL — Component Level Screenshot")
    public void test2B_ComponentScreenshot() throws InterruptedException, IOException {
        System.out.println("\n━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━");
        System.out.println("🔍 TEST 2B: Component Screenshot");
        System.out.println("━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━");

        Files.createDirectories(Paths.get("./test-screenshots/2B"));

        driver.get("https://playwright.dev");
        new FluentWait<>(driver)
                .withTimeout(Duration.ofSeconds(30))
                .pollingEvery(Duration.ofMillis(500))
                .until(d -> ((JavascriptExecutor) d)
                        .executeScript("return document.readyState").equals("complete"));

        showStep(1, "Navbar capture করছি...");
        // Playwright: page.locator('nav').first() → Selenium: findElements().get(0)
        List<WebElement> navbars = wait.until(
                ExpectedConditions.visibilityOfAllElementsLocatedBy(By.cssSelector("nav"))
        );
        WebElement navbar = navbars.get(0);
        highlightElement(navbar, "#e74c3c");

        // Component screenshot — Ashot library ছাড়া করতে হলে JS scroll + full screenshot
        // এখানে simple approach: element screenshot (Selenium 4 supports it)
        File navbarShot = navbar.getScreenshotAs(OutputType.FILE);
        Files.copy(navbarShot.toPath(),
                Paths.get("./test-screenshots/2B/navbar.png"),
                StandardCopyOption.REPLACE_EXISTING);
        System.out.println("  📸 navbar.png saved");

        showStep(2, "Main section capture করছি...");
        List<WebElement> mains = driver.findElements(By.cssSelector("main"));
        if (!mains.isEmpty()) {
            highlightElement(mains.get(0), "#27ae60");
            File mainShot = mains.get(0).getScreenshotAs(OutputType.FILE);
            Files.copy(mainShot.toPath(),
                    Paths.get("./test-screenshots/2B/main.png"),
                    StandardCopyOption.REPLACE_EXISTING);
            System.out.println("  📸 main.png saved");
        }

        showStatus("✅ 2B DONE! Screenshots saved!", "success");
        Thread.sleep(2000);
        System.out.println("\n  ✅ 2B COMPLETE!\n");
    }


    // ── TEST 2C: Before/After Screenshot Compare ───────────────────────
    @Test(description = "2C VISUAL — Before/After Screenshot Compare")
    public void test2C_BeforeAfterCompare() throws InterruptedException, IOException {
        System.out.println("\n━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━");
        System.out.println("🔄 TEST 2C: Before/After Compare");
        System.out.println("━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━");

        Files.createDirectories(Paths.get("./screenshots"));

        driver.get("https://playwright.dev");
        new FluentWait<>(driver)
                .withTimeout(Duration.ofSeconds(30))
                .pollingEvery(Duration.ofMillis(500))
                .until(d -> ((JavascriptExecutor) d)
                        .executeScript("return document.readyState").equals("complete"));

        showStep(1, "BEFORE screenshot...");
        highlightElement(By.cssSelector("nav"), "#3498db");
        byte[] before = ((TakesScreenshot) driver).getScreenshotAs(OutputType.BYTES);
        Files.write(Paths.get("./screenshots/before.png"), before);
        System.out.println("  📸 before.png saved");

        showStep(2, "Scroll করছি...");
        js.executeScript("window.scrollTo(0, 300)");
        Thread.sleep(500);

        showStep(3, "AFTER screenshot...");
        highlightElement(By.cssSelector("main"), "#e74c3c");
        byte[] after = ((TakesScreenshot) driver).getScreenshotAs(OutputType.BYTES);
        Files.write(Paths.get("./screenshots/after.png"), after);
        System.out.println("  📸 after.png saved");

        showStep(4, "Compare করছি...");
        boolean isDifferent = !Arrays.equals(before, after);
        System.out.println("  📊 " + (isDifferent ? "DIFFERENT ⚠️" : "SAME ✅"));

        showStatus("📸 2C DONE! " + (isDifferent ? "Different" : "Same"),
                isDifferent ? "warning" : "success");
        Thread.sleep(2000);
        System.out.println("\n  ✅ 2C COMPLETE!\n");
    }


    // ── TEST 2D: Responsive Screenshots (3 viewports) ─────────────────
    @Test(description = "2D VISUAL — Responsive Screenshots (3 viewports)")
    public void test2D_ResponsiveScreenshots() throws InterruptedException, IOException {
        System.out.println("\n━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━");
        System.out.println("📱 TEST 2D: Responsive Viewports");
        System.out.println("━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━");

        Files.createDirectories(Paths.get("./test-screenshots/2D"));

        // Playwright: viewports array → Selenium: Dimension array
        int[][] viewports = {
                {375,  812},   // mobile
                {768,  1024},  // tablet
                {1440, 900}    // desktop
        };
        String[] names  = {"mobile", "tablet", "desktop"};
        String[] colors = {"#e74c3c", "#f39c12", "#27ae60"};

        for (int i = 0; i < viewports.length; i++) {
            int width  = viewports[i][0];
            int height = viewports[i][1];
            String name  = names[i];
            String color = colors[i];

            // Playwright: page.setViewportSize() → Selenium: manage().window().setSize()
            driver.manage().window().setSize(new Dimension(width, height));
            driver.get("https://playwright.dev");
            new FluentWait<>(driver)
                    .withTimeout(Duration.ofSeconds(30))
                    .pollingEvery(Duration.ofMillis(500))
                    .until(d -> ((JavascriptExecutor) d)
                            .executeScript("return document.readyState").equals("complete"));
            Thread.sleep(500);

            showStep(i + 1, name.toUpperCase() + ": " + width + "x" + height);

            // Viewport badge inject
            js.executeScript(
                    "var old = document.getElementById('__vp__');" +
                            "if (old) old.remove();" +
                            "var d = document.createElement('div');" +
                            "d.id = '__vp__';" +
                            "d.style.cssText = 'position:fixed;bottom:10px;right:10px;z-index:99999;" +
                            "background:" + color + ";color:white;padding:8px 14px;" +
                            "border-radius:8px;font:bold 13px monospace;';" +
                            "d.textContent = '📱 " + name + " (" + width + "x" + height + ")';" +
                            "document.body.appendChild(d);"
            );

            highlightElement(By.cssSelector("nav"), color);
            saveScreenshot("./test-screenshots/2D", name + ".png");
            System.out.println("  ✅ " + name + ": screenshot done!");

            showStatus("✅ " + name + " done!", "success");
            Thread.sleep(800);
        }

        System.out.println("\n  ✅ 2D COMPLETE!\n");
    }
}
 