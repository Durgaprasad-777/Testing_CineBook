package tests;

import base.BaseTest;
import org.testng.Assert;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;
import pages.AdminBookingsPage;
import pages.AdminMoviesPage;
import pages.AdminShowsPage;
import pages.AnalyticsPage;
import utils.ExcelUtils;

import java.util.List;
import java.util.Map;

public class AdminTests extends BaseTest {


    @Test(groups = {"sanity", "regression", "admin", "FRD_2_9"},
            description = "FRD_2.9: Admin can open Manage Movies and see movie form/table")
    public void adminManageMoviesPageLoads() {
        loginAsAdmin();
        AdminMoviesPage page = new AdminMoviesPage(driver).open();
        Assert.assertTrue(page.isDisplayed(), "Manage Movies page should be displayed.");
        Assert.assertTrue(page.hasMovieFormFields(), "Manage Movies form fields should be displayed.");
        Assert.assertTrue(page.hasTableOrEmptyState(), "Movie table or empty state should be displayed.");
    }

    @Test(groups = {"regression", "admin", "FRD_2_9"},
            description = "FRD_2.9.4: Admin movie form should keep validation active for missing required fields")
    public void adminMovieFormRequiresMandatoryFields() {
        loginAsAdmin();
        AdminMoviesPage page = new AdminMoviesPage(driver).open();
        page.submitEmptyForm();
        Assert.assertTrue(page.formStillDisplayed(), "Invalid movie form should remain displayed.");
    }

    @Test(groups = {"sanity", "regression", "admin", "FRD_2_10"},
            description = "FRD_2.10: Admin can open Manage Shows and see show form/table")
    public void adminManageShowsPageLoads() {
        loginAsAdmin();
        AdminShowsPage page = new AdminShowsPage(driver).open();
        Assert.assertTrue(page.isDisplayed(), "Manage Shows page should be displayed.");
        Assert.assertTrue(page.hasShowFormFields(), "Manage Shows form fields should be displayed.");
        Assert.assertTrue(page.hasTableOrEmptyState(), "Show table or empty state should be displayed.");
    }

    @Test(groups = {"sanity", "regression", "admin", "FRD_2_11"},
            description = "FRD_2.11.1-2.11.3: Admin can view and filter booking records")
    public void adminBookingsPageLoadsAndTabsWork() {
        loginAsAdmin();
        AdminBookingsPage page = new AdminBookingsPage(driver).open();
        Assert.assertTrue(page.isDisplayed(), "Manage Bookings page should be displayed.");
        page.switchStatusTabs();
        Assert.assertTrue(page.hasTableOrEmptyRow(), "Admin bookings table or empty row should be displayed.");
    }

    @Test(groups = {"sanity", "regression", "admin", "FRD_2_11"},
            description = "FRD_2.11.4-2.11.6: Admin can open analytics dashboard with KPIs")
    public void adminAnalyticsDashboardLoads() {
        loginAsAdmin();
        AnalyticsPage page = new AnalyticsPage(driver).open();
        Assert.assertTrue(page.isDisplayed(), "Analytics dashboard should be displayed.");
        Assert.assertTrue(page.hasKpis(), "Analytics KPI cards should be displayed.");
        page.refresh();
    }



}
