package tests;

import base.BaseTest;
import org.testng.Assert;
import org.testng.SkipException;
import org.testng.annotations.Test;
import pages.MyBookingsPage;

public class MyBookingsTests extends BaseTest {

    @Test(groups = {"regression", "my-bookings", "FRD_2_7"},
            description = "FRD_2.7.1-2.7.2: My Bookings page should display booking list or empty state")
    public void FRD_271_myBookingsPageDisplaysBookingsOrEmptyState() {
        loginAsUser();
        MyBookingsPage page = new MyBookingsPage(driver).open();
        Assert.assertTrue(page.isDisplayed(), "My Bookings page should be displayed.");
        Assert.assertTrue(page.hasBookingsOrEmptyState(), "Bookings list or empty state should be displayed.");
    }

    @Test(groups = {"regression", "my-bookings", "FRD_2_7"},
            description = "FRD_2.7.3: My Bookings status tabs should be selectable")
    public void FRD_273_myBookingsStatusTabsAreSelectable() {
        loginAsUser();
        MyBookingsPage page = new MyBookingsPage(driver).open();
        if (!page.hasTabs()) {
            throw new SkipException("My Bookings status tabs are not available for the current booking state.");
        }
        page.switchTabs();
        Assert.assertTrue(page.hasBookingsOrEmptyState(), "Switching status tabs should keep valid booking content visible.");
    }

    @Test(groups = {"regression", "my-bookings", "FRD_2_7"},
            description = "FRD_2.7.4-2.7.5: Cancellation dialog can be opened without confirming cancellation")
    public void FRD_274_cancelDialogOpensWithoutConfirmingCancellation() {
        loginAsUser();
        MyBookingsPage page = new MyBookingsPage(driver).open();
        if (!page.openFirstCancelDialog()) {
            throw new SkipException("No cancellable bookings are available.");
        }
        Assert.assertTrue(page.isCancelDialogVisible(), "Cancel dialog should open.");
        page.closeCancelDialog();
    }

    @Test(groups = {"regression", "my-bookings", "FRD_2_7"},
            description = "FRD_2.7 7.2: My Bookings page should rate button and submit rating")
    public void FRD_275MyBookingspageshouldratebuttonandsubmitrating() {
        loginAsUser();
        MyBookingsPage page = new MyBookingsPage(driver).open();
        if (!page.openrFirstRateDialog()) {
            throw new SkipException("No rate able shows are available.");
        }
        Assert.assertTrue(page.isrCancelDialogVisible(), "Cancel dialog should open.");
        page.closeRateDialog();
    }

}
