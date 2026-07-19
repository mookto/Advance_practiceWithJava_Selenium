import io.github.bonigarcia.wdm.WebDriverManager;
import org.openqa.selenium.*;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.openqa.selenium.interactions.Actions;
import org.openqa.selenium.support.ui.*;
import org.testng.Assert;
import org.testng.annotations.*;

import java.io.File;
import java.io.IOException;
import java.nio.file.*;
import java.time.Duration;
import java.util.*;
import java.util.NoSuchElementException;

class DriverManager {
//*** Comment code diye -- 4 topic and Authhandelling er code run hoi ********************
//    protected WebDriver driver;
//    protected WebDriverWait wait;
//    protected JavascriptExecutor js;
//
//    // ── Driver Setup ────────────────────────────────────────────────────
//    @BeforeMethod
//    public void setUp() {
//        WebDriverManager.chromedriver().setup();
//
//        ChromeOptions options = new ChromeOptions();
//        // headless: false → ভিজ্যুয়ালি দেখার জন্য (Playwright: headless:false)
//        // options.addArguments("--headless=new");  // headless চাইলে এটা uncomment করো
//        options.addArguments("--start-maximized");
//        options.addArguments("--disable-notifications");
//        options.addArguments("--disable-popup-blocking");
//
//        driver = new ChromeDriver(options);
//        driver.manage().timeouts().implicitlyWait(Duration.ofSeconds(10));
//        driver.manage().timeouts().pageLoadTimeout(Duration.ofSeconds(30));
//
//        wait = new WebDriverWait(driver, Duration.ofSeconds(15));
//        js   = (JavascriptExecutor) driver;
//    }
//
//    @AfterMethod
//    public void tearDown() {
//        if (driver != null) {
//            driver.quit();
//        }
//    }
//    // ── HELPER: highlightElement (Playwright এর মতো visual feedback) ──
//    protected void highlightElement(WebElement element, String color) throws InterruptedException {
//        String originalStyle = element.getAttribute("style");
//        js.executeScript(
//                "arguments[0].style.cssText += 'outline: 4px solid " + color + " !important;" +
//                        "box-shadow: 0 0 20px " + color + " !important;" +
//                        "transition: all 0.3s ease;';",
//                element
//        );
//        Thread.sleep(1500);
//        // original style restore
//        js.executeScript("arguments[0].style.cssText = arguments[1];", element,
//                originalStyle != null ? originalStyle : "");
//    }
//
//    protected void highlightElement(By locator, String color) throws InterruptedException {
//        try {
//            WebElement el = driver.findElement(locator);
//            highlightElement(el, color);
//        } catch (NoSuchElementException e) {
//            System.out.println("  ⚠️  Highlight skip: element not found → " + locator);
//        }
//    }
//
//    // ── HELPER: showStep (console + browser banner) ───────────────────
//    protected void showStep(int num, String msg) throws InterruptedException {
//        System.out.println("\n" + "─".repeat(55));
//        System.out.println("  STEP " + num + ": " + msg);
//        System.out.println("─".repeat(55));
//
//        js.executeScript(
//                "var old = document.getElementById('__step__');" +
//                        "if (old) old.remove();" +
//                        "var d = document.createElement('div');" +
//                        "d.id = '__step__';" +
//                        "d.style.cssText = 'position:fixed;top:0;left:0;right:0;z-index:99999;" +
//                        "background:#1a1a2e;color:#00d4aa;padding:10px 16px;" +
//                        "font:bold 14px monospace;border-bottom:3px solid #e94560;';" +
//                        "d.textContent = 'STEP " + num + ": " + msg + "';" +
//                        "document.body.prepend(d);"
//        );
//        Thread.sleep(1200);
//    }
//
//    // ── HELPER: showStatus (colored toast notification) ───────────────
//    protected void showStatus(String message, String type) throws InterruptedException {
//        Map<String, String> colors = new HashMap<>();
//        colors.put("info",    "#3498db");
//        colors.put("success", "#27ae60");
//        colors.put("warning", "#f39c12");
//        colors.put("error",   "#e74c3c");
//        colors.put("mock",    "#9b59b6");
//
//        String color = colors.getOrDefault(type, "#3498db");
//
//        js.executeScript(
//                "var old = document.getElementById('pw-status-bar');" +
//                        "if (old) old.remove();" +
//                        "var bar = document.createElement('div');" +
//                        "bar.id = 'pw-status-bar';" +
//                        "bar.style.cssText = 'position:fixed;top:10px;left:50%;transform:translateX(-50%);" +
//                        "background:" + color + ";color:white;padding:12px 24px;" +
//                        "border-radius:8px;font:bold 15px monospace;z-index:999999;" +
//                        "box-shadow:0 4px 20px rgba(0,0,0,0.3);max-width:90%;text-align:center;';" +
//                        "bar.textContent = '🎯 " + message + "';" +
//                        "document.body.appendChild(bar);"
//        );
//        Thread.sleep(1200);
//    }
//
//    // ── HELPER: screenshot save ────────────────────────────────────────
//    protected void saveScreenshot(String folderPath, String fileName) throws IOException {
//        Files.createDirectories(Paths.get(folderPath));
//        File screenshot = ((TakesScreenshot) driver).getScreenshotAs(OutputType.FILE);
//        Files.copy(screenshot.toPath(),
//                Paths.get(folderPath, fileName),
//                StandardCopyOption.REPLACE_EXISTING);
//        System.out.println("  📸 Screenshot saved: " + folderPath + "/" + fileName);
//    }
//
//    // ── HELPER: slowMo simulate (Playwright slowMo:700 এর equivalent) ─
//    protected void slowMo() throws InterruptedException {
//        Thread.sleep(700);
//    }
//    // ── HELPER: Safe localStorage clear ────────────────────────────────────
//    protected void safeClearLocalStorage() {
//        try {
//            // Check if we're on a page that supports localStorage
//            Boolean isLocalStorageAvailable = (Boolean) js.executeScript(
//                    "try { return !!window.localStorage; } catch(e) { return false; }"
//            );
//
//            if (isLocalStorageAvailable) {
//                js.executeScript("localStorage.clear();");
//                System.out.println("  ✅ localStorage cleared");
//            } else {
//                System.out.println("  ⚠️ localStorage not available on this page");
//            }
//        } catch (Exception e) {
//            System.out.println("  ⚠️ Could not clear localStorage: " + e.getMessage());
//        }
//    }
//
//    // ── HELPER: Safe localStorage getItem ──────────────────────────────────
//    protected String safeGetLocalStorageItem(String key) {
//        try {
//            Boolean isLocalStorageAvailable = (Boolean) js.executeScript(
//                    "try { return !!window.localStorage; } catch(e) { return false; }"
//            );
//
//            if (isLocalStorageAvailable) {
//                return (String) js.executeScript("return localStorage.getItem(arguments[0]);", key);
//            }
//        } catch (Exception e) {
//            System.out.println("  ⚠️ Could not get localStorage item: " + e.getMessage());
//        }
//        return null;
//    }
//
//    // ── HELPER: Safe localStorage setItem ──────────────────────────────────
//    protected void safeSetLocalStorageItem(String key, String value) {
//        try {
//            Boolean isLocalStorageAvailable = (Boolean) js.executeScript(
//                    "try { return !!window.localStorage; } catch(e) { return false; }"
//            );
//
//            if (isLocalStorageAvailable) {
//                js.executeScript("localStorage.setItem(arguments[0], arguments[1]);", key, value);
//            } else {
//                System.out.println("  ⚠️ localStorage not available, skipping setItem for: " + key);
//            }
//        } catch (Exception e) {
//            System.out.println("  ⚠️ Could not set localStorage item: " + e.getMessage());
//        }
//    }

    protected WebDriver driver;
    protected WebDriverWait wait;
    protected JavascriptExecutor js;

    // ── Driver Setup ────────────────────────────────────────────────────
    @BeforeMethod
    public void setUp() {
        WebDriverManager.chromedriver().setup();

        ChromeOptions options = new ChromeOptions();
        options.addArguments("--start-maximized");
        options.addArguments("--disable-notifications");
        options.addArguments("--disable-popup-blocking");

        driver = new ChromeDriver(options);
        driver.manage().timeouts().implicitlyWait(Duration.ofSeconds(10));
        driver.manage().timeouts().pageLoadTimeout(Duration.ofSeconds(30));

        wait = new WebDriverWait(driver, Duration.ofSeconds(15));
        js = (JavascriptExecutor) driver;
    }

    @AfterMethod
    public void tearDown() {
        if (driver != null) {
            driver.quit();
        }
    }

    // ── HELPER: highlightElement ──────────────────────────────────────
    protected void highlightElement(WebElement element, String color) throws InterruptedException {
        String originalStyle = element.getAttribute("style");
        js.executeScript(
                "arguments[0].style.cssText += 'outline: 4px solid " + color + " !important;" +
                        "box-shadow: 0 0 20px " + color + " !important;" +
                        "transition: all 0.3s ease;';",
                element
        );
        Thread.sleep(1500);
        js.executeScript("arguments[0].style.cssText = arguments[1];", element,
                originalStyle != null ? originalStyle : "");
    }

    protected void highlightElement(By locator, String color) throws InterruptedException {
        try {
            WebElement el = driver.findElement(locator);
            highlightElement(el, color);
        } catch (NoSuchElementException e) {
            System.out.println("  ⚠️ Highlight skip: element not found → " + locator);
        }
    }

    // ── HELPER: highlight with label (like Playwright version) ────────
    protected void highlightWithLabel(WebElement element, String color, String label) throws InterruptedException {
        // Highlight the element
        String originalStyle = element.getAttribute("style");
        js.executeScript(
                "arguments[0].style.cssText += 'outline: 4px solid " + color + " !important;" +
                        "box-shadow: 0 0 20px " + color + " !important;" +
                        "transition: all 0.3s ease;';",
                element
        );

        // Show floating label
        if (label != null && !label.isEmpty()) {
            js.executeScript(
                    "var old = document.getElementById('__pw_highlight__');" +
                            "if (old) old.remove();" +
                            "var div = document.createElement('div');" +
                            "div.id = '__pw_highlight__';" +
                            "div.innerText = arguments[0];" +
                            "div.style.position = 'fixed';" +
                            "div.style.top = '20px';" +
                            "div.style.right = '20px';" +
                            "div.style.zIndex = '999999';" +
                            "div.style.padding = '10px 18px';" +
                            "div.style.background = '" + color + "';" +
                            "div.style.color = 'white';" +
                            "div.style.fontSize = '18px';" +
                            "div.style.fontWeight = 'bold';" +
                            "div.style.borderRadius = '8px';" +
                            "div.style.boxShadow = '0 2px 10px rgba(0,0,0,0.3)';" +
                            "document.body.appendChild(div);",
                    label
            );
        }

        Thread.sleep(1200);
        // Restore original style
        js.executeScript("arguments[0].style.cssText = arguments[1];", element,
                originalStyle != null ? originalStyle : "");
    }

    // ── HELPER: showStep ──────────────────────────────────────────────
    protected void showStep(int num, String msg) throws InterruptedException {
        System.out.println("\n" + "─".repeat(55));
        System.out.println("  STEP " + num + ": " + msg);
        System.out.println("─".repeat(55));

        js.executeScript(
                "var old = document.getElementById('__step__');" +
                        "if (old) old.remove();" +
                        "var d = document.createElement('div');" +
                        "d.id = '__step__';" +
                        "d.style.cssText = 'position:fixed;top:0;left:0;right:0;z-index:99999;" +
                        "background:#1a1a2e;color:#00d4aa;padding:10px 16px;" +
                        "font:bold 14px monospace;border-bottom:3px solid #e94560;';" +
                        "d.textContent = 'STEP " + num + ": " + msg + "';" +
                        "document.body.prepend(d);"
        );
        Thread.sleep(1200);
    }

    // ── HELPER: showStatus ─────────────────────────────────────────────
    protected void showStatus(String message, String type) throws InterruptedException {
        Map<String, String> colors = new HashMap<>();
        colors.put("info", "#3498db");
        colors.put("success", "#27ae60");
        colors.put("warning", "#f39c12");
        colors.put("error", "#e74c3c");
        colors.put("mock", "#9b59b6");

        String color = colors.getOrDefault(type, "#3498db");

        js.executeScript(
                "var old = document.getElementById('pw-status-bar');" +
                        "if (old) old.remove();" +
                        "var bar = document.createElement('div');" +
                        "bar.id = 'pw-status-bar';" +
                        "bar.style.cssText = 'position:fixed;top:10px;left:50%;transform:translateX(-50%);" +
                        "background:" + color + ";color:white;padding:12px 24px;" +
                        "border-radius:8px;font:bold 15px monospace;z-index:999999;" +
                        "box-shadow:0 4px 20px rgba(0,0,0,0.3);max-width:90%;text-align:center;';" +
                        "bar.textContent = '🎯 " + message + "';" +
                        "document.body.appendChild(bar);"
        );
        Thread.sleep(1200);
    }

    // ── HELPER: scrollToBottom ──────────────────────────────────────────
    protected void scrollToBottom() {
        js.executeScript("window.scrollTo(0, document.body.scrollHeight);");
    }

    // ── HELPER: scrollIntoView ─────────────────────────────────────────
    protected void scrollIntoView(WebElement element) {
        js.executeScript("arguments[0].scrollIntoViewIfNeeded();", element);
    }

    // ── HELPER: screenshot save ────────────────────────────────────────
    protected void saveScreenshot(String folderPath, String fileName) throws IOException {
        Files.createDirectories(Paths.get(folderPath));
        File screenshot = ((TakesScreenshot) driver).getScreenshotAs(OutputType.FILE);
        Files.copy(screenshot.toPath(),
                Paths.get(folderPath, fileName),
                StandardCopyOption.REPLACE_EXISTING);
        System.out.println("  📸 Screenshot saved: " + folderPath + "/" + fileName);
    }

    // ── HELPER: wait for element to be visible ─────────────────────────
    protected WebElement waitForVisible(By locator) {
        return wait.until(ExpectedConditions.visibilityOfElementLocated(locator));
    }

    // ── HELPER: check if element exists ────────────────────────────────
    protected boolean elementExists(By locator) {
        try {
            driver.findElement(locator);
            return true;
        } catch (NoSuchElementException e) {
            return false;
        }
    }

    // ── HELPER: Safe localStorage methods ──────────────────────────────
    protected void safeClearLocalStorage() {
        try {
            Boolean isLocalStorageAvailable = (Boolean) js.executeScript(
                    "try { return !!window.localStorage; } catch(e) { return false; }"
            );
            if (isLocalStorageAvailable) {
                js.executeScript("localStorage.clear();");
                System.out.println("  ✅ localStorage cleared");
            } else {
                System.out.println("  ⚠️ localStorage not available on this page");
            }
        } catch (Exception e) {
            System.out.println("  ⚠️ Could not clear localStorage: " + e.getMessage());
        }
    }

    protected String safeGetLocalStorageItem(String key) {
        try {
            Boolean isLocalStorageAvailable = (Boolean) js.executeScript(
                    "try { return !!window.localStorage; } catch(e) { return false; }"
            );
            if (isLocalStorageAvailable) {
                return (String) js.executeScript("return localStorage.getItem(arguments[0]);", key);
            }
        } catch (Exception e) {
            System.out.println("  ⚠️ Could not get localStorage item: " + e.getMessage());
        }
        return null;
    }

    protected void safeSetLocalStorageItem(String key, String value) {
        try {
            Boolean isLocalStorageAvailable = (Boolean) js.executeScript(
                    "try { return !!window.localStorage; } catch(e) { return false; }"
            );
            if (isLocalStorageAvailable) {
                js.executeScript("localStorage.setItem(arguments[0], arguments[1]);", key, value);
            } else {
                System.out.println("  ⚠️ localStorage not available, skipping setItem for: " + key);
            }
        } catch (Exception e) {
            System.out.println("  ⚠️ Could not set localStorage item: " + e.getMessage());
        }
    }
}