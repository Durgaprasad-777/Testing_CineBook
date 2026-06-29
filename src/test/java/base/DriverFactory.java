package base;

import io.github.bonigarcia.wdm.WebDriverManager;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.openqa.selenium.edge.EdgeDriver;
import org.openqa.selenium.edge.EdgeOptions;
import org.openqa.selenium.firefox.FirefoxDriver;
import org.openqa.selenium.firefox.FirefoxOptions;
import utils.ConfigReader;

import java.time.Duration;

public final class DriverFactory {
    private static final ThreadLocal<WebDriver> DRIVER = new ThreadLocal<>();

    private DriverFactory() {
    }

    public static void createDriver(String browserName, boolean headless) {
        String browser = browserName == null || browserName.isBlank()
                ? ConfigReader.get("browser", "chrome")
                : browserName;

        WebDriver driver = switch (browser.toLowerCase()) {
            case "edge" -> createEdge(headless);
            case "firefox" -> createFirefox(headless);
            default -> createChrome(headless);
        };

        driver.manage().timeouts().implicitlyWait(Duration.ofSeconds(ConfigReader.getInt("implicitWaitSeconds", 0)));
        driver.manage().timeouts().pageLoadTimeout(Duration.ofSeconds(ConfigReader.getInt("pageLoadTimeoutSeconds", 40)));
        driver.manage().window().maximize();
        DRIVER.set(driver);
    }

    public static WebDriver getDriver() {
        return DRIVER.get();
    }

    public static void quitDriver() {
        WebDriver driver = DRIVER.get();
        if (driver != null) {
            driver.quit();
            DRIVER.remove();
        }
    }

    private static WebDriver createChrome(boolean headless) {
        if (ConfigReader.getBoolean("useWebDriverManager", true)) {
            WebDriverManager.chromedriver().setup();
        }
        ChromeOptions options = new ChromeOptions();
        options.addArguments("--remote-allow-origins=*", "--disable-notifications");
        if (headless) {
            options.addArguments("--headless=new", "--window-size=1440,1000");
        }
        return new ChromeDriver(options);
    }

    private static WebDriver createEdge(boolean headless) {
//        if (ConfigReader.getBoolean("useWebDriverManager", true)) {
//            WebDriverManager.edgedriver().setup();
//        }
//        EdgeOptions options = new EdgeOptions();
//        options.addArguments("--disable-notifications");
//        if (headless) {
//            options.addArguments("--headless=new", "--window-size=1440,1000");
//        }
        return new EdgeDriver();
    }

    private static WebDriver createFirefox(boolean headless) {
        if (ConfigReader.getBoolean("useWebDriverManager", true)) {
            WebDriverManager.firefoxdriver().setup();
        }
        FirefoxOptions options = new FirefoxOptions();
        if (headless) {
            options.addArguments("-headless");
        }
        return new FirefoxDriver(options);
    }
}
