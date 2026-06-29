package pages;

import org.openqa.selenium.By;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.ExpectedConditions;
import utils.ConfigReader;

import java.util.List;

public class BookingPage extends BasePage {
    private final By page = id("booking-page");
    private final By confirmButton = id("booking-confirm");
    private final By error = id("booking-error");
    private final By total = id("booking-total");
    private final By seatCount = id("booking-seat-count");
    private final By availableSeats = By
            .cssSelector("[id^='seat-']:not(.disabled):not([disabled]), .seat-available:not([disabled])");
    private final By shows = By.cssSelector("[id^='show-']:not([disabled])");
    private final By seatsLoaded = By.cssSelector("[id^='seat-']");

    // private final By seatsLeft = By.cssSelector("p[class='text-[0.65rem]
    // font-semibold uppercase tracking-wider text-gray-500']");
    public BookingPage(WebDriver driver) {
        super(driver);
    }

    public BookingPage waitForBookingPage() {
        wait.until(ExpectedConditions.urlContains("/book"));
        visible(page);
        return this;
    }

    public void openMoviesAndBook(String movieId) {
        driver.get(ConfigReader.baseUrl() + "/movies");
        visible(id("movies-page"));
        click(By.id(movieId));
        waitForBookingPage();
    }

    public boolean isDisplayed() {
        return isVisible(page);
    }

    public boolean selectFirstShowIfPresent() {
        List<WebElement> showButtons = visibleElements(shows).stream()
                .filter(WebElement::isEnabled)
                .toList();
        if (showButtons.isEmpty()) {
            return false;
        }
        click(showButtons.get(0));
        return true;
    }

    public boolean selectFirstAvailableSeat() {
        List<WebElement> seats = visibleElements(seatsLoaded).stream()
                .filter(WebElement::isEnabled)
                .toList();
        if (seats.isEmpty()) {
            return false;
        }
        click(seats.get(0));
        return true;
    }

    public boolean hasSelectedSeatSummary() {
        return isVisible(seatCount) && !text(seatCount).isBlank() && isVisible(total);
    }

    public void proceedToPay() {
        driver.findElement(confirmButton).click();
    }

    public boolean waitForErrorOrStillOnBooking() {
        // wait.until(ExpectedConditions.or(
        // ExpectedConditions.visibilityOfElementLocated(error),
        // ExpectedConditions.urlContains("/book")
        // ));
        return currentUrl().contains("/book");

    }

    public boolean navigatedToPaymentOrSuccess() {
        wait.until(driver -> {
            String url = driver.getCurrentUrl().toLowerCase();
            return url.contains("checkout.stripe") || url.contains("/payment/") || url.contains("stripe.com");
        });
        return true;
    }


    public String getSeatsLeft(String showId) {
        JavascriptExecutor js = (JavascriptExecutor) driver;

        String script = "const el = document.querySelector('#" + showId + " p[class*=\\\"text-[0.65rem]\\\"]');" +
                "return el ? el.innerText.trim() : '';";
        return (String) js.executeScript(script);
    }

    public void selectMovie(String id) {
        click(By.id(id));
    }

    public void selectShow(String id) {
        click(By.id(id));
    }

    public int findEnabledSeats() {
        List<WebElement> seats = driver.findElements(By.cssSelector("button[id^='seat']"));
        int enabledCount = 0;

        for (WebElement seat : seats) {
            if (seat.isEnabled()) {
                enabledCount++;
            }
        }

        return enabledCount;
    }
}
