

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
import java.util.NoSuchElementException;



// ════════════════════════════════════════════════════════════════════════
// TOPIC 3: Infinite Scroll / Lazy Loading
// ════════════════════════════════════════════════════════════════════════

public class
InfiniteScroll_LazyLoading extends DriverManager {

    // ── 3A. BASIC INFINITE SCROLL (Books) ──────────────────────────────
    @Test
    public void test3A_BooksToscrape_CollectAllBooks() throws InterruptedException {
        System.out.println("\n━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━");
        System.out.println("📚 TEST 3A: Collect all books from books.toscrape.com");
        System.out.println("━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━");

        driver.get("https://books.toscrape.com");
        Thread.sleep(2000);

        Set<String> collectedBooks = new LinkedHashSet<>();
        int pageCount = 0;
        int maxPages = 20; // Safety limit

        while (pageCount < maxPages) {
            pageCount++;

            // Get all book titles on current page
            List<WebElement> bookElements = driver.findElements(By.cssSelector(".product_pod h3 a"));
            for (WebElement book : bookElements) {
                String title = book.getText().trim();
                if (!title.isEmpty()) {
                    collectedBooks.add(title);
                }
            }

            System.out.println("📚 Collected " + collectedBooks.size() + " books so far... (Page " + pageCount + ")");

            // Check if "next" button exists
            List<WebElement> nextButtons = driver.findElements(By.cssSelector("li.next a"));
            if (nextButtons.isEmpty()) {
                System.out.println("🏁 No more pages. All books collected!");
                break;
            }

            // Click next button
            highlightElement(nextButtons.get(0), "#f39c12");
            nextButtons.get(0).click();
            Thread.sleep(500);
            wait.until(ExpectedConditions.presenceOfElementLocated(By.cssSelector(".product_pod")));
        }

        System.out.println("✅ Site 3 Total books collected: " + collectedBooks.size());
        Assert.assertTrue(collectedBooks.size() > 20, "Should collect more than 20 books");

        // Display first 10 books
        System.out.println("\n📖 First 10 books:");
        int count = 0;
        for (String book : collectedBooks) {
            if (count++ >= 10) break;
            System.out.println("  " + count + ". " + book);
        }
    }

    // ── 3B. SCROLL UNTIL SPECIFIC ITEM FOUND ──────────────────────────
    @Test
    public void test3B_ScrollUntilTargetFound() throws InterruptedException {
        System.out.println("\n━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━");
        System.out.println("🎯 TEST 3B: Scroll until target item is found");
        System.out.println("━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━");

        driver.get("https://www.startech.com.bd/laptop-notebook");
        Thread.sleep(3000);

        String TARGET_TEXT = "Walton Prelude N41 Pro Celeron N4120 14\" FHD Laptop";
        boolean found = false;
        int scrollCount = 0;
        int MAX_SCROLLS = 20;

        while (!found && scrollCount < MAX_SCROLLS) {
            scrollCount++;

            // Check if target item exists using findElements (doesn't throw exception)
            List<WebElement> targetItems = driver.findElements(
                    By.xpath("//*[contains(text(), '" + TARGET_TEXT + "')]")
            );

            if (!targetItems.isEmpty()) {
                // Found it!
                WebElement targetItem = targetItems.get(0);
                scrollIntoView(targetItem);
                highlightWithLabel(targetItem, "#27ae60", "✅ TARGET FOUND!");
                System.out.println("✅ Found \"" + TARGET_TEXT + "\" after " + scrollCount + " scrolls!");
                found = true;
                break;
            } else {
                // Not found yet, scroll more
                System.out.println("  Scroll " + scrollCount + ": Target not found, scrolling...");
                scrollToBottom();
                Thread.sleep(1500); // Wait for content to load
            }
        }

        if (!found) {
            System.out.println("❌ \"" + TARGET_TEXT + "\" not found after " + MAX_SCROLLS + " scrolls");
        }

        Assert.assertTrue(found, "Target item should be found");
    }

    // ── 3C. LAZY LOADING IMAGES ────────────────────────────────────────
    @Test
    public void test3C_LazyLoadingImages_Pixabay() throws InterruptedException {
        System.out.println("\n━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━");
        System.out.println("🖼️ TEST 3C: Lazy loading images on Pixabay");
        System.out.println("━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━");

        driver.get("https://pixabay.com/images/search/nature/");
        Thread.sleep(3000);

        // Try to find lazy images
        List<WebElement> lazyImages = driver.findElements(By.cssSelector("img[loading=\"lazy\"]"));
        System.out.println("Total lazy images found: " + lazyImages.size());

        if (lazyImages.isEmpty()) {
            // Fallback: use regular images
            System.out.println("⚠️ No img[loading=\"lazy\"] found. Using fallback selector...");
            List<WebElement> allImages = driver.findElements(By.cssSelector(".results img, .media img"));
            System.out.println("Found " + allImages.size() + " images with fallback selector");

            int checked = Math.min(allImages.size(), 5);
            for (int i = 0; i < checked; i++) {
                WebElement img = allImages.get(i);
                scrollIntoView(img);
                Thread.sleep(500);

                Boolean isLoaded = (Boolean) js.executeScript(
                        "return arguments[0].complete && arguments[0].naturalWidth > 0;",
                        img
                );
                System.out.println("Image " + (i + 1) + ": " + (isLoaded ? "✅ Loaded" : "❌ Failed"));
            }
            return;
        }

        // Check lazy images
        int imageCount = lazyImages.size();
        int checked = Math.min(imageCount, 10);

        for (int i = 0; i < checked; i++) {
            WebElement img = lazyImages.get(i);
            scrollIntoView(img);
            Thread.sleep(500);

            Boolean isLoaded = (Boolean) js.executeScript(
                    "return arguments[0].complete && arguments[0].naturalWidth > 0;",
                    img
            );

            if (isLoaded) {
                System.out.println("✅ Image " + (i + 1) + ": Loaded successfully");
                highlightElement(img, "#27ae60");
            } else {
                System.out.println("❌ Image " + (i + 1) + ": Failed to load!");
                highlightElement(img, "#e74c3c");
            }

            // Verify src is not empty
            String src = img.getAttribute("src");
            Assert.assertNotNull(src);
            Assert.assertFalse(src.isEmpty(), "Image src should not be empty");
        }

        System.out.println("✅ 3C: Lazy loading image verification complete!");
    }

    // ── 3CA BEST: Self-hosted lazy image page ──────────────────────────
    @Test
    public void test3CA_Best_LazyImageSelfHosted() throws InterruptedException {
        System.out.println("\n━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━");
        System.out.println("🖼️ TEST 3CA BEST: Self-hosted lazy image page");
        System.out.println("━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━");

        // Create HTML content
        String htmlContent =
                "<!DOCTYPE html>\n" +
                        "<html>\n" +
                        "<head>\n" +
                        "  <style>\n" +
                        "    body { margin: 0; font-family: sans-serif; }\n" +
                        "    .container { max-width: 800px; margin: 0 auto; padding: 20px; }\n" +
                        "    img {\n" +
                        "      width: 100%;\n" +
                        "      height: 300px;\n" +
                        "      object-fit: cover;\n" +
                        "      display: block;\n" +
                        "      margin-bottom: 20px;\n" +
                        "      background: #eee;\n" +
                        "    }\n" +
                        "    .spacer { height: 100vh; background: #f0f0f0;\n" +
                        "              display: flex; align-items: center;\n" +
                        "              justify-content: center; font-size: 1.5rem; }\n" +
                        "  </style>\n" +
                        "</head>\n" +
                        "<body>\n" +
                        "  <div class=\"container\">\n" +
                        "    <h1>Lazy Loading Test Page</h1>\n" +
                        "    <img src=\"https://picsum.photos/800/300?random=1\" loading=\"eager\" alt=\"Eager Image 1\"/>\n" +
                        "    <div class=\"spacer\">⬇️ Scroll down to load lazy images</div>\n" +
                        "    <img src=\"https://picsum.photos/800/300?random=2\" loading=\"lazy\" alt=\"Lazy Image 1\"/>\n" +
                        "    <img src=\"https://picsum.photos/800/300?random=3\" loading=\"lazy\" alt=\"Lazy Image 2\"/>\n" +
                        "    <img src=\"https://picsum.photos/800/300?random=4\" loading=\"lazy\" alt=\"Lazy Image 3\"/>\n" +
                        "    <img src=\"https://picsum.photos/800/300?random=5\" loading=\"lazy\" alt=\"Lazy Image 4\"/>\n" +
                        "    <img src=\"https://picsum.photos/800/300?random=6\" loading=\"lazy\" alt=\"Lazy Image 5\"/>\n" +
                        "    <img src=\"https://picsum.photos/800/300?random=7\" loading=\"lazy\" alt=\"Lazy Image 6\"/>\n" +
                        "  </div>\n" +
                        "</body>\n" +
                        "</html>";

        // Save to temp file and load
        try {
            String tempFilePath = System.getProperty("java.io.tmpdir") + "/lazy_load_test.html";
            Files.write(Paths.get(tempFilePath), htmlContent.getBytes());
            driver.get("file://" + tempFilePath);
            Thread.sleep(2000);

            List<WebElement> lazyImages = driver.findElements(By.cssSelector("img[loading=\"lazy\"]"));
            System.out.println("Total lazy images: " + lazyImages.size());

            int imageCount = lazyImages.size();
            int checked = Math.min(imageCount, 10);

            for (int i = 0; i < checked; i++) {
                WebElement img = lazyImages.get(i);
                scrollIntoView(img);
                Thread.sleep(500);

                Boolean isLoaded = (Boolean) js.executeScript(
                        "return arguments[0].complete && arguments[0].naturalWidth > 0;",
                        img
                );

                if (isLoaded) {
                    System.out.println("✅ Image " + (i + 1) + ": Loaded successfully");
                    highlightElement(img, "#27ae60");
                } else {
                    System.out.println("❌ Image " + (i + 1) + ": Failed to load!");
                    highlightElement(img, "#e74c3c");
                }

                String src = img.getAttribute("src");
                Assert.assertNotNull(src);
                Assert.assertFalse(src.isEmpty(), "Image src should not be empty");
            }

            Assert.assertTrue(imageCount > 0, "Should have at least one lazy image");
            System.out.println("✅ 3C BEST: Lazy loading image verification complete!");

            // Clean up
            Files.deleteIfExists(Paths.get(tempFilePath));

        } catch (IOException e) {
            System.out.println("⚠️ Could not create temp file: " + e.getMessage());
        }
    }

    // ── 3D. IntersectionObserver + API CALL ON SCROLL ─────────────────
    @Test
    public void test3D_InfiniteScrollWithAPI() throws InterruptedException {
        System.out.println("\n━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━");
        System.out.println("🌐 TEST 3D: Infinite scroll + API call");
        System.out.println("━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━");

        // Create HTML with infinite scroll
        String htmlContent =
                "<!DOCTYPE html>\n" +
                        "<html>\n" +
                        "<head>\n" +
                        "  <style>\n" +
                        "    body { font-family: Arial; margin: 0; background: #f5f5f5; }\n" +
                        "    .container { max-width: 700px; margin: auto; padding: 20px; }\n" +
                        "    .post-item {\n" +
                        "      background: white;\n" +
                        "      margin-bottom: 16px;\n" +
                        "      padding: 18px;\n" +
                        "      border-radius: 10px;\n" +
                        "      border: 1px solid #ddd;\n" +
                        "    }\n" +
                        "    .post-item h3 { margin: 0 0 10px; }\n" +
                        "    #loader { text-align: center; padding: 20px; font-size: 22px; }\n" +
                        "    #sentinel { height: 40px; }\n" +
                        "  </style>\n" +
                        "</head>\n" +
                        "<body>\n" +
                        "  <div class=\"container\">\n" +
                        "    <h1>Infinite Feed Demo</h1>\n" +
                        "    <div id=\"feed\"></div>\n" +
                        "    <div id=\"loader\">⏳ Loading...</div>\n" +
                        "    <div id=\"sentinel\"></div>\n" +
                        "  </div>\n" +
                        "  <script>\n" +
                        "    let currentPage = 1;\n" +
                        "    let loading = false;\n" +
                        "    async function loadPosts() {\n" +
                        "      if (loading) return;\n" +
                        "      loading = true;\n" +
                        "      const offset = (currentPage - 1) * 5;\n" +
                        "      const res = await fetch(\n" +
                        "        'https://jsonplaceholder.typicode.com/posts?_start=' +\n" +
                        "        offset + '&_limit=5'\n" +
                        "      );\n" +
                        "      const posts = await res.json();\n" +
                        "      const feed = document.getElementById('feed');\n" +
                        "      posts.forEach((post) => {\n" +
                        "        const div = document.createElement('div');\n" +
                        "        div.className = 'post-item';\n" +
                        "        div.innerHTML = '<h3>' + post.title + '</h3><p>' + post.body + '</p>';\n" +
                        "        feed.appendChild(div);\n" +
                        "      });\n" +
                        "      currentPage++;\n" +
                        "      loading = false;\n" +
                        "    }\n" +
                        "    const observer = new IntersectionObserver((entries) => {\n" +
                        "      if (entries[0].isIntersecting) { loadPosts(); }\n" +
                        "    });\n" +
                        "    observer.observe(document.getElementById('sentinel'));\n" +
                        "    loadPosts();\n" +
                        "  </script>\n" +
                        "</body>\n" +
                        "</html>";

        try {
            String tempFilePath = System.getProperty("java.io.tmpdir") + "/infinite_scroll_test.html";
            Files.write(Paths.get(tempFilePath), htmlContent.getBytes());
            driver.get("file://" + tempFilePath);
            Thread.sleep(3000);

            // Wait for initial posts
            wait.until(ExpectedConditions.presenceOfElementLocated(By.className("post-item")));
            List<WebElement> initialPosts = driver.findElements(By.className("post-item"));
            int initialCount = initialPosts.size();
            System.out.println("Initial posts: " + initialCount);

            // Highlight first post
            if (initialCount > 0) {
                highlightWithLabel(initialPosts.get(0), "#27ae60", "✅ Initial Posts Loaded");
            }

            // Scroll to trigger more loading
            System.out.println("⬇️ Scrolling to trigger API...");
            scrollToBottom();
            Thread.sleep(3000);

            // Wait for new posts
            wait.until(ExpectedConditions.presenceOfAllElementsLocatedBy(By.className("post-item")));
            List<WebElement> newPosts = driver.findElements(By.className("post-item"));
            int newCount = newPosts.size();
            System.out.println("Posts after scroll: " + newCount);

            // Highlight new post
            if (newCount > initialCount) {
                WebElement newPost = newPosts.get(newCount - 1);
                highlightWithLabel(newPost, "#f39c12", "🔥 New Posts Loaded After Scroll");
            }

            Assert.assertTrue(newCount > initialCount, "New posts should load after scrolling");
            System.out.println("✅ New posts loaded successfully");
            System.out.println("✅ Total posts: " + newCount);

            // Clean up
            Files.deleteIfExists(Paths.get(tempFilePath));

        } catch (IOException e) {
            System.out.println("⚠️ Could not create temp file: " + e.getMessage());
        }
    }

    // ── EXTRA: Test all 3A, 3B, 3C together ───────────────────────────
    @Test
    public void testAll_InfiniteScroll_Combined() throws InterruptedException {
        System.out.println("\n═══════════════════════════════════════════════════════════");
        System.out.println("🚀 RUNNING ALL INFINITE SCROLL TESTS");
        System.out.println("═══════════════════════════════════════════════════════════");

        test3A_BooksToscrape_CollectAllBooks();
        Thread.sleep(1000);

        test3B_ScrollUntilTargetFound();
        Thread.sleep(1000);

        test3C_LazyLoadingImages_Pixabay();
        Thread.sleep(1000);

        test3CA_Best_LazyImageSelfHosted();
        Thread.sleep(1000);

        test3D_InfiniteScrollWithAPI();

        System.out.println("\n═══════════════════════════════════════════════════════════");
        System.out.println("✅ ALL INFINITE SCROLL TESTS COMPLETED!");
        System.out.println("═══════════════════════════════════════════════════════════");
    }
}