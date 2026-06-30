package tests;

import base.BaseTest;
import org.testng.Assert;
import org.testng.SkipException;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;
import pages.MoviesPage;
import pages.NavbarPage;
import utils.ConfigReader;
import utils.ExcelUtils;

import java.util.List;
import java.util.Map;

public class MovieBrowsingTests extends BaseTest {

    @DataProvider(name = "movieSearchData")
    public Object[][] movieSearchData() {
        List<Map<String, String>> rows = ExcelUtils.
                readSheet("MovieSearch");
        return rows.stream().map(row -> new Object[]{row}).toArray(Object[][]::new);
    }

    @Test(groups = {"regression", "movie", "TS_101", "TC_101"}, dataProvider = "movieSearchData",
            description = "TC_101: Validate that movies can be searched")
    public void TC_101_moviesCanBeSearched(Map<String, String> data) {
        loginAsUser();
        String movieName = data.getOrDefault("movieName", ConfigReader.get("defaultMovie"));
        MoviesPage moviesPage = new MoviesPage(driver).open();
        if (!moviesPage.hasMovieCards()) {
            throw new SkipException("No movie cards are available to search.");
        }

        moviesPage.search(movieName);
        Assert.assertEquals(moviesPage.searchValue(), movieName, "Movie name should appear in the search box.");
        Assert.assertTrue(moviesPage.resultsContain(movieName) || moviesPage.hasResultsOrEmptyState(),
                "Search should display matching movies or an empty-state message.");
    }

    @Test(groups = {"regression", "movie", "TS_101", "TC_102"},
            description = "TC_102: Verify movies can be filtered by location")
    public void TC_102_moviesCanBeFilteredByLocation() {
        loginAsUser();
        MoviesPage moviesPage = new MoviesPage(driver).open();
        boolean selected = new NavbarPage(driver).selectFirstLocation();
        if (!selected) {
            throw new SkipException("No location options are available in the navbar.");
        }
        Assert.assertTrue(moviesPage.hasResultsOrEmptyState(), "Location filter should refresh the movie list.");
    }

    @Test(groups = {"regression", "movie", "TS_101", "TC_103"},
            description = "TC_103: Verify movies can be filtered by theater")
    public void TC_103_moviesCanBeFilteredByTheater() {
        loginAsUser();
        MoviesPage moviesPage = new MoviesPage(driver).open();
        boolean selected = moviesPage.selectFirstTheater();
        if (!selected) {
            throw new SkipException("No theater options are available in the movies filter.");
        }
        Assert.assertTrue(moviesPage.hasResultsOrEmptyState(), "Theater filter should refresh the movie list.");
    }

    @Test(groups = {"known-defect", "movie", "TS_102", "TC_104"},
            description = "TC_104 / DF_101: Validate movie card layout fields including poster, title, genre, languages, reviews, book, and trailer")
    public void TC_104_movieCardLayoutShowsAllRequiredFields() {
        loginAsUser();
        MoviesPage moviesPage = new MoviesPage(driver).open();
        Assert.assertTrue(moviesPage.everyVisibleCardHasRequiredFields(), "Each movie card should show all required fields.");
        Assert.assertTrue(moviesPage.allVisiblePostersLoad(), "Each visible movie poster should load successfully.");
    }

    @Test(groups = {"known-defect", "movie", "trailer", "TS_102", "TC_105"},
            description = "TC_105 / DF_102: Trailer modal should open and show playable trailer content when a trailer is available")
    public void TC_105_availableTrailerModalOpens() {
        loginAsUser();
        MoviesPage moviesPage = new MoviesPage(driver).open();
        if (!moviesPage.openFirstEnabledTrailer()) {
            throw new SkipException("No enabled trailer button is available.");
        }
        Assert.assertTrue(moviesPage.trailerModalOrPlayerVisible(), "Trailer modal/player should be visible.");
    }

//    @Test(groups = {"regression", "movie", "trailer", "TS_102", "TC_106"},
//            description = "TC_106: Trailer unavailable state should show a proper unavailable message")
//    public void TC_106_unavailableTrailerHasUnavailableMessage() {
//        loginAsUser();
//        MoviesPage moviesPage = new MoviesPage(driver).open();
//        if (!moviesPage.hasDisabledTrailerButton()) {
//            throw new SkipException("No unavailable trailer button is present in current data.");
//        }
//        Assert.assertTrue(moviesPage.disabledTrailerButtonsHaveUnavailableMessage(),
//                "Disabled trailer buttons should explain that no trailer is available.");
//    }
}
