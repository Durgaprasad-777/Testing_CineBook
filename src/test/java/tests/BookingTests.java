package tests;

import base.BaseTest;
import org.openqa.selenium.WebElement;
import org.testng.Assert;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;
import pages.BookingPage;

import java.util.List;

public class BookingTests extends BaseTest {

    @DataProvider(name = "movies")
    public Object[][] movieIds() {
        return new Object[][] { { "movie-book-3", "show-12" } };
    }

    @Test(dataProvider = "movies", groups = { "regression", "booking", "FRD_2_5" },
            description = "FRD_2.5.7: Proceeding without a selected seat should show validation feedback")
    public void FRD_251_bookingRequiresAtLeastOneSeat(String movieId, String showId) throws InterruptedException {
        loginAsUser();
        Thread.sleep(3000);

        BookingPage bookingPage = new BookingPage(driver);
        bookingPage.openMoviesAndBook(movieId);

        Thread.sleep(3000);
        bookingPage.selectFirstShowIfPresent();

        Thread.sleep(3000);
        bookingPage.proceedToPay();

        Thread.sleep(3000);
        Assert.assertTrue(bookingPage.waitForErrorOrStillOnBooking(),
                "Booking should ask the user to select at least one seat.");
    }

    @Test(dataProvider = "movies", groups = { "booking", "regression", "FRD_2_5" },
            description = "Verify already booked seats are disabled/unselectable.")
    public void FRD_252_alreadyBookedSeatsAreDisabledAndUnselectable(String movieId, String showId)
            throws InterruptedException {
        loginAsUser();
        Thread.sleep(3000);

        BookingPage bookingPage = new BookingPage(driver);
        bookingPage.openMoviesAndBook(movieId);

        Thread.sleep(3000);
        bookingPage.selectFirstShowIfPresent();

        Thread.sleep(3000);
        List<WebElement> bookedSeats = bookingPage.getBookedSeats();

        Assert.assertFalse(bookedSeats.isEmpty(),
                "Expected at least one already-booked seat to be present for this show.");

        for (WebElement seat : bookedSeats) {
            Assert.assertTrue(bookingPage.isSeatUnselectable(seat),
                    "Booked seat should be disabled/unselectable: " + seat.getAttribute("id"));
        }

        Assert.assertTrue(bookingPage.areBookedSeatsDisabled(),
                "All booked seats should be disabled/unselectable.");
    }

    @Test(dataProvider = "movies", groups = { "payment", "destructive", "booking", "TS_103", "TC_107" },
            description = "TC_107: Verify booking is successful for selected movie through payment redirect boundary")
    public void TC_107_bookingCanProceedToPaymentForSelectedMovie(String movieId, String showId)
            throws InterruptedException {
        loginAsUser();
        Thread.sleep(3000);

        BookingPage bookingPage = new BookingPage(driver);
        bookingPage.openMoviesAndBook(movieId);

        Thread.sleep(3000);
        bookingPage.selectFirstShowIfPresent();

        Thread.sleep(3000);
        bookingPage.selectFirstAvailableSeat();

        Thread.sleep(3000);
        Assert.assertTrue(bookingPage.hasSelectedSeatSummary(),
                "Selected seat and total should appear in booking summary.");

        bookingPage.proceedToPay();

        Thread.sleep(3000);
        Assert.assertTrue(bookingPage.navigatedToPaymentOrSuccess(),
                "Proceeding should redirect to payment or payment result page.");
    }
}