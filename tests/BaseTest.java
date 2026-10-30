package tests;

import io.github.bonigarcia.wdm.WebDriverManager;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.edge.EdgeDriver;
import org.openqa.selenium.edge.EdgeOptions;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Listeners;

import java.time.Duration;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

@Listeners(listeners.TestListener.class)
public class BaseTest {
    protected WebDriver driver;

    @BeforeMethod
    public void setUp() {
        Path localDriver = Paths.get(System.getProperty("user.dir"), "msedgedriver.exe").toAbsolutePath();
        if (Files.isRegularFile(localDriver)) {
            System.setProperty("webdriver.edge.driver", localDriver.toString());
        } else {
            try {
                WebDriverManager.edgedriver().setup();
            } catch (RuntimeException e) {
                throw new IllegalStateException(
                        "EdgeDriver is missing at " + localDriver + " and WebDriverManager could not download it. " +
                        "Place a driver matching your Edge version in the project root or restore network access.", e);
            }
        }
        EdgeOptions options = new EdgeOptions();
        options.addArguments("--disable-notifications", "--disable-popup-blocking", "--window-size=1920,1080");
        driver = new EdgeDriver(options);
        driver.manage().timeouts().implicitlyWait(Duration.ZERO);
        driver.manage().timeouts().pageLoadTimeout(Duration.ofSeconds(45));
    }

    @AfterMethod(alwaysRun = true)
    public void tearDown() {
        if (driver != null) {
            driver.quit();
            driver = null;
        }
    }

    public WebDriver getDriver() {
        return driver;
    }
}
