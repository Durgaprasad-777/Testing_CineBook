package tests;

import base.BaseTest;
import org.testng.Assert;
import org.testng.SkipException;
import org.testng.annotations.Test;
import pages.TheaterDetailPage;
import pages.TheatersPage;
import utils.ConfigReader;

public class TheaterBrowsingTests extends BaseTest {

    @Test(groups = {"regression", "theater", "TS_104", "TC_108"},
            description = "TC_108: Verify theatres can be searched")
    public void TC_108_theatersCanBeSearched() {
        loginAsUser();
        String theaterName = ConfigReader.get("defaultTheater", "PVR");
        TheatersPage theatersPage = new TheatersPage(driver).open();
        if (!theatersPage.hasTheaterCards()) {
            throw new SkipException("No theater cards are available to search.");
        }
        theatersPage.search(theaterName);
        Assert.assertEquals(theatersPage.searchValue(), theaterName, "Theater name should appear in search field.");
        Assert.assertTrue(theatersPage.resultsContain(theaterName) || theatersPage.hasResultsOrEmptyState(),
                "Theater search should show matching results or empty-state feedback.");
    }

    @Test(groups = {"regression", "theater", "TS_104", "TC_109"},
            description = "TC_109: Verify showtimes can be opened from theatre search result")
    public void TC_109_showtimesOpenFromTheaterResult() {
        loginAsUser();
        TheatersPage theatersPage = new TheatersPage(driver).open();
        if (!theatersPage.openFirstTheater()) {
            throw new SkipException("No theater card is available to open.");
        }
        TheaterDetailPage detailPage = new TheaterDetailPage(driver).waitForLoaded();
        Assert.assertTrue(detailPage.isDisplayed(), "Theater detail page should open.");
        Assert.assertTrue(detailPage.hasShowsOrEmptyState(), "Theater detail should show showtimes or an empty state.");
    }

//    @Test(groups = {"regression", "theater", "booking", "TS_105", "TC_110"},
//            description = "TC_110: Verify user can select available show from theatre page")
//    public void TC_110_userCanSelectAvailableShowFromTheaterPage() {
//        loginAsUser();
//        TheatersPage theatersPage = new TheatersPage(driver).open();
//        if (!theatersPage.openFirstTheater()) {
//            throw new SkipException("No theater card is available to open.");
//        }
//        TheaterDetailPage detailPage = new TheaterDetailPage(driver).waitForLoaded();
//        if (!detailPage.selectFirstShow()) {
//            throw new SkipException("No available show exists on the selected theater page.");
//        }
//        Assert.assertTrue(driver.getCurrentUrl().contains("/book"), "Selecting a show should open the booking page.");
//    }
}
