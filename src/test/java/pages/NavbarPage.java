package pages;

import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.ExpectedConditions;
import utils.ConfigReader;

import java.util.List;

public class NavbarPage extends BasePage {
    private final By moviesTab = id("nav-tab-movies");
    private final By theatersTab = id("nav-tab-theaters");
    private final By bookingsTab = id("nav-tab-bookings");
    private final By logoutButton = id("navbar-logout-btn");
    private final By locationTrigger = id("nav-location-trigger");
    private final By locationMenu = id("nav-location-menu");
    private final By locationRows = By.cssSelector("[id^='nav-location-row-']:not(#nav-location-row-all)");
    public NavbarPage(WebDriver driver) {
        super(driver);
    }

    public void goToMovies() {
        click(moviesTab);
        wait.until(ExpectedConditions.urlContains("/movies"));
    }

    public void goToTheaters() {
        click(theatersTab);
        wait.until(ExpectedConditions.urlContains("/theaters"));
    }

    public void goToMyBookings() {
        click(bookingsTab);
        wait.until(ExpectedConditions.urlContains("/my-bookings"));
    }

    public void logout() {
        click(logoutButton);
        wait.until(ExpectedConditions.urlContains("/login"));
    }

    public boolean isLogoutVisible() {
        return isVisible(logoutButton);
    }

    public boolean selectFirstLocation() {
        if (!isVisible(locationTrigger)) {
            System.out.println("Trigger not visible");
            return false;
        }

        click(locationTrigger);

        // Wait until locations appear
        wait.until(ExpectedConditions.presenceOfAllElementsLocatedBy(locationRows));

        List<WebElement> locations = driver.findElements(locationRows);

        System.out.println("Locations found: " + locations.size());

        if (locations.isEmpty()) {
            return false;
        }

        locations.get(0).click();
        return true;
    }

    public String selectedLocationText() {
        return isVisible(locationTrigger) ? text(locationTrigger) : "";
    }
}
