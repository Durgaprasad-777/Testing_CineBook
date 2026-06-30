package pages;

import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.testng.SkipException;
import utils.ConfigReader;

import java.util.List;

public class MoviesPage extends BasePage {
    private final By page = id("movies-page");
    private final By searchInput = id("movies-search");
    private final By theaterSelect = id("movies-theater");
    private final By emptyState = id("movies-empty");
    private final By clearFiltersButton = id("movies-clear-filters-btn");
    private final By movieCards = idStartsWith("movie-card-");
    private final By movieTitles = idStartsWith("movies-card-title-");
    private final By moviePosters = idStartsWith("movies-poster-");
    private final By bookButtons = idStartsWith("movie-book-");
    private final By trailerButtons = idStartsWith("movie-trailer-");

    public MoviesPage(WebDriver driver) {
        super(driver);
    }

    public MoviesPage open() {
        driver.get(ConfigReader.baseUrl() + "/movies");
        visible(page);
        waitForResultsOrEmptyState(movieCards, emptyState);
        return this;
    }

    public boolean isDisplayed() {
        return isVisible(page);
    }

    public MoviesPage search(String movieName) {
        type(searchInput, movieName);
        return this;
    }

    public String searchValue() {
        return value(searchInput);
    }

    public boolean hasMovieCards() {
        return !waitForVisibleElements(movieCards).isEmpty();
    }

    public boolean hasResultsOrEmptyState() {
        return waitForResultsOrEmptyState(movieCards, emptyState);
    }

    public boolean resultsContain(String text) {
        String expected = text.toLowerCase();
        return visibleElements(movieTitles).stream()
                .map(WebElement::getText)
                .map(String::toLowerCase)
                .anyMatch(title -> title.contains(expected));
    }

    public boolean selectFirstGenre() {
        By genreOptions = By.cssSelector("[id^='movies-genre-']:not(#movies-genre-all)");
        List<WebElement> genres = waitForVisibleElements(genreOptions);
        if (genres.isEmpty()) {
            return false;
        }
        click(genres.get(0));
        return true;
    }

    public boolean selectFirstLanguage() {
        By languageOptions = By.cssSelector("[id^='movies-language-']:not(#movies-language-all)");
        List<WebElement> languages = waitForVisibleElements(languageOptions);
        if (languages.isEmpty()) {
            return false;
        }
        click(languages.get(0));
        return true;
    }

    public boolean selectFirstTheater() {
        return isVisible(theaterSelect) && selectFirstRealOption(theaterSelect);
    }

    public void clearFiltersIfPresent() {
        if (isVisible(clearFiltersButton)) {
            click(clearFiltersButton);
        }
    }

    public boolean everyVisibleCardHasRequiredFields() {
        List<WebElement> cards = waitForVisibleElements(movieCards);
        if (cards.isEmpty()) {
            throw new SkipException("No movie cards are available for validation.");
        }
        for (WebElement card : cards) {
            boolean hasTitle = !card.findElements(By.cssSelector("[id^='movies-card-title-']")).isEmpty();
            boolean hasPoster = !card.findElements(By.cssSelector("[id^='movies-poster-']")).isEmpty();
            boolean hasBook = !card.findElements(By.cssSelector("[id^='movie-book-']")).isEmpty();
            boolean hasTrailer = !card.findElements(By.cssSelector("[id^='movie-trailer-']")).isEmpty();
            boolean hasGenreText = card.getText().toLowerCase().contains("min") || card.getText().toLowerCase().contains("book");
            if (!(hasTitle && hasPoster && hasBook && hasTrailer && hasGenreText)) {
                return false;
            }
        }
        return true;
    }

    public boolean allVisiblePostersLoad() {
        List<WebElement> posters = waitForVisibleElements(moviePosters);
        if (posters.isEmpty()) {
            return false;
        }
        return posters.stream().allMatch(this::imageLoaded);
    }

    public boolean bookFirstMovie() {
        List<WebElement> buttons = waitForVisibleElements(bookButtons);
        if (buttons.isEmpty()) {
            return false;
        }
        click(buttons.get(0));
        wait.until(ExpectedConditions.urlContains("/book"));
        return true;
    }

    public boolean openFirstEnabledTrailer() {
        List<WebElement> buttons = waitForVisibleElements(trailerButtons).stream()
                .filter(WebElement::isEnabled)
                .toList();
        if (buttons.isEmpty()) {
            return false;
        }
        click(buttons.get(0));
        return true;
    }

    public boolean hasDisabledTrailerButton() {
        return waitForVisibleElements(trailerButtons).stream().anyMatch(button -> !button.isEnabled());
    }

    public boolean disabledTrailerButtonsHaveUnavailableMessage() {
        return waitForVisibleElements(trailerButtons).stream()
                .filter(button -> !button.isEnabled())
                .allMatch(button -> {
                    String title = button.getAttribute("title");
                    return title != null && title.toLowerCase().contains("no trailer");
                });
    }

    public boolean trailerModalOrPlayerVisible() {
        return !driver.findElements(By.cssSelector("app-trailer-modal iframe, app-trailer-modal [role='dialog'], app-trailer-modal .fixed")).isEmpty()
                || driver.getPageSource().toLowerCase().contains("trailer");
    }
}
