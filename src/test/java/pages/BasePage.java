package pages;

import org.openqa.selenium.By;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.Keys;
import org.openqa.selenium.TimeoutException;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.Select;
import org.openqa.selenium.support.ui.WebDriverWait;
import utils.ConfigReader;

import java.time.Duration;
import java.util.List;
import java.util.Optional;

public abstract class BasePage {
    protected final WebDriver driver;
    protected final WebDriverWait wait;

    protected BasePage(WebDriver driver) {
        this.driver = driver;
        this.wait = new WebDriverWait(driver, Duration.ofSeconds(ConfigReader.getInt("explicitWaitSeconds", 20)));
    }

    protected By id(String value) {
        return By.id(value);
    }

    protected By idStartsWith(String prefix) {
        return By.cssSelector("[id^='" + prefix + "']");
    }

    protected WebElement visible(By locator) {
        return wait.until(ExpectedConditions.visibilityOfElementLocated(locator));
    }

    protected WebDriverWait dataWait() {
        return new WebDriverWait(driver, Duration.ofSeconds(ConfigReader.getInt("dataLoadTimeoutSeconds", 35)));
    }

    protected boolean waitForAnyVisible(By... locators) {
        try {
            dataWait().until(driver -> {
                for (By locator : locators) {
                    if (isVisible(locator)) {
                        return true;
                    }
                }
                return false;
            });
            return true;
        } catch (TimeoutException e) {
            return false;
        }
    }

    protected List<WebElement> waitForVisibleElements(By locator) {
        try {
            dataWait().until(driver -> !visibleElements(locator).isEmpty());
        } catch (TimeoutException ignored) {
        }
        return visibleElements(locator);
    }

    protected boolean waitForResultsOrEmptyState(By resultsLocator, By emptyStateLocator) {
        return waitForAnyVisible(resultsLocator, emptyStateLocator);
    }

    protected WebElement clickable(By locator) {
        return wait.until(ExpectedConditions.elementToBeClickable(locator));
    }

    protected void click(By locator) {
        clickable(locator).click();
    }

    protected void click(WebElement element) {
        wait.until(ExpectedConditions.elementToBeClickable(element)).click();
    }

    protected void jsClick(WebElement element) {
        ((JavascriptExecutor) driver).executeScript("arguments[0].click();", element);
    }

    protected void type(By locator, String value) {
        WebElement element = visible(locator);
        element.clear();
        element.sendKeys(value);
    }

    protected void typeAndEnter(By locator, String value) {
        WebElement element = visible(locator);
        element.clear();
        element.sendKeys(value, Keys.ENTER);
    }

    protected boolean isVisible(By locator) {
        try {
            return driver.findElement(locator).isDisplayed();
        } catch (RuntimeException e) {
            return false;
        }
    }

    protected String text(By locator) {
        return visible(locator).getText().trim();
    }

    protected List<WebElement> elements(By locator) {
        return driver.findElements(locator);
    }

    protected List<WebElement> visibleElements(By locator) {
        return elements(locator).stream().filter(WebElement::isDisplayed).toList();
    }

    protected Optional<WebElement> firstVisible(By locator) {
        return visibleElements(locator).stream().findFirst();
    }

    protected boolean selectFirstRealOption(By locator) {
        WebElement element = visible(locator);
        try {
            dataWait().until(driver -> new Select(element).getOptions().size() > 1);
        } catch (TimeoutException e) {
            return false;
        }
        Select select = new Select(element);
        select.selectByIndex(1);
        return true;
    }

    protected boolean imageLoaded(WebElement image) {
        Object result = ((JavascriptExecutor) driver).executeScript(
                "return arguments[0].complete && arguments[0].naturalWidth > 0;", image);
        return Boolean.TRUE.equals(result);
    }

    protected String value(By locator) {
        return visible(locator).getAttribute("value");
    }

    public String currentUrl() {
        return driver.getCurrentUrl();
    }
}
