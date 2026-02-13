package org.example;
import io.github.bonigarcia.wdm.WebDriverManager;
import java.util.List;
import java.util.Set;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;

public class TockScraper {
    private WebDriver driver;
    private static final String TOCK_URL = "https://www.exploretock.com/wanderlustwinecollective/";

    public void setupDriver() {
        WebDriverManager.chromedriver().setup();
        ChromeOptions options = new ChromeOptions();

        // Add these options to avoid Cloudflare detection
        options.addArguments("--disable-blink-features=AutomationControlled");
        options.setExperimentalOption("excludeSwitches", new String[]{"enable-automation"});
        options.setExperimentalOption("useAutomationExtension", false);

        driver = new ChromeDriver(options);

        // Remove the webdriver property that Cloudflare checks
        ((org.openqa.selenium.JavascriptExecutor) driver).executeScript(
                "Object.defineProperty(navigator, 'webdriver', {get: () => undefined})"
        );

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

    public void checkAndBook() {
        try {
            Thread.sleep(3000);

            List<WebElement> bookButtons = driver.findElements(
                    By.cssSelector("a[data-testid*='offering-book-button']")
            );

            if (bookButtons.size() > 0) {
                System.out.println("Found " + bookButtons.size() + " buttons");
                System.out.println("Checking first...");
                bookButtons.get(0).click();

                Thread.sleep(3000);
            } else {
                System.out.println("No buttons founds");
            }
        } catch (Exception e) {
            System.out.println("Error: " + e.getMessage());
            e.printStackTrace();
        }
    }

    public void savePageSource(String filename) {
        try {
            String html = driver.getPageSource();

            java.nio.file.Files.write(
                    java.nio.file.Paths.get(filename),
                    html.getBytes()
            );

            System.out.println("Saved HTML to: " + filename);

        } catch (Exception e) {
            System.out.println("Error saving HTML: " + e.getMessage());
        }
    }

    public void navigateToMonth(String targetMonth) {
        try {
            System.out.println("Navigating to " + targetMonth + "...");

            int maxClicks = 12;
            int clicks = 0;

            while (clicks < maxClicks) {
                WebElement monthHeading = driver.findElement(
                        By.cssSelector("div[data-testid='month-heading_calendar-first']")
                );
                String currentMonth = monthHeading.getText();
                System.out.println("Current month: " + currentMonth);

                if (currentMonth.contains(targetMonth)) {
                    System.out.println("Reached " + targetMonth);
                    return;
                }

                // Click next month button
                WebElement nextButton = driver.findElement(
                        By.cssSelector("button[data-testid='calendar-next-button_calendar-first']")
                );

                ((org.openqa.selenium.JavascriptExecutor) driver).executeScript(
                    "arguments[0].click();", nextButton
                );
                Thread.sleep(500); // Wait for month to load
                clicks++;
            }

            System.out.println("Could not reach " + targetMonth + " after " + maxClicks + " clicks");

        } catch (Exception e) {
            System.out.println("Error navigating to month: " + e.getMessage());
        }
    }

    public String findFirstAvailableDate() {
        try {
            System.out.println("Searching for first available date from September 2026 onwards...");

            // Navigate to September first
            navigateToMonth("September 2026");
            Thread.sleep(1000);

            // Now check September through December
            String[] monthsToCheck = {"September 2026", "October 2026", "November 2026", "December 2026"};

            for (String month : monthsToCheck) {
                // Make sure we're on the right month
                navigateToMonth(month);
                Thread.sleep(1000);

                // Find all day buttons that are NOT disabled
                List<WebElement> availableDays = driver.findElements(
                        By.cssSelector("button[data-testid*='consumer-calendar-day']:not([disabled])")
                );

                System.out.println("Found " + availableDays.size() + " available dates visible");

                // Filter by actual date
                for (WebElement day : availableDays) {
                    String dateLabel = day.getAttribute("aria-label");

                    // Parse the date (format: 2026-12-31)
                    if (dateLabel != null && isDateInRange(dateLabel)) {
                        System.out.println("Available in range: " + dateLabel);
                        return dateLabel;
                    }
                }
            }

            System.out.println("No available dates found from September-December 2026");
            return null;

        } catch (Exception e) {
            System.out.println("Error finding available date: " + e.getMessage());
            e.printStackTrace();
            return null;
        }
    }

    public void clickDate(String dateStr) {
        try {
            System.out.println("Attempting to click date: " + dateStr);

            // Find the button with this aria-label
            WebElement dateButton = driver.findElement(
                    By.cssSelector("button[aria-label='" + dateStr + "']")
            );

            // Use JavaScript click (more reliable)
            ((org.openqa.selenium.JavascriptExecutor) driver).executeScript(
                    "arguments[0].click();", dateButton
            );

            System.out.println("Clicked date: " + dateStr);
            Thread.sleep(2000); // Wait for next step to load

        } catch (Exception e) {
            System.out.println("Error clicking date: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private boolean isDateInRange(String dateStr) {
        try {
            // Parse date in format: 2026-09-01
            String[] parts = dateStr.split("-");
            int year = Integer.parseInt(parts[0]);
            int month = Integer.parseInt(parts[1]);

            // Must be 2026
            if (year != 2026) return false;

            // Must be September (9) through December (12)
            return month >= 9 && month <= 12;

        } catch (Exception e) {
            return false;
        }
    }

    public void clickFirstTimeSlot() {
        try {
            System.out.println("Looking for available time slots...");
            Thread.sleep(1000); // Wait for modal to load

            // Use data-testid like we did for other buttons
            List<WebElement> bookButtons = driver.findElements(
                    By.cssSelector("button[data-testid='booking-card-button']")
            );

            System.out.println("Found " + bookButtons.size() + " Book buttons");

            if (bookButtons.size() > 0) {
                System.out.println("Clicking first Book button...");
                ((org.openqa.selenium.JavascriptExecutor) driver).executeScript(
                        "arguments[0].click();", bookButtons.get(0)
                );
                System.out.println("Clicked first time slot");
                Thread.sleep(2000);
            } else {
                System.out.println("No Book buttons found");
            }

        } catch (Exception e) {
            System.out.println("Error: " + e.getMessage());
            e.printStackTrace();
        }
    }

    public void waitForCloudflare() {
        try {
            System.out.println("Waiting for Cloudflare check to complete...");

            // Wait until we can see the "Book now" button (means page loaded)
            int maxWait = 30; // 30 seconds max
            int waited = 0;

            while (waited < maxWait) {
                try {
                    driver.findElement(By.cssSelector("a[data-testid*='offering-book-button']"));
                    System.out.println("Page loaded successfully!");
                    return;
                } catch (Exception e) {
                    System.out.println("Still waiting... (" + waited + "s)");
                    Thread.sleep(1000);
                    waited += 1;
                }
            }

            System.out.println("Warning: Cloudflare check may have failed");

        } catch (Exception e) {
            System.out.println("Error waiting for Cloudflare: " + e.getMessage());
        }
    }

    public void saveCookies(String filename) {
        try {
            Set<org.openqa.selenium.Cookie> cookies = driver.manage().getCookies();

            // Save cookies to file as JSON
            StringBuilder cookieData = new StringBuilder();
            for (org.openqa.selenium.Cookie cookie : cookies) {
                cookieData.append(cookie.getName()).append("=")
                        .append(cookie.getValue()).append(";")
                        .append(cookie.getDomain()).append(";")
                        .append(cookie.getPath()).append(";")
                        .append(cookie.getExpiry()).append("\n");
            }

            java.nio.file.Files.write(
                    java.nio.file.Paths.get(filename),
                    cookieData.toString().getBytes()
            );

            System.out.println("Saved cookies to: " + filename);

        } catch (Exception e) {
            System.out.println("Error saving cookies: " + e.getMessage());
        }
    }

    public void loadCookies(String filename) {
        try {
            if (!java.nio.file.Files.exists(java.nio.file.Paths.get(filename))) {
                System.out.println("Cookie file not found: " + filename);
                return;
            }

            List<String> lines = java.nio.file.Files.readAllLines(
                    java.nio.file.Paths.get(filename)
            );

            for (String line : lines) {
                String[] parts = line.split(";");
                if (parts.length >= 3) {
                    String[] nameValue = parts[0].split("=", 2);
                    if (nameValue.length == 2) {
                        org.openqa.selenium.Cookie cookie = new org.openqa.selenium.Cookie(
                                nameValue[0], nameValue[1], parts[1], parts[2], null
                        );
                        driver.manage().addCookie(cookie);
                    }
                }
            }

            System.out.println("Loaded cookies from: " + filename);

        } catch (Exception e) {
            System.out.println("Error loading cookies: " + e.getMessage());
            e.printStackTrace();
        }
    }

    public static void saveLoginCookies() {
        TockScraper scraper = new TockScraper();
        scraper.setupDriver();
        scraper.loadPage();

        System.out.println("\n========================================");
        System.out.println("Please log into Tock manually in the browser");
        System.out.println("Press ENTER when you're logged in...");
        System.out.println("========================================\n");

        try {
            System.in.read(); // Wait for user to press Enter
            scraper.saveCookies("tock_cookies.txt");
            System.out.println("Cookies saved! You can close the browser now.");
            Thread.sleep(3000);
            scraper.closePage();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void main(String[] args) {
        System.out.println("========== STARTING SCRAPER ==========");
        TockScraper scraper = new TockScraper();

        try {
            scraper.setupDriver();
            scraper.loadPage();

            // Load saved cookies BEFORE waiting for Cloudflare
            scraper.loadCookies("tock_cookies.txt");
            scraper.driver.navigate().refresh(); // Refresh to apply cookies

            scraper.waitForCloudflare();
            scraper.checkAndBook();
            Thread.sleep(3000);

            String firstAvailable = scraper.findFirstAvailableDate();

            if (firstAvailable != null) {
                System.out.println("\nFOUND AVAILABLE DATE: " + firstAvailable);
                scraper.clickDate(firstAvailable);
                scraper.clickFirstTimeSlot();

                System.out.println("\nShould be logged in and at checkout");
            } else {
                System.out.println("\nNo availability found");
            }

        } catch (Exception e) {
            System.out.println("Error: " + e.getMessage());
            e.printStackTrace();
        } finally {
            //scraper.closePage();
        }
    }
}
