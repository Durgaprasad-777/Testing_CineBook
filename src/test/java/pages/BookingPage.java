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

    private final By seatsLoaded = By.cssSelector("[id^='seat-']");
    private final By allSeats = By.cssSelector("button[id^='seat-']");
    private final By bookedSeats = By.cssSelector(
            "[id^='seat-'].booked, [id^='seat-'].seat-booked, [id^='seat-'][data-booked='true'], [id^='seat-'][aria-disabled='true']"
    );
    private final By availableSeats = By.cssSelector(
            "[id^='seat-']:not(.disabled):not([disabled]):not(.booked):not(.seat-booked), .seat-available:not([disabled])"
    );
    private final By shows = By.cssSelector("[id^='show-']:not([disabled])");

    public BookingPage(WebDriver driver) {
        super(driver);
    }

    public BookingPage waitForBookingPage() {
        wait.until(ExpectedConditions.urlContains("/book"));
        visible(page);
        return this;
    }

    public BookingPage open(String path) {
        driver.get(ConfigReader.baseUrl() + path);
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
        List<WebElement> seats = visibleElements(availableSeats).stream()
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
        String script = "const el = document.querySelector('#" + showId + " p[class*=\\\"text-[0.65rem]\\\"]');"
                + "return el ? el.innerText.trim() : '';";
        return (String) js.executeScript(script);
    }

    public void selectMovie(String id) {
        click(By.cssSelector("[id='" + id + "']"));
    }

    public void selectShow(String id) {
        click(By.cssSelector("[id='" + id + "']"));
    }

    public int findEnabledSeats() {
        List<WebElement> seats = driver.findElements(allSeats);
        int enabledCount = 0;

        for (WebElement seat : seats) {
            if (seat.isEnabled() && !isSeatMarkedDisabled(seat)) {
                enabledCount++;
            }
        }

        return enabledCount;
    }

    public List<WebElement> getBookedSeats() {
        return driver.findElements(bookedSeats);
    }

    public boolean areBookedSeatsDisabled() {
        List<WebElement> seats = getBookedSeats();
        if (seats.isEmpty()) {
            return false;
        }

        for (WebElement seat : seats) {
            if (!isSeatMarkedDisabled(seat) || seat.isEnabled()) {
                return false;
            }
        }

        return true;
    }

    public boolean isSeatUnselectable(WebElement seat) {
        return isSeatMarkedDisabled(seat) || !seat.isEnabled();
    }

    private boolean isSeatMarkedDisabled(WebElement seat) {
        String disabledAttr = seat.getAttribute("disabled");
        String ariaDisabled = seat.getAttribute("aria-disabled");
        String className = seat.getAttribute("class");

        return disabledAttr != null
                || "true".equalsIgnoreCase(ariaDisabled)
                || (className != null && (
                className.contains("disabled")
                        || className.contains("booked")
                        || className.contains("seat-booked")));
    }
}