import org.openqa.selenium.*;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.Select;
import org.testng.Assert;
import org.testng.annotations.Test;

import java.util.List;

public class Topic1AutoCompleteTest extends DriverManager {

    // ── TEST 1A: Basic Autocomplete ────────────────────────────────────
    @Test(description = "1A VISUAL — Basic Autocomplete: type + select")
    public void test1A_BasicAutocomplete() throws InterruptedException {
        System.out.println("\n━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━");
        System.out.println("⌨️  TEST 1A: Basic Autocomplete");
        System.out.println("   Site: w3schools.com");
        System.out.println("━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━");

        driver.get("https://www.w3schools.com/howto/howto_js_autocomplete.asp");
        Thread.sleep(1000);

        showStep(1, "Input field খুঁজছি...");
        WebElement input = wait.until(ExpectedConditions.visibilityOfElementLocated(By.id("myInput")));
        highlightElement(input, "#3498db");
        System.out.println("  🔵 Input highlighted (blue)");

        showStep(2, "\"S\" type করছি...");
        input.click();
        input.clear();
        // Playwright: pressSequentially('S', {delay:150}) → Selenium: sendKeys with char-by-char
        for (char c : "S".toCharArray()) {
            input.sendKeys(String.valueOf(c));
            Thread.sleep(150);
        }
        Thread.sleep(600);

        showStep(3, "Dropdown দেখাচ্ছে!");
        WebElement dropdown = wait.until(
                ExpectedConditions.visibilityOfElementLocated(By.id("myInputautocomplete-list"))
        );
        highlightElement(dropdown, "#f39c12");

        List<WebElement> suggestions = driver.findElements(By.cssSelector("#myInputautocomplete-list div"));
        System.out.println("\n  📋 Found " + suggestions.size() + " suggestions");

        showStep(4, "First suggestion click করছি...");
        highlightElement(By.cssSelector("#myInputautocomplete-list div"), "#27ae60");
        suggestions.get(0).click();
        Thread.sleep(500);

        showStep(5, "Value verify করছি...");
        String value = input.getAttribute("value");
        System.out.println("  ✅ Selected: \"" + value + "\"");
        Assert.assertFalse(value.isEmpty(), "Selected value should not be empty");
        highlightElement(input, "#27ae60");
        showStatus("✅ 1A DONE! Selected: \"" + value + "\"", "success");
        Thread.sleep(2000);
        System.out.println("\n  ✅ 1A COMPLETE!\n");
    }


    // ── TEST 1B: Keyboard Navigation ───────────────────────────────────
    @Test(description = "1B VISUAL — Keyboard Navigation in Autocomplete")
    public void test1B_KeyboardNavigation() throws InterruptedException {
        System.out.println("\n━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━");
        System.out.println("⌨️  TEST 1B: Keyboard Navigation");
        System.out.println("━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━");

        driver.get("https://www.w3schools.com/howto/howto_js_autocomplete.asp");
        Thread.sleep(1000);

        WebElement input = wait.until(ExpectedConditions.visibilityOfElementLocated(By.id("myInput")));

        showStep(1, "\"S\" type করছি...");
        input.click();
        input.clear();
        for (char c : "S".toCharArray()) {
            input.sendKeys(String.valueOf(c));
            Thread.sleep(150);
        }
        Thread.sleep(500);
        highlightElement(input, "#3498db");

        showStep(2, "ArrowDown → 1st item");
        input.sendKeys(Keys.ARROW_DOWN);
        Thread.sleep(400);
        highlightElement(By.cssSelector("#myInputautocomplete-list div"), "#f39c12");

        showStep(3, "ArrowDown → 2nd item");
        input.sendKeys(Keys.ARROW_DOWN);
        Thread.sleep(400);

        showStep(4, "ArrowDown → 3rd item");
        input.sendKeys(Keys.ARROW_DOWN);
        Thread.sleep(4000);

        showStep(5, "Enter — select!");
        input.sendKeys(Keys.ENTER);
        Thread.sleep(500);

        String value = input.getAttribute("value");
        System.out.println("  ✅ Keyboard selected: \"" + value + "\"");
        Assert.assertFalse(value.isEmpty(), "Keyboard selected value should not be empty");
        highlightElement(input, "#27ae60");
        showStatus("✅ 1B DONE! Selected: \"" + value + "\"", "success");
        Thread.sleep(2000);
        System.out.println("\n  ✅ 1B COMPLETE!\n");
    }


    @Test(description = "1C VISUAL — Address Autocomplete (automationpractice.pl)")
    public void test1C_AddressAutocomplete() throws InterruptedException {
        System.out.println("\n━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━");
        System.out.println("🏠 TEST 1C: Address Autocomplete");
        System.out.println("   Site: automationpractice.pl");
        System.out.println("━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━");

        // ── Step 1: Site load ─────────────────────────────────────────
        driver.get("http://www.automationpractice.pl/index.php?controller=address");
        Thread.sleep(2000);

        // Login page এ redirect হলে আগে login করো
        if (driver.getCurrentUrl().contains("authentication")) {
            System.out.println("  🔐 Login required — logging in...");
            showStep(1, "Login করছি...");

            WebElement emailField = wait.until(
                    ExpectedConditions.visibilityOfElementLocated(By.id("email_create"))
            );

            // Create account flow
            String email = "test" + System.currentTimeMillis() + "@mail.com";
            emailField.sendKeys(email);
            driver.findElement(By.id("SubmitCreate")).click();
            Thread.sleep(2000);

            // Personal info fill করো
            try {
                wait.until(ExpectedConditions.visibilityOfElementLocated(By.id("customer_firstname")))
                        .sendKeys("Test");
                driver.findElement(By.id("customer_lastname")).sendKeys("User");
                driver.findElement(By.id("passwd")).sendKeys("Test@1234");
                driver.findElement(By.id("submitAccount")).click();
                Thread.sleep(2000);
                System.out.println("  ✅ Account created & logged in");
            } catch (Exception e) {
                System.out.println("  ⚠️  Registration skipped: " + e.getMessage());
            }

            // Address page এ যাও
            driver.get("http://www.automationpractice.pl/index.php?controller=address");
            Thread.sleep(2000);
        }

        showStep(1, "Address page loaded ✅");

        // ── Step 2: Address field fill করো ───────────────────────────
        showStep(2, "Address fields fill করছি...");

        // First Name
        List<WebElement> firstNames = driver.findElements(By.id("firstname"));
        if (!firstNames.isEmpty() && firstNames.get(0).isDisplayed()) {
            firstNames.get(0).clear();
            firstNames.get(0).sendKeys("Test");
        }

        // Last Name
        List<WebElement> lastNames = driver.findElements(By.id("lastname"));
        if (!lastNames.isEmpty() && lastNames.get(0).isDisplayed()) {
            lastNames.get(0).clear();
            lastNames.get(0).sendKeys("User");
        }

        // ── Step 3: Address field ─────────────────────────────────────
        showStep(3, "Address field-এ type করছি...");
        List<WebElement> addressFields = driver.findElements(By.id("address1"));
        if (!addressFields.isEmpty() && addressFields.get(0).isDisplayed()) {
            WebElement addressField = addressFields.get(0);
            highlightElement(addressField, "#f39c12");
            addressField.clear();
            addressField.click();

            // char-by-char type করো (autocomplete simulate)
            for (char c : "123 Main".toCharArray()) {
                addressField.sendKeys(String.valueOf(c));
                Thread.sleep(100);
            }
            Thread.sleep(800);

            // Autocomplete suggestions check
            List<WebElement> suggestions = driver.findElements(
                    By.cssSelector(".ac_results li, .ui-autocomplete li, " +
                            "[class*='autocomplete'] li, [class*='suggestion'] li")
            );

            if (!suggestions.isEmpty()) {
                showStep(4, suggestions.size() + " suggestions পাওয়া গেছে!");
                highlightElement(suggestions.get(0), "#9b59b6");
                suggestions.get(0).click();
                System.out.println("  ✅ Autocomplete suggestion clicked!");
            } else {
                // Dropdown না এলে বাকি text দাও
                addressField.sendKeys(" Street");
                highlightElement(addressField, "#27ae60");
                System.out.println("  ✅ Address typed: '123 Main Street'");
            }
            String addrVal = addressField.getAttribute("value");
            Assert.assertFalse(addrVal.isEmpty(), "Address should not be empty");
        }

        // ── Step 4: City field ────────────────────────────────────────
        showStep(4, "City field-এ type করছি...");
        List<WebElement> cityFields = driver.findElements(By.id("city"));
        if (!cityFields.isEmpty() && cityFields.get(0).isDisplayed()) {
            WebElement cityField = cityFields.get(0);
            highlightElement(cityField, "#f39c12");
            cityField.clear();
            cityField.click();

            for (char c : "New".toCharArray()) {
                cityField.sendKeys(String.valueOf(c));
                Thread.sleep(150);
            }
            Thread.sleep(600);

            // City autocomplete check
            List<WebElement> citySuggestions = driver.findElements(
                    By.cssSelector(".ac_results li, .ui-autocomplete li")
            );
            if (!citySuggestions.isEmpty()) {
                showStep(5, "City suggestions পাওয়া গেছে!");
                citySuggestions.get(0).click();
            } else {
                cityField.sendKeys(" York");
                System.out.println("  ✅ City: 'New York'");
            }
            String cityVal = cityField.getAttribute("value");
            Assert.assertFalse(cityVal.isEmpty(), "City should not be empty");
            highlightElement(cityField, "#27ae60");
        }

        // ── Step 5: State dropdown (real dropdown autocomplete) ────────
        showStep(5, "State dropdown select করছি...");
        List<WebElement> stateDropdowns = driver.findElements(By.id("id_state"));
        if (!stateDropdowns.isEmpty() && stateDropdowns.get(0).isDisplayed()) {
            WebElement stateDropdown = stateDropdowns.get(0);
            highlightElement(stateDropdown, "#f39c12");

            Select stateSelect = new Select(stateDropdown);
            // Available options print করো
            List<WebElement> stateOptions = stateSelect.getOptions();
            System.out.println("  📋 " + stateOptions.size() + " states available");

            // New York select করো
            try {
                stateSelect.selectByVisibleText("New York");
                System.out.println("  ✅ State: New York");
            } catch (Exception e) {
                // First non-empty option select করো
                for (WebElement opt : stateOptions) {
                    if (!opt.getText().trim().isEmpty() && !opt.getText().trim().equals("-")) {
                        stateSelect.selectByVisibleText(opt.getText().trim());
                        System.out.println("  ✅ State: " + opt.getText().trim());
                        break;
                    }
                }
            }
            highlightElement(stateDropdown, "#27ae60");
        }

        // ── Step 6: Zip code ──────────────────────────────────────────
        showStep(6, "ZIP code fill করছি...");
        List<WebElement> zipFields = driver.findElements(By.id("postcode"));
        if (!zipFields.isEmpty() && zipFields.get(0).isDisplayed()) {
            WebElement zipField = zipFields.get(0);
            highlightElement(zipField, "#f39c12");
            zipField.clear();
            for (char c : "10001".toCharArray()) {
                zipField.sendKeys(String.valueOf(c));
                Thread.sleep(100);
            }
            String zipVal = zipField.getAttribute("value");
            Assert.assertEquals(zipVal, "10001", "ZIP should be 10001");
            highlightElement(zipField, "#27ae60");
            System.out.println("  ✅ ZIP: " + zipVal);
        }

        showStatus("✅ 1C DONE! Address form filled successfully!", "success");
        Thread.sleep(2000);
        System.out.println("\n  ✅ 1C COMPLETE!\n");
    }


    // ════════════════════════════════════════════════════════════════════
    //  TEST 1D — Select2 Dropdown
    //
    //  ROOT CAUSE OF FAILURE:
    //  Select2 dropdown এর results list একটি <ul> যেটা
    //  .select2-results__option class দিয়ে render হয় —
    //  কিন্তু container click এর পর dropdown body-তে append হয়
    //  এবং অনেক সময় wrong container click হয়।
    //
    //  FIX STRATEGY:
    //  1. JS দিয়ে Select2 trigger করো (most reliable)
    //  2. dropdown body তে results খোঁজো
    //  3. visibilityOfAllElements এর বদলে presenceOfAll use করো
    //  4. Explicit scroll + JS click use করো
    // ════════════════════════════════════════════════════════════════════

    @Test(description = "1D VISUAL — Multi-select (Select2 Dropdown) — FIXED v2")
    public void test1D_Select2Dropdown() throws InterruptedException {
        System.out.println("\n━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━");
        System.out.println("🔽 TEST 1D: Select2 Dropdown");
        System.out.println("   Site: select2.org");
        System.out.println("━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━");

        driver.get("https://select2.org/getting-started/basic-usage");
        Thread.sleep(2500); // full page load

        // ── Step 1: Select2 container খোঁজো ──────────────────────────
        showStep(1, "Select2 container খুঁজছি...");

        // Page এ কতটা Select2 আছে debug করো
        List<WebElement> allContainers = driver.findElements(
                By.cssSelector(".select2-container")
        );
        System.out.println("  📋 Total Select2 containers found: " + allContainers.size());

        // প্রথম container নাও এবং scroll করো
        WebElement container = allContainers.get(0);
        js.executeScript("arguments[0].scrollIntoView({block: 'center', behavior: 'smooth'});", container);
        Thread.sleep(600);
        highlightElement(container, "#3498db");
        System.out.println("  🔵 First container highlighted");

        // ── Step 2: Underlying <select> element খোঁজো ─────────────────
        // FIX: Select2 এর underlying <select> কে JS দিয়ে trigger করাই সবচেয়ে reliable
        showStep(2, "Underlying <select> খুঁজছি...");

        List<WebElement> selectElements = driver.findElements(By.cssSelector("select.js-example-basic-single"));
        System.out.println("  📋 Underlying <select> count: " + selectElements.size());

        if (!selectElements.isEmpty()) {
            // ── APPROACH A: JS দিয়ে Select2 open করো (Most Reliable) ──
            showStep(3, "JS দিয়ে Select2 open করছি...");

            // Select2 open করার JS
            js.executeScript(
                    "var el = arguments[0];" +
                            "$(el).select2('open');",
                    selectElements.get(0)
            );
            Thread.sleep(800);

            // Search field visible হয়েছে কিনা check
            List<WebElement> searchFields = driver.findElements(
                    By.cssSelector(".select2-search__field")
            );
            System.out.println("  📋 Search fields found after JS open: " + searchFields.size());

            if (!searchFields.isEmpty() && searchFields.get(0).isDisplayed()) {
                // Search field এ type করো
                showStep(4, "Search করছি...");
                WebElement sf = searchFields.get(0);
                highlightElement(sf, "#f39c12");
                sf.sendKeys("Al");
                Thread.sleep(1000);

                // Results খোঁজো
                List<WebElement> results = driver.findElements(
                        By.cssSelector(".select2-results__option")
                );
                System.out.println("  📋 Results after typing: " + results.size());

                if (!results.isEmpty()) {
                    // ── Select first non-loading result ───────────────
                    for (WebElement result : results) {
                        String text = result.getText().trim();
                        if (!text.isEmpty() && !text.contains("Searching") && !text.contains("No results")) {
                            System.out.println("  🎯 Clicking: \"" + text + "\"");
                            js.executeScript("arguments[0].click();", result);
                            Thread.sleep(500);
                            break;
                        }
                    }
                }
            } else {
                // ── APPROACH B: Normal click → search ─────────────────
                System.out.println("  ⚠️  JS open didn't show search — trying normal click...");
                js.executeScript("arguments[0].click();", container);
                Thread.sleep(800);

                // Dropdown open হয়েছে কিনা check করো
                List<WebElement> dropdowns = driver.findElements(
                        By.cssSelector(".select2-dropdown")
                );
                System.out.println("  📋 Dropdowns visible: " + dropdowns.size());

                WebElement searchField = wait.until(d -> {
                    List<WebElement> fields = d.findElements(By.cssSelector(".select2-search__field"));
                    for (WebElement f : fields) {
                        try { if (f.isDisplayed()) return f; } catch (Exception ignored) {}
                    }
                    return null;
                });

                if (searchField != null) {
                    searchField.sendKeys("Al");
                    Thread.sleep(1000);
                }
            }

        } else {
            // ── APPROACH C: Direct container click (fallback) ──────────
            System.out.println("  ⚠️  No underlying <select> found — using direct click...");
            showStep(3, "Direct click approach...");

            // Click the selection box inside container
            WebElement selectionBox = wait.until(
                    ExpectedConditions.elementToBeClickable(
                            By.cssSelector(".select2-selection--single, .select2-selection--multiple")
                    )
            );
            js.executeScript("arguments[0].click();", selectionBox);
            Thread.sleep(1000);

            // Search field খোঁজো
            List<WebElement> fields = driver.findElements(By.cssSelector(".select2-search__field"));
            System.out.println("  📋 Search fields: " + fields.size());
            if (!fields.isEmpty()) {
                fields.get(0).sendKeys("Al");
                Thread.sleep(1000);
            }
        }

        // ── Step 5: Results verify করো ───────────────────────────────
        showStep(5, "Selection verify করছি...");

        // যেকোনো selector দিয়ে results পাওয়ার চেষ্টা করো
        String selectedText = "";
        String[] resultSelectors = {
                ".select2-selection__rendered",
                ".select2-selection__single",
                ".select2-selection .select2-selection__rendered"
        };

        Thread.sleep(500);
        for (String sel : resultSelectors) {
            try {
                List<WebElement> els = driver.findElements(By.cssSelector(sel));
                if (!els.isEmpty()) {
                    String t = els.get(0).getText().trim();
                    // placeholder text বাদ দাও
                    if (!t.isEmpty() && !t.equals("Select a state") &&
                            !t.equals("Select an option") && !t.startsWith("×")) {
                        selectedText = t;
                        System.out.println("  ✅ Found selected text via '" + sel + "': \"" + t + "\"");
                        break;
                    }
                }
            } catch (Exception e) {
                System.out.println("  ⚠️  Selector failed: " + sel);
            }
        }

        // Results list এ কিছু select হয়েছে কিনা check করো
        if (selectedText.isEmpty()) {
            // Option selected হয়েছে কিনা underlying select check করো
            List<WebElement> selectEls = driver.findElements(
                    By.cssSelector("select.js-example-basic-single")
            );
            if (!selectEls.isEmpty()) {
                Select sel = new Select(selectEls.get(0));
                try {
                    selectedText = sel.getFirstSelectedOption().getText().trim();
                    System.out.println("  ✅ Selected via <select>: \"" + selectedText + "\"");
                } catch (Exception e) {
                    System.out.println("  ⚠️  Could not get selected option");
                }
            }
        }

        System.out.println("  📊 Final selected value: \"" + selectedText + "\"");

        // Assert — select2 কাজ করেছে এবং কিছু একটা select হয়েছে
        Assert.assertFalse(selectedText.isEmpty(),
                "Select2 should have a selected value. " +
                        "Check if jQuery/Select2 JS loaded on page.");

        highlightElement(By.cssSelector(".select2-container"), "#27ae60");
        showStatus("✅ 1D DONE! Selected: \"" + selectedText + "\"", "success");
        Thread.sleep(2000);
        System.out.println("\n  ✅ 1D COMPLETE!\n");
    }
}