package org.example;
import io.github.bonigarcia.wdm.WebDriverManager;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;

public class TockScraper {
    private WebDriver driver;
    private static final String TOCK_URL = "https://www.exploretock.com/tsukeedomae";

    public void setupDriver() {
        WebDriverManager.chromedriver().setup();
        ChromeOptions options = new ChromeOptions();
        driver = new ChromeDriver(options);

        System.out.println("Chrome Driver Initialized");
    }

    public void loadPage() {
        System.out.println("Loading page: " + TOCK_URL);
        driver.get(TOCK_URL);
        System.out.println("Page Loaded");
    }

    public void closePage() {
        System.out.println("Closing Page...");
        driver.quit();
        System.out.println("Page Closed");
    }

    public static void main(String[] args) {
        TockScraper scraper = new TockScraper();

        try {
            scraper.setupDriver();
            scraper.loadPage();

            Thread.sleep(5000);
        } catch (Exception e) {
            System.out.println("Error: " + e.getMessage());
            e.printStackTrace();
        } finally {
            scraper.closePage();
        }
    }
}
