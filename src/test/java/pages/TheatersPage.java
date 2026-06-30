package pages;

import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.ExpectedConditions;
import utils.ConfigReader;

import java.util.List;

public class TheatersPage extends BasePage {
    private final By page = id("theaters-page");
    private final By searchInput = id("theaters-search");
    private final By theaterCards = idStartsWith("theater-");
    private final By theaterNames = idStartsWith("theaters-card-name-");
    private final By emptyState = id("theaters-empty");

    public TheatersPage(WebDriver driver) {
        super(driver);
    }

    public TheatersPage open() {
        driver.get(ConfigReader.baseUrl() + "/theaters");
        visible(page);
        waitForResultsOrEmptyState(theaterNames, emptyState);
        return this;
    }

    public boolean isDisplayed() {
        return isVisible(page);
    }

    public TheatersPage search(String theaterName) {
        type(searchInput, theaterName);
        return this;
    }

    public String searchValue() {
        return value(searchInput);
    }

    public boolean hasTheaterCards() {
        return !waitForVisibleElements(theaterNames).isEmpty();
    }

    public boolean hasResultsOrEmptyState() {
        return waitForResultsOrEmptyState(theaterNames, emptyState);
    }

    public boolean resultsContain(String query) {
        String expected = query.toLowerCase();
        return visibleElements(theaterNames).stream()
                .map(WebElement::getText)
                .map(String::toLowerCase)
                .anyMatch(name -> name.contains(expected));
    }

    public boolean openFirstTheater() {
        List<WebElement> cards = waitForVisibleElements(theaterCards).stream()
                .filter(element -> "a".equalsIgnoreCase(element.getTagName()))
                .toList();
        if (cards.isEmpty()) {
            return false;
        }
        click(cards.get(0));
        wait.until(ExpectedConditions.urlContains("/theaters/"));
        return true;
    }
}
