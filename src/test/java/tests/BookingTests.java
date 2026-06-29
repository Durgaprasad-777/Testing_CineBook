package tests;

import base.BaseTest;
import base.DriverFactory;
import org.openqa.selenium.WebDriver;
import org.testng.Assert;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;
import pages.BookingPage;
import pages.LoginPage;

import java.util.concurrent.atomic.AtomicBoolean;

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
        bookingPage.selectMovie(movieId);
        Thread.sleep(3000);
        bookingPage.selectShow(showId);
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
        bookingPage.selectMovie(movieId);
        Thread.sleep(3000);
        bookingPage.selectShow(showId);
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
            "TC_CONCURRENT" }, description = "TC_CONCURRENT: Two users racing to book the same seat — only one should succeed")
    public void TC_CONCURRENT_onlyOneUserShouldBookSameSeat() throws InterruptedException {
        final String movieId = "movie-book-3";
        final String showId  = "show-12";

        // ── Driver 1 – rahulkumar ─────────────────────────────────────────────────
        DriverFactory.createDriver(null, false);
        WebDriver driver1 = DriverFactory.getDriver();

        // ── Driver 2 – sanjaykumar ────────────────────────────────────────────────
        DriverFactory.createDriver(null, false);
        WebDriver driver2 = DriverFactory.getDriver();

        AtomicBoolean user1ReachedPayment = new AtomicBoolean(false);
        AtomicBoolean user2ReachedPayment = new AtomicBoolean(false);

        // ── Thread 1: rahulkumar ──────────────────────────────────────────────────
        Thread t1 = new Thread(() -> {
            try {
                LoginPage lp1 = new LoginPage(driver1).open();
                lp1.login("rahulkumar", "123456");
                lp1.waitUntilLoggedIn();
                Thread.sleep(3000);

                BookingPage bp1 = new BookingPage(driver1);
                bp1.selectMovie(movieId);
                Thread.sleep(3000);
                bp1.selectShow(showId);
                Thread.sleep(3000);
                bp1.selectFirstAvailableSeat();
                Thread.sleep(3000);
                bp1.proceedToPay();
                Thread.sleep(3000);

                user1ReachedPayment.set(bp1.navigatedToPaymentOrSuccess());
            } catch (Exception e) {
                System.out.println("[rahulkumar] Exception: " + e.getMessage());
                user1ReachedPayment.set(false);
            }
        });

        // ── Thread 2: sanjaykumar ─────────────────────────────────────────────────
        Thread t2 = new Thread(() -> {
            try {
                LoginPage lp2 = new LoginPage(driver2).open();
                lp2.login("sanjaykumar", "123456");
                lp2.waitUntilLoggedIn();
                Thread.sleep(3000);

                BookingPage bp2 = new BookingPage(driver2);
                bp2.selectMovie(movieId);
                Thread.sleep(3000);
                bp2.selectShow(showId);
                Thread.sleep(3000);
                bp2.selectFirstAvailableSeat();
                Thread.sleep(3000);
                bp2.proceedToPay();
                Thread.sleep(3000);

                user2ReachedPayment.set(bp2.navigatedToPaymentOrSuccess());
            } catch (Exception e) {
                System.out.println("[sanjaykumar] Exception: " + e.getMessage());
                user2ReachedPayment.set(false);
            }
        });

        // ── Launch both simultaneously ─────────────────────────────────────────────
        t1.start();
        t2.start();

        // ── Wait for both to finish (max 3 minutes) ───────────────────────────────
        t1.join(180_000);
        t2.join(180_000);

        System.out.println("[rahulkumar]  payment reached: " + user1ReachedPayment.get());
        System.out.println("[sanjaykumar] payment reached: " + user2ReachedPayment.get());

        // ── Tear down both drivers ────────────────────────────────────────────────
        try { driver1.quit(); } catch (Exception ignored) {}
        try { driver2.quit(); } catch (Exception ignored) {}

        // ── Assertion: if BOTH reached payment, the system has no seat-lock guard ─
        boolean bothSucceeded = user1ReachedPayment.get() && user2ReachedPayment.get();
        Assert.assertFalse(bothSucceeded,
                "FAIL: Both rahulkumar and sanjaykumar reached payment for the SAME seat. " +
                "The system must prevent double-booking — only one user should succeed.");
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
