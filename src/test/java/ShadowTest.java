import org.openqa.selenium.*;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.openqa.selenium.interactions.Actions;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.Select;
import org.openqa.selenium.support.ui.WebDriverWait;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import java.time.Duration;
import java.util.HashMap;
import java.util.List;

public class ShadowTest {

    private WebDriver driver;

    @BeforeMethod
    public void setUp() {
        // Singleton থেকে driver নেওয়া
        driver = DriverManager.getDriver();
    }

    @Test
    public void shadowDom() throws InterruptedException {
        driver.get("https://selectorshub.com/xpath-practice-page/");
        Thread.sleep(1000);
        JavascriptExecutor js = (JavascriptExecutor) driver;

        // Scroll down by 500 pixels (positive Y value)
        js.executeScript("window.scrollBy(0, 2000);");
        Thread.sleep(2000);

// disabled element checking
        WebElement f2 = driver.findElement(By.xpath("//input[@placeholder='Enter Last name']"));
// Forcefully fill korle JavascriptExecutor bebohar korte hoy
        JavascriptExecutor js2 = (JavascriptExecutor) driver;
        js2.executeScript("arguments[0].setAttribute('value', 'Forced Text')", f2);
        Thread.sleep(2000);


        //dropdown checking

        WebElement carDropdown = driver.findElement(By.xpath("//select[@id='cars']")); // Assuming id="cars" from image
        Select select = new Select(carDropdown);

// 3 bhabe select kora jay
        // select.selectByVisibleText("Opel");
        select.selectByValue("opel");
        //select.selectByIndex(2);
        Thread.sleep(2000);


        WebElement datePicker = driver.findElement(By.xpath("//input[@type='date']"));
// Standard format format provide korte hoy
        datePicker.sendKeys("12102026"); // MM-DD-YYYY pattern

        Thread.sleep(2000);

        js.executeScript("window.scrollBy(0, 1000);");
        Thread.sleep(2000);

//
        // Use WebDriverWait to wait for the element to be clickable
        WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(10));

// Improved XPath using contains to handle potential whitespace issues
        WebElement downloadbtn = wait.until(ExpectedConditions.elementToBeClickable(
                By.xpath("//a[contains(text(),'Click to Download PNG File')]")
        ));

        downloadbtn.click();
//--------- Upload File -----------------------
        WebElement uploadElement = driver.findElement(By.id("myFile"));
        uploadElement.sendKeys("C:\\Users\\mahed\\Desktop\\DUA\\99 names.jpg");
        Thread.sleep(2000);

// Alert
        driver.findElement(By.xpath("//button[text()='Click To Open Window Alert']")).click();
        Alert alert = driver.switchTo().alert();
        alert.accept();

// Prompt
        // 1. Click to trigger prompt
        driver.findElement(By.xpath("//button[contains(text(),'Window Prompt Alert')]")).click();

// 2. Wait for the alert to be present
        wait.until(ExpectedConditions.alertIsPresent());

        //3. Switch and send keys
        Alert prompt = driver.switchTo().alert();
        prompt.sendKeys("Gemini User"); // Note: You might not see this text in the UI
        Thread.sleep(20000);
        prompt.accept();


//------------- Bottom Modal -------------------

        WebDriverWait wait2 = new WebDriverWait(driver, Duration.ofSeconds(10));

        WebElement modal = wait2.until(ExpectedConditions.visibilityOfElementLocated(By.xpath("//button[@id='myBtn']")));
        modal.click();
        Thread.sleep(2000);
        modal.findElement(By.xpath("//span[@class='close']")).click();
/////---------------------- Pagination looop -----------------///////////
        js.executeScript("window.scrollBy(0, 1000);");
        Thread.sleep(2000);

        WebDriverWait wait10 = new WebDriverWait(driver, Duration.ofSeconds(10));

        while (true) {
            // 1. Perform your actions on the current page (e.g., scrape table data)
            System.out.println("Processing current page...");

            // 2. Locate the 'Next' button using the aria-label or class shown in the image
            List<WebElement> nextButton = driver.findElements(By.xpath("//button[@aria-label='Next']"));

            // 3. Check if the button exists and is not disabled
            if (!nextButton.isEmpty() && nextButton.get(0).isEnabled() &&
                    !nextButton.get(0).getAttribute("class").contains("disabled")) {

                // Scroll to the button to ensure it's clickable
                ((JavascriptExecutor) driver).executeScript("arguments[0].scrollIntoView(true);", nextButton.get(0));

                nextButton.get(0).click();

                // Wait for the table to refresh or for the next page's specific element
                wait10.until(ExpectedConditions.stalenessOf(nextButton.get(0)));
            } else {
                System.out.println("Reached the last page.");
                break; // Exit the loop
            }
        }
    }
    @AfterMethod
    public void tearDown() {
        // সব টেস্ট শেষ হলে ব্রাউজার বন্ধ
        DriverManager.quitDriver();
    }
}