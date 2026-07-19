import com.google.gson.Gson;
import io.github.bonigarcia.wdm.WebDriverManager;
import org.openqa.selenium.*;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.openqa.selenium.support.ui.*;
import org.testng.Assert;
import org.testng.annotations.*;

import java.io.*;
import java.nio.file.*;
import java.time.Duration;
import java.util.*;

public class AuthHandling_VisualDebug extends DriverManager {

    private static final String AUTH_FILE_PATH = "./tests/auth/saucedemo-auth.json";

    // ── HELPER: Check if auth file exists ─────────────────────────────
    private boolean authFileExists() {
        return Files.exists(Paths.get(AUTH_FILE_PATH));
    }

    // ── HELPER: Create auth directory ──────────────────────────────────
    private void createAuthDirectory() {
        try {
            Files.createDirectories(Paths.get("./tests/auth"));
        } catch (IOException e) {
            System.out.println("  ⚠️ Could not create auth directory: " + e.getMessage());
        }
    }

    // ─────────────────────────────────────────────────────────────────────
    //  2A. COOKIE INJECTION
    // ─────────────────────────────────────────────────────────────────────

    @Test
    public void test2A_CookieInjection() throws InterruptedException {
        System.out.println("\n━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━");
        System.out.println("🍪 TEST 2A: Cookie Injection");
        System.out.println("   Site: www.saucedemo.com");
        System.out.println("━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━");

        // STEP 1: Go to site
        driver.get("https://www.saucedemo.com");
        showStep(1, "Site-এ এলাম — এখনো login করিনি");
        System.out.println("  → Currently on login page (not logged in)");

        // STEP 2: Inject cookie manually
        showStep(2, "Cookie manually inject করছি...");
        System.out.println("  → Injecting session-username cookie...");

        // Add cookie
        Cookie cookie = new Cookie.Builder("session-username", "standard_user")
                .domain("www.saucedemo.com")
                .path("/")
                .isSecure(false)
                .isHttpOnly(false)
                .build();
        driver.manage().addCookie(cookie);

        // Verify cookies
        Set<Cookie> cookies = driver.manage().getCookies();
        System.out.println("\n  📦 Injected cookies:");
        for (Cookie c : cookies) {
            System.out.println("     " + c.getName() + " = " + c.getValue() + " (domain: " + c.getDomain() + ")");
        }

        // STEP 3: Go to protected page
        showStep(3, "Protected page-এ যাচ্ছি — login ছাড়াই!");
        driver.get("https://www.saucedemo.com/inventory.html");
        Thread.sleep(2000);

        String currentUrl = driver.getCurrentUrl();
        System.out.println("\n  Current URL: " + currentUrl);

        if (currentUrl.contains("inventory")) {
            // ✅ Cookie worked!
            showStep(4, "Inventory page দেখাচ্ছে! Cookie কাজ করেছে!");
            WebElement inventoryList = wait.until(
                    ExpectedConditions.presenceOfElementLocated(By.className("inventory_list"))
            );
            highlightElement(inventoryList, "#27ae60");
            showStatus("✅ Cookie injection worked!", "success");
            System.out.println("  ✅ Cookie auth successful! No login needed!");
        } else {
            // Cookie failed → manual login
            showStep(4, "Cookie কাজ করেনি → manual login করছি...");
            System.out.println("  ⚠️ Cookie auth failed. Doing manual login...");

            WebElement usernameField = wait.until(
                    ExpectedConditions.presenceOfElementLocated(By.id("user-name"))
            );
            usernameField.sendKeys("standard_user");

            WebElement passwordField = driver.findElement(By.id("password"));
            passwordField.sendKeys("secret_sauce");

            highlightElement(By.id("login-button"), "#3498db");
            driver.findElement(By.id("login-button")).click();
            Thread.sleep(2000);

            // Now check cookies after manual login
            Set<Cookie> newCookies = driver.manage().getCookies();
            System.out.println("\n  📦 Cookies after manual login:");
            for (Cookie c : newCookies) {
                System.out.println("     " + c.getName() + " = " + c.getValue());
            }
        }

        // Final verify
        WebElement inventoryList = wait.until(
                ExpectedConditions.visibilityOfElementLocated(By.className("inventory_list"))
        );
        highlightElement(inventoryList, "#27ae60");
        showStatus("✅ 2A DONE! Inventory accessible!", "success");
        Assert.assertTrue(driver.getCurrentUrl().contains("inventory"));

        System.out.println("\n  ✅ 2A COMPLETE!\n");
    }

    // ─────────────────────────────────────────────────────────────────────
    //  2B. LOCALSTORAGE INJECTION
    // ─────────────────────────────────────────────────────────────────────

    @Test
    public void test2B_LocalStorageInjection() throws InterruptedException, IOException {
        System.out.println("\n━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━");
        System.out.println("🗄️  TEST 2B: localStorage Injection");
        System.out.println("━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━");

        // Create HTML content
        String htmlContent = "<!DOCTYPE html><html><head><style>\n" +
                "body{font-family:sans-serif;margin:0;background:#ecf0f1;}\n" +
                ".card{background:white;max-width:420px;margin:60px auto;\n" +
                "padding:32px;border-radius:12px;\n" +
                "box-shadow:0 4px 20px rgba(0,0,0,0.1);text-align:center;}\n" +
                ".login-view  h2{color:#e74c3c;}\n" +
                ".dash-view   h2{color:#27ae60;}\n" +
                ".dash-view{display:none;}\n" +
                ".info-row{display:flex;justify-content:space-between;\n" +
                "padding:8px 12px;background:#f8f9fa;\n" +
                "border-radius:6px;margin:8px 0;font-size:14px;}\n" +
                ".label{color:#666;} .value{font-weight:bold;color:#2c3e50;}\n" +
                ".token-box{background:#2c3e50;color:#00ff88;padding:12px;\n" +
                "border-radius:6px;font-family:monospace;\n" +
                "font-size:11px;word-break:break-all;margin-top:12px;text-align:left;}\n" +
                ".badge{display:inline-block;background:#27ae60;color:white;\n" +
                "padding:3px 10px;border-radius:20px;font-size:12px;margin-left:6px;}\n" +
                "</style></head><body>\n" +
                "<div class='card'>\n" +
                "<div class='login-view' id='loginView'>\n" +
                "<div style='font-size:48px'>🔒</div>\n" +
                "<h2>Please Login First</h2>\n" +
                "<p style='color:#666'>No auth token found in localStorage</p>\n" +
                "</div>\n" +
                "<div class='dash-view' id='dashView'>\n" +
                "<div style='font-size:48px'>🎉</div>\n" +
                "<h2>Welcome to Dashboard!</h2>\n" +
                "<div class='info-row'>\n" +
                "<span class='label'>User ID</span>\n" +
                "<span class='value' id='userId'>-</span>\n" +
                "</div>\n" +
                "<div class='info-row'>\n" +
                "<span class='label'>Role</span>\n" +
                "<span class='value' id='userRole'>-</span>\n" +
                "</div>\n" +
                "<div class='info-row'>\n" +
                "<span class='label'>Status</span>\n" +
                "<span class='value'>Logged In <span class='badge'>✓</span></span>\n" +
                "</div>\n" +
                "<div class='token-box' id='tokenBox'>Token: loading...</div>\n" +
                "</div>\n" +
                "</div>\n" +
                "<script>\n" +
                "function checkAuth() {\n" +
                "var token = localStorage.getItem('authToken');\n" +
                "var uid   = localStorage.getItem('userId');\n" +
                "var role  = localStorage.getItem('userRole');\n" +
                "if (token && uid) {\n" +
                "document.getElementById('loginView').style.display = 'none';\n" +
                "document.getElementById('dashView').style.display  = 'block';\n" +
                "document.getElementById('userId').textContent   = uid;\n" +
                "document.getElementById('userRole').textContent = role || 'user';\n" +
                "document.getElementById('tokenBox').textContent =\n" +
                "'Token: ' + token.substring(0,40) + '...';\n" +
                "}\n" +
                "}\n" +
                "checkAuth();\n" +
                "</script>\n" +
                "</body></html>";

        // Save to temp file
        String tempFilePath = System.getProperty("java.io.tmpdir") + "/jwtapp.html";
        Files.write(Paths.get(tempFilePath), htmlContent.getBytes());

        try {
            // STEP 1: No token
            driver.get("file://" + tempFilePath);
            showStep(1, "Token নেই — Login page দেখাচ্ছে");
            WebElement loginView = wait.until(
                    ExpectedConditions.presenceOfElementLocated(By.xpath("//h2[contains(text(),'Please Login First')]"))
            );
            highlightElement(loginView, "#e74c3c");
            System.out.println("  ❌ No token → Login page shown");

            // STEP 2: Inject token
            showStep(2, "localStorage-এ JWT token inject করছি...");

            safeSetLocalStorageItem("authToken", "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.payload.signature");
            safeSetLocalStorageItem("userId", "12345");
            safeSetLocalStorageItem("userRole", "admin");
            safeSetLocalStorageItem("isLoggedIn", "true");

            // Verify localStorage
            String token = safeGetLocalStorageItem("authToken");
            String userId = safeGetLocalStorageItem("userId");
            String role = safeGetLocalStorageItem("userRole");

            System.out.println("\n  📦 localStorage contents:");
            System.out.println("     authToken = " + (token != null ? token.substring(0, Math.min(30, token.length())) + "..." : "null"));
            System.out.println("     userId    = " + userId);
            System.out.println("     userRole  = " + role);

            // STEP 3: Reload page
            showStep(3, "Page reload করছি — token এখন আছে!");
            driver.navigate().refresh();
            Thread.sleep(2000);

            // STEP 4: Check Dashboard
            WebElement dashboard = wait.until(
                    ExpectedConditions.presenceOfElementLocated(By.xpath("//h2[contains(text(),'Welcome to Dashboard')]"))
            );
            showStep(4, "Dashboard দেখাচ্ছে! Token কাজ করেছে!");
            highlightElement(dashboard, "#27ae60");
            showStatus("✅ 2B DONE! Dashboard accessible!", "success");

            System.out.println("\n  ✅ 2B COMPLETE! JWT injection worked!\n");

        } finally {
            // Clean up temp file
            Files.deleteIfExists(Paths.get(tempFilePath));
        }
    }

    // ─────────────────────────────────────────────────────────────────────
    //  2C. PERSISTENT CONTEXT (storageState)
    // ─────────────────────────────────────────────────────────────────────

    @Test
    public void test2C_Step1_LoginAndSaveAuth() throws InterruptedException {
        System.out.println("\n━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━");
        System.out.println("💾 TEST 2C-STEP1: Login & Save");
        System.out.println("   Site: www.saucedemo.com");
        System.out.println("━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━");

        createAuthDirectory();

        // STEP 1: Go to login page
        driver.get("https://www.saucedemo.com");
        showStep(1, "Login page-এ এলাম");
        highlightElement(By.id("user-name"), "#3498db");

        // STEP 2: Fill credentials
        showStep(2, "Username + Password fill করছি...");
        System.out.println("  → Username: standard_user");
        System.out.println("  → Password: secret_sauce");

        WebElement usernameField = wait.until(
                ExpectedConditions.presenceOfElementLocated(By.id("user-name"))
        );
        usernameField.sendKeys("standard_user");
        highlightElement(usernameField, "#27ae60");

        WebElement passwordField = driver.findElement(By.id("password"));
        passwordField.sendKeys("secret_sauce");
        highlightElement(passwordField, "#27ae60");

        // STEP 3: Click login
        showStep(3, "Login button click করছি...");
        highlightElement(By.id("login-button"), "#f39c12");
        driver.findElement(By.id("login-button")).click();

        // STEP 4: Verify login success
        wait.until(ExpectedConditions.urlContains("inventory.html"));
        showStep(4, "Login সফল! Inventory page দেখাচ্ছে!");
        WebElement inventoryList = wait.until(
                ExpectedConditions.presenceOfElementLocated(By.className("inventory_list"))
        );
        highlightElement(inventoryList, "#27ae60");
        System.out.println("  ✅ Login successful!");

        // STEP 5: Save auth state
        showStep(5, "Auth state JSON-এ save করছি...");

        // Get all cookies
        Set<Cookie> cookies = driver.manage().getCookies();

        // Get localStorage data
        String localStorage = (String) js.executeScript(
                "var items = {};" +
                        "for (var i = 0; i < localStorage.length; i++) {" +
                        "  var key = localStorage.key(i);" +
                        "  items[key] = localStorage.getItem(key);" +
                        "}" +
                        "return JSON.stringify(items);"
        );

        // Save to file
        Map<String, Object> authState = new HashMap<>();
        List<Map<String, Object>> cookieList = new ArrayList<>();

        for (Cookie cookie : cookies) {
            Map<String, Object> cookieMap = new HashMap<>();
            cookieMap.put("name", cookie.getName());
            cookieMap.put("value", cookie.getValue());
            cookieMap.put("domain", cookie.getDomain());
            cookieMap.put("path", cookie.getPath());
            cookieMap.put("secure", cookie.isSecure());
            cookieMap.put("httpOnly", cookie.isHttpOnly());
            cookieMap.put("sameSite", cookie.getSameSite() != null ? cookie.getSameSite() : "Lax");
            cookieList.add(cookieMap);
        }

        authState.put("cookies", cookieList);
        authState.put("localStorage", localStorage);

        try {
            String json = new Gson().toJson(authState);
            Files.write(Paths.get(AUTH_FILE_PATH), json.getBytes());
            showStatus("✅ Auth state saved!", "success");
            System.out.println("  ✅ Saved to: " + AUTH_FILE_PATH);
            System.out.println("  → এই file-এ cookies + localStorage সব আছে");
        } catch (IOException e) {
            System.out.println("  ❌ Failed to save auth state: " + e.getMessage());
        }

        System.out.println("\n  ✅ 2C-STEP1 COMPLETE! Run STEP2 now.\n");
    }

    @Test(dependsOnMethods = {"test2C_Step1_LoginAndSaveAuth"})
    public void test2C_Step2_ReuseAuthState() throws InterruptedException {
        System.out.println("\n━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━");
        System.out.println("🔓 TEST 2C-STEP2: Reuse State");
        System.out.println("   Login ছাড়াই access করবো!");
        System.out.println("━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━");

        if (!authFileExists()) {
            System.out.println("  ⚠️ Auth file not found! Run STEP1 first.");
            Assert.fail("Auth file not found. Please run test2C_Step1_LoginAndSaveAuth first.");
            return;
        }

        // STEP 1: First navigate to the domain to set cookies
        System.out.println("\n  → Navigating to domain to set cookies...");
        driver.get("https://www.saucedemo.com");
        Thread.sleep(1000);

        // Clear existing state
        driver.manage().deleteAllCookies();
        safeClearLocalStorage();

        // STEP 2: Load saved state
        System.out.println("\n  → Loading saved auth state...");
        try {
            String json = new String(Files.readAllBytes(Paths.get(AUTH_FILE_PATH)));
            Map<String, Object> authState = new com.google.gson.Gson().fromJson(json, Map.class);

            // Add cookies - NOW we're on the correct domain
            List<Map<String, Object>> cookies = (List<Map<String, Object>>) authState.get("cookies");
            for (Map<String, Object> cookieMap : cookies) {
                String domain = (String) cookieMap.get("domain");
                // For saucedemo.com, we might need to handle the domain properly
                if (domain != null && domain.startsWith(".")) {
                    domain = domain.substring(1); // Remove leading dot if present
                }

                Cookie cookie = new Cookie.Builder(
                        (String) cookieMap.get("name"),
                        (String) cookieMap.get("value")
                )
                        .domain(domain)
                        .path((String) cookieMap.get("path"))
                        .isSecure((Boolean) cookieMap.get("secure"))
                        .isHttpOnly((Boolean) cookieMap.get("httpOnly"))
                        .build();

                try {
                    driver.manage().addCookie(cookie);
                    System.out.println("  ✅ Added cookie: " + cookie.getName() + "=" + cookie.getValue());
                } catch (Exception e) {
                    System.out.println("  ⚠️ Could not add cookie " + cookie.getName() + ": " + e.getMessage());
                }
            }

            // Add localStorage
            String localStorage = (String) authState.get("localStorage");
            if (localStorage != null && !localStorage.isEmpty()) {
                Map<String, String> localStorageItems = new com.google.gson.Gson().fromJson(localStorage, Map.class);
                for (Map.Entry<String, String> entry : localStorageItems.entrySet()) {
                    safeSetLocalStorageItem(entry.getKey(), entry.getValue());
                }
            }

        } catch (IOException e) {
            System.out.println("  ❌ Failed to load auth state: " + e.getMessage());
            Assert.fail("Failed to load auth state");
        }

        // STEP 3: Go directly to inventory (now with cookies set)
        showStep(1, "Saved state দিয়ে context তৈরি — Login page-এ না গিয়েই...");
        driver.get("https://www.saucedemo.com/inventory.html");
        Thread.sleep(2000);

        // STEP 4: Verify
        try {
            WebElement inventoryList = wait.until(
                    ExpectedConditions.visibilityOfElementLocated(By.className("inventory_list"))
            );
            showStep(2, "Inventory page! Login ছাড়াই ঢুকে গেলাম!");
            highlightElement(inventoryList, "#27ae60");
            showStatus("✅ No login needed!", "success");

            System.out.println("  ✅ URL: " + driver.getCurrentUrl());
            System.out.println("  ✅ Directly on inventory — no login redirect!");

            // STEP 5: Go to cart page
            showStep(3, "Cart page-এও যাচ্ছি — still logged in!");
            driver.get("https://www.saucedemo.com/cart.html");
            Thread.sleep(2000);

            WebElement cartList = wait.until(
                    ExpectedConditions.visibilityOfElementLocated(By.className("cart_list"))
            );
            highlightElement(cartList, "#9b59b6");
            System.out.println("  ✅ Cart page accessible too!");

        } catch (TimeoutException e) {
            System.out.println("  ❌ Failed to access inventory page. Auth state might be invalid.");
            System.out.println("  Current URL: " + driver.getCurrentUrl());
            // Take screenshot for debugging
            try {
                saveScreenshot("./tests/screenshots", "auth_failure.png");
            } catch (IOException ioException) {
                System.out.println("  ⚠️ Could not save screenshot: " + ioException.getMessage());
            }
            Assert.fail("Could not access inventory page with saved auth state");
        }

        System.out.println("\n  ✅ 2C-STEP2 COMPLETE!\n");
    }

    // ─────────────────────────────────────────────────────────────────────
    //  2D. MULTIPLE USER ROLES
    // ─────────────────────────────────────────────────────────────────────

    @Test
    public void test2D_MultipleRoles() throws InterruptedException {
        System.out.println("\n━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━");
        System.out.println("👥 TEST 2D: Multiple User Roles");
        System.out.println("   standard_user vs locked_out_user");
        System.out.println("━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━");

        // ── USER 1: Standard User ──────────────────────────────────────
        System.out.println("\n  ── Testing Standard User ──");

        driver.manage().deleteAllCookies();
        safeClearLocalStorage();
        driver.get("https://www.saucedemo.com");
        Thread.sleep(1000);

        showStep(1, "Standard User — login করছি...");

        WebElement usernameField = wait.until(
                ExpectedConditions.presenceOfElementLocated(By.id("user-name"))
        );
        usernameField.sendKeys("standard_user");

        WebElement passwordField = driver.findElement(By.id("password"));
        passwordField.sendKeys("secret_sauce");

        highlightElement(By.id("login-button"), "#27ae60");
        driver.findElement(By.id("login-button")).click();

        wait.until(ExpectedConditions.urlContains("inventory.html"));
        showStep(2, "Standard User — Access GRANTED! ✅");

        WebElement inventoryList = wait.until(
                ExpectedConditions.presenceOfElementLocated(By.className("inventory_list"))
        );
        highlightElement(inventoryList, "#27ae60");
        showStatus("✅ Standard User: Access granted!", "success");
        System.out.println("  ✅ Standard user: CAN access inventory");

        // Logout - Go back to login page
        driver.get("https://www.saucedemo.com/");
        Thread.sleep(1000);
        driver.manage().deleteAllCookies();
        safeClearLocalStorage();
        driver.navigate().refresh();
        Thread.sleep(2000);

        // ── USER 2: Locked Out User ────────────────────────────────────
        System.out.println("\n  ── Testing Locked Out User ──");

        driver.get("https://www.saucedemo.com");
        Thread.sleep(1000);
        showStep(1, "Locked Out User — login চেষ্টা করছি...");

        usernameField = wait.until(
                ExpectedConditions.presenceOfElementLocated(By.id("user-name"))
        );
        usernameField.sendKeys("locked_out_user");

        passwordField = driver.findElement(By.id("password"));
        passwordField.sendKeys("secret_sauce");

        highlightElement(By.id("login-button"), "#e74c3c");
        driver.findElement(By.id("login-button")).click();
        Thread.sleep(1000);

        // ── Find error message with multiple selectors ─────────────────
        WebElement errorElement = null;
        String errorText = "";

        // Try different selectors for the error message
        By[] errorSelectors = {
                By.cssSelector("[data-test='error']"),           // Modern selector
                By.cssSelector(".error-message-container.error"), // Alternative
                By.cssSelector("h3[data-test='error']"),         // H3 with data-test
                By.xpath("//div[@class='error-message-container']//h3"), // XPath
                By.xpath("//h3[contains(text(), 'Epic sadface')]"), // Text-based
                By.cssSelector(".error-button")                  // Error button
        };

        for (By selector : errorSelectors) {
            try {
                errorElement = wait.until(
                        ExpectedConditions.visibilityOfElementLocated(selector)
                );
                errorText = errorElement.getText();
                System.out.println("  ✅ Found error with selector: " + selector);
                break;
            } catch (TimeoutException e) {
                // Try next selector
                continue;
            }
        }

        // If still not found, try with findElements (non-waiting)
        if (errorElement == null) {
            System.out.println("  ⚠️ Trying fallback: findElements...");
            List<WebElement> errorElements = driver.findElements(
                    By.cssSelector("[data-test='error'], .error-message-container, h3[data-test='error']")
            );
            if (!errorElements.isEmpty()) {
                errorElement = errorElements.get(0);
                errorText = errorElement.getText();
                System.out.println("  ✅ Found error with fallback");
            }
        }

        // Verify error is displayed
        if (errorElement != null) {
            showStep(2, "Locked User — Access DENIED! ❌");
            highlightElement(errorElement, "#e74c3c");
            showStatus("❌ Locked user blocked!", "error");
            System.out.println("  ❌ Locked user blocked: " + errorText.trim());
            Assert.assertTrue(errorText.contains("locked out"),
                    "Error message should indicate user is locked out");
        } else {
            // Take screenshot for debugging
            try {
                saveScreenshot("./tests/screenshots", "locked_user_no_error.png");
            } catch (IOException e) {
                System.out.println("  ⚠️ Could not save screenshot: " + e.getMessage());
            }
            Assert.fail("Error message not displayed for locked_out_user");
        }

        System.out.println("\n  ✅ 2D COMPLETE! Role-based access verified!\n");
    }
}