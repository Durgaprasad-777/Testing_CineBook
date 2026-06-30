package pages;

import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.ExpectedConditions;
import utils.ConfigReader;

import java.util.List;

public class MyBookingsPage extends BasePage {
    private final By page = id("my-bookings-page");
    private final By list = id("my-bookings-list");
    private final By emptyState = id("my-bookings-empty");
    private final By allTab = id("my-bookings-tab-all");
    private final By confirmedTab = id("my-bookings-tab-confirmed");
    private final By cancelledTab = id("my-bookings-tab-cancelled");
    private final By cancelButtons = idStartsWith("cancel-");
    private final By cancelDialog = id("my-bookings-cancel-dialog");
    private final By closeCancel = id("my-bookings-cancel-close-btn");
    private final By rateButton = idStartsWith("rate-");
    private final By fourStar = id("rate-star-4");
    private final By rateSubmit = id("rate-submit");
    private final By rateDialog = id("my-bookings-rate-dialog");
    private  final By closerate = id("my-bookings-rate-cancel-btn");

    public MyBookingsPage(WebDriver driver) {
        super(driver);
    }

    public MyBookingsPage open() {
        driver.get(ConfigReader.baseUrl() + "/my-bookings");
        visible(page);
        return this;
    }

    public boolean isDisplayed() {
        return isVisible(page);
    }

    public boolean hasBookingsOrEmptyState() {
        return isVisible(list) || isVisible(emptyState);
    }

    public boolean hasTabs() {
        return isVisible(allTab) && isVisible(confirmedTab) && isVisible(cancelledTab);
    }

    public void switchTabs() {
        click(allTab);
        click(confirmedTab);
        click(cancelledTab);
    }

    public boolean openFirstCancelDialog() {
        try {
            wait.until(ExpectedConditions.presenceOfElementLocated(cancelButtons));
        } catch (Exception e) {
            return false;
        }
        List<WebElement> buttons = visibleElements(cancelButtons).stream()
                .filter(button -> button.getAttribute("id") != null)
                .filter(button -> button.getAttribute("id").startsWith("cancel-"))
                .filter(WebElement::isEnabled)
                .toList();
        if (buttons.isEmpty()) {
            return false;
        }
        click(buttons.get(0));
        visible(cancelDialog);
        return true;
    }

    public boolean isCancelDialogVisible() {
        return isVisible(cancelDialog);
    }

    public void closeCancelDialog() {
        if (isVisible(closeCancel)) {
            click(closeCancel);
        }
    }

    public boolean openrFirstRateDialog() {
        try {
            wait.until(ExpectedConditions.presenceOfElementLocated(rateButton));
        } catch (Exception e) {
            return false;
        }
        List<WebElement> buttons = visibleElements(rateButton).stream()
                .filter(button -> button.getAttribute("id") != null)
                .filter(button -> button.getAttribute("id").startsWith("rate-"))
                .filter(WebElement::isEnabled)
                .toList();
        if (buttons.isEmpty()) {
            return false;
        }
        click(buttons.get(0));
        visible(rateDialog);
        click(fourStar);
        return true;
    }
    public boolean isrCancelDialogVisible() {
        return isVisible(rateDialog);
    }
    public void closeRateDialog() {
        if (isVisible(closerate)) {
            click(closerate);
        }
    }

}
