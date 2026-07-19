

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

import java.io.*;
import java.net.InetSocketAddress;
import java.net.URL;
import java.net.HttpURLConnection;
import java.nio.file.*;
import java.time.Duration;
import java.util.*;

// ════════════════════════════════════════════════════════════════════════
//  TOPIC 1 — Mock Network Testing
//  
//  1A — Mock API Response
//  1B — Modify Real Response  
//  1C — Slow Network Simulation
//  1D — 500 Error Injection
//  1E — Request Block (Images Block)
// ════════════════════════════════════════════════════════════════════════

public class MockNetworkTest extends DriverManager {

    // ════════════════════════════════════════════════════════════════════
    //  TEST 1A — Mock API Response
    //
    //  Playwright: page.route(url, route => route.fulfill({...}))
    //  Selenium:   Java HttpServer দিয়ে localhost এ real mock server চালাও
    //              Browser সেই localhost URL এ request করবে
    //
    //  Flow:
    //    1. HttpServer port 8770 এ start করো
    //    2. /api/products → fake JSON return করে
    //    3. / → HTML page serve করে যেটা /api/products fetch করে
    //    4. Fake products page এ দেখা যাচ্ছে কিনা verify করো
    // ════════════════════════════════════════════════════════════════════

    @Test(description = "1A VISUAL — Mock API Response")
    public void test1A_MockApiResponse() throws InterruptedException, IOException {
        System.out.println("\n━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━");
        System.out.println("🎭 TEST 1A: Mock API Response");
        System.out.println("━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━");

        // ── Fake JSON data ────────────────────────────────────────────
        final String fakeJson =
                "{\"products\":[" +
                        "{\"id\":1,\"name\":\"Fake Product A\",\"price\":100}," +
                        "{\"id\":2,\"name\":\"Fake Product B\",\"price\":200}" +
                        "],\"total\":2}";

        // ── HTML page — /api/products fetch করে display করে ──────────
        final String htmlPage =
                "<!DOCTYPE html><html><head><style>" +
                        "body{font-family:sans-serif;padding:20px;background:#f5f5f5;margin:0;}" +
                        "h1{color:#333;margin-bottom:8px;}" +
                        "#mock-banner{background:linear-gradient(135deg,#9b59b6,#8e44ad);" +
                        "color:white;padding:10px 16px;border-radius:8px;margin-bottom:20px;font-size:13px;}" +
                        ".product-card{background:white;border:1px solid #ddd;" +
                        "border-radius:8px;padding:16px;margin-bottom:12px;" +
                        "display:flex;justify-content:space-between;align-items:center;}" +
                        ".product-name{font-weight:bold;color:#2c3e50;font-size:16px;}" +
                        ".product-price{color:#27ae60;font-weight:bold;font-size:18px;}" +
                        ".badge{display:inline-block;background:#3498db;color:white;" +
                        "font-size:11px;padding:2px 8px;border-radius:10px;margin-left:8px;}" +
                        "#loading{color:#666;padding:20px;text-align:center;font-size:16px;}" +
                        "</style></head><body>" +
                        "<div id='mock-banner'>🎭 MOCK MODE: Real server এ যাচ্ছে না — fake data আসছে!</div>" +
                        "<h1>Product List</h1>" +
                        "<div id='loading'>⏳ Fetching products from /api/products...</div>" +
                        "<div id='product-list'></div>" +
                        "<script>" +
                        "fetch('http://localhost:8770/api/products')" +
                        ".then(function(r){return r.json();})" +
                        ".then(function(data){" +
                        "  document.getElementById('loading').style.display='none';" +
                        "  var list=document.getElementById('product-list');" +
                        "  data.products.forEach(function(p,i){" +
                        "    var card=document.createElement('div');" +
                        "    card.className='product-card';" +
                        "    card.id='product-'+(i+1);" +
                        "    card.innerHTML='<span class=\"product-name\">'+p.name+" +
                        "      '<span class=\"badge\">MOCK</span></span>" +
                        "      <span class=\"product-price\">৳'+p.price+'</span>';" +
                        "    list.appendChild(card);" +
                        "  });" +
                        "}).catch(function(e){" +
                        "  document.getElementById('loading').textContent='Error: '+e.message;" +
                        "});" +
                        "</script></body></html>";

        // ── HttpServer start করো ──────────────────────────────────────
        HttpServer server = HttpServer.create(new InetSocketAddress(8770), 0);

        // /api/products → fake JSON
        server.createContext("/api/products", new HttpHandler() {
            @Override
            public void handle(HttpExchange ex) throws IOException {
                System.out.println("\n🔴 INTERCEPTED! /api/products request আসছে...");
                System.out.println("   Fake JSON return হচ্ছে!");

                // CORS header দরকার কারণ HTML page ও localhost থেকে serve হচ্ছে
                ex.getResponseHeaders().add("Content-Type", "application/json");
                ex.getResponseHeaders().add("Access-Control-Allow-Origin", "*");
                byte[] resp = fakeJson.getBytes("UTF-8");
                ex.sendResponseHeaders(200, resp.length);
                ex.getResponseBody().write(resp);
                ex.getResponseBody().close();
                System.out.println("✅ Fake JSON পাঠানো হলো!");
            }
        });

        // / → HTML page
        server.createContext("/", new HttpHandler() {
            @Override
            public void handle(HttpExchange ex) throws IOException {
                byte[] resp = htmlPage.getBytes("UTF-8");
                ex.getResponseHeaders().add("Content-Type", "text/html; charset=UTF-8");
                ex.sendResponseHeaders(200, resp.length);
                ex.getResponseBody().write(resp);
                ex.getResponseBody().close();
            }
        });

        server.setExecutor(null);
        server.start();
        System.out.println("  🌐 Mock server: http://localhost:8770");

        try {
            // Step 1: Page load
            showStep(1, "Mock server চালু — page এ যাচ্ছি...");
            driver.get("http://localhost:8770/");
            Thread.sleep(500);

            // Step 2: Fetch হচ্ছে
            showStep(2, "Browser /api/products fetch করছে → mock intercept হবে!");
            Thread.sleep(1000);

            // Step 3: Fake Product A verify
            showStep(3, "Fake products দেখাচ্ছে কিনা verify করছি...");
            wait.until(ExpectedConditions.visibilityOfElementLocated(
                    By.id("product-1")));
            highlightElement(By.id("product-1"), "#27ae60");
            System.out.println("\n🟢 \"Fake Product A\" দেখা গেছে — highlighted!");

            // Fake Product B verify
            wait.until(ExpectedConditions.visibilityOfElementLocated(
                    By.id("product-2")));
            highlightElement(By.id("product-2"), "#27ae60");
            System.out.println("🟢 \"Fake Product B\" দেখা গেছে — highlighted!");

            // Text verify
            String p1Text = driver.findElement(By.id("product-1")).getText();
            String p2Text = driver.findElement(By.id("product-2")).getText();
            Assert.assertTrue(p1Text.contains("Fake Product A"), "Product A should be visible");
            Assert.assertTrue(p2Text.contains("Fake Product B"), "Product B should be visible");

            showStatus("✅ 1A PASSED! Mock data visible!", "success");
            Thread.sleep(2000);
            System.out.println("\n✅ 1A PASSED: Mock API response কাজ করছে!\n");

        } finally {
            server.stop(0);
        }
    }

    // ════════════════════════════════════════════════════════════════════
    //  TEST 1B — Modify Real Response
    //
    //  Playwright: route.fetch() → real response আনো → modify করো → fulfill
    //  Selenium:   HttpServer দিয়ে proxy করো:
    //              Browser → Mock Server → Real Server → Modify → Browser
    //
    //  Flow:
    //    1. Mock server /users/1 এ real jsonplaceholder API call করে
    //    2. Response modify করে (name prefix + isAdmin add)
    //    3. Browser modified data দেখে
    // ════════════════════════════════════════════════════════════════════

    @Test(description = "1B VISUAL — Modify Real API Response - Simplified")
    public void test1B_ModifyRealResponse_Simplified() throws InterruptedException, IOException {
        System.out.println("\n━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━");
        System.out.println("🔧 TEST 1B: Modify Real Response (Simplified)");
        System.out.println("━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━");

        // HTML page
        final String htmlPage =
                "<!DOCTYPE html><html><head><style>" +
                        "body{font-family:sans-serif;padding:20px;background:#f8f9fa;}" +
                        ".grid{display:grid;grid-template-columns:1fr 1fr;gap:20px;margin-top:20px;}" +
                        ".box{background:white;border-radius:10px;padding:20px;" +
                        "box-shadow:0 2px 10px rgba(0,0,0,0.08);}" +
                        ".box h3{margin:0 0 16px;padding-bottom:8px;border-bottom:2px solid #eee;}" +
                        ".box.original h3{color:#e74c3c;}" +
                        ".box.modified h3{color:#27ae60;}" +
                        ".field{display:flex;gap:8px;margin-bottom:8px;padding:8px;" +
                        "background:#f8f9fa;border-radius:6px;}" +
                        ".label{color:#666;font-size:13px;min-width:90px;}" +
                        ".value{font-weight:600;color:#2c3e50;font-size:13px;}" +
                        ".new-field{background:#d5f5e3;}" +
                        ".changed{background:#fef9e7;}" +
                        "#loading{text-align:center;padding:40px;color:#666;}" +
                        "</style></head><body>" +
                        "<h1>User Profile — Before vs After</h1>" +
                        "<div id='loading'>⏳ Fetching user data...</div>" +
                        "<div class='grid' id='comparison' style='display:none'>" +
                        "<div class='box original'><h3>❌ Original</h3><div id='orig'></div></div>" +
                        "<div class='box modified'><h3>✅ Modified</h3><div id='mod'></div></div>" +
                        "</div>" +
                        "<script>" +
                        "var orig={name:'Leanne Graham',email:'Sincere@april.biz'," +
                        "isAdmin:'undefined (ছিল না)',extraField:'undefined (ছিল না)'};" +
                        "fetch('http://localhost:8771/users/1')" +
                        ".then(function(r){return r.json();})" +
                        ".then(function(data){" +
                        "  document.getElementById('loading').style.display='none';" +
                        "  document.getElementById('comparison').style.display='grid';" +
                        "  var o=document.getElementById('orig');" +
                        "  [['name',orig.name],['email',orig.email]," +
                        "   ['isAdmin',orig.isAdmin],['extraField',orig.extraField]]" +
                        "  .forEach(function(f){o.innerHTML+=" +
                        "    '<div class=\"field\"><span class=\"label\">'+f[0]+':</span>" +
                        "    <span class=\"value\">'+f[1]+'</span></div>';});" +
                        "  var m=document.getElementById('mod');" +
                        "  [['name',data.name,'changed'],['email',data.email,'']," +
                        "   ['isAdmin',String(data.isAdmin),'new-field']," +
                        "   ['extraField',data.extraField,'new-field']]" +
                        "  .forEach(function(f){m.innerHTML+=" +
                        "    '<div class=\"field '+f[2]+'\"><span class=\"label\">'+f[0]+':</span>" +
                        "    <span class=\"value\">'+f[1]+'</span></div>';});" +
                        "});" +
                        "</script></body></html>";

        HttpServer server = HttpServer.create(new InetSocketAddress(8771), 0);

        server.createContext("/users/1", new HttpHandler() {
            @Override
            public void handle(HttpExchange ex) throws IOException {
                System.out.println("\n🔴 /users/1 intercepted!");

                try {
                    // Call real API
                    URL url = new URL("https://jsonplaceholder.typicode.com/users/1");
                    HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                    conn.setRequestMethod("GET");
                    conn.setConnectTimeout(5000);

                    BufferedReader reader = new BufferedReader(
                            new InputStreamReader(conn.getInputStream()));
                    StringBuilder sb = new StringBuilder();
                    String line;
                    while ((line = reader.readLine()) != null) sb.append(line);
                    reader.close();
                    conn.disconnect();

                    String originalBody = sb.toString();
                    System.out.println("   Original: " + originalBody);

                    // Extract values using simple string operations
                    String name = extractJsonValue(originalBody, "name");
                    String email = extractJsonValue(originalBody, "email");
                    String username = extractJsonValue(originalBody, "username");
                    String phone = extractJsonValue(originalBody, "phone");

                    System.out.println("   Extracted: name=" + name + ", email=" + email);

                    // Build modified JSON manually
                    String modified = "{" +
                            "\"id\":1," +
                            "\"name\":\"MODIFIED: " + name + "\"," +
                            "\"username\":\"" + username + "\"," +
                            "\"email\":\"" + email + "\"," +
                            "\"phone\":\"" + phone + "\"," +
                            "\"isAdmin\":true," +
                            "\"extraField\":\"injected\"," +
                            "\"address\":{\"street\":\"Kulas Light\",\"suite\":\"Apt. 556\",\"city\":\"Gwenborough\",\"zipcode\":\"92998-3874\"}," +
                            "\"company\":{\"name\":\"Romaguera-Crona\",\"catchPhrase\":\"Multi-layered client-server neural-net\",\"bs\":\"harness real-time e-markets\"}" +
                            "}";

                    System.out.println("   Modified: " + modified);

                    ex.getResponseHeaders().add("Content-Type", "application/json");
                    ex.getResponseHeaders().add("Access-Control-Allow-Origin", "*");
                    byte[] resp = modified.getBytes("UTF-8");
                    ex.sendResponseHeaders(200, resp.length);
                    ex.getResponseBody().write(resp);
                    ex.getResponseBody().close();

                } catch (Exception e) {
                    System.err.println("   ❌ Error: " + e.getMessage());
                    String errorJson = "{\"error\":\"Failed\"}";
                    ex.getResponseHeaders().add("Content-Type", "application/json");
                    ex.getResponseHeaders().add("Access-Control-Allow-Origin", "*");
                    byte[] resp = errorJson.getBytes("UTF-8");
                    ex.sendResponseHeaders(500, resp.length);
                    ex.getResponseBody().write(resp);
                    ex.getResponseBody().close();
                }
            }
        });

        server.createContext("/", new HttpHandler() {
            @Override
            public void handle(HttpExchange ex) throws IOException {
                byte[] resp = htmlPage.getBytes("UTF-8");
                ex.getResponseHeaders().add("Content-Type", "text/html; charset=UTF-8");
                ex.sendResponseHeaders(200, resp.length);
                ex.getResponseBody().write(resp);
                ex.getResponseBody().close();
            }
        });

        server.setExecutor(null);
        server.start();
        System.out.println("  🌐 Proxy server: http://localhost:8771");

        try {
            showStep(1, "Proxy server চালু — page load হচ্ছে...");
            driver.get("http://localhost:8771/");
            Thread.sleep(1500);

            showStep(2, "Real API call → modify → browser এ দেখাচ্ছে...");

            wait.until(ExpectedConditions.visibilityOfElementLocated(
                    By.cssSelector(".box.modified")));
            highlightElement(By.cssSelector(".box.modified"), "#27ae60");

            Thread.sleep(2000);

            // Verify
            String modText = driver.findElement(By.id("mod")).getText();
            System.out.println("   Modified text: " + modText);

            Assert.assertTrue(modText.contains("MODIFIED:"),
                    "Modified name should contain 'MODIFIED:'");
            Assert.assertTrue(modText.contains("true"),
                    "isAdmin should be true");

            showStatus("✅ 1B PASSED! Response modified!", "success");
            Thread.sleep(2000);
            System.out.println("\n✅ 1B PASSED!\n");

        } finally {
            server.stop(0);
        }
    }

    // Improved JSON value extractor
    private String extractJsonValue(String json, String key) {
        try {
            // Try different patterns
            String[] patterns = {
                    "\"" + key + "\":\"",
                    "\"" + key + "\": \"",
                    "\"" + key + "\":"
            };

            for (String pattern : patterns) {
                int start = json.indexOf(pattern);
                if (start != -1) {
                    start += pattern.length();
                    // If pattern didn't include the quote, find it
                    if (!pattern.endsWith("\"")) {
                        start = json.indexOf("\"", start);
                        if (start != -1) {
                            start++;
                        } else {
                            continue;
                        }
                    }
                    int end = json.indexOf("\"", start);
                    if (end != -1) {
                        return json.substring(start, end);
                    }
                }
            }
        } catch (Exception e) {
            System.out.println("   ⚠️ Error extracting " + key + ": " + e.getMessage());
        }
        return "";
    }

    // ════════════════════════════════════════════════════════════════════
    //  TEST 1C — Slow Network Simulation
    //
    //  Playwright: setTimeout(DELAY_MS) inside route handler
    //  Selenium:   HttpServer handler এ Thread.sleep(DELAY_MS) করো
    //
    //  Flow:
    //    1. /api/data → 3 second delay দিয়ে response দেয়
    //    2. Page এ spinner দেখায়, timer চলে
    //    3. 3s পর content দেখা যায়
    // ════════════════════════════════════════════════════════════════════

    @Test(description = "1C VISUAL — Slow Network Simulation")
    public void test1C_SlowNetwork() throws InterruptedException, IOException {
        System.out.println("\n━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━");
        System.out.println("⏳ TEST 1C: Slow Network");
        System.out.println("━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━");

        final int DELAY_MS = 3000;
        final String dataJson = "{\"message\":\"Data loaded!\",\"items\":[1,2,3]}";

        final String htmlPage =
                "<!DOCTYPE html><html><head><style>" +
                        "body{font-family:sans-serif;padding:20px;text-align:center;}" +
                        "h1{color:#2c3e50;}" +
                        ".loading-spinner{width:60px;height:60px;border:6px solid #ecf0f1;" +
                        "border-top:6px solid #3498db;border-radius:50%;" +
                        "animation:spin 0.8s linear infinite;margin:30px auto;}" +
                        "@keyframes spin{0%{transform:rotate(0deg);}100%{transform:rotate(360deg);}}" +
                        "#timer{font-size:48px;font-weight:bold;color:#3498db;" +
                        "margin:10px 0;font-family:monospace;}" +
                        "#timer-label{color:#666;font-size:14px;}" +
                        "#progress-bar-container{background:#ecf0f1;border-radius:10px;" +
                        "height:10px;width:300px;margin:20px auto;overflow:hidden;}" +
                        "#progress-bar{background:linear-gradient(90deg,#3498db,#9b59b6);" +
                        "height:100%;width:0%;border-radius:10px;transition:width 0.1s;}" +
                        "#network-info{background:#fff3cd;border:1px solid #ffc107;" +
                        "border-radius:8px;padding:12px;max-width:300px;margin:20px auto;" +
                        "font-size:13px;color:#856404;}" +
                        ".content{display:none;background:#d5f5e3;border-radius:12px;" +
                        "padding:30px;max-width:400px;margin:20px auto;}" +
                        ".content h2{color:#27ae60;margin:0 0 10px;}" +
                        "</style></head><body>" +
                        "<h1>Dashboard</h1>" +
                        "<div id='network-info'>🐢 Slow Network Simulated: 3G Speed</div>" +
                        "<div class='loading-spinner' id='spinner'></div>" +
                        "<div id='timer'>0.0s</div>" +
                        "<div id='timer-label'>waiting for server response...</div>" +
                        "<div id='progress-bar-container'><div id='progress-bar'></div></div>" +
                        "<div class='content' id='content'>" +
                        "<h2>✅ Data Loaded!</h2>" +
                        "<p>Server response received after 3 second delay!</p></div>" +
                        "<script>" +
                        "var startTime=Date.now();var totalDelay=3000;" +
                        "var ti=setInterval(function(){" +
                        "  var e=(Date.now()-startTime)/1000;" +
                        "  document.getElementById('timer').textContent=e.toFixed(1)+'s';" +
                        "  var p=Math.min((e/(totalDelay/1000))*100,95);" +
                        "  document.getElementById('progress-bar').style.width=p+'%';" +
                        "},100);" +
                        "fetch('http://localhost:8772/api/data')" +
                        ".then(function(r){return r.json();})" +
                        ".then(function(data){" +
                        "  clearInterval(ti);" +
                        "  var e=((Date.now()-startTime)/1000).toFixed(1);" +
                        "  document.getElementById('timer').textContent=e+'s';" +
                        "  document.getElementById('progress-bar').style.width='100%';" +
                        "  document.getElementById('timer-label').textContent=" +
                        "    'Response received after '+e+' seconds!';" +
                        "  document.getElementById('spinner').style.display='none';" +
                        "  document.getElementById('content').style.display='block';" +
                        "});" +
                        "</script></body></html>";

        HttpServer server = HttpServer.create(new InetSocketAddress(8772), 0);

        // /api/data → 3s delay দিয়ে response
        server.createContext("/api/data", new HttpHandler() {
            @Override
            public void handle(HttpExchange ex) throws IOException {
                System.out.println("\n🔴 /api/data intercepted! " + DELAY_MS + "ms delay inject হচ্ছে...");
                try {
                    Thread.sleep(DELAY_MS);
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                }
                System.out.println("✅ Delay শেষ! Data পাঠানো হচ্ছে...");
                ex.getResponseHeaders().add("Content-Type", "application/json");
                ex.getResponseHeaders().add("Access-Control-Allow-Origin", "*");
                byte[] resp = dataJson.getBytes("UTF-8");
                ex.sendResponseHeaders(200, resp.length);
                ex.getResponseBody().write(resp);
                ex.getResponseBody().close();
            }
        });

        server.createContext("/", new HttpHandler() {
            @Override
            public void handle(HttpExchange ex) throws IOException {
                byte[] resp = htmlPage.getBytes("UTF-8");
                ex.getResponseHeaders().add("Content-Type", "text/html; charset=UTF-8");
                ex.sendResponseHeaders(200, resp.length);
                ex.getResponseBody().write(resp);
                ex.getResponseBody().close();
            }
        });

        server.setExecutor(null);
        server.start();

        try {
            driver.get("http://localhost:8772/");
            Thread.sleep(500);

            // Spinner visible
            showStep(1, "Page loaded — Spinner দেখাচ্ছে!");
            wait.until(ExpectedConditions.visibilityOfElementLocated(
                    By.cssSelector(".loading-spinner")));
            System.out.println("\n⏳ Spinner visible! 3 seconds delay হচ্ছে...");

            // 3s পর content দেখাবে (timeout 10s)
            showStep(2, "3 seconds delay চলছে... wait করছি...");
            WebDriverWait longWait = new WebDriverWait(driver, Duration.ofSeconds(10));
            longWait.until(ExpectedConditions.visibilityOfElementLocated(
                    By.cssSelector(".content")));
            System.out.println("✅ Content appeared after delay!");

            highlightElement(By.cssSelector(".content"), "#27ae60");
            showStatus("✅ 1C PASSED! Spinner → Content after 3s!", "success");
            Thread.sleep(2000);
            System.out.println("\n✅ 1C PASSED: Slow network simulation কাজ করছে!\n");

        } finally {
            server.stop(0);
        }
    }

    // ════════════════════════════════════════════════════════════════════
    //  TEST 1D — 500 Error Injection
    //
    //  Playwright: route.fulfill({status: 500, body: error_json})
    //  Selenium:   HttpServer 500 status code return করে
    //
    //  Flow:
    //    1. /api/products → 500 status + error JSON return করে
    //    2. Page এ error message দেখায়
    // ════════════════════════════════════════════════════════════════════

    @Test(description = "1D VISUAL — 500 Server Error Injection")
    public void test1D_500ErrorInjection() throws InterruptedException, IOException {
        System.out.println("\n━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━");
        System.out.println("💥 TEST 1D: 500 Error Injection");
        System.out.println("━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━");

        final String errorJson =
                "{\"error\":\"Internal Server Error\"," +
                        "\"message\":\"Something went wrong on our end\"}";

        final String htmlPage =
                "<!DOCTYPE html><html><head><style>" +
                        "body{font-family:sans-serif;padding:20px;}" +
                        "h1{color:#2c3e50;}" +
                        ".error-message{background:#fde8e8;color:#c0392b;" +
                        "border:1px solid #e74c3c;border-radius:10px;" +
                        "padding:20px;display:none;margin-top:20px;}" +
                        ".error-message .error-icon{font-size:40px;text-align:center;}" +
                        ".error-message h3{text-align:center;margin:8px 0 4px;}" +
                        ".error-text{text-align:center;color:#666;}" +
                        "#status-code{display:inline-block;background:#e74c3c;color:white;" +
                        "padding:4px 12px;border-radius:20px;font-family:monospace;" +
                        "font-size:14px;margin-top:8px;}" +
                        "#network-log{position:fixed;bottom:16px;left:16px;" +
                        "background:#1e1e1e;color:#d4d4d4;border-radius:8px;padding:16px;" +
                        "width:320px;font-family:monospace;font-size:12px;" +
                        "box-shadow:0 4px 20px rgba(0,0,0,0.3);}" +
                        "#network-log h4{margin:0 0 10px;color:#9cdcfe;}" +
                        ".log-entry{padding:4px 0;border-bottom:1px solid #2d2d2d;}" +
                        ".log-500{color:#f48771;}" +
                        "</style></head><body>" +
                        "<h1>Products Page</h1>" +
                        "<div id='network-log'>" +
                        "<h4>📡 Network Log</h4>" +
                        "<div id='log-entries'><div class='log-entry'>Waiting...</div></div></div>" +
                        "<div id='product-list'>⏳ Loading products...</div>" +
                        "<div class='error-message' id='error-message'>" +
                        "<div class='error-icon'>💥</div>" +
                        "<h3>Server Error!</h3>" +
                        "<p class='error-text'>Something went wrong on our end</p>" +
                        "<div style='text-align:center'>" +
                        "<span id='status-code'>500 Internal Server Error</span></div></div>" +
                        "<script>" +
                        "function addLog(msg,cls){" +
                        "  var log=document.getElementById('log-entries');" +
                        "  log.innerHTML='';" +
                        "  var e=document.createElement('div');" +
                        "  e.className='log-entry '+cls;e.textContent=msg;" +
                        "  log.appendChild(e);}" +
                        "addLog('→ GET /api/products','');" +
                        "fetch('http://localhost:8773/api/products')" +
                        ".then(function(res){" +
                        "  addLog('← '+res.status+' /api/products',res.ok?'log-200':'log-500');" +
                        "  if(!res.ok){return res.json().then(function(e){throw new Error(e.message);});}" +
                        "  return res.json();" +
                        "}).then(function(data){" +
                        "  document.getElementById('product-list').textContent=data.products.length+' products';" +
                        "}).catch(function(err){" +
                        "  document.getElementById('product-list').style.display='none';" +
                        "  var errDiv=document.getElementById('error-message');" +
                        "  errDiv.style.display='block';" +
                        "  errDiv.querySelector('.error-text').textContent=err.message;" +
                        "});" +
                        "</script></body></html>";

        HttpServer server = HttpServer.create(new InetSocketAddress(8773), 0);

        server.createContext("/api/products", new HttpHandler() {
            @Override
            public void handle(HttpExchange ex) throws IOException {
                System.out.println("\n🔴 /api/products intercepted!");
                System.out.println("   500 Internal Server Error inject হচ্ছে!");
                ex.getResponseHeaders().add("Content-Type", "application/json");
                ex.getResponseHeaders().add("Access-Control-Allow-Origin", "*");
                byte[] resp = errorJson.getBytes("UTF-8");
                ex.sendResponseHeaders(500, resp.length);
                ex.getResponseBody().write(resp);
                ex.getResponseBody().close();
                System.out.println("💥 500 error পাঠানো হলো!");
            }
        });

        server.createContext("/", new HttpHandler() {
            @Override
            public void handle(HttpExchange ex) throws IOException {
                byte[] resp = htmlPage.getBytes("UTF-8");
                ex.getResponseHeaders().add("Content-Type", "text/html; charset=UTF-8");
                ex.sendResponseHeaders(200, resp.length);
                ex.getResponseBody().write(resp);
                ex.getResponseBody().close();
            }
        });

        server.setExecutor(null);
        server.start();

        try {
            showStep(1, "Page load হচ্ছে — 500 error inject হবে!");
            driver.get("http://localhost:8773/");
            Thread.sleep(500);

            showStep(2, "Error message দেখাচ্ছে কিনা verify করছি...");
            wait.until(ExpectedConditions.visibilityOfElementLocated(
                    By.cssSelector(".error-message")));
            System.out.println("\n💥 Error message দেখা গেছে!");

            highlightElement(By.cssSelector(".error-message"), "#e74c3c");

            String errorText = driver.findElement(By.cssSelector(".error-text")).getText();
            Assert.assertTrue(errorText.contains("Something went wrong"),
                    "Error message should contain 'Something went wrong'");

            showStatus("✅ 1D PASSED! 500 Error handled!", "success");
            Thread.sleep(2000);
            System.out.println("\n✅ 1D PASSED: 500 error injection কাজ করছে!\n");

        } finally {
            server.stop(0);
        }
    }

    // ════════════════════════════════════════════════════════════════════
    //  TEST 1E — Request Block (Images Block)
    //
    //  Playwright: page.route('**/*.{png,jpg,...}', route => route.abort())
    //  Selenium:   ChromeOptions দিয়ে images disable করো
    //              Chrome prefs: images.default = 2 → images block করে
    //
    //  Flow:
    //    1. Chrome prefs দিয়ে images block করো
    //    2. books.toscrape.com load করো
    //    3. Image ছাড়া page load হয়েছে কিনা verify করো
    //    4. Screenshot নাও
    // ════════════════════════════════════════════════════════════════════

    @Test(description = "1E VISUAL — Request Block: Images blocked দেখো")
    public void test1E_RequestBlock() throws InterruptedException, IOException {
        System.out.println("\n━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━");
        System.out.println("🚫 TEST 1E: Request Block");
        System.out.println("   Images block করো — books.toscrape.com");
        System.out.println("━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━");

        Files.createDirectories(Paths.get("./test-screenshots/1E"));

        // ── এই test এ নতুন driver দরকার — image block prefs সহ ──────
        // DriverManager এর default driver এ image block নেই
        // তাই এখানে আলাদা driver তৈরি করছি
        WebDriverManager.chromedriver().setup();
        ChromeOptions imageBlockOpts = new ChromeOptions();
        imageBlockOpts.addArguments("--start-maximized");
        imageBlockOpts.addArguments("--disable-notifications");

        // ✅ Chrome Preferences use করো
        // images.default = 2 → সব image block
        Map<String, Object> prefs = new HashMap<>();
        prefs.put("profile.default_content_setting_values.images", 2);
        imageBlockOpts.setExperimentalOption("prefs", prefs);

        WebDriver imageDriver = new ChromeDriver(imageBlockOpts);
        JavascriptExecutor imageJs = (JavascriptExecutor) imageDriver;
        WebDriverWait imageWait = new WebDriverWait(imageDriver, Duration.ofSeconds(15));

        try {
            // Page load করো — images automatically block হবে
            System.out.println("  🚫 Image block via Chrome prefs — images.default = 2");
            long t0 = System.currentTimeMillis();

            imageDriver.get("https://books.toscrape.com");
            long loadTime = System.currentTimeMillis() - t0;

            Thread.sleep(1000);

            // Step 1: Page loaded
            System.out.println("\n" + "─".repeat(55));
            System.out.println("  STEP 1: Loaded in " + loadTime + "ms — images blocked!");
            System.out.println("─".repeat(55));

            // Show step banner in browser
            imageJs.executeScript(
                    "var old=document.getElementById('__step__');if(old)old.remove();" +
                            "var d=document.createElement('div');" +
                            "d.id='__step__';" +
                            "d.style.cssText='position:fixed;top:0;left:0;right:0;z-index:99999;" +
                            "background:#1a1a2e;color:#00d4aa;padding:10px 16px;" +
                            "font:bold 14px monospace;border-bottom:3px solid #e94560;';" +
                            "d.textContent='STEP 1: Loaded in " + loadTime + "ms — images blocked!';" +
                            "document.body.prepend(d);"
            );
            Thread.sleep(800);

            // Screenshot নাও — images ছাড়া
            File screenshot1 = ((TakesScreenshot) imageDriver).getScreenshotAs(OutputType.FILE);
            Files.copy(screenshot1.toPath(),
                    Paths.get("./test-screenshots/1E/01-no-images.png"),
                    StandardCopyOption.REPLACE_EXISTING);
            System.out.println("  📸 01-no-images.png saved!");

            // Step 2: Text content verify
            imageWait.until(ExpectedConditions.visibilityOfElementLocated(
                    By.cssSelector(".product_pod")));

            System.out.println("\n" + "─".repeat(55));
            System.out.println("  STEP 2: Text content ঠিকঠাক আছে!");
            System.out.println("─".repeat(55));

            // H1 highlight করো
            WebElement h1 = imageDriver.findElement(By.cssSelector("h1"));
            String originalStyle = h1.getAttribute("style");
            imageJs.executeScript(
                    "arguments[0].style.cssText += 'outline:4px solid #3498db!important;" +
                            "box-shadow:0 0 20px #3498db!important;'", h1);
            Thread.sleep(1500);
            imageJs.executeScript("arguments[0].style.cssText = arguments[1];",
                    h1, originalStyle != null ? originalStyle : "");

            // Screenshot 2
            File screenshot2 = ((TakesScreenshot) imageDriver).getScreenshotAs(OutputType.FILE);
            Files.copy(screenshot2.toPath(),
                    Paths.get("./test-screenshots/1E/02-text-ok.png"),
                    StandardCopyOption.REPLACE_EXISTING);
            System.out.println("  📸 02-text-ok.png saved!");

            // Image tag গুলো count করো — src আছে কিন্তু render হয়নি
            List<WebElement> imgTags = imageDriver.findElements(By.tagName("img"));
            System.out.println("\n  📊 Image tags found in HTML: " + imgTags.size());
            System.out.println("  🚫 But all images are BLOCKED by Chrome prefs!");
            System.out.println("  ✅ Load time: " + loadTime + "ms (faster without images)");

            // naturalWidth check করো — blocked images এর width 0 হয়
            int blockedImages = 0;
            for (WebElement img : imgTags) {
                try {
                    Long naturalWidth = (Long) imageJs.executeScript(
                            "return arguments[0].naturalWidth;", img);
                    if (naturalWidth != null && naturalWidth == 0) {
                        blockedImages++;
                    }
                } catch (Exception e) {
                    blockedImages++;
                }
            }
            System.out.println("  🚫 Confirmed blocked (naturalWidth=0): " + blockedImages);

            // Text content still accessible
            String h1Text = imageDriver.findElement(By.cssSelector("h1")).getText();
            Assert.assertFalse(h1Text.isEmpty(), "H1 text should be visible");
            System.out.println("  ✅ H1 text: \"" + h1Text + "\"");

            // Product pods visible
            List<WebElement> products = imageDriver.findElements(By.cssSelector(".product_pod"));
            Assert.assertFalse(products.isEmpty(), "Products should be visible");
            System.out.println("  ✅ Products visible: " + products.size());

            // Status banner
            imageJs.executeScript(
                    "var old=document.getElementById('pw-status-bar');if(old)old.remove();" +
                            "var bar=document.createElement('div');" +
                            "bar.id='pw-status-bar';" +
                            "bar.style.cssText='position:fixed;top:10px;left:50%;transform:translateX(-50%);" +
                            "background:#27ae60;color:white;padding:12px 24px;" +
                            "border-radius:8px;font:bold 15px monospace;z-index:999999;" +
                            "box-shadow:0 4px 20px rgba(0,0,0,0.3);';" +
                            "bar.textContent='✅ 1E PASSED! Images blocked, text OK!';" +
                            "document.body.appendChild(bar);"
            );
            Thread.sleep(2000);

            System.out.println("\n  ✅ 1E COMPLETE!\n");

        } finally {
            imageDriver.quit();
        }
    }
}