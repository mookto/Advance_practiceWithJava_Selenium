import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpServer;
import io.github.bonigarcia.wdm.WebDriverManager;
import org.openqa.selenium.*;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;
import org.testng.Assert;
import org.testng.annotations.Test;

import java.io.IOException;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

public class Topic4MultiTabContextTest extends DriverManager {

    // ── TEST 4A: New Tab Handling ──────────────────────────────────────
    @Test(description = "4A VISUAL — New Tab Handling")
    public void test4A_NewTabHandling() throws InterruptedException {
        System.out.println("\n━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━");
        System.out.println("🗂️  TEST 4A: New Tab");
        System.out.println("   Site: the-internet.herokuapp.com");
        System.out.println("━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━");

        driver.get("https://the-internet.herokuapp.com/windows");
        Thread.sleep(1000);

        showStep(1, "Link খুঁজছি...");
        // Playwright: page.locator('a', { hasText: 'Click Here' }).first()
        WebElement link = wait.until(
                ExpectedConditions.elementToBeClickable(
                        By.xpath("//a[contains(text(),'Click Here')]")
                )
        );
        Assert.assertTrue(link.isDisplayed());
        // Playwright: highlightElement(page, '.example a', '#3498db')
        highlightElement(By.cssSelector(".example a"), "#3498db");
        System.out.println("  🔵 Link highlighted!");

        // Get all current window handles before click
        String originalWindow = driver.getWindowHandle();
        Set<String> windowsBefore = driver.getWindowHandles();

        showStep(2, "Click → নতুন tab খুলবে!");
        link.click();

        // Playwright: context.waitForEvent('page') → Selenium: wait for new handle
        wait.until(d -> d.getWindowHandles().size() > windowsBefore.size());

        // নতুন tab handle খুঁজো
        String newTabHandle = null;
        for (String handle : driver.getWindowHandles()) {
            if (!windowsBefore.contains(handle)) {
                newTabHandle = handle;
                break;
            }
        }

        driver.switchTo().window(newTabHandle);
        wait.until(d -> ((JavascriptExecutor) d)
                .executeScript("return document.readyState").equals("complete"));
        System.out.println("  ✅ New tab URL: " + driver.getCurrentUrl());

        showStep(3, "নতুন tab-এ আছি!");
        // নতুন tab-এ banner inject
        String currentUrl = driver.getCurrentUrl();
        js.executeScript(
                "var d = document.createElement('div');" +
                        "d.style.cssText = 'position:fixed;top:50px;left:50%;transform:translateX(-50%);" +
                        "z-index:99999;background:#27ae60;color:white;padding:12px 20px;" +
                        "border-radius:8px;font:bold 14px monospace;';" +
                        "d.textContent = '🆕 NEW TAB: " + currentUrl + "';" +
                        "document.body.appendChild(d);"
        );
        highlightElement(By.cssSelector("h3"), "#27ae60");
        showStatus("✅ New tab verified!", "success");
        Thread.sleep(1500);

        showStep(4, "Tab বন্ধ → original-এ ফিরছি...");
        driver.close();
        driver.switchTo().window(originalWindow);
        // Playwright: page.bringToFront() → Selenium: switchTo().window()
        highlightElement(By.cssSelector("h3"), "#f39c12");
        showStatus("✅ 4A DONE! Tab opened + closed!", "success");
        Thread.sleep(2000);
        System.out.println("\n  ✅ 4A COMPLETE!\n");
    }


    // ── TEST 4B: Multiple Tabs Management ─────────────────────────────
    @Test(description = "4B VISUAL — Multiple Tabs Management")
    public void test4B_MultipleTabsManagement() throws InterruptedException {
        System.out.println("\n━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━");
        System.out.println("🗂️  TEST 4B: Multiple Tabs");
        System.out.println("━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━");

        // Playwright: context.newPage() → Selenium: JS window.open()
        String originalWindow = driver.getWindowHandle();

        // Tab 1 (current window)
        showStep(1, "Tab 1: playwright.dev");
        driver.get("https://playwright.dev");
        highlightElement(By.cssSelector("nav"), "#e74c3c");
        js.executeScript(
                "var d = document.createElement('div');" +
                        "d.style.cssText = 'position:fixed;bottom:10px;right:10px;z-index:99999;" +
                        "background:#e74c3c;color:white;padding:8px 14px;" +
                        "border-radius:8px;font:bold 13px monospace;';" +
                        "d.textContent = '🗂️ TAB 1';" +
                        "document.body.appendChild(d);"
        );
        System.out.println("  🔴 Tab 1: " + driver.getTitle());
        Thread.sleep(800);

        // Tab 2 — নতুন window open করো
        js.executeScript("window.open('https://github.com/microsoft/playwright', '_blank');");
        wait.until(d -> d.getWindowHandles().size() == 2);
        List<String> handles2 = new ArrayList<>(driver.getWindowHandles());
        driver.switchTo().window(handles2.get(1));
        wait.until(ExpectedConditions.titleContains("playwright"));
        showStep(2, "Tab 2: github.com");
        highlightElement(By.cssSelector("h1"), "#f39c12");
        js.executeScript(
                "var d = document.createElement('div');" +
                        "d.style.cssText = 'position:fixed;bottom:10px;right:10px;z-index:99999;" +
                        "background:#f39c12;color:white;padding:8px 14px;" +
                        "border-radius:8px;font:bold 13px monospace;';" +
                        "d.textContent = '🗂️ TAB 2';" +
                        "document.body.appendChild(d);"
        );
        System.out.println("  🟠 Tab 2: " + driver.getTitle());
        Thread.sleep(800);

        // Tab 3
        js.executeScript("window.open('https://www.npmjs.com/package/@playwright/test', '_blank');");
        wait.until(d -> d.getWindowHandles().size() == 3);
        List<String> handles3 = new ArrayList<>(driver.getWindowHandles());
        driver.switchTo().window(handles3.get(2));
        showStep(3, "Tab 3: npmjs.com");
        highlightElement(By.cssSelector("h1"), "#27ae60");
        js.executeScript(
                "var d = document.createElement('div');" +
                        "d.style.cssText = 'position:fixed;bottom:10px;right:10px;z-index:99999;" +
                        "background:#27ae60;color:white;padding:8px 14px;" +
                        "border-radius:8px;font:bold 13px monospace;';" +
                        "d.textContent = '🗂️ TAB 3';" +
                        "document.body.appendChild(d);"
        );
        System.out.println("  🟢 Tab 3: " + driver.getTitle());
        Thread.sleep(800);

        int totalTabs = driver.getWindowHandles().size();
        System.out.println("  📊 Total tabs: " + totalTabs);
        Assert.assertEquals(totalTabs, 3);

        // Tab 2 ও 3 বন্ধ করো
        driver.close(); // Tab 3
        driver.switchTo().window(handles3.get(1));
        driver.close(); // Tab 2
        driver.switchTo().window(originalWindow);

        showStatus("✅ 4B DONE! " + totalTabs + " tabs managed!", "success");
        Thread.sleep(2000);
        System.out.println("\n  ✅ 4B COMPLETE!\n");
    }


    // ── TEST 4C: Multi-Context: Two Different Users ────────────────────
    // Playwright: browser.newContext() → Selenium: নতুন WebDriver instance (incognito)
    @Test(description = "4C VISUAL — Multi-Context: Two Different Users")
    public void test4C_MultiContextUsers() throws InterruptedException {
        System.out.println("\n━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━");
        System.out.println("👥 TEST 4C: Multi-Context Users");
        System.out.println("   Site: saucedemo.com");
        System.out.println("━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━");

        // ── User 1: Standard User ─────────────────────────────────────
        System.out.println("\n  👤 User 1: standard_user");
        // Playwright: browser.newContext() → Selenium: নতুন ChromeDriver (isolated session)
        WebDriverManager.chromedriver().setup();
        ChromeOptions options1 = new ChromeOptions();
        options1.addArguments("--start-maximized");
        WebDriver driver1 = new ChromeDriver(options1);
        JavascriptExecutor js1 = (JavascriptExecutor) driver1;
        WebDriverWait wait1 = new WebDriverWait(driver1, Duration.ofSeconds(10));

        try {
            driver1.get("https://www.saucedemo.com");
            driver1.findElement(By.id("user-name")).sendKeys("standard_user");
            driver1.findElement(By.id("password")).sendKeys("secret_sauce");

            // highlight login button
            WebElement loginBtn1 = driver1.findElement(By.id("login-button"));
            js1.executeScript("arguments[0].style.cssText += 'outline:4px solid #27ae60!important;'", loginBtn1);
            Thread.sleep(500);
            loginBtn1.click();

            wait1.until(ExpectedConditions.urlContains("inventory.html"));

            js1.executeScript(
                    "var d = document.createElement('div');" +
                            "d.style.cssText='position:fixed;top:10px;left:50%;transform:translateX(-50%);" +
                            "z-index:99999;background:#27ae60;color:white;padding:12px 24px;" +
                            "border-radius:8px;font:bold 15px monospace;';" +
                            "d.textContent='✅ User 1: Access GRANTED!';" +
                            "document.body.appendChild(d);"
            );
            System.out.println("  ✅ Standard user: access granted");

            WebElement inventory = wait1.until(
                    ExpectedConditions.visibilityOfElementLocated(By.cssSelector(".inventory_list"))
            );
            js1.executeScript("arguments[0].style.cssText += 'outline:4px solid #27ae60!important;'", inventory);
            Thread.sleep(1500);
        } finally {
            driver1.quit();
        }

        // ── User 2: Locked Out User ───────────────────────────────────
        System.out.println("\n  👤 User 2: locked_out_user");
        ChromeOptions options2 = new ChromeOptions();
        options2.addArguments("--start-maximized");
        WebDriver driver2 = new ChromeDriver(options2);
        JavascriptExecutor js2 = (JavascriptExecutor) driver2;
        WebDriverWait wait2 = new WebDriverWait(driver2, Duration.ofSeconds(10));

        try {
            driver2.get("https://www.saucedemo.com");
            driver2.findElement(By.id("user-name")).sendKeys("locked_out_user");
            driver2.findElement(By.id("password")).sendKeys("secret_sauce");

            WebElement loginBtn2 = driver2.findElement(By.id("login-button"));
            js2.executeScript("arguments[0].style.cssText += 'outline:4px solid #e74c3c!important;'", loginBtn2);
            Thread.sleep(500);
            loginBtn2.click();

            WebElement errorMsg = wait2.until(
                    ExpectedConditions.visibilityOfElementLocated(By.cssSelector("[data-test='error']"))
            );
            Assert.assertTrue(errorMsg.isDisplayed(), "Error message should be visible for locked user");

            js2.executeScript("arguments[0].style.cssText += 'outline:4px solid #e74c3c!important;'", errorMsg);
            js2.executeScript(
                    "var d = document.createElement('div');" +
                            "d.style.cssText='position:fixed;top:10px;left:50%;transform:translateX(-50%);" +
                            "z-index:99999;background:#e74c3c;color:white;padding:12px 24px;" +
                            "border-radius:8px;font:bold 15px monospace;';" +
                            "d.textContent='❌ User 2: Access DENIED!';" +
                            "document.body.appendChild(d);"
            );
            System.out.println("  ❌ Locked user: blocked");
            Thread.sleep(1500);
        } finally {
            driver2.quit();
        }

        System.out.println("\n  ✅ 4C COMPLETE!\n");
    }

    @Test(description = "4D VISUAL — Real-time Chat (Sender + Receiver)")
    public void test4D_RealtimeChat() throws InterruptedException, IOException {
        System.out.println("\n━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━");
        System.out.println("💬 TEST 4D: Real-time Chat");
        System.out.println("━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━");

        final String htmlContent =
                "<!DOCTYPE html><html><head><style>" +
                        "body{font-family:sans-serif;margin:0;padding:20px;background:#f0f4f8;}" +
                        "h1{color:#2c3e50;margin-bottom:4px;}" +
                        ".user-badge{display:inline-block;padding:4px 12px;border-radius:20px;" +
                        "color:white;font-size:13px;margin-bottom:16px;}" +
                        "#messages{background:white;border:1px solid #ddd;border-radius:10px;" +
                        "padding:16px;height:250px;overflow-y:auto;margin-bottom:12px;}" +
                        ".message{padding:8px 12px;border-radius:8px;margin-bottom:8px;max-width:70%;}" +
                        ".msg-alice{background:#3498db;color:white;margin-left:auto;text-align:right;}" +
                        ".msg-bob{background:#ecf0f1;color:#2c3e50;}" +
                        ".input-row{display:flex;gap:10px;}" +
                        "input{flex:1;padding:10px;border:1px solid #ddd;border-radius:8px;}" +
                        "button{padding:10px 20px;background:#3498db;color:white;border:none;" +
                        "border-radius:8px;cursor:pointer;}" +
                        "</style></head><body>" +
                        "<h1>Chat Room</h1>" +
                        "<div id='user-badge' class='user-badge'>Loading...</div>" +
                        "<div id='messages'></div>" +
                        "<div class='input-row'>" +
                        "<input id='msg-input' placeholder='Type message...'/>" +
                        "<button onclick='sendMsg()'>Send</button>" +
                        "</div>" +
                        "<script>" +
                        "var user=new URLSearchParams(window.location.search).get('user')||'User';" +
                        "var colors={Alice:'#e74c3c',Bob:'#27ae60'};" +
                        "var badge=document.getElementById('user-badge');" +
                        "badge.textContent='User: '+user;" +
                        "badge.style.background=colors[user]||'#3498db';" +
                        "var ch=new BroadcastChannel('chat-room');" +
                        "var msgs=[];" +
                        "function render(){" +
                        "  var box=document.getElementById('messages');" +
                        "  box.innerHTML='';" +
                        "  msgs.forEach(function(m){" +
                        "    var d=document.createElement('div');" +
                        "    d.className='message msg-'+m.user.toLowerCase();" +
                        "    d.textContent=m.user+': '+m.text;" +
                        "    box.appendChild(d);" +
                        "  });" +
                        "  box.scrollTop=box.scrollHeight;" +
                        "}" +
                        "function sendMsg(){" +
                        "  var inp=document.getElementById('msg-input');" +
                        "  var txt=inp.value.trim();" +
                        "  if(!txt)return;" +
                        "  var msg={id:Date.now(),user:user,text:txt};" +
                        "  msgs.push(msg);ch.postMessage(msg);render();inp.value='';" +
                        "}" +
                        "ch.onmessage=function(e){msgs.push(e.data);render();};" +
                        "document.getElementById('msg-input').addEventListener('keypress',function(e){" +
                        "  if(e.key==='Enter')sendMsg();" +
                        "});" +
                        "</script></body></html>";

        // ✅ FIX: lambda এর বদলে anonymous HttpHandler class use করো
        //        এতে import error হবে না
        HttpServer server = HttpServer.create(new InetSocketAddress(8765), 0);
        server.createContext("/chat", new HttpHandler() {
            @Override
            public void handle(HttpExchange exchange) throws IOException {
                byte[] response = htmlContent.getBytes("UTF-8");
                exchange.getResponseHeaders().add("Content-Type", "text/html; charset=UTF-8");
                exchange.sendResponseHeaders(200, response.length);
                OutputStream os = exchange.getResponseBody();
                os.write(response);
                os.close();
            }
        });
        server.setExecutor(null); // default executor
        server.start();
        System.out.println("  Server started: http://localhost:8765/chat");

        String baseUrl = "http://localhost:8765/chat";

        try {
            // ── Alice window ───────────────────────────────────────────
            driver.get(baseUrl + "?user=Alice");
            Thread.sleep(800);
            String aliceWindow = driver.getWindowHandle();

            showStep(1, "Alice joined!");
            wait.until(ExpectedConditions.textToBePresentInElementLocated(
                    By.id("user-badge"), "Alice"));
            highlightElement(By.id("user-badge"), "#e74c3c");
            System.out.println("  Alice badge: "
                    + driver.findElement(By.id("user-badge")).getText());

            // ── Bob window ─────────────────────────────────────────────
            js.executeScript("window.open(arguments[0], '_blank');", baseUrl + "?user=Bob");
            wait.until(d -> d.getWindowHandles().size() == 2);

            List<String> handles = new ArrayList<>(driver.getWindowHandles());
            String bobWindow = handles.stream()
                    .filter(h -> !h.equals(aliceWindow))
                    .findFirst().orElseThrow();

            driver.switchTo().window(bobWindow);
            Thread.sleep(800);
            showStep(2, "Bob joined!");
            wait.until(ExpectedConditions.textToBePresentInElementLocated(
                    By.id("user-badge"), "Bob"));
            highlightElement(By.id("user-badge"), "#27ae60");
            System.out.println("  Bob badge: "
                    + driver.findElement(By.id("user-badge")).getText());

            // ── Alice message পাঠাও ───────────────────────────────────
            driver.switchTo().window(aliceWindow);
            showStep(3, "Alice message পাঠাচ্ছে...");
            WebElement aliceInput = wait.until(
                    ExpectedConditions.visibilityOfElementLocated(By.id("msg-input")));
            aliceInput.clear();
            aliceInput.sendKeys("Hello Bob! This is Alice");
            highlightElement(aliceInput, "#e74c3c");
            aliceInput.sendKeys(Keys.ENTER);
            Thread.sleep(600);

            wait.until(ExpectedConditions.textToBePresentInElementLocated(
                    By.id("messages"), "Alice: Hello Bob!"));
            highlightElement(By.id("messages"), "#27ae60");
            System.out.println("  Alice sent!");

            // ── Bob reply ─────────────────────────────────────────────
            driver.switchTo().window(bobWindow);
            showStep(4, "Bob reply পাঠাচ্ছে...");
            WebElement bobInput = wait.until(
                    ExpectedConditions.visibilityOfElementLocated(By.id("msg-input")));
            bobInput.clear();
            bobInput.sendKeys("Hi Alice! Got it");
            highlightElement(bobInput, "#27ae60");
            bobInput.sendKeys(Keys.ENTER);
            Thread.sleep(600);

            wait.until(ExpectedConditions.textToBePresentInElementLocated(
                    By.id("messages"), "Bob: Hi Alice!"));
            highlightElement(By.id("messages"), "#27ae60");
            System.out.println("  Bob replied!");

            driver.switchTo().window(aliceWindow);
            showStatus("4D DONE! Chat complete!", "success");
            Thread.sleep(2000);

            driver.switchTo().window(bobWindow);
            driver.close();
            driver.switchTo().window(aliceWindow);

        } finally {
            server.stop(0);
            System.out.println("  Server stopped");
        }

        System.out.println("\n  4D COMPLETE!\n");
    }


    // ════════════════════════════════════════════════════════════════════
    //  TEST 4E — Buyer + Seller
    //  FIX: standard_user use করো — সবচেয়ে reliable
    //       specific button id দিয়ে click করো
    // ════════════════════════════════════════════════════════════════════
    @Test(description = "4E VISUAL — Buyer + Seller (saucedemo)")
    public void test4E_BuyerSeller() throws InterruptedException {
        System.out.println("\n━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━");
        System.out.println("🛒 TEST 4E: Buyer + Seller");
        System.out.println("   Site: saucedemo.com");
        System.out.println("━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━");

        // ── SELLER: visual_user ────────────────────────────────────────
        System.out.println("\n  🏪 SELLER...");
        WebDriverManager.chromedriver().setup();
        ChromeOptions sellerOpts = new ChromeOptions();
        sellerOpts.addArguments("--start-maximized");
        WebDriver sellerDriver = new ChromeDriver(sellerOpts);
        JavascriptExecutor sellerJs = (JavascriptExecutor) sellerDriver;
        WebDriverWait sellerWait = new WebDriverWait(sellerDriver, Duration.ofSeconds(15));

        try {
            sellerDriver.get("https://www.saucedemo.com");
            sellerDriver.findElement(By.id("user-name")).sendKeys("visual_user");
            sellerDriver.findElement(By.id("password")).sendKeys("secret_sauce");
            WebElement sellerBtn = sellerDriver.findElement(By.id("login-button"));
            sellerJs.executeScript(
                    "arguments[0].style.cssText+='outline:4px solid #9b59b6!important;'", sellerBtn);
            Thread.sleep(400);
            sellerBtn.click();
            sellerWait.until(ExpectedConditions.urlContains("inventory.html"));

            WebElement inv = sellerWait.until(ExpectedConditions.visibilityOfElementLocated(
                    By.cssSelector(".inventory_list")));
            sellerJs.executeScript(
                    "arguments[0].style.cssText+='outline:4px solid #9b59b6!important;'", inv);

            int count = sellerDriver.findElements(By.cssSelector(".inventory_item")).size();
            System.out.println("  📦 " + count + " products listed");

            sellerJs.executeScript(
                    "var d=document.createElement('div');" +
                            "d.style.cssText='position:fixed;top:60px;right:10px;z-index:99999;" +
                            "background:#9b59b6;color:white;padding:12px 16px;" +
                            "border-radius:8px;font:bold 13px monospace;';" +
                            "d.textContent='🏪 SELLER | 📦 " + count + " products';" +
                            "document.body.appendChild(d);"
            );
            sellerJs.executeScript(
                    "var b=document.createElement('div');" +
                            "b.style.cssText='position:fixed;top:10px;left:50%;transform:translateX(-50%);" +
                            "background:#9b59b6;color:white;padding:12px 24px;" +
                            "border-radius:8px;font:bold 15px monospace;z-index:999999;';" +
                            "b.textContent='🏪 Seller: Viewing inventory';" +
                            "document.body.appendChild(b);"
            );
            Thread.sleep(1500);
        } finally {
            sellerDriver.quit();
        }

        // ── BUYER: standard_user ───────────────────────────────────────
        System.out.println("\n  🛒 BUYER...");
        driver.get("https://www.saucedemo.com");
        driver.findElement(By.id("user-name")).sendKeys("standard_user");
        driver.findElement(By.id("password")).sendKeys("secret_sauce");
        WebElement buyerBtn = driver.findElement(By.id("login-button"));
        js.executeScript(
                "arguments[0].style.cssText+='outline:4px solid #27ae60!important;'", buyerBtn);
        Thread.sleep(400);
        buyerBtn.click();
        wait.until(ExpectedConditions.urlContains("inventory.html"));

        showStep(2, "Buyer — Products দেখছে...");
        wait.until(ExpectedConditions.visibilityOfElementLocated(By.cssSelector(".inventory_list")));
        highlightElement(By.cssSelector(".inventory_list"), "#27ae60");

        // ── DEBUG: page এ কোন buttons আছে সেটা print করো ─────────────
        showStep(3, "Cart এ add করছে...");
        List<WebElement> allBtns = driver.findElements(By.cssSelector(".inventory_item button"));
        System.out.println("  📋 Found " + allBtns.size() + " buttons on page:");
        for (WebElement b : allBtns) {
            System.out.println("     id='" + b.getAttribute("id")
                    + "' text='" + b.getText()
                    + "' class='" + b.getAttribute("class") + "'");
        }

        // ✅ FIX: যেকোনো Add button খুঁজো — id নির্দিষ্ট করো না
        //        class দিয়ে খোঁজো, অথবা data-test attribute দিয়ে
        WebElement addBtn = null;

        // Strategy 1: data-test attribute দিয়ে খোঁজো (most reliable)
        List<WebElement> dataTestBtns = driver.findElements(
                By.cssSelector("[data-test^='add-to-cart']"));
        if (!dataTestBtns.isEmpty()) {
            addBtn = dataTestBtns.get(0);
            System.out.println("  ✅ Found via data-test: " + addBtn.getAttribute("data-test"));
        }

        // Strategy 2: button text দিয়ে খোঁজো
        if (addBtn == null) {
            List<WebElement> textBtns = driver.findElements(
                    By.xpath("//button[contains(text(),'Add to cart') or contains(text(),'ADD TO CART')]"));
            if (!textBtns.isEmpty()) {
                addBtn = textBtns.get(0);
                System.out.println("  ✅ Found via text: " + addBtn.getText());
            }
        }

        // Strategy 3: btn_inventory class দিয়ে খোঁজো
        if (addBtn == null) {
            List<WebElement> classBtns = driver.findElements(
                    By.cssSelector("button.btn_inventory, button.btn_primary.btn_inventory"));
            if (!classBtns.isEmpty()) {
                addBtn = classBtns.get(0);
                System.out.println("  ✅ Found via class: " + addBtn.getAttribute("class"));
            }
        }

        // Strategy 4: inventory item এর ভেতরের যেকোনো button
        if (addBtn == null) {
            List<WebElement> itemBtns = driver.findElements(
                    By.cssSelector(".inventory_item button"));
            if (!itemBtns.isEmpty()) {
                addBtn = itemBtns.get(0);
                System.out.println("  ✅ Found via .inventory_item button: "
                        + addBtn.getAttribute("id"));
            }
        }

        Assert.assertNotNull(addBtn, "Add to cart button must be found on page");

        // Button click করো
        js.executeScript("arguments[0].scrollIntoView(true);", addBtn);
        Thread.sleep(300);
        highlightElement(addBtn, "#f39c12");

        // ✅ JS click use করো — intercept এর সমস্যা avoid করতে
        js.executeScript("arguments[0].click();", addBtn);
        Thread.sleep(1000);

        // ── Cart verify ────────────────────────────────────────────────
        // ✅ FIX: Remove button বা cart badge — যেটা আগে আসে সেটা দিয়ে verify
        boolean cartUpdated = false;

        // Check 1: cart badge
        List<WebElement> badges = driver.findElements(By.cssSelector(".shopping_cart_badge"));
        if (!badges.isEmpty() && badges.get(0).isDisplayed()) {
            System.out.println("  ✅ Cart badge: " + badges.get(0).getText());
            highlightElement(badges.get(0), "#27ae60");
            cartUpdated = true;
        }

        // Check 2: Remove button appeared
        if (!cartUpdated) {
            List<WebElement> removeBtns = driver.findElements(
                    By.cssSelector("[data-test^='remove'], button[id^='remove']"));
            if (!removeBtns.isEmpty()) {
                System.out.println("  ✅ Remove button appeared: " + removeBtns.get(0).getText());
                highlightElement(removeBtns.get(0), "#27ae60");
                cartUpdated = true;
            }
        }

        // Check 3: page source এ "remove" আছে কিনা
        if (!cartUpdated) {
            boolean hasRemove = driver.getPageSource().contains("remove-");
            System.out.println("  📊 Page has remove button in source: " + hasRemove);
            cartUpdated = hasRemove;
        }

        Assert.assertTrue(cartUpdated, "Item should be added to cart");

        js.executeScript(
                "var d=document.createElement('div');" +
                        "d.style.cssText='position:fixed;top:60px;right:10px;z-index:99999;" +
                        "background:#27ae60;color:white;padding:12px 16px;" +
                        "border-radius:8px;font:bold 13px monospace;';" +
                        "d.textContent='🛒 BUYER | ✅ Item in cart!';" +
                        "document.body.appendChild(d);"
        );
        showStatus("✅ 4E DONE! Cart updated!", "success");
        Thread.sleep(2000);
        System.out.println("\n  ✅ 4E COMPLETE!\n");
    }
}
