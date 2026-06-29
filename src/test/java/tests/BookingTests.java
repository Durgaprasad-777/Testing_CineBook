package tests;

import base.BaseTest;
import base.DriverFactory;
import org.openqa.selenium.WebDriver;
import org.testng.Assert;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;
import pages.BookingPage;
import pages.LoginPage;

public class BookingTests extends BaseTest {

    @DataProvider(name = "movies")
    public Object[][] movieIds() {
        return new Object[][] { { "movie-book-3", "show-12" } };
    }

    @Test(dataProvider = "movies", groups = { "regression", "booking",
            "FRD_2_5" }, description = "FRD_2.5.7: Proceeding without a selected seat should show validation feedback")
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

    @Test(dataProvider = "movies", groups = { "payment", "destructive", "booking", "TS_103",
            "TC_107" }, description = "TC_107: Verify booking is successful for selected movie through payment redirect boundary")
    public void TC_107_bookingCanProceedToPaymentForSelectedMovie(String movieId, String showId)
            throws InterruptedException {
        // skipIfPaymentTestsDisabled();
        // skipIfDestructiveTestsDisabled();
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

    @Test(groups = { "concurrency", "booking",
            "TC_CONCURRENT" }, description = "TC_CONCURRENT: Two users select the same seat — only one should succeed at payment")
    public void TC_CONCURRENT_onlyOneUserShouldBookSameSeat() throws InterruptedException {

        // Close the browser that BaseTest @BeforeMethod auto-opened
        DriverFactory.quitDriver();

        DriverFactory.createDriver(null, false);
        WebDriver driver1 = DriverFactory.getDriver();

        DriverFactory.createDriver(null, false);
        WebDriver driver2 = DriverFactory.getDriver();

        boolean seatSelected1 = false;
        boolean seatSelected2 = false;

        try {
            // ── rahulkumar: login → booking page → select show → select seat ──────
            new LoginPage(driver1).open().login("rahulkumar", "123456");
            Thread.sleep(3000);
            BookingPage bp1 = new BookingPage(driver1).open("/movies/3/book");
            Thread.sleep(3000);
            bp1.selectShow("show-12");
            Thread.sleep(3000);
            bp1.selectFirstAvailableSeat();
            Thread.sleep(3000);
            seatSelected1 = true;
            System.out.println("[rahulkumar] Seat selected.");

            // ── sanjaykumar: login → booking page → select show → select seat ─────
            new LoginPage(driver2).open().login("sanjaykumar", "123456");
            Thread.sleep(3000);
            BookingPage bp2 = new BookingPage(driver2).open("/movies/3/book");
            Thread.sleep(3000);
            bp2.selectShow("show-12");
            Thread.sleep(3000);
            bp2.selectFirstAvailableSeat();
            Thread.sleep(3000);
            seatSelected2 = true;
            System.out.println("[sanjaykumar] Seat selected.");

            // ── Wait until both users have selected their seat ────────────────────
            while (!seatSelected1 || !seatSelected2) {
                Thread.sleep(1000);
            }

            System.out.println("Both seats selected — proceeding to payment on both.");
            Thread.sleep(3000);

            // ── Both click Proceed to Payment ─────────────────────────────────────
            bp1.proceedToPay();
            bp2.proceedToPay();
            Thread.sleep(5000);

            // ── Check if both got redirected to payment page ──────────────────────
            String url1 = driver1.getCurrentUrl().toLowerCase();
            String url2 = driver2.getCurrentUrl().toLowerCase();

            boolean user1Redirected = url1.contains("stripe") || url1.contains("/payment/") || url1.contains("checkout");
            boolean user2Redirected = url2.contains("stripe") || url2.contains("/payment/") || url2.contains("checkout");

            System.out.println("[rahulkumar]  payment redirect: " + user1Redirected + " | url: " + url1);
            System.out.println("[sanjaykumar] payment redirect: " + user2Redirected + " | url: " + url2);

            // If BOTH got redirected → double booking happened → FAIL
            Assert.assertFalse(user1Redirected && user2Redirected,
                    "FAIL: Both rahulkumar and sanjaykumar were redirected to payment for the same seat. Double booking is possible!");

        } finally {
            try { driver1.quit(); } catch (Exception ignored) {}
            try { driver2.quit(); } catch (Exception ignored) {}
        }
    }


    // @Test(dataProvider = "movies")
    // public void checkLeftSeats(String movieId, String showId) throws
    // InterruptedException {
    // loginAsUser();
    // Thread.sleep(3000);
    // BookingPage bookingPage = new BookingPage(driver);
    // bookingPage.selectMovie(movieId);
    // Thread.sleep(3000);
    // int leftSeats =
    // Integer.parseInt(bookingPage.getSeatsLeft(showId).trim().split(" ")[0]);
    // bookingPage.selectShow(showId);
    // int enabledSeats = bookingPage.findEnabledSeats();
    //
    // Assert.assertEquals(enabledSeats, leftSeats,
    // "The number of enabled seats should match the showtime's available seat
    // count.");
    // }
}
