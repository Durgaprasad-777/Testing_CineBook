package tests;

import base.BaseTest;
import org.openqa.selenium.WebElement;
import org.testng.Assert;
import org.testng.SkipException;
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
            description = "Proceeding without a selected seat should show validation feedback")
    public void bookingRequiresAtLeastOneSeat(String movieId, String showId) throws InterruptedException {
        loginAsUser();
        Thread.sleep(3000);

        BookingPage bookingPage = new BookingPage(driver);
        bookingPage.openMoviesAndBook(movieId);

        Thread.sleep(3000);
        if (!bookingPage.selectFirstShowIfPresent()) {
            throw new SkipException("No selectable shows are available for this movie.");
        }

        Thread.sleep(3000);
        if (!bookingPage.hasConfirmButton()) {
            throw new SkipException("Booking confirmation button is not available for the selected show.");
        }
        bookingPage.proceedToPay();

        Thread.sleep(3000);
        Assert.assertTrue(bookingPage.waitForErrorOrStillOnBooking(),
                "Booking should ask the user to select at least one seat.");
    }

    @Test(dataProvider = "movies", groups = { "booking", "regression", "FRD_2_5" },
            description = "Verify already booked seats are disabled/unselectable.")
    public void alreadyBookedSeatsAreDisabledAndUnselectable(String movieId, String showId)
            throws InterruptedException {
        loginAsUser();
        Thread.sleep(3000);

        BookingPage bookingPage = new BookingPage(driver);
        bookingPage.openMoviesAndBook(movieId);

        Thread.sleep(3000);
        if (!bookingPage.selectFirstShowIfPresent()) {
            throw new SkipException("No selectable shows are available for this movie.");
        }

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
            description = "Verify booking is successful for selected movie through payment redirect boundary")
    public void bookingCanProceedToPaymentForSelectedMovie(String movieId, String showId)
            throws InterruptedException {
        loginAsUser();
        Thread.sleep(3000);

        BookingPage bookingPage = new BookingPage(driver);
        bookingPage.openMoviesAndBook(movieId);

        Thread.sleep(3000);
        if (!bookingPage.selectFirstShowIfPresent()) {
            throw new SkipException("No selectable shows are available for this movie.");
        }

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


    @Test(dataProvider = "movies", groups = { "regression", "booking", "FRD_2_5" },
            description = "Verify the total price matches the selected movie and show seat")
    public void checkPrice(String movieId, String showId) throws InterruptedException {
        loginAsUser();
        Thread.sleep(3000);
        BookingPage bookingPage = new BookingPage(driver);
        bookingPage.selectMovie(movieId);
        Thread.sleep(3000);
        bookingPage.selectShow(showId);
        Thread.sleep(3000);
        bookingPage.selectFirstAvailableSeat();
        String totalPrice = bookingPage.getTotalPrice();
        Assert.assertEquals(totalPrice,"₹224.20");
    }
}
