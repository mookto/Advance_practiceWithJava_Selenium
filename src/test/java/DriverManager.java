import io.github.bonigarcia.wdm.WebDriverManager;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.chrome.ChromeDriver;

public class DriverManager {

    // একটা প্রাইভেট static WebDriver — শুধু এটাই থাকবে সারা প্রজেক্টে
    private static WebDriver driver;

    // প্রাইভেট কনস্ট্রাক্টর — কেউ new DriverManager() করতে পারবে না
    private DriverManager() {
        // এখানে কিছু করার দরকার নেই
    }

    // এই মেথড দিয়ে driver পাওয়া যাবে (একবার তৈরি হলে আর তৈরি হবে না)
    public static WebDriver getDriver() {
        if (driver == null) {
            // WebDriverManager দিয়ে ChromeDriver অটো সেটআপ
            WebDriverManager.chromedriver().setup();

            // Chrome ব্রাউজার খোলা
            driver = new ChromeDriver();

            // ব্রাউজারকে ম্যাক্সিমাইজ করা
            driver.manage().window().maximize();
        }
        return driver;
    }

    // টেস্ট শেষে ব্রাউজার বন্ধ করার জন্য
    public static void quitDriver() {
        if (driver != null) {
            driver.quit();
            driver = null; // পরের টেস্টে নতুন করে খুলবে
        }
    }
}